package com.ownly.dash.presentation.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ownly.dash.domain.model.AppConfig
import com.ownly.dash.ui.components.DashButton
import com.ownly.dash.ui.components.DashCard
import com.ownly.dash.ui.theme.DashColors

/** Staging tab: one-tap build with fixed staging/debug/apk/release defaults. */
@Composable
internal fun StagingBuildSection(
    app: AppConfig,
    isTriggering: Boolean,
    triggerBlocked: Boolean,
    onBuild: () -> Unit,
) {
    DashCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Quick staging build",
            style = MaterialTheme.typography.titleMedium,
            color = DashColors.TextPrimary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Triggers ${app.owner}/${app.repo} with staging · debug · apk on branch release.",
            style = MaterialTheme.typography.bodyMedium,
            color = DashColors.TextSecondary,
        )
        Spacer(modifier = Modifier.height(20.dp))
        DashButton(
            text = "Build App",
            loading = isTriggering,
            enabled = !triggerBlocked,
            onClick = onBuild,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
