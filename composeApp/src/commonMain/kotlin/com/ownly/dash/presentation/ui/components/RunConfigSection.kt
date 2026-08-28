package com.ownly.dash.presentation.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ownly.dash.domain.model.AppConfig
import com.ownly.dash.ui.components.DashButton
import com.ownly.dash.ui.components.DashCard
import com.ownly.dash.ui.components.DropdownField
import com.ownly.dash.ui.theme.DashColors
import com.ownly.dash.ui.theme.DashShapes
import com.ownly.dash.ui.theme.dashTextFieldColors

/** Run Configuration tab: full workflow inputs + branch + trigger button. */
@Composable
internal fun RunConfigSection(
    app: AppConfig,
    inputSelections: Map<String, String>,
    branch: String,
    isTriggering: Boolean,
    triggerBlocked: Boolean,
    onInputChange: (key: String, value: String) -> Unit,
    onBranchChange: (String) -> Unit,
    onTrigger: () -> Unit,
) {
    DashCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Workflow inputs",
            style = MaterialTheme.typography.titleMedium,
            color = DashColors.TextPrimary,
        )
        Text(
            text = "${app.workflowFileName} on ${app.owner}/${app.repo}",
            style = MaterialTheme.typography.bodyMedium,
            color = DashColors.TextMuted,
        )
        Spacer(modifier = Modifier.height(16.dp))

        app.inputs.forEach { field ->
            DropdownField(
                label = field.label,
                selected = inputSelections[field.key] ?: field.default,
                options = field.options,
                onSelected = { onInputChange(field.key, it) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = branch,
            onValueChange = onBranchChange,
            label = { Text("Branch name") },
            singleLine = true,
            colors = dashTextFieldColors(),
            shape = DashShapes.field,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(20.dp))
        DashButton(
            text = "Trigger workflow",
            loading = isTriggering,
            enabled = !triggerBlocked,
            onClick = onTrigger,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
