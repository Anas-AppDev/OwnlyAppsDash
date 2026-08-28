package com.ownly.dash.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ownly.dash.platform.currentDashPlatform
import com.ownly.dash.platform.isWeb

object DashLayout {
    val screenHorizontalPadding: Dp get() = if (currentDashPlatform.isWeb) 32.dp else 16.dp
    val screenVerticalPadding: Dp get() = if (currentDashPlatform.isWeb) 28.dp else 20.dp
    val sectionGap: Dp get() = 20.dp
    val contentMaxWidth: Dp get() = if (currentDashPlatform.isWeb) 720.dp else Dp.Unspecified
}
