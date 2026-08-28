package com.ownly.dash.domain.model

/** GitHub repo + workflow metadata for one triggerable app. */
data class AppConfig(
    val id: String,
    val displayName: String,
    val owner: String,
    val repo: String,
    val workflowFileName: String,
    val defaultRef: String,
    val inputs: List<WorkflowInputField> = emptyList(),
)
