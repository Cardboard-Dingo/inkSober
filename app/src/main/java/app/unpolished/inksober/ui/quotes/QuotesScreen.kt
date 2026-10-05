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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.unpolished.inksober.data.local.entity.RecoveryQuoteEntity
import app.unpolished.inksober.ui.components.*
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
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            EInkButton(
                onClick = onAddQuoteClick,
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Quote",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Quote", fontWeight = FontWeight.Bold)
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
                        text = "RECOVERY QUOTES",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.quotes.size} Quotes",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // Search Bar
            item {
                EInkOutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search quotes or authors...") },
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

            if (uiState.quotes.isEmpty()) {
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
                                text = "No quotes match your search.",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try searching with a different term or add a new custom quote.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.quotes) { quote ->
                    QuoteItemCard(
                        quote = quote,
                        onCopy = { onCopyQuote(quote) },
                        onShare = { onShareQuote(quote) }
                    )
                }
            }
        }
    }
}

@Composable
fun QuoteItemCard(
    quote: RecoveryQuoteEntity,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    EInkCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "\"${quote.quoteText}\"",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "— ${quote.author}",
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))
        EInkDivider()
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onCopy, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = "Copy Quote",
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = onShare, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = "Share Quote",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AddQuoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Custom Quote", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EInkOutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Quote Text") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                EInkOutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author / Source") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            EInkButton(
                enabled = text.isNotBlank(),
                onClick = {
                    val finalAuthor = if (author.isBlank()) "Anonymous" else author.trim()
                    onConfirm(text.trim(), finalAuthor)
                }
            ) {
                Text("Add", fontWeight = FontWeight.Bold)
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

private fun copyQuoteToClipboard(context: Context, quote: RecoveryQuoteEntity) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Recovery Quote", "\"${quote.quoteText}\" — ${quote.author}")
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Quote copied to clipboard", Toast.LENGTH_SHORT).show()
}

private fun shareQuote(context: Context, quote: RecoveryQuoteEntity) {
    val shareText = "\"${quote.quoteText}\" — ${quote.author}\n\nShared via inkSober"
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
                    RecoveryQuoteEntity(1, "One day at a time.", "AA Literature"),
                    RecoveryQuoteEntity(2, "Progress, not perfection.", "Traditional Recovery")
                )
            ),
            onSearchQueryChange = {},
            onCopyQuote = {},
            onShareQuote = {},
            onAddQuoteClick = {}
        )
    }
}
