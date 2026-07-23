package me.nancex.logophile.ui.navigation

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object Settings : Screen("settings")
    data object About : Screen("about")
    data object ImportExport : Screen("import_export")
}
