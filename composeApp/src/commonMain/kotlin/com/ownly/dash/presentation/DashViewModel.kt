package com.ownly.dash.presentation

import com.ownly.dash.domain.AppRegistry
import com.ownly.dash.domain.model.AppConfig
import com.ownly.dash.domain.model.WorkflowRunDetails
import com.ownly.dash.domain.model.WorkflowRunStatus
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

/**
 * Coordinates UI with GitHub workflow use cases.
 *
 * After a trigger (or when an active run is found), polls the **list-runs** API every 5s
 * for the branch. Does **not** repeatedly call GET /actions/runs/{id}.
 * Banner + trigger lock come from whether that list has queued / in_progress runs.
 */
internal class DashViewModel(
    private val scope: CoroutineScope,
    private val triggerWorkflow: TriggerWorkflowUseCase,
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
        loadHistory(startPollingIfActive = true)
    }

    /** Switches the target app/repo and clears any active run state. */
    fun selectApp(appId: String) {
        _uiState.value.apps.firstOrNull { it.id == appId } ?: return
        pollingJob?.cancel()
        _uiState.update {
            it.copy(
                selectedAppId = appId,
                currentRun = null,
                triggerError = null,
                pendingDispatchRef = null,
                historyRuns = emptyList(),
                historyError = null,
            )
        }
        seedDefaultsForSelectedApp()
        loadHistory(startPollingIfActive = true)
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
            loadHistory(startPollingIfActive = true)
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

    fun setHistoryFilter(filter: HistoryFilter) {
        _uiState.update { it.copy(historyFilter = filter) }
    }

    fun refreshHistory() {
        loadHistory(force = true, startPollingIfActive = true)
    }

    private fun triggerWith(app: AppConfig, ref: String, inputs: Map<String, String>) {
        val normalizedRef = ref.trim()
        val state = _uiState.value

        if (state.isTriggering) return
        if (state.isTriggerBlockedOn(normalizedRef)) {
            val active = state.historyRuns.firstOrNull { !it.status.isTerminal }
            _uiState.update {
                it.copy(
                    triggerError =
                        "A workflow is already active on branch \"$normalizedRef\"" +
                            (active?.let { run -> " (run #${run.runNumber.ifZero(run.runId)})" } ?: "") +
                            ". Wait for it to finish before triggering another.",
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isTriggering = true,
                pendingDispatchRef = normalizedRef,
                triggerError = null,
            )
        }

        scope.launch {
            val historyResult = listWorkflowRuns(app, normalizedRef)
            if (normalizedRef == _uiState.value.branch.trim()) {
                historyResult.onSuccess { runs -> applyHistory(runs) }
            }

            val active = historyResult.getOrNull()?.firstOrNull { !it.status.isTerminal }
            if (active != null) {
                _uiState.update {
                    it.copy(
                        isTriggering = false,
                        pendingDispatchRef = null,
                        triggerError =
                            "A workflow is already ${active.status.statusLabel()} on branch \"$normalizedRef\" " +
                                "(run #${active.runNumber.ifZero(active.runId)}). " +
                                "Wait for it to finish before triggering another.",
                    )
                }
                startBranchListPolling(app, normalizedRef)
                return@launch
            }

            triggerWorkflow(app, normalizedRef, inputs)
                .onSuccess { runId ->
                    _uiState.update {
                        it.copy(
                            isTriggering = false,
                            pendingDispatchRef = normalizedRef,
                            branch = normalizedRef,
                            currentRun = RunUiState(
                                runId = runId,
                                appId = app.id,
                                ref = normalizedRef,
                                summary = inputs.entries.joinToString(" · ") { entry ->
                                    "${entry.key}=${entry.value}"
                                },
                                status = WorkflowRunStatus.Queued,
                                htmlUrl = "",
                            ),
                        )
                    }
                    // List-runs polling only — no GET /runs/{id} loop.
                    loadHistory(force = true, startPollingIfActive = false)
                    startBranchListPolling(app, normalizedRef)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isTriggering = false,
                            pendingDispatchRef = null,
                            triggerError = error.message ?: "Failed to trigger workflow.",
                        )
                    }
                }
        }
    }

    /**
     * Polls workflow **list** for [ref] every 5s.
     * Stops when there is no queued/in-progress run and no pending dispatch lock.
     */
    private fun startBranchListPolling(app: AppConfig, ref: String) {
        val normalizedRef = ref.trim()
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive) {
                delay(POLL_INTERVAL_MS)
                val result = listWorkflowRuns(app, normalizedRef)
                result
                    .onSuccess { runs ->
                        if (normalizedRef == _uiState.value.branch.trim()) {
                            applyHistory(runs)
                        } else {
                            // Branch field changed; still clear pending for this ref if done.
                            val hasActive = runs.any { !it.status.isTerminal }
                            if (!hasActive) {
                                _uiState.update { state ->
                                    if (state.pendingDispatchRef?.trim() == normalizedRef) {
                                        state.copy(pendingDispatchRef = null)
                                    } else {
                                        state
                                    }
                                }
                            }
                        }
                    }
                    .onFailure { /* keep trying next tick */ }

                val state = _uiState.value
                val activeOnBranch = state.historyRuns.any { !it.status.isTerminal } &&
                    state.branch.trim() == normalizedRef
                val pending = state.pendingDispatchRef?.trim() == normalizedRef
                if (!activeOnBranch && !pending) break
                // If we're viewing this branch and list has no active but pending remains,
                // keep polling until the new run shows up or we clear pending after a few empty lists.
                if (!activeOnBranch && pending) {
                    // Keep waiting for the dispatched run to appear in the list.
                    continue
                }
            }
        }
    }

    private fun loadHistory(force: Boolean = false, startPollingIfActive: Boolean = false) {
        val app = _uiState.value.selectedApp ?: return
        val ref = _uiState.value.branch.trim()
        if (ref.isBlank()) {
            pollingJob?.cancel()
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
                .onSuccess { runs ->
                    applyHistory(runs)
                    if (startPollingIfActive && runs.any { !it.status.isTerminal }) {
                        startBranchListPolling(app, ref)
                    }
                }
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
        val hasActiveOnGithub = runs.any { !it.status.isTerminal }
        val active = runs.firstOrNull { !it.status.isTerminal }

        _uiState.update { state ->
            val pending = state.pendingDispatchRef
            val trackedId = state.currentRun?.runId
            val sawTrackedRun = trackedId != null && runs.any { it.runId == trackedId }
            val clearPending = pending != null && (hasActiveOnGithub || sawTrackedRun)

            state.copy(
                isLoadingHistory = false,
                historyError = null,
                historyRuns = runs.map { it.toHistoryUi() },
                pendingDispatchRef = if (clearPending) null else pending,
                currentRun = if (active != null) {
                    RunUiState(
                        runId = active.runId,
                        appId = state.selectedAppId.orEmpty(),
                        ref = active.branch.ifBlank { state.branch },
                        summary = active.title,
                        status = active.status,
                        htmlUrl = active.htmlUrl,
                    )
                } else {
                    null
                },
                triggerError = if (hasActiveOnGithub) null else state.triggerError,
            )
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
