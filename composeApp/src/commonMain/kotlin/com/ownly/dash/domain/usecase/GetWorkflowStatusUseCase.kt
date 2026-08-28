package com.ownly.dash.domain.usecase

import com.ownly.dash.domain.model.AppConfig
import com.ownly.dash.domain.model.WorkflowRunDetails
import com.ownly.dash.domain.repository.GithubActionsRepository

/** Fetches the latest status for a workflow run (used by polling and manual refresh). */
class GetWorkflowStatusUseCase(
    private val repository: GithubActionsRepository,
) {
    suspend operator fun invoke(app: AppConfig, runId: Long): Result<WorkflowRunDetails> =
        repository.getRunDetails(app, runId)
}
