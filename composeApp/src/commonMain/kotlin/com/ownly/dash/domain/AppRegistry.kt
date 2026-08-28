package com.ownly.dash.domain

import com.ownly.dash.domain.model.AppConfig
import com.ownly.dash.domain.model.WorkflowInputField

/** Apps that can be triggered from this dashboard. Add new entries here later. */
object AppRegistry {

    /** Fixed inputs for the Staging tab quick build. */
    object StagingQuickBuild {
        const val BRANCH = "release"
        const val FLAVOR = "staging"
        const val BUILD_TYPE = "debug"
        const val ARTIFACT = "apk"
    }

    val partnerApp = AppConfig(
        id = "restaurant-app-ownly",
        displayName = "Ownly - Partner App",
        owner = "nutanalabs",
        repo = "restaurant-app",
        workflowFileName = "firebase-app-distribution.yml",
        defaultRef = StagingQuickBuild.BRANCH,
        inputs = listOf(
            WorkflowInputField(
                key = "flavor",
                label = "Flavor",
                options = listOf("develop", "staging", "production"),
                default = StagingQuickBuild.FLAVOR,
            ),
            WorkflowInputField(
                key = "build_type",
                label = "Build type",
                options = listOf("debug", "minifiedDebug", "release"),
                default = StagingQuickBuild.BUILD_TYPE,
            ),
            WorkflowInputField(
                key = "artifact_type",
                label = "Artifact",
                options = listOf("apk", "aab"),
                default = StagingQuickBuild.ARTIFACT,
            ),
        ),
    )

    val apps: List<AppConfig> = listOf(partnerApp)
}
