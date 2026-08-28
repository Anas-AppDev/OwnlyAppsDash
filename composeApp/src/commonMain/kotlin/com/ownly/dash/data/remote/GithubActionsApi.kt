package com.ownly.dash.data.remote

import com.ownly.dash.data.DispatchRequest
import com.ownly.dash.data.DispatchResponse
import com.ownly.dash.data.GithubError
import com.ownly.dash.data.RunsList
import com.ownly.dash.data.WorkflowRun
import com.ownly.dash.data.createGithubHttpClient
import com.ownly.dash.domain.model.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

private const val GITHUB_API = "https://api.github.com"
private val json = Json { ignoreUnknownKeys = true }

/** Low-level GitHub Actions REST calls. No business logic here. */
internal class GithubActionsApi(
    private val client: HttpClient = createGithubHttpClient(),
) {
    /** POST workflow dispatch. Returns run id when the proxy includes it in the body. */
    suspend fun dispatchWorkflow(
        app: AppConfig,
        ref: String,
        inputs: Map<String, String>,
    ): Long? {
        val response = client.post(app.dispatchesUrl()) {
            contentType(ContentType.Application.Json)
            setBody(DispatchRequest(ref = ref, inputs = inputs))
        }
        checkResponse(response, "dispatch ${app.repo}")
        val body = response.bodyAsText()
        if (body.isBlank()) return null
        return json.decodeFromString<DispatchResponse>(body).workflowRunId
    }

    /** GET a single workflow run by id. */
    suspend fun getRun(app: AppConfig, runId: Long): WorkflowRun {
        val response = client.get(app.runUrl(runId))
        checkResponse(response, "get run $runId")
        return response.body()
    }

    /**
     * GET recent workflow_dispatch runs.
     * Pass [ref] to filter by branch; omit it to list across all branches.
     */
    suspend fun listRecentRuns(
        app: AppConfig,
        ref: String? = null,
        perPage: Int = 30,
    ): List<WorkflowRun> {
        val response = client.get(app.runsUrl()) {
            parameter("event", "workflow_dispatch")
            if (!ref.isNullOrBlank()) parameter("branch", ref)
            parameter("per_page", perPage)
        }
        checkResponse(response, "list runs for ${app.repo}")
        return response.body<RunsList>().workflowRuns
    }

    private suspend fun checkResponse(response: HttpResponse, action: String) {
        if (response.status.isSuccess()) return
        val body = response.bodyAsText()
        val message = runCatching { json.decodeFromString<GithubError>(body).message }.getOrNull()
        error(
            "GitHub $action failed (${response.status.value})" +
                (message?.let { ": $it" } ?: if (body.isNotBlank()) ": $body" else ""),
        )
    }

    private fun AppConfig.dispatchesUrl() =
        "$GITHUB_API/repos/$owner/$repo/actions/workflows/$workflowFileName/dispatches"

    private fun AppConfig.runsUrl() =
        "$GITHUB_API/repos/$owner/$repo/actions/workflows/$workflowFileName/runs"

    private fun AppConfig.runUrl(runId: Long) =
        "$GITHUB_API/repos/$owner/$repo/actions/runs/$runId"
}
