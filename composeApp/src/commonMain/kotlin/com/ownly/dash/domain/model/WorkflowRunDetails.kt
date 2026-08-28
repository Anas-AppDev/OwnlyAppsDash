package com.ownly.dash.domain.model

/** Normalized workflow run returned from the repository layer. */
data class WorkflowRunDetails(
    val runId: Long,
    val status: WorkflowRunStatus,
    val htmlUrl: String,
)
