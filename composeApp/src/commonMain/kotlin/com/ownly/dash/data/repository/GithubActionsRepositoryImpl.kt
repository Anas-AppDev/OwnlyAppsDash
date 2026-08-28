@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.ownly.dash.data.repository

import com.ownly.dash.data.WorkflowRun
import com.ownly.dash.data.remote.GithubActionsApi
import com.ownly.dash.domain.model.AppConfig
import com.ownly.dash.domain.model.WorkflowRunDetails
import com.ownly.dash.domain.model.WorkflowRunStatus
import com.ownly.dash.domain.repository.GithubActionsRepository
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/** Maps GitHub API responses to domain models and resolves run id after dispatch. */
internal class GithubActionsRepositoryImpl(
    private val api: GithubActionsApi,
) : GithubActionsRepository {

    override suspend fun triggerWorkflow(
        app: AppConfig,
        ref: String,
        inputs: Map<String, String>,
    ): Result<Long> = runCatching {
        val dispatchedAt = Clock.System.now() - 5.seconds
        val runIdFromBody = api.dispatchWorkflow(app, ref, inputs)
        runIdFromBody
            ?: waitForNewRunId(app, ref, dispatchedAt)
            ?: error(
                "Workflow dispatched, but the new run is not listed yet. " +
                    "Check github.com/${app.owner}/${app.repo}/actions.",
            )
    }

    override suspend fun getRunDetails(app: AppConfig, runId: Long): Result<WorkflowRunDetails> =
        runCatching {
            val run = api.getRun(app, runId)
            WorkflowRunDetails(
                runId = run.id,
                status = WorkflowRunStatus.fromApi(run.status, run.conclusion),
                htmlUrl = run.htmlUrl,
            )
        }

    /** Polls recent runs until a new run appears after dispatch. */
    private suspend fun waitForNewRunId(app: AppConfig, ref: String, since: Instant): Long? {
        repeat(10) { attempt ->
            delay(if (attempt == 0) 1_500L else 2_000L)
            val newRun = api.listRecentRuns(app, ref).firstOrNull { it.wasCreatedAfter(since) }
            if (newRun != null) return newRun.id
        }
        return null
    }

    private fun WorkflowRun.wasCreatedAfter(since: Instant): Boolean {
        val created = runCatching { Instant.parse(createdAt) }.getOrNull() ?: return false
        return created >= since
    }
}
