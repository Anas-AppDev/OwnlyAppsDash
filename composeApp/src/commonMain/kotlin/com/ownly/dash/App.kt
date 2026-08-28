package com.ownly.dash

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ownly.dash.presentation.ui.DashMainScreen
import com.ownly.dash.ui.theme.DashTheme

/** App entry point — single-screen GitHub Actions build dashboard. */
@Composable
fun App() {
    DashTheme {
        DashMainScreen(modifier = Modifier.fillMaxSize())
    }
}
