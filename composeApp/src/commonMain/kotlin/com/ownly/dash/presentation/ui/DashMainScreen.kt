package com.ownly.dash.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ownly.dash.platform.currentDashPlatform
import com.ownly.dash.platform.isWeb
import com.ownly.dash.presentation.rememberDashViewModel
import com.ownly.dash.presentation.ui.components.AppPickerDropdown
import com.ownly.dash.presentation.ui.components.RunConfigSection
import com.ownly.dash.presentation.ui.components.RunStatusSection
import com.ownly.dash.presentation.ui.components.StagingBuildSection
import com.ownly.dash.ui.components.DashTabRow
import com.ownly.dash.ui.theme.DashColors
import com.ownly.dash.ui.theme.DashLayout
import com.ownly.dash.ui.theme.DashTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

private enum class BuildTab(val label: String) {
    Staging("Staging"),
    RunConfiguration("Run Configuration"),
}

@Composable
fun DashMainScreen(modifier: Modifier = Modifier) {
    val viewModel = rememberDashViewModel()
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(BuildTab.Staging.ordinal) }

    // UI
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DashColors.Background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = DashLayout.screenHorizontalPadding,
                    vertical = DashLayout.screenVerticalPadding,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (currentDashPlatform.isWeb) {
                            Modifier.widthIn(max = DashLayout.contentMaxWidth)
                        } else {
                            Modifier
                        },
                    ),
            ) {
                Text(
                    text = "Ownly Apps Dash",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DashColors.TextPrimary,
                )
                Text(
                    text = "Trigger Firebase App Distribution builds",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DashColors.TextSecondary,
                )

                Spacer(modifier = Modifier.height(DashLayout.sectionGap))
                AppPickerDropdown(
                    apps = state.apps,
                    selectedAppId = state.selectedAppId,
                    onSelectApp = viewModel::selectApp,
                )

                val selectedApp = state.selectedApp
                if (selectedApp != null) {
                    Spacer(modifier = Modifier.height(DashLayout.sectionGap))

                    DashTabRow(
                        tabs = BuildTab.entries.map { it.label },
                        selectedIndex = selectedTab,
                        onTabSelected = { selectedTab = it },
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    when (BuildTab.entries[selectedTab]) {
                        BuildTab.Staging -> StagingBuildSection(
                            app = selectedApp,
                            isTriggering = state.isTriggering,
                            onBuild = viewModel::triggerStagingBuild,
                        )
                        BuildTab.RunConfiguration -> RunConfigSection(
                            app = selectedApp,
                            inputSelections = state.inputSelections,
                            branch = state.branch,
                            isTriggering = state.isTriggering,
                            onInputChange = viewModel::updateInput,
                            onBranchChange = viewModel::updateBranch,
                            onTrigger = viewModel::triggerCustomBuild,
                        )
                    }

                    RunStatusSection(
                        triggerError = state.triggerError,
                        currentRun = state.currentRun,
                        onRefresh = viewModel::refreshCurrentStatus,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun DashMainScreenPreview() {
    DashTheme {
        DashMainScreen()
    }
}
