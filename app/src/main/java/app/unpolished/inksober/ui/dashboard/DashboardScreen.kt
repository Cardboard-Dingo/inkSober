package app.unpolished.inksober.ui.dashboard

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.unpolished.inksober.R
import app.unpolished.inksober.data.local.entity.RecoveryQuoteEntity
import app.unpolished.inksober.data.local.entity.SubstanceEntity
import app.unpolished.inksober.domain.model.Milestone
import app.unpolished.inksober.domain.model.PresetSubstance
import app.unpolished.inksober.domain.model.SobrietyDuration
import app.unpolished.inksober.ui.components.*
import app.unpolished.inksober.ui.substances.AddEditSubstanceDialog
import app.unpolished.inksober.ui.substances.EInkExactDateTimePicker
import app.unpolished.inksober.ui.substances.QuickTimePresetRow
import app.unpolished.inksober.ui.theme.InkSoberTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToSubstances: () -> Unit = {},
    onNavigateToJournal: (Long?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showResetDialog by remember { mutableStateOf(false) }
    var showAddSubstanceDialog by remember { mutableStateOf(false) }
    var showLogJournalDialog by remember { mutableStateOf(false) }

    DashboardContent(
        uiState = uiState,
        onCycleQuote = { viewModel.cycleQuote() },
        onResetClick = { showResetDialog = true },
        onAddSubstanceClick = { showAddSubstanceDialog = true },
        onLogJournalClick = { showLogJournalDialog = true },
        onShareClick = { shareMilestone(context, uiState) },
        onSelectSubstance = { id -> viewModel.setActiveSubstance(id) },
        modifier = modifier
    )

    if (showResetDialog && uiState.activeSubstance != null) {
        ResetSobrietyDateDialog(
            substanceName = uiState.activeSubstance?.name ?: "Sobriety",
            currentStartDateMillis = uiState.activeSubstance?.startDateMillis ?: System.currentTimeMillis(),
            onDismiss = { showResetDialog = false },
            onConfirmReset = { newStartMillis ->
                uiState.activeSubstance?.id?.let { id ->
                    viewModel.resetSobrietyDate(id, newStartMillis)
                }
                showResetDialog = false
            }
        )
    }

    if (showAddSubstanceDialog) {
        AddEditSubstanceDialog(
            initialSubstance = null,
            onDismiss = { showAddSubstanceDialog = false },
            onConfirm = { name, category, icon, startDateMillis, notes ->
                viewModel.addSubstance(name, category, icon, startDateMillis, notes)
                showAddSubstanceDialog = false
            }
        )
    }

    if (showLogJournalDialog) {
        LogJournalQuickDialog(
            substanceName = uiState.activeSubstance?.name ?: "Sobriety",
            onDismiss = { showLogJournalDialog = false },
            onConfirm = {
                onNavigateToJournal(uiState.activeSubstance?.id)
                showLogJournalDialog = false
            }
        )
    }
}

