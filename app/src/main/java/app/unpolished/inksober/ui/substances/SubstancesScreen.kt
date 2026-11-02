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
                    style = MaterialTheme.typography.bodyMedium
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
            shape = RoundedCornerShape(8.dp)
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
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
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
                Text("Add New Substance", fontWeight = FontWeight.Bold)
            }
        }

        item {
            Text(
                text = "TRACKED SUBSTANCES (${uiState.substances.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        if (uiState.substances.isEmpty()) {
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
                            text = "No substances tracked yet.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Add New Substance' above to start tracking.",
                            style = MaterialTheme.typography.bodySmall,
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

@Composable
fun SubstanceItemCard(
    item: SubstanceItem,
    onSetPrimaryClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
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
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.entity.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (item.isPrimary) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Primary",
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "PRIMARY",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Category: ${item.entity.category}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit Substance",
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete Substance",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        EInkDivider()
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SOBRIETY DURATION",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${item.duration.totalDays} Days (${item.duration.formatFormattedString()})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Started: $startDateStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!item.isPrimary) {
                Spacer(modifier = Modifier.width(6.dp))
                EInkOutlinedButton(
                    onClick = onSetPrimaryClick,
                    modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                ) {
                    Text("Make Primary", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                }
            }
        }

        if (item.entity.initialNote.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Note: ${item.entity.initialNote}",
                style = MaterialTheme.typography.bodySmall,
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
                        duration = SobrietyDuration(days = 5, months = 0, years = 0, totalDays = 5),
                        isPrimary = true
                    ),
                    SubstanceItem(
                        entity = SubstanceEntity(2, "Nicotine", System.currentTimeMillis() - 86400000L * 12, "Substances", "smoking_rooms"),
                        duration = SobrietyDuration(days = 12, months = 0, years = 0, totalDays = 12),
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
