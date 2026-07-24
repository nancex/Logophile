package me.nancex.logophile

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.nancex.logophile.ui.navigation.AppNavGraph
import me.nancex.logophile.ui.theme.AppFont
import me.nancex.logophile.ui.theme.AppLanguage
import me.nancex.logophile.ui.theme.AppTheme
import me.nancex.logophile.ui.theme.LogophileTheme
import me.nancex.logophile.ui.theme.SettingsManager

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun attachBaseContext(newBase: Context?) {
        // Apply saved locale before the activity is created
        if (newBase != null) {
            val settingsManager = SettingsManager(newBase)
            val savedLang = runBlocking { settingsManager.languageFlow.first() }
            val locale = when (savedLang) {
                AppLanguage.CHINESE -> LocaleListCompat.forLanguageTags("zh-CN")
                AppLanguage.ENGLISH -> LocaleListCompat.forLanguageTags("en-US")
            }
            AppCompatDelegate.setApplicationLocales(locale)
            Log.d(TAG, "attachBaseContext: applied locale=${savedLang.code}")
        }
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: current locale tags=${AppCompatDelegate.getApplicationLocales().toLanguageTags()}")

        enableEdgeToEdge()

        val settingsManager = SettingsManager(this)

        setContent {
            val theme by settingsManager.themeFlow.collectAsState(initial = AppTheme.LIGHT)
            val font by settingsManager.fontFlow.collectAsState(initial = AppFont.DEFAULT)

            LogophileTheme(theme = theme, font = font) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController, currentFont = font)
                }
            }
        }
    }
}
