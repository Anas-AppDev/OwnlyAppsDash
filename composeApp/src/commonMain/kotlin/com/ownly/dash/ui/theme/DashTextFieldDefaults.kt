package com.ownly.dash.ui.theme

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable

@Composable
fun dashTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = DashColors.SurfaceElevated,
    unfocusedContainerColor = DashColors.SurfaceElevated,
    focusedTextColor = DashColors.TextPrimary,
    unfocusedTextColor = DashColors.TextPrimary,
    focusedBorderColor = DashColors.Pink,
    unfocusedBorderColor = DashColors.Border,
    focusedLabelColor = DashColors.PinkSoft,
    unfocusedLabelColor = DashColors.TextSecondary,
    cursorColor = DashColors.Pink,
)
