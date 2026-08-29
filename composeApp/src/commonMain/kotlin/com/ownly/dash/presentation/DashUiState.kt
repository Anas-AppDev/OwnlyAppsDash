package com.ownly.dash.presentation

import com.ownly.dash.domain.model.AppConfig
import com.ownly.dash.domain.model.WorkflowRunStatus

/** Live workflow run shown in the status section. */
data class RunUiState(
    val runId: Long,
    val appId: String,
    val ref: String,
    val summary: String,
    val status: WorkflowRunStatus,
    val htmlUrl: String = "",
)

/** One row in the workflow history list. */
data class HistoryRunUiState(
    val runId: Long,
    val runNumber: Int,
    val title: String,
    val branch: String,
    val status: WorkflowRunStatus,
    val htmlUrl: String,
    val createdAt: String,
)

/** Filter chips for the history section. */
enum class HistoryFilter {
    All,
    InProgress,
    Completed,
}

/** Banner above history while a non-terminal run exists on the branch. */
enum class ActiveWorkflowBanner {
    None,
    Queued,
    InProgress,
}

/** Screen state for [DashViewModel]. */
data class DashUiState(
    val apps: List<AppConfig> = emptyList(),
    val selectedAppId: String? = null,
    val inputSelections: Map<String, String> = emptyMap(),
    val branch: String = "",
    val isTriggering: Boolean = false,
    /** Branch ref locked while a dispatch is in flight or waiting to appear on GitHub. */
    val pendingDispatchRef: String? = null,
    val triggerError: String? = null,
    val currentRun: RunUiState? = null,
    val historyRuns: List<HistoryRunUiState> = emptyList(),
    val historyFilter: HistoryFilter = HistoryFilter.All,
    val isLoadingHistory: Boolean = false,
    val historyError: String? = null,
) {
    val selectedApp: AppConfig?
        get() = apps.firstOrNull { it.id == selectedAppId }

    /**
     * True when a queued / in-progress / just-dispatched run blocks new triggers
     * for the currently selected history branch.
     */
    val hasActiveRunOnBranch: Boolean
        get() = isTriggerBlockedOn(branch)

    /** UI banner: queued vs actually running (not the same copy). */
    val activeWorkflowBanner: ActiveWorkflowBanner
        get() {
            val normalizedBranch = branch.trim()
            if (pendingDispatchRef?.trim() == normalizedBranch &&
                (currentRun == null || currentRun.status is WorkflowRunStatus.Queued)
            ) {
                return ActiveWorkflowBanner.Queued
            }
            val status = currentRun
                ?.takeIf { !it.status.isTerminal && it.ref.trim() == normalizedBranch }
                ?.status
                ?: historyRuns.firstOrNull { !it.status.isTerminal }?.status
                ?: return if (pendingDispatchRef?.trim() == normalizedBranch) {
                    ActiveWorkflowBanner.Queued
                } else {
                    ActiveWorkflowBanner.None
                }
            return when (status) {
                WorkflowRunStatus.InProgress -> ActiveWorkflowBanner.InProgress
                WorkflowRunStatus.Queued -> ActiveWorkflowBanner.Queued
                else -> if (!status.isTerminal) ActiveWorkflowBanner.Queued else ActiveWorkflowBanner.None
            }
        }

    /** Whether [ref] already has an active (or pending) workflow. */
    fun isTriggerBlockedOn(ref: String): Boolean {
        val normalized = ref.trim()
        if (normalized.isEmpty()) return false
        if (pendingDispatchRef?.trim() == normalized) return true
        val tracked = currentRun
        if (tracked != null &&
            !tracked.status.isTerminal &&
            tracked.ref.trim() == normalized
        ) {
            return true
        }
        if (branch.trim() == normalized && historyRuns.any { !it.status.isTerminal }) {
            return true
        }
        return false
    }

    val filteredHistoryRuns: List<HistoryRunUiState>
        get() = when (historyFilter) {
            HistoryFilter.All -> historyRuns
            HistoryFilter.InProgress -> historyRuns.filter { !it.status.isTerminal }
            HistoryFilter.Completed -> historyRuns.filter { it.status.isTerminal }
        }
}
