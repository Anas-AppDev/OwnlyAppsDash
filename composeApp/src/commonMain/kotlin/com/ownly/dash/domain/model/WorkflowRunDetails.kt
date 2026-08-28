package com.ownly.dash.domain.model

/** Normalized workflow run returned from the repository layer. */
data class WorkflowRunDetails(
    val runId: Long,
    val status: WorkflowRunStatus,
    val htmlUrl: String,
    val runNumber: Int = 0,
    val branch: String = "",
    val title: String = "",
    val createdAt: String = "",
)
