package com.ownly.dash.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ownly.dash.ui.components.DashCard
import com.ownly.dash.ui.theme.DashColors

/** Shows trigger failures above the history section. */
@Composable
internal fun TriggerErrorCard(triggerError: String?) {
    AnimatedVisibility(
        visible = triggerError != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        triggerError?.let { error ->
            ColumnBlock {
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
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ColumnBlock(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}
