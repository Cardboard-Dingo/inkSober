package app.unpolished.inksober.ui.journal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.unpolished.inksober.data.local.entity.JournalEntryEntity
import app.unpolished.inksober.data.local.entity.SubstanceEntity
import app.unpolished.inksober.ui.components.*
import app.unpolished.inksober.ui.theme.InkSoberTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun JournalScreen(
    viewModel: JournalViewModel,
    preselectedSubstanceId: Long? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var entryToEdit by remember { mutableStateOf<JournalEntryEntity?>(null) }
    var entryToView by remember { mutableStateOf<JournalEntryEntity?>(null) }
    var entryToDelete by remember { mutableStateOf<JournalEntryEntity?>(null) }

    LaunchedEffect(preselectedSubstanceId) {
        if (preselectedSubstanceId != null) {
            showAddDialog = true
        }
    }

    JournalContent(
        uiState = uiState,
        onSearchQueryChange = { viewModel.setSearchQuery(it) },
        onMoodFilterSelect = { viewModel.setMoodFilter(it) },
        onAddClick = { showAddDialog = true },
        onEntryClick = { entryToView = it },
        onEditClick = { entryToEdit = it },
        onDeleteClick = { entryToDelete = it },
        modifier = modifier
    )

    if (showAddDialog) {
        JournalFormDialog(
            dialogTitle = "New Reflection Note",
            initialEntry = null,
            substances = uiState.substances,
            defaultSubstanceId = preselectedSubstanceId,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, body, mood, substanceId ->
                viewModel.addJournalEntry(title, body, mood, substanceId)
                showAddDialog = false
            }
        )
    }

    entryToEdit?.let { entry ->
        JournalFormDialog(
            dialogTitle = "Edit Reflection",
            initialEntry = entry,
            substances = uiState.substances,
            defaultSubstanceId = entry.substanceId,
            onDismiss = { entryToEdit = null },
            onConfirm = { title, body, mood, substanceId ->
                viewModel.updateJournalEntry(
                    entry.copy(
                        title = title,
                        body = body,
                        mood = mood,
                        substanceId = substanceId
                    )
                )
                entryToEdit = null
            }
        )
    }

    entryToView?.let { entry ->
        val substanceName = uiState.substances.find { it.id == entry.substanceId }?.name ?: "General"
        val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

        Dialog(
            onDismissRequest = { entryToView = null },
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
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(onClick = { entryToView = null }, modifier = Modifier.size(36.dp)) {
                            Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close")
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = entry.mood,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "${dateFormat.format(Date(entry.timestampMillis))} • $substanceName",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

                    Text(
                        text = entry.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        EInkButton(
                            onClick = {
                                entryToEdit = entry
                                entryToView = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Edit Reflection", fontWeight = FontWeight.Bold)
                        }

                        EInkOutlinedButton(
                            onClick = { entryToView = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }

    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete Reflection?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${entry.title}'?") },
            confirmButton = {
                EInkButton(
                    onClick = {
                        viewModel.deleteJournalEntry(entry)
                        entryToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                EInkOutlinedButton(onClick = { entryToDelete = null }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun JournalContent(
    uiState: JournalUiState,
    onSearchQueryChange: (String) -> Unit,
    onMoodFilterSelect: (String?) -> Unit,
    onAddClick: () -> Unit,
    onEntryClick: (JournalEntryEntity) -> Unit,
    onEditClick: (JournalEntryEntity) -> Unit,
    onDeleteClick: (JournalEntryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val moods = listOf("All", "Grateful", "Strong", "Peaceful", "Struggling", "Grounded", "Hopeful")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Top Header Action Button
        item {
            EInkButton(
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log Reflection Note", fontWeight = FontWeight.Bold)
            }
        }

        // Search Bar
        item {
            EInkOutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search reflections...", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search", modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Mood Filter Chips Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(moods) { mood ->
                    val isSelected = if (mood == "All") uiState.selectedMoodFilter == null else uiState.selectedMoodFilter == mood
                    EInkFilterChip(
                        selected = isSelected,
                        onClick = {
                            if (mood == "All") onMoodFilterSelect(null) else onMoodFilterSelect(mood)
                        },
                        label = mood
                    )
                }
            }
        }

        if (uiState.filteredEntries.isEmpty()) {
            item {
                EInkCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No reflections logged.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Log Reflection Note' above to keep track of your thoughts and moods.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(uiState.filteredEntries) { entry ->
                val substanceName = uiState.substances.find { it.id == entry.substanceId }?.name ?: "General"
                JournalEntryCard(
                    entry = entry,
                    substanceName = substanceName,
                    onClick = { onEntryClick(entry) },
                    onEditClick = { onEditClick(entry) },
                    onDeleteClick = { onDeleteClick(entry) }
                )
            }
        }
    }
}

@Composable
fun JournalEntryCard(
    entry: JournalEntryEntity,
    substanceName: String,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val dateStr = remember(entry.timestampMillis) { dateFormat.format(Date(entry.timestampMillis)) }

    EInkCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = entry.mood.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    Text(
                        text = "$dateStr • $substanceName",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row {
                IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.Rounded.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.Rounded.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                }
            }
        }

        if (entry.body.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun JournalFormDialog(
    dialogTitle: String,
    initialEntry: JournalEntryEntity?,
    substances: List<SubstanceEntity>,
    defaultSubstanceId: Long?,
    onDismiss: () -> Unit,
    onConfirm: (title: String, body: String, mood: String, substanceId: Long) -> Unit
) {
    var titleText by remember { mutableStateOf(initialEntry?.title ?: "") }
    var bodyText by remember { mutableStateOf(initialEntry?.body ?: "") }
    var selectedMood by remember { mutableStateOf(initialEntry?.mood ?: "Grateful") }

    val initialSubstanceId = initialEntry?.substanceId
        ?: defaultSubstanceId
        ?: substances.firstOrNull()?.id
        ?: 0L

    var selectedSubstanceId by remember { mutableLongStateOf(initialSubstanceId) }

    val moodOptions = listOf("Grateful", "Strong", "Peaceful", "Struggling", "Grounded", "Hopeful")

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
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

                // 1. Reflection Title / Short Note
                EInkOutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("Reflection / Quick Note") },
                    placeholder = { Text("e.g. Day 30 — Feeling energized!") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. Mood Grid (2x3 Grid)
                Text(
                    text = "MOOD",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(moodOptions) { mood ->
                            val isSelected = selectedMood == mood
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .clickable { selectedMood = mood },
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(2.dp)
                                ) {
                                    Text(
                                        text = mood,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Associated Substance Selection
                if (substances.isNotEmpty()) {
                    Text(
                        text = "ASSOCIATED SUBSTANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    var expandedDropdown by remember { mutableStateOf(false) }
                    val currentSubstanceName = substances.find { it.id == selectedSubstanceId }?.name ?: "Select"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 44.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                            .clickable { expandedDropdown = true }
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = currentSubstanceName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Icon(imageVector = Icons.Rounded.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            substances.forEach { substance ->
                                DropdownMenuItem(
                                    text = { Text(substance.name) },
                                    onClick = {
                                        selectedSubstanceId = substance.id
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 4. Detailed Notes (Optional)
                EInkOutlinedTextField(
                    value = bodyText,
                    onValueChange = { bodyText = it },
                    label = { Text("Details / Journal Notes (Optional)") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                // Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EInkButton(
                        enabled = titleText.isNotBlank(),
                        onClick = {
                            onConfirm(titleText.trim(), bodyText.trim(), selectedMood, selectedSubstanceId)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Reflection", fontWeight = FontWeight.Bold)
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

@Preview(showBackground = true)
@Composable
fun JournalPreview() {
    InkSoberTheme {
        JournalContent(
            uiState = JournalUiState(
                entries = listOf(
                    JournalEntryEntity(
                        id = 1,
                        dateEpochDay = 19000,
                        substanceId = 1,
                        title = "One Month Sober!",
                        body = "Feeling incredible energy today. Took a long walk in the morning.",
                        mood = "Grateful"
                    )
                ),
                filteredEntries = listOf(
                    JournalEntryEntity(
                        id = 1,
                        dateEpochDay = 19000,
                        substanceId = 1,
                        title = "One Month Sober!",
                        body = "Feeling incredible energy today. Took a long walk in the morning.",
                        mood = "Grateful"
                    )
                ),
                substances = listOf(SubstanceEntity(1, "Alcohol", 0L))
            ),
            onSearchQueryChange = {},
            onMoodFilterSelect = {},
            onAddClick = {},
            onEntryClick = {},
            onDeleteClick = {},
            onEditClick = {}
        )
    }
}
