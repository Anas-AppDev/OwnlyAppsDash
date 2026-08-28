package com.ownly.dash.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ownly.dash.presentation.RunUiState
import com.ownly.dash.ui.components.DashCard
import com.ownly.dash.ui.components.StatusBadge
import com.ownly.dash.ui.theme.DashColors

/** Shows trigger errors and live workflow status (polled every 5 s). */
@Composable
internal fun RunStatusSection(
    triggerError: String?,
    currentRun: RunUiState?,
    onRefresh: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(
            visible = triggerError != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            triggerError?.let { error ->
                DashCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Trigger failed",
                        style = MaterialTheme.typography.labelLarge,
                        color = DashColors.Danger,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DashColors.TextSecondary,
                    )
                }
            }
        }

        if (triggerError != null) {
            Spacer(modifier = Modifier.height(12.dp))
        }

        DashCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Workflow status",
                    style = MaterialTheme.typography.titleMedium,
                    color = DashColors.TextPrimary,
                )
                TextButton(
                    onClick = onRefresh,
                    enabled = currentRun != null,
                ) {
                    Text("Refresh", color = DashColors.Pink)
                }
            }

            if (currentRun == null) {
                Text(
                    text = "No workflow triggered yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DashColors.TextMuted,
                    modifier = Modifier.padding(top = 4.dp),
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = DashColors.Border)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusDetailRow(
                        label = "Run",
                        value = "#${currentRun.runId}",
                        modifier = Modifier.weight(1f),
                    )
                    StatusBadge(status = currentRun.status)
                }

                Spacer(modifier = Modifier.height(10.dp))
                StatusDetailRow(label = "Branch", value = currentRun.ref)

                if (currentRun.summary.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusDetailRow(label = "Config", value = currentRun.summary)
                }

                if (currentRun.htmlUrl.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusDetailRow(
                        label = "GitHub",
                        value = currentRun.htmlUrl,
                        valueColor = DashColors.Pink,
                    )
                }

                if (!currentRun.status.isTerminal) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = DashColors.Border)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Auto-refreshes every 5 seconds while running.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DashColors.TextMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = DashColors.TextPrimary,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = DashColors.TextMuted,
            modifier = Modifier.weight(0.28f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            modifier = Modifier.weight(0.72f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
