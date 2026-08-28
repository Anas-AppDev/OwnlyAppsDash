package com.ownly.dash.domain.model

/** Normalized workflow run status mapped from GitHub API fields. */
sealed class WorkflowRunStatus {
    data object Queued : WorkflowRunStatus()
    data object InProgress : WorkflowRunStatus()
    data object Success : WorkflowRunStatus()
    data class Failed(val conclusion: String) : WorkflowRunStatus()
    data object Cancelled : WorkflowRunStatus()
    data class Unknown(val rawStatus: String, val rawConclusion: String?) : WorkflowRunStatus()

    /** Polling stops when the run reaches a terminal state. */
    val isTerminal: Boolean
        get() = this is Success || this is Failed || this is Cancelled

    companion object {
        fun fromApi(status: String, conclusion: String?): WorkflowRunStatus = when (status) {
            "queued", "requested", "waiting", "pending" -> Queued
            "in_progress" -> InProgress
            "completed" -> when (conclusion) {
                "success" -> Success
                "cancelled" -> Cancelled
                null -> Unknown(status, conclusion)
                else -> Failed(conclusion)
            }
            else -> Unknown(status, conclusion)
        }
    }
}
