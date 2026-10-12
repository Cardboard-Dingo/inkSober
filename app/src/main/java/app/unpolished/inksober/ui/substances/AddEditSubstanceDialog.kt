package app.unpolished.inksober.ui.substances

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.unpolished.inksober.data.local.entity.SubstanceEntity
import app.unpolished.inksober.domain.model.PresetSubstance
import app.unpolished.inksober.ui.components.EInkButton
import app.unpolished.inksober.ui.components.EInkOutlinedButton
import app.unpolished.inksober.ui.components.EInkOutlinedTextField
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddEditSubstanceDialog(
    initialSubstance: SubstanceEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, icon: String, startDateMillis: Long, initialNote: String) -> Unit
) {
    var selectedPreset by remember {
        mutableStateOf(
            if (initialSubstance != null) {
                PresetSubstance.ALL_PRESETS.find {
                    it.displayName.equals(initialSubstance.name, ignoreCase = true) ||
                            it.iconName == initialSubstance.icon
                } ?: PresetSubstance.ALL_PRESETS.last() // Custom
            } else {
                PresetSubstance.ALL_PRESETS.first() // Alcohol default
            }
        )
    }

    var customName by remember { mutableStateOf(initialSubstance?.name ?: selectedPreset.displayName) }
    var customCategory by remember { mutableStateOf(initialSubstance?.category ?: selectedPreset.category) }
    var iconName by remember { mutableStateOf(initialSubstance?.icon ?: selectedPreset.iconName) }
    var notes by remember { mutableStateOf(initialSubstance?.initialNote ?: selectedPreset.defaultNote) }

    // Start date/time state
    val initialCalendar = remember(initialSubstance) {
        Calendar.getInstance().apply {
            timeInMillis = initialSubstance?.startDateMillis ?: System.currentTimeMillis()
        }
    }

    var selectedYear by remember { mutableIntStateOf(initialCalendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(initialCalendar.get(Calendar.MONTH) + 1) } // 1-12
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

    val isEditMode = initialSubstance != null
    val dialogTitle = if (isEditMode) "Edit Substance" else "Add Substance or Habit"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
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
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dialogTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                HorizontalDivider(thickness = 1.5.dp, color = MaterialTheme.colorScheme.outline)

                // 1. Preset Substance Choices Grid
                Text(
                    text = "1. CHOOSE PRESET OR CUSTOM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Bold
                )

                PresetSubstanceGrid(
                    presets = PresetSubstance.ALL_PRESETS,
                    selectedPreset = selectedPreset,
                    onPresetSelected = { preset ->
                        selectedPreset = preset
                        iconName = preset.iconName
                        customCategory = preset.category
                        if (preset.id != "custom" || customName.isBlank()) {
                            customName = preset.displayName
                        }
                        if (notes.isBlank() || notes == selectedPreset.defaultNote) {
                            notes = preset.defaultNote
                        }
                    }
                )

                // 2. Custom Name & Category
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EInkOutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Substance / Habit Name") },
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )

                    EInkOutlinedTextField(
                        value = customCategory,
                        onValueChange = { customCategory = it },
                        label = { Text("Category") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 3. Sobriety Start Date & Time Selector
                Text(
                    text = "2. SOBRIETY START DATE & TIME",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Bold
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

                // Summary of Selected Timestamp
                val formattedDate = remember(calculatedMillis) {
                    val sdf = SimpleDateFormat("EEE, MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
                    sdf.format(Date(calculatedMillis))
                }

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
                            text = "Starts: $formattedDate",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 4. Initial Reflection / Note
                EInkOutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Initial Reflection / Note (Optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Action Bar
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
                            if (customName.isNotBlank()) {
                                onConfirm(
                                    customName.trim(),
                                    customCategory.trim(),
                                    iconName,
                                    calculatedMillis,
                                    notes.trim()
                                )
                            }
                        },
                        enabled = customName.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isEditMode) "Save Changes" else "Add Substance", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PresetSubstanceGrid(
    presets: List<PresetSubstance>,
    selectedPreset: PresetSubstance,
    onPresetSelected: (PresetSubstance) -> Unit
) {
    // 2-column or 3-column scrollable grid optimized for 4.3" screen height constraint
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(presets, key = { it.id }) { preset ->
                val isSelected = preset.id == selectedPreset.id
                val icon = PresetSubstance.getIconForSubstance(preset.iconName, preset.category)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp)
                        .clickable { onPresetSelected(preset) },
                    shape = RoundedCornerShape(4.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.5.dp,
                        color = MaterialTheme.colorScheme.outline
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = preset.displayName,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = preset.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickTimePresetRow(
    onSelectPreset: (daysAgo: Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val options = listOf(
            Pair("Now", 0),
            Pair("1 Day", 1),
            Pair("1 Week", 7),
            Pair("1 Month", 30)
        )

        options.forEach { (label, days) ->
            EInkOutlinedButton(
                onClick = { onSelectPreset(days) },
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun EInkExactDateTimePicker(
    year: Int,
    month: Int,
    day: Int,
    hour: Int,
    minute: Int,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    val monthNames = remember {
        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Date Controls: Year, Month, Day
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Month Stepper
            NumberStepperBox(
                label = "MONTH",
                displayValue = monthNames.getOrElse(month - 1) { "$month" },
                onDecrement = { onMonthChange(if (month > 1) month - 1 else 12) },
                onIncrement = { onMonthChange(if (month < 12) month + 1 else 1) },
                modifier = Modifier.weight(1.1f)
            )

            // Day Stepper
            NumberStepperBox(
                label = "DAY",
                displayValue = day.toString().padStart(2, '0'),
                onDecrement = { onDayChange(if (day > 1) day - 1 else 31) },
                onIncrement = { onDayChange(if (day < 31) day + 1 else 1) },
                modifier = Modifier.weight(1f)
            )

            // Year Stepper
            NumberStepperBox(
                label = "YEAR",
                displayValue = year.toString(),
                onDecrement = { onYearChange(year - 1) },
                onIncrement = { onYearChange(year + 1) },
                modifier = Modifier.weight(1.2f)
            )
        }

        // Time Controls: Hour, Minute
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Hour Stepper (0-23)
            NumberStepperBox(
                label = "HOUR (0-23)",
                displayValue = hour.toString().padStart(2, '0'),
                onDecrement = { onHourChange(if (hour > 0) hour - 1 else 23) },
                onIncrement = { onHourChange(if (hour < 23) hour + 1 else 0) },
                modifier = Modifier.weight(1f)
            )

            // Minute Stepper (0-59)
            NumberStepperBox(
                label = "MINUTE",
                displayValue = minute.toString().padStart(2, '0'),
                onDecrement = { onMinuteChange(if (minute > 0) minute - 1 else 59) },
                onIncrement = { onMinuteChange(if (minute < 59) minute + 1 else 0) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun NumberStepperBox(
    label: String,
    displayValue: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Decrement (-) Button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onDecrement() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "-",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Value Text
            Text(
                text = displayValue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Increment (+) Button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onIncrement() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
