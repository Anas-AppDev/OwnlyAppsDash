package com.ownly.dash.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ownly.dash.domain.model.WorkflowRunStatus
import com.ownly.dash.ui.theme.DashColors
import com.ownly.dash.ui.theme.DashShapes

private data class StatusVisual(val label: String, val color: Color)

private fun WorkflowRunStatus.toVisual(): StatusVisual = when (this) {
    WorkflowRunStatus.Queued -> StatusVisual("Queued", DashColors.PinkSoft)
    WorkflowRunStatus.InProgress -> StatusVisual("Running", DashColors.Pink)
    WorkflowRunStatus.Success -> StatusVisual("Success", DashColors.Success)
    is WorkflowRunStatus.Failed -> StatusVisual("Failed", DashColors.Danger)
    WorkflowRunStatus.Cancelled -> StatusVisual("Cancelled", DashColors.Warning)
    is WorkflowRunStatus.Unknown -> StatusVisual(rawStatus.replace('_', ' '), DashColors.TextSecondary)
}

@Composable
fun StatusBadge(status: WorkflowRunStatus, modifier: Modifier = Modifier) {
    val visual = status.toVisual()
    Row(
        modifier = modifier
            .background(visual.color.copy(alpha = 0.15f), DashShapes.pill)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(8.dp)
                .background(visual.color, CircleShape),
        )
        Text(
            text = visual.label,
            color = visual.color,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
