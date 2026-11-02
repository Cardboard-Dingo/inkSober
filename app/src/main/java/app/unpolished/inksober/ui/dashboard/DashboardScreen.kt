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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.unpolished.inksober.data.local.entity.RecoveryQuoteEntity
import app.unpolished.inksober.data.local.entity.SubstanceEntity
import app.unpolished.inksober.domain.model.Milestone
import app.unpolished.inksober.domain.model.PresetSubstance
import app.unpolished.inksober.domain.model.SobrietyDuration
import app.unpolished.inksober.ui.components.*
import app.unpolished.inksober.ui.substances.AddEditSubstanceDialog
import app.unpolished.inksober.ui.substances.KeypadDatePickerRows
import app.unpolished.inksober.ui.substances.QuickTimePresetRow
import app.unpolished.inksober.ui.theme.AmberMilestone
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

    var daysAgoInput by remember { mutableStateOf("") }

    val calculatedMillis by remember(selectedYear, selectedMonth, selectedDay) {
        derivedStateOf {
            Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedYear)
                set(Calendar.MONTH, selectedMonth - 1)
                set(Calendar.DAY_OF_MONTH, maxOf(1, minOf(selectedDay, getActualMaximum(Calendar.DAY_OF_MONTH))))
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
    }

    val formattedSelectedDate = remember(calculatedMillis) {
        val sdf = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
        sdf.format(Date(calculatedMillis))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 4.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reset Start Date",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Select start date for '$substanceName':",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        daysAgoInput = daysAgo.toString()
                    }
                )

                // Direct Days Ago Entry
                EInkOutlinedTextField(
                    value = daysAgoInput,
                    onValueChange = { input ->
                        val digitsOnly = input.filter { it.isDigit() }
                        daysAgoInput = digitsOnly
                        val days = digitsOnly.toIntOrNull()
                        if (days != null && days >= 0) {
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.DAY_OF_YEAR, -days)
                            selectedYear = cal.get(Calendar.YEAR)
                            selectedMonth = cal.get(Calendar.MONTH) + 1
                            selectedDay = cal.get(Calendar.DAY_OF_MONTH)
                        }
                    },
                    label = { Text("Or Enter Days Ago (e.g. 14)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // 3 Vertical Rows for Day, Month, Year
                KeypadDatePickerRows(
                    year = selectedYear,
                    month = selectedMonth,
                    day = selectedDay,
                    onYearChange = { selectedYear = it },
                    onMonthChange = { selectedMonth = it },
                    onDayChange = { selectedDay = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp)),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "New Start Date: $formattedSelectedDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EInkButton(
                        onClick = {
                            onConfirmReset(calculatedMillis)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set Start Date", fontWeight = FontWeight.Bold)
                    }

                    EInkOutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
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
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Active Substance Card
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
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TRACKING SOBRIETY",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = uiState.activeSubstance?.name ?: "No Substance Selected",
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Bold
                                )
                                if (uiState.allSubstances.size > 1) {
                                    IconButton(
                                        onClick = { expandedDropdown = true },
                                        modifier = Modifier.size(32.dp)
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
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = onAddSubstanceClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Add Substance",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Hero Sobriety Counter Banner
        item {
            ElapsedCounterView(duration = uiState.sobrietyDuration)
        }

        // Quick Actions Stacked Buttons (Full Width, flexible height, unclipped text)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                EInkOutlinedButton(
                    onClick = onResetClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reset Date",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                EInkOutlinedButton(
                    onClick = onLogJournalClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Notes,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Reflection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                EInkButton(
                    onClick = onShareClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Share Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Quote Card
        item {
            QuoteCard(
                quote = uiState.quoteOfTheDay,
                onCycleQuote = onCycleQuote
            )
        }

        // Milestones Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MILESTONES",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${uiState.milestones.count { it.isAchieved }} / ${uiState.milestones.size} Done",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Milestone Cards
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
                text = "SOBRIETY DURATION",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Main Total Days Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${duration.totalDays}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (duration.totalDays == 1L) "TOTAL DAY SOBER" else "TOTAL DAYS SOBER",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Years, Months, Days breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CounterUnitBox(
                    value = duration.years.toString(),
                    label = "YEARS",
                    modifier = Modifier.weight(1f)
                )
                CounterUnitBox(
                    value = duration.months.toString(),
                    label = "MONTHS",
                    modifier = Modifier.weight(1f)
                )
                CounterUnitBox(
                    value = duration.days.toString(),
                    label = "DAYS",
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
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(6.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                maxLines = 1
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
            IconButton(
                onClick = onCycleQuote,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Autorenew,
                    contentDescription = "Cycle Quote",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        if (quote != null) {
            Text(
                text = "\"${quote.quoteText}\"",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "— ${quote.author}",
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold,
                color = AmberMilestone,
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
    val borderColor = if (milestone.isAchieved) AmberMilestone else MaterialTheme.colorScheme.outline
    val backgroundColor = if (milestone.isAchieved) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (milestone.isAchieved) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (milestone.isAchieved) AmberMilestone else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (milestone.isAchieved) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
                border = BorderStroke(1.dp, if (milestone.isAchieved) AmberMilestone else MaterialTheme.colorScheme.outline),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (milestone.isAchieved) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Completed",
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = "${(milestone.progressPercentage * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = milestone.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!milestone.isAchieved) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { milestone.progressPercentage },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
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
        title = { Text("Log Reflection", fontWeight = FontWeight.Bold) },
        text = {
            Text("Create a journal entry for '$substanceName'.")
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
        shape = RoundedCornerShape(8.dp)
    )
}

private fun shareMilestone(context: Context, uiState: DashboardUiState) {
    val substanceName = uiState.activeSubstance?.name ?: "Sobriety"
    val days = uiState.sobrietyDuration.totalDays
    val quoteText = uiState.quoteOfTheDay?.quoteText ?: "One day at a time."

    val shareText = "I have been sober from $substanceName for $days days! \"$quoteText\" - Shared via SoberSide"

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
                sobrietyDuration = SobrietyDuration(days = 0, months = 1, years = 0, totalDays = 30),
                milestones = listOf(
                    Milestone("1 Day", 1, true, description = "Achieved 1 Day sober"),
                    Milestone("1 Week", 7, true, description = "Achieved 1 Week sober"),
                    Milestone("1 Month", 30, true, description = "Achieved 1 Month sober"),
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
