package me.nancex.logophile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.nancex.logophile.LogophileApp
import me.nancex.logophile.data.local.WordEntry
import me.nancex.logophile.data.repository.WordRepository
import me.nancex.logophile.ui.theme.TimeRange

data class AddWordState(
    val input: String = "",
    val phonetic: String? = null,
    val definitionJson: String? = null,
    val definitionDisplay: List<Pair<String, String>> = emptyList(),
    val audioUrl: String? = null,
    val isLoading: Boolean = false,
    val alreadyExists: Boolean = false,
    val hasResult: Boolean = false,
    val errorMessage: String? = null,
    val searchedWord: String = "",
    val wasModifiedAfterSearch: Boolean = false
)

data class MemoryState(
    val currentWord: WordEntry? = null,
    val isShowingTip: Boolean = false,
    val wordCount: Int = 0,
    val currentIndex: Int = 0,
    val hasPrevious: Boolean = false,
    val isRevisit: Boolean = false,
    val timeRange: TimeRange = TimeRange.ALL
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MainViewModel"
        private const val MAX_PREVIOUS = 2
    }

    private val repository = (application as LogophileApp).repository
    private val engine = MemoryEngine()

    private val _addWordState = MutableStateFlow(AddWordState())
    val addWordState: StateFlow<AddWordState> = _addWordState.asStateFlow()

    private val _memoryState = MutableStateFlow(MemoryState())
    val memoryState: StateFlow<MemoryState> = _memoryState.asStateFlow()

    private var wordList: List<WordEntry> = emptyList()
    private val previousWords = mutableListOf<WordEntry>()

    init {
        viewModelScope.launch {
            repository.allWords.collect { words ->
                wordList = words
                val current = _memoryState.value.currentWord
                if (current == null && words.isNotEmpty()) {
                    val filtered = filterByTimeRange(words)
                    if (filtered.isNotEmpty()) {
                        _memoryState.value = _memoryState.value.copy(
                            currentWord = filtered.first(),
                            wordCount = filtered.size,
                            currentIndex = 0,
                            hasPrevious = false
                        )
                    }
                } else if (current != null && words.none { it.id == current.id }) {
                    Log.d(TAG, "collect: current word '${current.word}' deleted, advancing")
                    previousWords.removeAll { it.id == current.id }
                    engine.removeWord(current.id)
                    pickNextWord()
                } else {
                    val filtered = filterByTimeRange(words)
                    _memoryState.value = _memoryState.value.copy(wordCount = filtered.size)
                }
            }
        }
    }

    // ── Time range ─────────────────────────────────────────────────

    fun setTimeRange(range: TimeRange) {
        val current = _memoryState.value
        if (current.timeRange == range) return
        engine.clearQueue()
        _memoryState.value = current.copy(timeRange = range)
        viewModelScope.launch { pickNextWord() }
    }

    private fun filterByTimeRange(words: List<WordEntry>): List<WordEntry> {
        val range = _memoryState.value.timeRange
        val cutoff = range.days?.let { System.currentTimeMillis() - it * 86_400_000L }
        return if (cutoff != null) words.filter { it.addedTime >= cutoff } else words
    }

    // ── Add-word flow ──────────────────────────────────────────────

    fun updateAddWordInput(input: String) {
        val wasModified = _addWordState.value.hasResult && input != _addWordState.value.searchedWord
        _addWordState.value = _addWordState.value.copy(
            input = input, alreadyExists = false, errorMessage = null, wasModifiedAfterSearch = wasModified)
    }

    fun clearAddWordState() { _addWordState.value = AddWordState() }

    fun fetchWordDefinition() {
        val word = _addWordState.value.input.trim()
        if (word.isBlank()) return
        viewModelScope.launch {
            _addWordState.value = _addWordState.value.copy(isLoading = true, errorMessage = null)
            val existing = repository.findByWordAndLanguage(word.lowercase(), "eng")
            if (existing != null) {
                _addWordState.value = _addWordState.value.copy(
                    isLoading = false, alreadyExists = true, hasResult = false,
                    phonetic = null, definitionJson = null, definitionDisplay = emptyList(),
                    audioUrl = null, searchedWord = word, wasModifiedAfterSearch = false)
                return@launch
            }
            val result = repository.fetchWordDefinition(word.lowercase())
            if (result != null) {
                val (phonetic, defJson, audioUrl) = result
                val defDisplay = repository.parseDefinitionToDisplayText(defJson)
                val hasResult = phonetic != null || defDisplay.isNotEmpty()
                _addWordState.value = _addWordState.value.copy(
                    isLoading = false, phonetic = phonetic, definitionJson = defJson,
                    definitionDisplay = defDisplay, audioUrl = audioUrl,
                    hasResult = hasResult, alreadyExists = false,
                    searchedWord = word, wasModifiedAfterSearch = false)
            } else {
                _addWordState.value = _addWordState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to fetch word definition. Check network.",
                    searchedWord = "", wasModifiedAfterSearch = false)
            }
        }
    }

    fun addWord() {
        val state = _addWordState.value
        val word = state.input.trim()
        if (word.isBlank() || !state.hasResult || state.alreadyExists || state.wasModifiedAfterSearch) return
        viewModelScope.launch {
            val newWord = WordEntry(
                word = word.lowercase(), language = "eng",
                phonetic = state.phonetic, definition = state.definitionJson,
                audioUrl = state.audioUrl, addedTime = System.currentTimeMillis())
            val id = repository.insertWord(newWord)
            Log.d(TAG, "addWord: inserted id=$id word='${newWord.word}'")
            clearAddWordState()
        }
    }

    // ── Memory flow ────────────────────────────────────────────────

    fun passWord() {
        val current = _memoryState.value.currentWord ?: return
        val isRevisit = _memoryState.value.isRevisit
        val tipWasShown = _memoryState.value.isShowingTip
        viewModelScope.launch {
            if (!isRevisit) {
                repository.incrementPassCount(current.id)
                engine.enqueueIfTipShown(current, tipWasShown)
            }
            previousWords.add(current)
            while (previousWords.size > MAX_PREVIOUS) { previousWords.removeAt(0) }
            pickNextWord()
        }
    }

    fun showTip() {
        val current = _memoryState.value.currentWord ?: return
        _memoryState.value = _memoryState.value.copy(isShowingTip = true)
        viewModelScope.launch { repository.incrementTipCount(current.id) }
    }

    fun showPrevious() {
        if (previousWords.isEmpty()) return
        val prevWord = previousWords.removeAt(previousWords.lastIndex)
        _memoryState.value = _memoryState.value.copy(
            currentWord = prevWord, isShowingTip = false,
            isRevisit = true, hasPrevious = previousWords.isNotEmpty())
    }

    fun resetAllCounts() {
        viewModelScope.launch {
            repository.resetAllCounts()
            engine.clearQueue()
        }
    }

    // ── Word selection ─────────────────────────────────────────────

    private suspend fun pickNextWord() {
        val words = filterByTimeRange(repository.getAllWordsList())

        if (words.isEmpty()) {
            _memoryState.value = _memoryState.value.copy(
                currentWord = null, isShowingTip = false, currentIndex = 0,
                hasPrevious = previousWords.isNotEmpty(), isRevisit = false)
            return
        }

        val selected = engine.selectNext(words)
        val idx = words.indexOfFirst { it.id == selected.id }.coerceAtLeast(0)

        _memoryState.value = _memoryState.value.copy(
            currentWord = selected,
            isShowingTip = false,
            currentIndex = idx,
            hasPrevious = previousWords.isNotEmpty(),
            isRevisit = false)
    }

    // ── Delete ─────────────────────────────────────────────────────

    fun deleteWord(word: WordEntry) {
        viewModelScope.launch {
            repository.deleteWord(word)
            engine.removeWord(word.id)
            if (_memoryState.value.currentWord?.id == word.id) { pickNextWord() }
            previousWords.removeAll { it.id == word.id }
        }
    }
}