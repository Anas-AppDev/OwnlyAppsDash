package com.ownly.dash.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ownly.dash.ui.theme.DashColors
import com.ownly.dash.ui.theme.DashShapes

/** Flat card with a subtle border — no glass/blur. */
@Composable
fun DashCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = DashShapes.card,
        colors = CardDefaults.cardColors(containerColor = DashColors.Surface),
        border = BorderStroke(1.dp, DashColors.Border),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), content = content)
    }
}