@Composable
fun ResetSobrietyDateDialog(
    substanceName: String,
    currentStartDateMillis: Long,
    onDismiss: () -> Unit,
    onConfirmReset: (newStartDateMillis: Long) -> Unit
) {
    val initialCalendar = remember(currentStartDateMillis) {
        Calendar.getInstance().apply { timeInMillis = currentStartDateMillis }
    }

    var selectedYear by remember { mutableIntStateOf(initialCalendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(initialCalendar.get(Calendar.MONTH) + 1) }
    var selectedDay by remember { mutableIntStateOf(initialCalendar.get(Calendar.DAY_OF_MONTH)) }
    var selectedHour by remember { mutableIntStateOf(initialCalendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(initialCalendar.get(Calendar.MINUTE)) }

    val calculatedMillis by remember(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute) {
        derivedStateOf {
            Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedYear)
                set(Calendar.MONTH, selectedMonth - 1)
                set(Calendar.DAY_OF_MONTH, maxOf(1, minOf(selectedDay, getActualMaximum(Calendar.DAY_OF_MONTH))))
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
    }

    val formattedSelectedDate = remember(calculatedMillis) {
        val sdf = SimpleDateFormat("EEE, MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        sdf.format(Date(calculatedMillis))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 8.dp)
                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reset / Set Start Date",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Select exact past date & time when your sobriety for '$substanceName' started:",
                    style = MaterialTheme.typography.bodyMedium
                )

                QuickTimePresetRow(
                    onSelectPreset = { daysAgo ->
                        val cal = Calendar.getInstance()
                        if (daysAgo > 0) {
                            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
                        }
                        selectedYear = cal.get(Calendar.YEAR)
                        selectedMonth = cal.get(Calendar.MONTH) + 1
                        selectedDay = cal.get(Calendar.DAY_OF_MONTH)
                        selectedHour = cal.get(Calendar.HOUR_OF_DAY)
                        selectedMinute = cal.get(Calendar.MINUTE)
                    }
                )

                EInkExactDateTimePicker(
                    year = selectedYear,
                    month = selectedMonth,
                    day = selectedDay,
                    hour = selectedHour,
                    minute = selectedMinute,
                    onYearChange = { selectedYear = it },
                    onMonthChange = { selectedMonth = it },
                    onDayChange = { selectedDay = it },
                    onHourChange = { selectedHour = it },
                    onMinuteChange = { selectedMinute = it }
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "New Start Time: $formattedSelectedDate",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EInkOutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    EInkButton(
                        onClick = {
                            onConfirmReset(calculatedMillis)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Set Start Date", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onCycleQuote: () -> Unit,
    onResetClick: () -> Unit,
    onAddSubstanceClick: () -> Unit,
    onLogJournalClick: () -> Unit,
    onShareClick: () -> Unit,
    onSelectSubstance: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedDropdown by remember { mutableStateOf(false) }

    val categoryIcon = remember(uiState.activeSubstance?.icon, uiState.activeSubstance?.category) {
        PresetSubstance.getIconForSubstance(
            uiState.activeSubstance?.icon ?: "",
            uiState.activeSubstance?.category ?: ""
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top Header & Active Substance Switcher
        item {
            EInkCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = uiState.activeSubstance?.name ?: "Substance Icon",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TRACKING SOBRIETY",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = uiState.activeSubstance?.name ?: "No Substance Selected",
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Bold
                                )
                                if (uiState.allSubstances.size > 1) {
                                    IconButton(
                                        onClick = { expandedDropdown = true },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ArrowDropDown,
                                            contentDescription = "Switch Substance"
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = expandedDropdown,
                                        onDismissRequest = { expandedDropdown = false }
                                    ) {
                                        uiState.allSubstances.forEach { substance ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = substance.name,
                                                        fontWeight = if (substance.id == uiState.activeSubstance?.id) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                onClick = {
                                                    onSelectSubstance(substance.id)
                                                    expandedDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = uiState.activeSubstance?.category?.uppercase() ?: "GENERAL",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = onAddSubstanceClick,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Add Substance",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // Counter Cards (Days, Hours, Minutes, Seconds)
        item {
            ElapsedCounterView(duration = uiState.sobrietyDuration)
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                EInkOutlinedButton(
                    onClick = onResetClick,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset Date", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                EInkOutlinedButton(
                    onClick = onLogJournalClick,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Notes,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Note", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                EInkButton(
                    onClick = onShareClick,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Quote of the Day Card
        item {
            QuoteCard(
                quote = uiState.quoteOfTheDay,
                onCycleQuote = onCycleQuote
            )
        }

        // Milestones Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MILESTONES",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${uiState.milestones.count { it.isAchieved }} / ${uiState.milestones.size} Completed",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        // Milestone List Items
        items(uiState.milestones) { milestone ->
            MilestoneCard(milestone = milestone)
        }
    }
}

@Composable
fun ElapsedCounterView(duration: SobrietyDuration) {
    EInkCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TIME SOBER",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CounterUnitBox(
                    value = duration.days.toString(),
                    label = "DAYS",
                    modifier = Modifier.weight(1f)
                )
                CounterUnitBox(
                    value = duration.hours.toString().padStart(2, '0'),
                    label = "HOURS",
                    modifier = Modifier.weight(1f)
                )
                CounterUnitBox(
                    value = duration.minutes.toString().padStart(2, '0'),
                    label = "MINS",
                    modifier = Modifier.weight(1f)
                )
                CounterUnitBox(
                    value = duration.seconds.toString().padStart(2, '0'),
                    label = "SECS",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CounterUnitBox(value: String, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun QuoteCard(
    quote: RecoveryQuoteEntity?,
    onCycleQuote: () -> Unit
) {
    EInkCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "QUOTE OF THE DAY",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = onCycleQuote,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Autorenew,
                    contentDescription = "Cycle Quote",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (quote != null) {
            Text(
                text = "\"${quote.quoteText}\"",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "— ${quote.author}",
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "\"One day at a time.\"",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MilestoneCard(milestone: Milestone) {
    val borderColor = if (milestone.isAchieved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val backgroundColor = if (milestone.isAchieved) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (milestone.isAchieved) 2.dp else 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(6.dp)
            )
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (milestone.isAchieved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (milestone.isAchieved) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "${(milestone.progressPercentage * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = milestone.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!milestone.isAchieved) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { milestone.progressPercentage },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }
    }
}

@Composable
fun LogJournalQuickDialog(
    substanceName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Daily Note", fontWeight = FontWeight.Bold) },
        text = {
            Text("Create a reflection entry for '$substanceName'.")
        },
        confirmButton = {
            EInkButton(onClick = onConfirm) {
                Text("Go to Journal")
            }
        },
        dismissButton = {
            EInkOutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(6.dp)
    )
}

private fun shareMilestone(context: Context, uiState: DashboardUiState) {
    val substanceName = uiState.activeSubstance?.name ?: "Sobriety"
    val days = uiState.sobrietyDuration.days
    val quoteText = uiState.quoteOfTheDay?.quoteText ?: "One day at a time."

    val shareText = "I have been sober from $substanceName for $days days! \"$quoteText\" - Shared via inkSober"

    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Milestone")
    context.startActivity(shareIntent)
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    InkSoberTheme {
        DashboardContent(
            uiState = DashboardUiState(
                activeSubstance = SubstanceEntity(name = "Alcohol", startDateMillis = System.currentTimeMillis() - 86400000L * 30),
                sobrietyDuration = SobrietyDuration(days = 30, hours = 4, minutes = 12, seconds = 45),
                milestones = listOf(
                    Milestone("24 Hours", 1, true, description = "Achieved 24 Hours sober"),
                    Milestone("1 Week", 7, true, description = "Achieved 1 Week sober"),
                    Milestone("30 Days", 30, true, description = "Achieved 30 Days sober"),
                    Milestone("90 Days", 90, false, progressPercentage = 0.33f, description = "30 / 90 days completed")
                ),
                quoteOfTheDay = RecoveryQuoteEntity(quoteText = "One day at a time.", author = "AA Literature")
            ),
            onCycleQuote = {},
            onResetClick = {},
            onAddSubstanceClick = {},
            onLogJournalClick = {},
            onShareClick = {},
            onSelectSubstance = {}
        )
    }
}
