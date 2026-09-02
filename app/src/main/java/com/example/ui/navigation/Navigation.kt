package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.entries.EntriesScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.loans.LoanDetailScreen
import com.example.ui.screens.loans.LoanScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.report.ReportScreen
import com.example.ui.theme.PrimaryCoral
import com.example.viewmodel.HisabViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "হোম", Icons.Rounded.Home)
    data object Entries : Screen("entries", "হিসাব", Icons.Rounded.ReceiptLong)
    data object Loans : Screen("loans", "ধার-দেনা", Icons.Rounded.Handshake)
    data object Reports : Screen("reports", "রিপোর্ট", Icons.Rounded.BarChart)
    data object Profile : Screen("profile", "প্রোফাইল", Icons.Rounded.AccountCircle)
    data object Auth : Screen("auth", "লগইন", Icons.Rounded.AccountCircle)
    data object LoanDetail : Screen("loan_detail/{loanId}", "ধারের হিসাব", Icons.Rounded.Handshake) {
        fun createRoute(loanId: String) = "loan_detail/$loanId"
    }
    data object Recurring : Screen("recurring", "নিয়মিত শিডিউল", Icons.Rounded.DateRange)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Entries,
    Screen.Loans,
    Screen.Reports,
    Screen.Profile
)

@Composable
fun MainAppNavigation(
    viewModel: HisabViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryCoral,
                                selectedTextColor = PrimaryCoral,
                                indicatorColor = PrimaryCoral.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. Home Screen
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToEntries = {
                        navController.navigate(Screen.Entries.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onNavigateToRecurring = {
                        navController.navigate(Screen.Recurring.route)
                    },
                    onEntryClick = {
                        navController.navigate(Screen.Entries.route)
                    }
                )
            }

            // 2. Entries Screen
            composable(Screen.Entries.route) {
                EntriesScreen(viewModel = viewModel)
            }

            // 3. Loans Screen
            composable(Screen.Loans.route) {
                LoanScreen(
                    viewModel = viewModel,
                    onNavigateToLoanDetail = { loanId ->
                        navController.navigate(Screen.LoanDetail.createRoute(loanId))
                    }
                )
            }

            // 4. Reports Screen
            composable(Screen.Reports.route) {
                ReportScreen(viewModel = viewModel)
            }

            // 5. Profile Screen
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToAuth = {
                        navController.navigate(Screen.Auth.route)
                    },
                    onNavigateToRecurring = {
                        navController.navigate(Screen.Recurring.route)
                    }
                )
            }

            // 6. Recurring Schedule Screen
            composable(Screen.Recurring.route) {
                com.example.ui.screens.recurring.RecurringScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 6. Auth Screen
            composable(Screen.Auth.route) {
                AuthScreen(
                    onGoogleSignInSuccess = { id, name, email ->
                        viewModel.updateUserProfile(id, name, email)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    },
                    onContinueAsGuest = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }

            // 7. Loan Detail Screen
            composable(
                route = Screen.LoanDetail.route,
                arguments = listOf(navArgument("loanId") { type = NavType.StringType })
            ) { backStackEntry ->
                val loanId = backStackEntry.arguments?.getString("loanId") ?: ""
                LoanDetailScreen(
                    loanId = loanId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
