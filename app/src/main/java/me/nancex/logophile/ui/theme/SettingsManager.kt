package me.nancex.logophile.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
        val MEMORY_CURRENT_ID_KEY = intPreferencesKey("memory_current_id")
        val MEMORY_QUEUE_KEY = stringPreferencesKey("memory_queue")
        val MEMORY_PREVIOUS_KEY = stringPreferencesKey("memory_previous")
        val MEMORY_FORWARD_KEY = stringPreferencesKey("memory_forward")
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

    suspend fun getMemoryState(): SavedMemoryState {
        val prefs = context.dataStore.data.first()
        return SavedMemoryState(
            currentId = prefs[MEMORY_CURRENT_ID_KEY] ?: -1,
            queueIds = prefs[MEMORY_QUEUE_KEY]?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList(),
            previousIds = prefs[MEMORY_PREVIOUS_KEY]?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList(),
            forwardIds = prefs[MEMORY_FORWARD_KEY]?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
        )
    }

    suspend fun saveMemoryState(currentId: Int, queueIds: List<Int>, previousIds: List<Int>, forwardIds: List<Int>) {
        context.dataStore.edit { prefs ->
            prefs[MEMORY_CURRENT_ID_KEY] = currentId
            prefs[MEMORY_QUEUE_KEY] = queueIds.joinToString(",")
            prefs[MEMORY_PREVIOUS_KEY] = previousIds.joinToString(",")
            prefs[MEMORY_FORWARD_KEY] = forwardIds.joinToString(",")
        }
    }

    suspend fun clearMemoryState() {
        context.dataStore.edit { prefs ->
            prefs.remove(MEMORY_CURRENT_ID_KEY)
            prefs.remove(MEMORY_QUEUE_KEY)
            prefs.remove(MEMORY_PREVIOUS_KEY)
            prefs.remove(MEMORY_FORWARD_KEY)
        }
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

data class SavedMemoryState(
    val currentId: Int,
    val queueIds: List<Int>,
    val previousIds: List<Int>,
    val forwardIds: List<Int>
)