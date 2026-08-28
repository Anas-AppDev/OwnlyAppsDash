package com.ownly.dash.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** POST body for workflow dispatch. */
@Serializable
internal data class DispatchRequest(
    val ref: String,
    val inputs: Map<String, String>,
)

/** Some proxies return a run id in the dispatch response body. */
@Serializable
internal data class DispatchResponse(
    @SerialName("workflow_run_id") val workflowRunId: Long? = null,
)

@Serializable
internal data class RunsList(
    @SerialName("workflow_runs") val workflowRuns: List<WorkflowRun> = emptyList(),
)

@Serializable
internal data class WorkflowRun(
    val id: Long,
    val status: String,
    val conclusion: String? = null,
    @SerialName("html_url") val htmlUrl: String = "",
    @SerialName("created_at") val createdAt: String = "",
)

@Serializable
internal data class GithubError(
    val message: String? = null,
)
