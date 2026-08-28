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

/** Screen state for [DashViewModel]. */
data class DashUiState(
    val apps: List<AppConfig> = emptyList(),
    val selectedAppId: String? = null,
    val inputSelections: Map<String, String> = emptyMap(),
    val branch: String = "",
    val isTriggering: Boolean = false,
    val triggerError: String? = null,
    val currentRun: RunUiState? = null,
    val historyRuns: List<HistoryRunUiState> = emptyList(),
    val historyFilter: HistoryFilter = HistoryFilter.All,
    val isLoadingHistory: Boolean = false,
    val historyError: String? = null,
) {
    val selectedApp: AppConfig?
        get() = apps.firstOrNull { it.id == selectedAppId }

    /** True when any queued / in-progress run exists for the current history branch. */
    val hasActiveRunOnBranch: Boolean
        get() = historyRuns.any { !it.status.isTerminal }

    val filteredHistoryRuns: List<HistoryRunUiState>
        get() = when (historyFilter) {
            HistoryFilter.All -> historyRuns
            HistoryFilter.InProgress -> historyRuns.filter { !it.status.isTerminal }
            HistoryFilter.Completed -> historyRuns.filter { it.status.isTerminal }
        }
}
