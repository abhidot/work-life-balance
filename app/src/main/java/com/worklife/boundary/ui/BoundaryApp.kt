package com.worklife.boundary.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.worklife.boundary.ui.screens.BlocklistScreen
import com.worklife.boundary.ui.screens.EntryDetailScreen
import com.worklife.boundary.ui.screens.HomeScreen
import com.worklife.boundary.ui.screens.OnboardingScreen
import com.worklife.boundary.ui.screens.SettingsScreen
import com.worklife.boundary.ui.screens.WorkHoursScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val WORK_HOURS = "work_hours"
    const val BLOCKLIST = "blocklist"
    const val ENTRY = "entry/{id}"
    const val SETTINGS = "settings"

    fun entry(id: Long) = "entry/$id"
}

@Composable
fun BoundaryApp(viewModel: BoundaryViewModel = viewModel()) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings

    val start = if (settings?.onboardingComplete == true) Routes.HOME else Routes.ONBOARDING

    NavHost(navController = navController, startDestination = start) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                viewModel = viewModel,
                onComplete = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                uiState = uiState,
                viewModel = viewModel,
                onWorkHours = { navController.navigate(Routes.WORK_HOURS) },
                onBlocklist = { navController.navigate(Routes.BLOCKLIST) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.WORK_HOURS) {
            val s = settings ?: return@composable
            WorkHoursScreen(settings = s, viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.BLOCKLIST) {
            BlocklistScreen(
                entries = uiState.blocklist,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEntry = { navController.navigate(Routes.entry(it)) },
            )
        }
        composable(
            route = Routes.ENTRY,
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { backStack ->
            val id = backStack.arguments?.getLong("id") ?: return@composable
            val entry = uiState.blocklist.find { it.id == id } ?: return@composable
            EntryDetailScreen(
                entry = entry,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onDeleted = {
                    navController.popBackStack(Routes.BLOCKLIST, false)
                },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                settings = settings,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
