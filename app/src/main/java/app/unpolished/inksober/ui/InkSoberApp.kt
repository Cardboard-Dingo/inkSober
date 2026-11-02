package app.unpolished.inksober.ui

import android.view.KeyEvent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import app.unpolished.inksober.InkSoberApplication
import app.unpolished.inksober.R
import app.unpolished.inksober.navigation.*
import app.unpolished.inksober.ui.dashboard.DashboardScreen
import app.unpolished.inksober.ui.dashboard.DashboardViewModel
import app.unpolished.inksober.ui.journal.JournalScreen
import app.unpolished.inksober.ui.journal.JournalViewModel
import app.unpolished.inksober.ui.quotes.QuotesScreen
import app.unpolished.inksober.ui.quotes.QuotesViewModel
import app.unpolished.inksober.ui.substances.SubstancesScreen
import app.unpolished.inksober.ui.substances.SubstancesViewModel
import app.unpolished.inksober.ui.theme.InkSoberTheme

data class BottomNavItem(
    val route: NavKey,
    val title: String,
    val shortTitle: String,
    val icon: ImageVector,
    val keyShortcut: Int
)

@Composable
fun InkSoberApp() {
    val context = LocalContext.current
    val app = context.applicationContext as InkSoberApplication

    val backStack: NavBackStack<NavKey> = rememberNavBackStack(DashboardRoute)

    val currentRoute = backStack.lastOrNull() ?: DashboardRoute

    val navItems = listOf(
        BottomNavItem(DashboardRoute, "Dashboard", "Dash", Icons.Rounded.Dashboard, KeyEvent.KEYCODE_1),
        BottomNavItem(SubstancesRoute, "Substances", "Items", Icons.Rounded.Category, KeyEvent.KEYCODE_2),
        BottomNavItem(JournalRoute(), "Journal", "Journal", Icons.AutoMirrored.Rounded.Notes, KeyEvent.KEYCODE_3),
        BottomNavItem(QuotesRoute, "Quotes", "Quotes", Icons.Rounded.FormatQuote, KeyEvent.KEYCODE_4)
    )

    fun navigateToTab(targetRoute: NavKey) {
        val isSelected = when (targetRoute) {
            DashboardRoute -> currentRoute == DashboardRoute
            SubstancesRoute -> currentRoute == SubstancesRoute
            is JournalRoute -> currentRoute is JournalRoute
            QuotesRoute -> currentRoute == QuotesRoute
            else -> false
        }
        if (!isSelected) {
            backStack.clear()
            backStack.plusAssign(targetRoute)
        }
    }

    InkSoberTheme {
        Scaffold(
            modifier = Modifier.onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    val nativeEvent = keyEvent.nativeKeyEvent
                    when (nativeEvent.keyCode) {
                        KeyEvent.KEYCODE_1 -> { navigateToTab(DashboardRoute); true }
                        KeyEvent.KEYCODE_2 -> { navigateToTab(SubstancesRoute); true }
                        KeyEvent.KEYCODE_3 -> { navigateToTab(JournalRoute()); true }
                        KeyEvent.KEYCODE_4 -> { navigateToTab(QuotesRoute); true }
                        else -> false
                    }
                } else false
            },
            topBar = {
                // Ultra-Sleek 32dp Header Strip
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_tree_of_life),
                                contentDescription = "Emblem",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = when (currentRoute) {
                                    DashboardRoute -> "SOBERSIDE • DASHBOARD"
                                    SubstancesRoute -> "SOBERSIDE • SUBSTANCES"
                                    is JournalRoute -> "SOBERSIDE • JOURNAL"
                                    QuotesRoute -> "SOBERSIDE • QUOTES"
                                    else -> "SOBERSIDE"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            },
            bottomBar = {
                // Ultra-Low Profile Navigation Bar (reclaims content height, no huge green pills)
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Column {
                        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            navItems.forEach { item ->
                                val isSelected = when (item.route) {
                                    DashboardRoute -> currentRoute == DashboardRoute
                                    SubstancesRoute -> currentRoute == SubstancesRoute
                                    is JournalRoute -> currentRoute is JournalRoute
                                    QuotesRoute -> currentRoute == QuotesRoute
                                    else -> false
                                }

                                val activeColor = MaterialTheme.colorScheme.primary
                                val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable { navigateToTab(item.route) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.title,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (isSelected) activeColor else inactiveColor
                                        )
                                        Spacer(modifier = Modifier.height(1.dp))
                                        Text(
                                            text = item.shortTitle,
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = if (isSelected) activeColor else inactiveColor,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            NavDisplay<NavKey>(
                backStack = backStack,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                transitionSpec = { ContentTransform(EnterTransition.None, ExitTransition.None) },
                popTransitionSpec = { ContentTransform(EnterTransition.None, ExitTransition.None) },
                entryProvider = { key ->
                    NavEntry(key) {
                        when (key) {
                            DashboardRoute -> {
                                val dashboardViewModel: DashboardViewModel = viewModel(
                                    factory = AppViewModelFactory.createDashboardViewModelFactory(app)
                                )
                                DashboardScreen(
                                    viewModel = dashboardViewModel,
                                    onNavigateToSubstances = {
                                        backStack.clear()
                                        backStack.plusAssign(SubstancesRoute)
                                    },
                                    onNavigateToJournal = { preselectedSubstanceId ->
                                        backStack.clear()
                                        backStack.plusAssign(JournalRoute(preselectedSubstanceId))
                                    }
                                )
                            }

                            SubstancesRoute -> {
                                val substancesViewModel: SubstancesViewModel = viewModel(
                                    factory = AppViewModelFactory.createSubstancesViewModelFactory(app)
                                )
                                SubstancesScreen(
                                    viewModel = substancesViewModel
                                )
                            }

                            is JournalRoute -> {
                                val journalViewModel: JournalViewModel = viewModel(
                                    factory = AppViewModelFactory.createJournalViewModelFactory(app)
                                )
                                JournalScreen(
                                    viewModel = journalViewModel,
                                    preselectedSubstanceId = key.preselectedSubstanceId
                                )
                            }

                            QuotesRoute -> {
                                val quotesViewModel: QuotesViewModel = viewModel(
                                    factory = AppViewModelFactory.createQuotesViewModelFactory(app)
                                )
                                QuotesScreen(
                                    viewModel = quotesViewModel
                                )
                            }

                            else -> {
                                Text("Unknown destination")
                            }
                        }
                    }
                }
            )
        }
    }
}
