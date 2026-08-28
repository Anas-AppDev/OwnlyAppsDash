package com.ownly.dash.domain.model

/** One workflow_dispatch input shown as a dropdown. */
data class WorkflowInputField(
    val key: String,
    val label: String,
    val options: List<String>,
    val default: String = options.first(),
)
