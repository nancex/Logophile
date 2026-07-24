package me.nancex.logophile.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    companion object {
        val THEME_KEY = intPreferencesKey("app_theme")
        val FONT_KEY = intPreferencesKey("app_font")
        val LANGUAGE_KEY = stringPreferencesKey("app_language")
        val ORDER_KEY = stringPreferencesKey("wordbank_order")
        val MODE_KEY = stringPreferencesKey("wordbank_mode")
    }

    val themeFlow: Flow<AppTheme> = context.dataStore.data.map { prefs ->
        AppTheme.entries.getOrElse(prefs[THEME_KEY] ?: 0) { AppTheme.LIGHT }
    }

    val fontFlow: Flow<AppFont> = context.dataStore.data.map { prefs ->
        AppFont.entries.getOrElse(prefs[FONT_KEY] ?: 0) { AppFont.DEFAULT }
    }

    val languageFlow: Flow<AppLanguage> = context.dataStore.data.map { prefs ->
        val code = prefs[LANGUAGE_KEY] ?: "zh"
        AppLanguage.entries.find { it.code == code } ?: AppLanguage.CHINESE
    }

    val orderFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[ORDER_KEY] ?: "alpha"
    }

    val modeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[MODE_KEY] ?: "word"
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = theme.ordinal
        }
    }

    suspend fun setFont(font: AppFont) {
        context.dataStore.edit { prefs ->
            prefs[FONT_KEY] = font.ordinal
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = language.code
        }
    }

    suspend fun setOrder(order: String) {
        context.dataStore.edit { prefs ->
            prefs[ORDER_KEY] = order
        }
    }

    suspend fun setMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[MODE_KEY] = mode
        }
    }
}