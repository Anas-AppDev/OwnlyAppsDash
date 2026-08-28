package com.ownly.dash.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ownly.dash.ui.theme.DashColors
import com.ownly.dash.ui.theme.DashShapes

@Composable
fun DashButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.height(48.dp),
        shape = DashShapes.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = DashColors.Pink,
            contentColor = DashColors.TextPrimary,
            disabledContainerColor = DashColors.PinkMuted,
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = DashColors.TextPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(text)
        }
    }
}
