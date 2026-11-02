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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
                } ?: PresetSubstance.ALL_PRESETS.last()
            } else {
                PresetSubstance.ALL_PRESETS.first()
            }
        )
    }

    var customName by remember { mutableStateOf(initialSubstance?.name ?: selectedPreset.displayName) }
    var customCategory by remember { mutableStateOf(initialSubstance?.category ?: selectedPreset.category) }
    var iconName by remember { mutableStateOf(initialSubstance?.icon ?: selectedPreset.iconName) }
    var notes by remember { mutableStateOf(initialSubstance?.initialNote ?: selectedPreset.defaultNote) }

    val initialCalendar = remember(initialSubstance) {
        Calendar.getInstance().apply {
            timeInMillis = initialSubstance?.startDateMillis ?: System.currentTimeMillis()
        }
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

    val isEditMode = initialSubstance != null
    val dialogTitle = if (isEditMode) "Edit Substance" else "Add Substance"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dialogTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

                // 1. Preset Choice
                Text(
                    text = "1. CHOOSE PRESET OR CUSTOM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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

                // Name & Category
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EInkOutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Substance Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    EInkOutlinedTextField(
                        value = customCategory,
                        onValueChange = { customCategory = it },
                        label = { Text("Category") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 2. Start Date Keypad Picker
                Text(
                    text = "2. SOBRIETY START DATE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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

                val formattedDate = remember(calculatedMillis) {
                    val sdf = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
                    sdf.format(Date(calculatedMillis))
                }

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
                            text = "Starts: $formattedDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Reflection Note
                EInkOutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Reflection / Note (Optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isEditMode) "Save Changes" else "Add Substance",
                            fontWeight = FontWeight.Bold
                        )
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
fun PresetSubstanceGrid(
    presets: List<PresetSubstance>,
    selectedPreset: PresetSubstance,
    onPresetSelected: (PresetSubstance) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
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
                        .heightIn(min = 38.dp)
                        .clickable { onPresetSelected(preset) },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
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
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = preset.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            maxLines = 1,
                            softWrap = false,
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
            Pair("Today", 0),
            Pair("1 Day", 1),
            Pair("1 Wk", 7),
            Pair("1 Mo", 30)
        )

        options.forEach { (label, days) ->
            EInkOutlinedButton(
                onClick = { onSelectPreset(days) },
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun KeypadDatePickerRows(
    year: Int,
    month: Int,
    day: Int,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthNames = remember {
        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    }
    val currentMonthName = monthNames.getOrElse(month - 1) { "" }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: Day
        EInkOutlinedTextField(
            value = day.toString(),
            onValueChange = { input ->
                val d = input.filter { it.isDigit() }.take(2).toIntOrNull()
                if (d != null && d in 1..31) onDayChange(d)
            },
            label = { Text("Day (1–31)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // Row 2: Month
        EInkOutlinedTextField(
            value = if (currentMonthName.isNotBlank()) "$month ($currentMonthName)" else month.toString(),
            onValueChange = { input ->
                val m = input.filter { it.isDigit() }.take(2).toIntOrNull()
                if (m != null && m in 1..12) onMonthChange(m)
            },
            label = { Text("Month (1–12)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // Row 3: Year
        EInkOutlinedTextField(
            value = year.toString(),
            onValueChange = { input ->
                val y = input.filter { it.isDigit() }.take(4).toIntOrNull()
                if (y != null && y in 1900..2100) onYearChange(y)
            },
            label = { Text("Year (e.g. 2024)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
