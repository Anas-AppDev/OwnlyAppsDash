package com.ownly.dash.domain.usecase

import com.ownly.dash.domain.model.AppConfig
import com.ownly.dash.domain.model.WorkflowRunDetails
import com.ownly.dash.domain.repository.GithubActionsRepository

/** Lists recent workflow runs for a branch (history + in-progress checks). */
class ListWorkflowRunsUseCase(
    private val repository: GithubActionsRepository,
) {
    suspend operator fun invoke(app: AppConfig, ref: String): Result<List<WorkflowRunDetails>> {
        if (ref.isBlank()) {
            return Result.failure(IllegalArgumentException("Branch name is required."))
        }
        return repository.listRuns(app, ref.trim())
    }
}
