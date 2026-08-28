package com.ownly.dash.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.ownly.dash.data.remote.GithubActionsApi
import com.ownly.dash.data.repository.GithubActionsRepositoryImpl
import com.ownly.dash.domain.usecase.GetWorkflowStatusUseCase
import com.ownly.dash.domain.usecase.TriggerWorkflowUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/** Creates the ViewModel and wires Api → Repository → UseCases (no DI framework). */
@Composable
internal fun rememberDashViewModel(): DashViewModel {
    val scope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    val viewModel = remember {
        val api = GithubActionsApi()
        val repository = GithubActionsRepositoryImpl(api)
        DashViewModel(
            scope = scope,
            triggerWorkflow = TriggerWorkflowUseCase(repository),
            getWorkflowStatus = GetWorkflowStatusUseCase(repository),
        )
    }
    DisposableEffect(Unit) {
        onDispose { scope.cancel() }
    }
    return viewModel
}
