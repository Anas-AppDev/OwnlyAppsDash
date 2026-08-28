package com.ownly.dash.domain.repository

import com.ownly.dash.domain.model.AppConfig
import com.ownly.dash.domain.model.WorkflowRunDetails

/** Contract for triggering workflows and reading run status. */
interface GithubActionsRepository {
    /** Dispatches the workflow and returns the new run id. */
    suspend fun triggerWorkflow(
        app: AppConfig,
        ref: String,
        inputs: Map<String, String>,
    ): Result<Long>

    /** Fetches current run status and metadata for polling. */
    suspend fun getRunDetails(app: AppConfig, runId: Long): Result<WorkflowRunDetails>

    /** Lists recent workflow_dispatch runs for [ref] (branch). */
    suspend fun listRuns(app: AppConfig, ref: String): Result<List<WorkflowRunDetails>>
}
