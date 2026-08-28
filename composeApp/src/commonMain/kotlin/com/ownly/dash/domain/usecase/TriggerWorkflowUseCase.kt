package com.ownly.dash.domain.usecase

import com.ownly.dash.domain.model.AppConfig
import com.ownly.dash.domain.repository.GithubActionsRepository

/** Validates inputs and triggers a GitHub Actions workflow dispatch. */
class TriggerWorkflowUseCase(
    private val repository: GithubActionsRepository,
) {
    /** Returns the new workflow run id on success. */
    suspend operator fun invoke(
        app: AppConfig,
        ref: String,
        inputs: Map<String, String>,
    ): Result<Long> {
        if (ref.isBlank()) {
            return Result.failure(IllegalArgumentException("Branch name is required."))
        }
        if (inputs.isEmpty()) {
            return Result.failure(IllegalArgumentException("Workflow inputs are required."))
        }
        return repository.triggerWorkflow(app, ref.trim(), inputs)
    }
}
