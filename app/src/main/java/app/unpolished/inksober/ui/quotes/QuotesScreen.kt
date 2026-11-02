package app.unpolished.inksober.ui.quotes

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.unpolished.inksober.data.local.entity.RecoveryQuoteEntity
import app.unpolished.inksober.ui.components.*
import app.unpolished.inksober.ui.theme.AmberMilestone
import app.unpolished.inksober.ui.theme.InkSoberTheme

@Composable
fun QuotesScreen(
    viewModel: QuotesViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showAddQuoteDialog by remember { mutableStateOf(false) }

    QuotesContent(
        uiState = uiState,
        onSearchQueryChange = { viewModel.setSearchQuery(it) },
        onCopyQuote = { quote -> copyQuoteToClipboard(context, quote) },
        onShareQuote = { quote -> shareQuote(context, quote) },
        onAddQuoteClick = { showAddQuoteDialog = true },
        modifier = modifier
    )

    if (showAddQuoteDialog) {
        AddQuoteDialog(
            onDismiss = { showAddQuoteDialog = false },
            onConfirm = { text, author ->
                viewModel.addCustomQuote(text, author)
                showAddQuoteDialog = false
            }
        )
    }
}

@Composable
fun QuotesContent(
    uiState: QuotesUiState,
    onSearchQueryChange: (String) -> Unit,
    onCopyQuote: (RecoveryQuoteEntity) -> Unit,
    onShareQuote: (RecoveryQuoteEntity) -> Unit,
    onAddQuoteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Top Header Action Button (No floating FAB overlapping cards)
        item {
            EInkButton(
                onClick = onAddQuoteClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Custom Quote", fontWeight = FontWeight.Bold)
            }
        }

        item {
            Text(
                text = "RECOVERY QUOTES (${uiState.quotes.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Search Bar
        item {
            EInkOutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search quotes or authors...", fontSize = 12.sp) },
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

        if (uiState.quotes.isEmpty()) {
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
                            text = "No quotes match your search.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Add Custom Quote' above to save your favorite recovery quotes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(uiState.quotes) { quote ->
                QuoteItemCard(
                    quote = quote,
                    onCopyQuote = { onCopyQuote(quote) },
                    onShareQuote = { onShareQuote(quote) }
                )
            }
        }
    }
}

@Composable
fun QuoteItemCard(
    quote: RecoveryQuoteEntity,
    onCopyQuote: () -> Unit,
    onShareQuote: () -> Unit
) {
    EInkCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "\"${quote.quoteText}\"",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "— ${quote.author}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AmberMilestone,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row {
                    IconButton(onClick = onCopyQuote, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Copy Quote",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onShareQuote, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share Quote",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddQuoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (quoteText: String, author: String) -> Unit
) {
    var quoteText by remember { mutableStateOf("") }
    var authorText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Custom Quote",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EInkOutlinedTextField(
                    value = quoteText,
                    onValueChange = { quoteText = it },
                    label = { Text("Quote Text") },
                    placeholder = { Text("e.g. One day at a time.") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                EInkOutlinedTextField(
                    value = authorText,
                    onValueChange = { authorText = it },
                    label = { Text("Author / Source") },
                    placeholder = { Text("e.g. Anonymous / Literature") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            EInkButton(
                onClick = {
                    if (quoteText.isNotBlank()) {
                        onConfirm(
                            quoteText.trim(),
                            if (authorText.isBlank()) "Anonymous" else authorText.trim()
                        )
                    }
                },
                enabled = quoteText.isNotBlank()
            ) {
                Text("Add Quote", fontWeight = FontWeight.Bold)
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

private fun copyQuoteToClipboard(context: Context, quote: RecoveryQuoteEntity) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Recovery Quote", "\"${quote.quoteText}\" — ${quote.author}")
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Quote copied to clipboard", Toast.LENGTH_SHORT).show()
}

private fun shareQuote(context: Context, quote: RecoveryQuoteEntity) {
    val shareText = "\"${quote.quoteText}\" — ${quote.author}\n\nShared via SoberSide"
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Quote"))
}

@Preview(showBackground = true)
@Composable
fun QuotesPreview() {
    InkSoberTheme {
        QuotesContent(
            uiState = QuotesUiState(
                quotes = listOf(
                    RecoveryQuoteEntity(
                        id = 1,
                        quoteText = "One day at a time.",
                        author = "AA Literature"
                    )
                )
            ),
            onSearchQueryChange = {},
            onCopyQuote = {},
            onShareQuote = {},
            onAddQuoteClick = {}
        )
    }
}
