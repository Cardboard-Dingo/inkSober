package app.unpolished.inksober.ui.journal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
            title = "New Journal Entry",
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
            title = "Edit Journal Entry",
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
        val dateFormat = remember { SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault()) }

        AlertDialog(
            onDismissRequest = { entryToView = null },
            title = {
                Column {
                    Text(text = entry.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${dateFormat.format(Date(entry.timestampMillis))} • Mood: ${entry.mood} • Substance: $substanceName",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            text = {
                Text(
                    text = entry.body,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                EInkButton(onClick = { entryToView = null }) {
                    Text("Close")
                }
            },
            dismissButton = {
                EInkOutlinedButton(
                    onClick = {
                        entryToEdit = entry
                        entryToView = null
                    }
                ) {
                    Text("Edit")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(6.dp)
        )
    }

    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete Entry?", fontWeight = FontWeight.Bold) },
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
            shape = RoundedCornerShape(6.dp)
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
    val moods = listOf("All", "Peaceful", "Strong", "Struggling", "Grateful", "Grounded")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            EInkButton(
                onClick = onAddClick,
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "New Entry",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log Entry", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "DAILY REFLECTIONS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Search Bar
            item {
                EInkOutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search reflections...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(imageVector = Icons.Rounded.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Mood Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                            .padding(top = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No reflection entries found.",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Log your daily progress, thoughts, and moods to keep building momentum.",
                                style = MaterialTheme.typography.bodyMedium,
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
}

@Composable
fun JournalEntryCard(
    entry: JournalEntryEntity,
    substanceName: String,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
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
                Text(
                    text = "$dateStr • Mood: ${entry.mood}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text(
                    text = substanceName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = entry.body,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onEditClick, modifier = Modifier.size(48.dp)) {
                Icon(imageVector = Icons.Rounded.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = onDeleteClick, modifier = Modifier.size(48.dp)) {
                Icon(imageVector = Icons.Rounded.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun JournalFormDialog(
    title: String,
    initialEntry: JournalEntryEntity?,
    substances: List<SubstanceEntity>,
    defaultSubstanceId: Long?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Long) -> Unit
) {
    var titleText by remember { mutableStateOf(initialEntry?.title ?: "") }
    var bodyText by remember { mutableStateOf(initialEntry?.body ?: "") }
    var selectedMood by remember { mutableStateOf(initialEntry?.mood ?: "Peaceful") }

    val initialSubstanceId = initialEntry?.substanceId
        ?: defaultSubstanceId
        ?: substances.firstOrNull()?.id
        ?: 0L

    var selectedSubstanceId by remember { mutableLongStateOf(initialSubstanceId) }

    val moodOptions = listOf("Peaceful", "Strong", "Struggling", "Grateful", "Grounded")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EInkOutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "Mood", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(moodOptions) { mood ->
                        EInkFilterChip(
                            selected = selectedMood == mood,
                            onClick = { selectedMood = mood },
                            label = mood
                        )
                    }
                }

                if (substances.isNotEmpty()) {
                    Text(text = "Associated Substance", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    var expandedDropdown by remember { mutableStateOf(false) }
                    val currentSubstanceName = substances.find { it.id == selectedSubstanceId }?.name ?: "Select"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                            .clickable { expandedDropdown = true }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = currentSubstanceName, fontWeight = FontWeight.Bold)
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

                EInkOutlinedTextField(
                    value = bodyText,
                    onValueChange = { bodyText = it },
                    label = { Text("Reflection Body") },
                    minLines = 4,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            EInkButton(
                enabled = titleText.isNotBlank() && bodyText.isNotBlank(),
                onClick = {
                    onConfirm(titleText.trim(), bodyText.trim(), selectedMood, selectedSubstanceId)
                }
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
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
                        body = "Feeling incredible energy today. Took a long walk in the morning and spent quality time with family.",
                        mood = "Grateful"
                    )
                ),
                filteredEntries = listOf(
                    JournalEntryEntity(
                        id = 1,
                        dateEpochDay = 19000,
                        substanceId = 1,
                        title = "One Month Sober!",
                        body = "Feeling incredible energy today. Took a long walk in the morning and spent quality time with family.",
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
