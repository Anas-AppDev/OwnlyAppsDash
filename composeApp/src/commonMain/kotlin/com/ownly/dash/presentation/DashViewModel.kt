package com.ownly.dash.presentation

import com.ownly.dash.domain.AppRegistry
import com.ownly.dash.domain.model.AppConfig
import com.ownly.dash.domain.model.WorkflowRunDetails
import com.ownly.dash.domain.model.WorkflowRunStatus
import com.ownly.dash.domain.usecase.GetWorkflowStatusUseCase
import com.ownly.dash.domain.usecase.ListWorkflowRunsUseCase
import com.ownly.dash.domain.usecase.TriggerWorkflowUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val POLL_INTERVAL_MS = 5_000L
private const val BRANCH_HISTORY_DEBOUNCE_MS = 400L

/** Coordinates UI actions with GitHub workflow use cases and polls status every 5 seconds. */
internal class DashViewModel(
    private val scope: CoroutineScope,
    private val triggerWorkflow: TriggerWorkflowUseCase,
    private val getWorkflowStatus: GetWorkflowStatusUseCase,
    private val listWorkflowRuns: ListWorkflowRunsUseCase,
) {
    private val _uiState = MutableStateFlow(
        DashUiState(
            apps = AppRegistry.apps,
            selectedAppId = AppRegistry.apps.firstOrNull()?.id,
        ),
    )
    val uiState: StateFlow<DashUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null
    private var historyJob: Job? = null
    private var branchDebounceJob: Job? = null

    init {
        seedDefaultsForSelectedApp()
        loadHistory()
    }

    /** Switches the target app/repo and clears any active run state. */
    fun selectApp(appId: String) {
        val app = _uiState.value.apps.firstOrNull { it.id == appId } ?: return
        pollingJob?.cancel()
        _uiState.update {
            it.copy(
                selectedAppId = appId,
                currentRun = null,
                triggerError = null,
                historyRuns = emptyList(),
                historyError = null,
            )
        }
        seedDefaultsForSelectedApp()
        loadHistory()
    }

    /** Updates a workflow input dropdown value on the Run Configuration tab. */
    fun updateInput(key: String, value: String) {
        _uiState.update { it.copy(inputSelections = it.inputSelections + (key to value)) }
    }

    /** Updates the branch name field and reloads history for that branch. */
    fun updateBranch(value: String) {
        _uiState.update { it.copy(branch = value) }
        branchDebounceJob?.cancel()
        branchDebounceJob = scope.launch {
            delay(BRANCH_HISTORY_DEBOUNCE_MS)
            loadHistory()
        }
    }

    /** Staging tab: fixed staging / debug / apk on branch release. */
    fun triggerStagingBuild() {
        val app = _uiState.value.selectedApp ?: return
        triggerWith(
            app = app,
            ref = AppRegistry.StagingQuickBuild.BRANCH,
            inputs = mapOf(
                "flavor" to AppRegistry.StagingQuickBuild.FLAVOR,
                "build_type" to AppRegistry.StagingQuickBuild.BUILD_TYPE,
                "artifact_type" to AppRegistry.StagingQuickBuild.ARTIFACT,
            ),
        )
    }

    /** Run Configuration tab: uses dropdowns and branch field. */
    fun triggerCustomBuild() {
        val state = _uiState.value
        val app = state.selectedApp ?: return
        val ref = state.branch.ifBlank { app.defaultRef }
        val inputs = app.inputs.associate { field ->
            field.key to (state.inputSelections[field.key] ?: field.default)
        }
        triggerWith(app = app, ref = ref, inputs = inputs)
    }

    /** Manual refresh from the status section. */
    fun refreshCurrentStatus() {
        val run = _uiState.value.currentRun ?: return
        val app = _uiState.value.apps.firstOrNull { it.id == run.appId } ?: return
        scope.launch {
            fetchAndUpdateStatus(app, run.runId)
            loadHistory(force = true)
        }
    }

    fun setHistoryFilter(filter: HistoryFilter) {
        _uiState.update { it.copy(historyFilter = filter) }
    }

    fun refreshHistory() {
        loadHistory(force = true)
    }

    private fun triggerWith(app: AppConfig, ref: String, inputs: Map<String, String>) {
        if (_uiState.value.isTriggering) return

        scope.launch {
            // Fresh check for this branch before allowing a new dispatch.
            val historyResult = listWorkflowRuns(app, ref)
            if (ref.trim() == _uiState.value.branch.trim()) {
                historyResult.onSuccess { runs -> applyHistory(runs) }
            }

            val active = historyResult.getOrNull()?.firstOrNull { !it.status.isTerminal }
            if (active != null) {
                _uiState.update {
                    it.copy(
                        triggerError =
                            "A workflow is already ${active.status.statusLabel()} on branch \"$ref\" " +
                                "(run #${active.runNumber.ifZero(active.runId)}). " +
                                "Wait for it to finish before triggering another.",
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isTriggering = true, triggerError = null) }

            triggerWorkflow(app, ref, inputs)
                .onSuccess { runId ->
                    _uiState.update {
                        it.copy(
                            isTriggering = false,
                            branch = ref,
                            currentRun = RunUiState(
                                runId = runId,
                                appId = app.id,
                                ref = ref,
                                summary = inputs.entries.joinToString(" · ") { "${it.key}=${it.value}" },
                                status = WorkflowRunStatus.Queued,
                                htmlUrl = "",
                            ),
                        )
                    }
                    startPolling(app, runId)
                    loadHistory(force = true)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isTriggering = false,
                            triggerError = error.message ?: "Failed to trigger workflow.",
                        )
                    }
                }
        }
    }

    /** Polls GitHub every 5 seconds until the run reaches a terminal state. */
    private fun startPolling(app: AppConfig, runId: Long) {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive) {
                fetchAndUpdateStatus(app, runId)
                loadHistory(force = true)
                val run = _uiState.value.currentRun?.takeIf { it.runId == runId } ?: break
                if (run.status.isTerminal) break
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private suspend fun fetchAndUpdateStatus(app: AppConfig, runId: Long) {
        getWorkflowStatus(app, runId).onSuccess { details ->
            _uiState.update { state ->
                val current = state.currentRun
                state.copy(
                    currentRun = if (current?.runId == runId) {
                        current.copy(status = details.status, htmlUrl = details.htmlUrl)
                    } else {
                        current
                    },
                )
            }
        }
    }

    private fun loadHistory(force: Boolean = false) {
        val app = _uiState.value.selectedApp ?: return
        val ref = _uiState.value.branch.trim()
        if (ref.isBlank()) {
            _uiState.update {
                it.copy(historyRuns = emptyList(), historyError = null, isLoadingHistory = false)
            }
            return
        }
        if (!force && _uiState.value.isLoadingHistory) return

        historyJob?.cancel()
        historyJob = scope.launch {
            _uiState.update { it.copy(isLoadingHistory = true, historyError = null) }
            listWorkflowRuns(app, ref)
                .onSuccess { runs -> applyHistory(runs) }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingHistory = false,
                            historyError = error.message ?: "Failed to load workflow history.",
                        )
                    }
                }
        }
    }

    private fun applyHistory(runs: List<WorkflowRunDetails>) {
        _uiState.update { state ->
            state.copy(
                isLoadingHistory = false,
                historyError = null,
                historyRuns = runs.map { it.toHistoryUi() },
            )
        }

        // Resume polling after refresh if an active run exists and we aren't already tracking one.
        val active = runs.firstOrNull { !it.status.isTerminal } ?: return
        val current = _uiState.value.currentRun
        if (current == null || current.status.isTerminal) {
            val app = _uiState.value.selectedApp ?: return
            _uiState.update {
                it.copy(
                    currentRun = RunUiState(
                        runId = active.runId,
                        appId = app.id,
                        ref = active.branch.ifBlank { it.branch },
                        summary = active.title,
                        status = active.status,
                        htmlUrl = active.htmlUrl,
                    ),
                    triggerError = null,
                )
            }
            startPolling(app, active.runId)
        }
    }

    private fun seedDefaultsForSelectedApp() {
        val app = _uiState.value.selectedApp ?: return
        _uiState.update {
            it.copy(
                branch = app.defaultRef,
                inputSelections = app.inputs.associate { field -> field.key to field.default },
            )
        }
    }
}

private fun WorkflowRunDetails.toHistoryUi(): HistoryRunUiState =
    HistoryRunUiState(
        runId = runId,
        runNumber = runNumber,
        title = title.ifBlank { "Workflow run" },
        branch = branch,
        status = status,
        htmlUrl = htmlUrl,
        createdAt = createdAt,
    )

private fun WorkflowRunStatus.statusLabel(): String = when (this) {
    WorkflowRunStatus.Queued -> "queued"
    WorkflowRunStatus.InProgress -> "in progress"
    else -> "active"
}

private fun Int.ifZero(fallback: Long): String =
    if (this > 0) this.toString() else fallback.toString()
