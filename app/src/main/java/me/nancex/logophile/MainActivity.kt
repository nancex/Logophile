package me.nancex.logophile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import me.nancex.logophile.ui.navigation.AppNavGraph
import me.nancex.logophile.ui.theme.LogophileTheme
import me.nancex.logophile.ui.theme.SettingsManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsManager = SettingsManager(this)

        setContent {
            val theme by settingsManager.themeFlow.collectAsState(initial = me.nancex.logophile.ui.theme.AppTheme.LIGHT)
            val font by settingsManager.fontFlow.collectAsState(initial = me.nancex.logophile.ui.theme.AppFont.DEFAULT)

            LogophileTheme(theme = theme, font = font) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                }
            }
        }
    }
}

