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
        val WORD_SORT_DIR_KEY = stringPreferencesKey("wordbank_sort_dir")
        val WORD_TIME_RANGE_KEY = stringPreferencesKey("wordbank_time_range")
        val MEMORY_TIME_RANGE_KEY = stringPreferencesKey("memory_time_range")
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

    val wordSortDirFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[WORD_SORT_DIR_KEY] ?: "asc"
    }

    val wordTimeRangeFlow: Flow<TimeRange> = context.dataStore.data.map { prefs ->
        val key = prefs[WORD_TIME_RANGE_KEY] ?: TimeRange.ALL.key
        TimeRange.entries.find { it.key == key } ?: TimeRange.ALL
    }

    val memoryTimeRangeFlow: Flow<TimeRange> = context.dataStore.data.map { prefs ->
        val key = prefs[MEMORY_TIME_RANGE_KEY] ?: TimeRange.ALL.key
        TimeRange.entries.find { it.key == key } ?: TimeRange.ALL
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { prefs -> prefs[THEME_KEY] = theme.ordinal }
    }

    suspend fun setFont(font: AppFont) {
        context.dataStore.edit { prefs -> prefs[FONT_KEY] = font.ordinal }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs -> prefs[LANGUAGE_KEY] = language.code }
    }

    suspend fun setOrder(order: String) {
        context.dataStore.edit { prefs -> prefs[ORDER_KEY] = order }
    }

    suspend fun setMode(mode: String) {
        context.dataStore.edit { prefs -> prefs[MODE_KEY] = mode }
    }

    suspend fun setWordSortDir(dir: String) {
        context.dataStore.edit { prefs -> prefs[WORD_SORT_DIR_KEY] = dir }
    }

    suspend fun setWordTimeRange(range: TimeRange) {
        context.dataStore.edit { prefs -> prefs[WORD_TIME_RANGE_KEY] = range.key }
    }

    suspend fun setMemoryTimeRange(range: TimeRange) {
        context.dataStore.edit { prefs -> prefs[MEMORY_TIME_RANGE_KEY] = range.key }
    }
}