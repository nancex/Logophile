package me.nancex.logophile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import me.nancex.logophile.ui.screens.about.AboutScreen
import me.nancex.logophile.ui.screens.importexport.ImportExportScreen
import me.nancex.logophile.ui.screens.main.MainScreen
import me.nancex.logophile.ui.screens.settings.SettingsScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) },
                onNavigateToImportExport = { navController.navigate(Screen.ImportExport.route) }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ImportExport.route) {
            ImportExportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
