package app.unpolished.inksober.ui.substances

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.sp
import app.unpolished.inksober.data.local.entity.SubstanceEntity
import app.unpolished.inksober.domain.model.PresetSubstance
import app.unpolished.inksober.domain.model.SobrietyDuration
import app.unpolished.inksober.ui.components.*
import app.unpolished.inksober.ui.theme.InkSoberTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SubstancesScreen(
    viewModel: SubstancesViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var substanceToEdit by remember { mutableStateOf<SubstanceEntity?>(null) }
    var substanceToDelete by remember { mutableStateOf<SubstanceEntity?>(null) }

    SubstancesContent(
        uiState = uiState,
        onAddClick = { showAddDialog = true },
        onSetPrimaryClick = { viewModel.setPrimarySubstance(it) },
        onEditClick = { substanceToEdit = it },
        onDeleteClick = { substanceToDelete = it },
        modifier = modifier
    )

    if (showAddDialog) {
        AddEditSubstanceDialog(
            initialSubstance = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, category, icon, startDateMillis, notes ->
                viewModel.addSubstance(name, startDateMillis, category, icon, notes)
                showAddDialog = false
            }
        )
    }

    substanceToEdit?.let { substance ->
        AddEditSubstanceDialog(
            initialSubstance = substance,
            onDismiss = { substanceToEdit = null },
            onConfirm = { name, category, icon, startDateMillis, notes ->
                viewModel.updateSubstance(
                    substance.copy(
                        name = name,
                        category = category,
                        icon = icon,
                        startDateMillis = startDateMillis,
                        initialNote = notes
                    )
                )
                substanceToEdit = null
            }
        )
    }

    substanceToDelete?.let { substance ->
        AlertDialog(
            onDismissRequest = { substanceToDelete = null },
            title = {
                Text(
                    text = "Delete Substance?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${substance.name}'? All tracking data for this substance will be permanently removed.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                EInkButton(
                    onClick = {
                        viewModel.deleteSubstance(substance)
                        substanceToDelete = null
                    }
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                EInkOutlinedButton(onClick = { substanceToDelete = null }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(6.dp)
        )
    }
}

@Composable
fun SubstancesContent(
    uiState: SubstancesUiState,
    onAddClick: () -> Unit,
    onSetPrimaryClick: (Long) -> Unit,
    onEditClick: (SubstanceEntity) -> Unit,
    onDeleteClick: (SubstanceEntity) -> Unit,
    modifier: Modifier = Modifier
) {
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
                    contentDescription = "Add Substance",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Substance", fontWeight = FontWeight.Bold)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRACKED SUBSTANCES",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.substances.size} Total",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            if (uiState.substances.isEmpty()) {
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
                                text = "No substances tracked yet.",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Click the button below to add your first substance or habit.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.substances) { item ->
                    SubstanceItemCard(
                        item = item,
                        onSetPrimaryClick = { onSetPrimaryClick(item.entity.id) },
                        onEditClick = { onEditClick(item.entity) },
                        onDeleteClick = { onDeleteClick(item.entity) }
                    )
                }
            }
        }
    }
}

@Composable
fun SubstanceItemCard(
    item: SubstanceItem,
    onSetPrimaryClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val startDateStr = remember(item.entity.startDateMillis) {
        dateFormat.format(Date(item.entity.startDateMillis))
    }
    val icon = remember(item.entity.icon, item.entity.category) {
        PresetSubstance.getIconForSubstance(item.entity.icon, item.entity.category)
    }

    EInkCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (item.isPrimary) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = item.entity.name,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.entity.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (item.isPrimary) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "PRIMARY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Category: ${item.entity.category}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Row {
                IconButton(onClick = onEditClick, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit Substance",
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete Substance",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        EInkDivider()
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CURRENT SOBER DURATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "${item.duration.days}d ${item.duration.hours}h ${item.duration.minutes}m ${item.duration.seconds}s",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Started: $startDateStr",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            if (!item.isPrimary) {
                Spacer(modifier = Modifier.width(6.dp))
                EInkOutlinedButton(
                    onClick = onSetPrimaryClick,
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Make Primary", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (item.entity.initialNote.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Note: ${item.entity.initialNote}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SubstancesPreview() {
    InkSoberTheme {
        SubstancesContent(
            uiState = SubstancesUiState(
                substances = listOf(
                    SubstanceItem(
                        entity = SubstanceEntity(1, "Alcohol", System.currentTimeMillis() - 86400000L * 5, "Substances", "local_bar"),
                        duration = SobrietyDuration(5, 2, 30, 10),
                        isPrimary = true
                    ),
                    SubstanceItem(
                        entity = SubstanceEntity(2, "Nicotine", System.currentTimeMillis() - 86400000L * 12, "Substances", "smoking_rooms"),
                        duration = SobrietyDuration(12, 10, 15, 0),
                        isPrimary = false
                    )
                )
            ),
            onAddClick = {},
            onSetPrimaryClick = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}
