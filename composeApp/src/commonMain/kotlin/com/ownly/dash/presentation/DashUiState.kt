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

/** Screen state for [DashViewModel]. */
data class DashUiState(
    val apps: List<AppConfig> = emptyList(),
    val selectedAppId: String? = null,
    val inputSelections: Map<String, String> = emptyMap(),
    val branch: String = "",
    val isTriggering: Boolean = false,
    val triggerError: String? = null,
    val currentRun: RunUiState? = null,
) {
    val selectedApp: AppConfig?
        get() = apps.firstOrNull { it.id == selectedAppId }
}
