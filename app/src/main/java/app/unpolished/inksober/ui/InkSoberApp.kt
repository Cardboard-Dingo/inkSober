package app.unpolished.inksober.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import app.unpolished.inksober.InkSoberApplication
import app.unpolished.inksober.R
import app.unpolished.inksober.navigation.*
import app.unpolished.inksober.ui.components.EInkDivider
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
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InkSoberApp() {
    val context = LocalContext.current
    val app = context.applicationContext as InkSoberApplication

    val backStack: NavBackStack<NavKey> = rememberNavBackStack(DashboardRoute)

    val currentRoute = backStack.lastOrNull() ?: DashboardRoute

    val navItems = listOf(
        BottomNavItem(DashboardRoute, "Dashboard", Icons.Rounded.Dashboard),
        BottomNavItem(SubstancesRoute, "Substances", Icons.Rounded.Category),
        BottomNavItem(JournalRoute(), "Journal", Icons.AutoMirrored.Rounded.Notes),
        BottomNavItem(QuotesRoute, "Quotes", Icons.Rounded.FormatQuote)
    )

    InkSoberTheme {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_tree_of_life),
                                    contentDescription = "Tree of Life Emblem",
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when (currentRoute) {
                                        DashboardRoute -> "INKSOBER • DASHBOARD"
                                        SubstancesRoute -> "INKSOBER • SUBSTANCES"
                                        is JournalRoute -> "INKSOBER • JOURNAL"
                                        QuotesRoute -> "INKSOBER • QUOTES"
                                        else -> "INKSOBER"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    EInkDivider()
                }
            },
            bottomBar = {
                Column {
                    EInkDivider()
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 0.dp
                    ) {
                        navItems.forEach { item ->
                            val isSelected = when (item.route) {
                                DashboardRoute -> currentRoute == DashboardRoute
                                SubstancesRoute -> currentRoute == SubstancesRoute
                                is JournalRoute -> currentRoute is JournalRoute
                                QuotesRoute -> currentRoute == QuotesRoute
                                else -> false
                            }

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (!isSelected) {
                                        backStack.clear()
                                        backStack.plusAssign(item.route)
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    indicatorColor = MaterialTheme.colorScheme.primary
                                )
                            )
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
