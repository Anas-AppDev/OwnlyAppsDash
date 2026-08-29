package com.ownly.dash.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ownly.dash.domain.model.WorkflowRunStatus
import com.ownly.dash.platform.currentDashPlatform
import com.ownly.dash.platform.isWeb
import com.ownly.dash.presentation.ActiveWorkflowBanner
import com.ownly.dash.presentation.HistoryFilter
import com.ownly.dash.presentation.HistoryRunUiState
import com.ownly.dash.ui.components.DashCard
import com.ownly.dash.ui.components.StatusBadge
import com.ownly.dash.ui.theme.DashColors
import com.ownly.dash.ui.theme.DashShapes
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Branch-scoped workflow history with status filter chips. */
@Composable
internal fun WorkflowHistorySection(
    branch: String,
    runs: List<HistoryRunUiState>,
    filter: HistoryFilter,
    isLoading: Boolean,
    error: String?,
    activeBanner: ActiveWorkflowBanner,
    onFilterChange: (HistoryFilter) -> Unit,
    onRefresh: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(16.dp))

        when (activeBanner) {
            ActiveWorkflowBanner.Queued -> {
                DashCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Workflow queued",
                        style = MaterialTheme.typography.labelLarge,
                        color = DashColors.PinkSoft,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "A workflow on branch \"$branch\" is in the queue and will be " +
                            "in progress shortly. New triggers are blocked until it finishes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DashColors.TextSecondary,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            ActiveWorkflowBanner.InProgress -> {
                DashCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Workflow in progress",
                        style = MaterialTheme.typography.labelLarge,
                        color = DashColors.Warning,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Branch \"$branch\" already has a running workflow. " +
                            "New triggers are blocked until it finishes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DashColors.TextSecondary,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            ActiveWorkflowBanner.None -> Unit
        }

        DashCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Workflow history",
                        style = MaterialTheme.typography.titleMedium,
                        color = DashColors.TextPrimary,
                    )
                    Text(
                        text = if (branch.isBlank()) "Enter a branch to load runs" else "Branch: $branch",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DashColors.TextMuted,
                    )
                }
                TextButton(onClick = onRefresh, enabled = branch.isNotBlank() && !isLoading) {
                    Text("Refresh", color = DashColors.Pink)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HistoryFilterChips(selected = filter, onSelect = onFilterChange)
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DashColors.Border)
            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading && runs.isEmpty() -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(
                            color = DashColors.Pink,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                error != null && runs.isEmpty() -> {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DashColors.Danger,
                    )
                }
                runs.isEmpty() -> {
                    Text(
                        text = "No workflow runs for this branch.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DashColors.TextMuted,
                    )
                }
                else -> {
                    runs.forEachIndexed { index, run ->
                        HistoryRunRow(run = run)
                        if (index < runs.lastIndex) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = DashColors.Border.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryFilterChips(
    selected: HistoryFilter,
    onSelect: (HistoryFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HistoryFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            Text(
                text = when (filter) {
                    HistoryFilter.All -> "All"
                    HistoryFilter.InProgress -> "In Progress"
                    HistoryFilter.Completed -> "Completed"
                },
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) DashColors.TextPrimary else DashColors.TextSecondary,
                modifier = Modifier
                    .background(
                        color = if (isSelected) DashColors.Pink else DashColors.SurfaceElevated,
                        shape = DashShapes.pill,
                    )
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun HistoryRunRow(run: HistoryRunUiState) {
    val dateTime = remember(run.createdAt) { formatGithubTimestamp(run.createdAt) }
    val runLabel = if (run.runNumber > 0) "Run #${run.runNumber}" else "Run"
    val displayName = run.title.ifBlank { "Workflow run" }

    if (currentDashPlatform.isWeb) {
        HistoryRunRowWeb(
            runLabel = runLabel,
            runId = run.runId,
            displayName = displayName,
            dateTime = dateTime,
            status = run.status,
        )
    } else {
        HistoryRunRowMobile(
            runLabel = runLabel,
            runId = run.runId,
            displayName = displayName,
            dateTime = dateTime,
            status = run.status,
        )
    }
}

/** Single-line history row for WASM / wide layouts. */
@Composable
private fun HistoryRunRowWeb(
    runLabel: String,
    runId: Long,
    displayName: String,
    dateTime: FormattedDateTime?,
    status: WorkflowRunStatus,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = runLabel,
            style = MaterialTheme.typography.labelLarge,
            color = DashColors.Pink,
        )
        Text(
            text = "ID $runId",
            style = MaterialTheme.typography.bodyMedium,
            color = DashColors.TextMuted,
        )
        Text(
            text = displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = DashColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (dateTime != null) {
            MetaChip(text = dateTime.date)
            MetaChip(text = dateTime.time)
        }
        StatusBadge(status = status)
    }
}

/** Stacked history row for mobile so chips/status don’t overflow. */
@Composable
private fun HistoryRunRowMobile(
    runLabel: String,
    runId: Long,
    displayName: String,
    dateTime: FormattedDateTime?,
    status: WorkflowRunStatus,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = runLabel,
            style = MaterialTheme.typography.labelLarge,
            color = DashColors.Pink,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "ID $runId",
            style = MaterialTheme.typography.bodyMedium,
            color = DashColors.TextMuted,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = DashColors.TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (dateTime != null) {
                MetaChip(text = dateTime.date)
                MetaChip(text = dateTime.time)
            }
            StatusBadge(status = status)
        }
    }
}

@Composable
private fun MetaChip(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = DashColors.TextSecondary,
        modifier = Modifier
            .background(DashColors.SurfaceElevated, DashShapes.pill)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

private data class FormattedDateTime(val date: String, val time: String)

@OptIn(ExperimentalTime::class)
private fun formatGithubTimestamp(iso: String): FormattedDateTime? {
    if (iso.isBlank()) return null
    val local = runCatching {
        Instant.parse(iso).toLocalDateTime(TimeZone.currentSystemDefault())
    }.getOrNull() ?: return null

    val date = "${local.day} ${monthShort(local.month)}, ${local.year}"
    val time = format12Hour(local.hour, local.minute)
    return FormattedDateTime(date = date, time = time)
}

private fun monthShort(month: kotlinx.datetime.Month): String = when (month) {
    kotlinx.datetime.Month.JANUARY -> "Jan"
    kotlinx.datetime.Month.FEBRUARY -> "Feb"
    kotlinx.datetime.Month.MARCH -> "Mar"
    kotlinx.datetime.Month.APRIL -> "Apr"
    kotlinx.datetime.Month.MAY -> "May"
    kotlinx.datetime.Month.JUNE -> "Jun"
    kotlinx.datetime.Month.JULY -> "Jul"
    kotlinx.datetime.Month.AUGUST -> "Aug"
    kotlinx.datetime.Month.SEPTEMBER -> "Sep"
    kotlinx.datetime.Month.OCTOBER -> "Oct"
    kotlinx.datetime.Month.NOVEMBER -> "Nov"
    kotlinx.datetime.Month.DECEMBER -> "Dec"
}

private fun format12Hour(hour24: Int, minute: Int): String {
    val amPm = if (hour24 < 12) "AM" else "PM"
    val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }
    return "$hour12:${minute.toString().padStart(2, '0')} $amPm"
}
