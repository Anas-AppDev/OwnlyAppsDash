package com.ownly.dash.presentation.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ownly.dash.domain.model.AppConfig
import com.ownly.dash.ui.components.DropdownField
import com.ownly.dash.ui.theme.DashColors

/** Dropdown to pick which app/repo workflow to trigger. */
@Composable
internal fun AppPickerDropdown(
    apps: List<AppConfig>,
    selectedAppId: String?,
    onSelectApp: (String) -> Unit,
) {
    val selectedApp = apps.firstOrNull { it.id == selectedAppId } ?: return

    Text(
        text = "App",
        style = MaterialTheme.typography.titleMedium,
        color = DashColors.TextPrimary,
        modifier = Modifier.padding(bottom = 10.dp)
    )
    DropdownField(
        label = "Select app",
        selected = selectedApp.displayName,
        options = apps.map { it.displayName },
        onSelected = { name ->
            apps.firstOrNull { it.displayName == name }?.let { onSelectApp(it.id) }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}
