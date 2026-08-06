package me.nancex.logophile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.nancex.logophile.LogophileApp
import me.nancex.logophile.data.local.WordEntry
import me.nancex.logophile.data.repository.WordRepository
import me.nancex.logophile.ui.theme.SettingsManager
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

    private val app = application
    private val repository = (application as LogophileApp).repository
    private val engine = MemoryEngine()
    private val settingsManager = SettingsManager(application)

    private val _addWordState = MutableStateFlow(AddWordState())
    val addWordState: StateFlow<AddWordState> = _addWordState.asStateFlow()

    private val _memoryState = MutableStateFlow(MemoryState())
    val memoryState: StateFlow<MemoryState> = _memoryState.asStateFlow()

    private var wordList: List<WordEntry> = emptyList()
    private val previousWords = mutableListOf<WordEntry>()
    private val forwardWords = mutableListOf<WordEntry>()
    private var stateLoaded = false

    init {
        viewModelScope.launch {
            val savedRange = try {
                settingsManager.memoryTimeRangeFlow.first()
            } catch (e: Exception) {
                TimeRange.ALL
            }
            _memoryState.value = _memoryState.value.copy(timeRange = savedRange)

            repository.allWords.collect { words ->
                wordList = words

                if (!stateLoaded && words.isNotEmpty()) {
                    restoreState(words)
                    stateLoaded = true
                    return@collect
                }

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
                        saveState()
                    }
                } else if (current != null && words.none { it.id == current.id }) {
                    previousWords.removeAll { it.id == current.id }
                    forwardWords.removeAll { it.id == current.id }
                    engine.removeWord(current.id)
                    pickNextWord()
                } else {
                    val filtered = filterByTimeRange(words)
                    _memoryState.value = _memoryState.value.copy(wordCount = filtered.size)
                }
            }
        }
    }

    private suspend fun restoreState(words: List<WordEntry>) {
        val saved = settingsManager.getMemoryState()
        val wordById = words.associateBy { it.id }

        saved.queueIds.forEach { id -> wordById[id]?.let { engine.enqueueIfTipShown(it, true) } }
        saved.previousIds.forEach { id -> wordById[id]?.let { previousWords.add(it) } }
        saved.forwardIds.forEach { id -> wordById[id]?.let { forwardWords.add(it) } }

        val currentWord = saved.currentId.let { wordById[it] }
        if (currentWord != null) {
            val filtered = filterByTimeRange(words)
            val idx = filtered.indexOfFirst { it.id == currentWord.id }.coerceAtLeast(0)
            _memoryState.value = _memoryState.value.copy(
                currentWord = currentWord, wordCount = filtered.size,
                currentIndex = idx, hasPrevious = previousWords.isNotEmpty(),
                isShowingTip = saved.tipWasShown)
        } else {
            val filtered = filterByTimeRange(words)
            if (filtered.isNotEmpty()) {
                _memoryState.value = _memoryState.value.copy(
                    currentWord = filtered.first(), wordCount = filtered.size, currentIndex = 0)
            }
        }
    }

    private fun saveState() {
        viewModelScope.launch {
            val current = _memoryState.value
            settingsManager.saveMemoryState(
                currentId = current.currentWord?.id ?: -1,
                queueIds = engine.getQueueIds(),
                previousIds = previousWords.map { it.id },
                forwardIds = forwardWords.map { it.id },
                tipWasShown = current.isShowingTip)
        }
    }

    fun setTimeRange(range: TimeRange) {
        val current = _memoryState.value
        if (current.timeRange == range) return
        engine.clearQueue()
        forwardWords.clear()
        previousWords.clear()
        _memoryState.value = current.copy(timeRange = range)
        viewModelScope.launch { pickNextWord(); saveState() }
    }

    private fun filterByTimeRange(words: List<WordEntry>): List<WordEntry> {
        val range = _memoryState.value.timeRange
        val cutoff = range.days?.let { System.currentTimeMillis() - it * 86_400_000L }
        return if (cutoff != null) words.filter { it.addedTime >= cutoff } else words
    }

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
            val result = repository.fetchWordDefinition(word.lowercase()) { retry, max ->
                _addWordState.value = _addWordState.value.copy(
                    errorMessage = app.getString(R.string.dict_retrying, retry, max)
                )
            }
            if (result.error != null) {
                _addWordState.value = _addWordState.value.copy(
                    isLoading = false,
                    errorMessage = result.error,
                    searchedWord = "", wasModifiedAfterSearch = false)
                return@launch
            }
            val data = result.data!!
            val defDisplay = repository.parseDefinitionToDisplayText(data.definitionJson)
            val hasResult = data.phonetic != null || defDisplay.isNotEmpty()
            _addWordState.value = _addWordState.value.copy(
                isLoading = false, phonetic = data.phonetic, definitionJson = data.definitionJson,
                definitionDisplay = defDisplay, audioUrl = data.audioUrl,
                hasResult = hasResult, alreadyExists = false,
                searchedWord = word, wasModifiedAfterSearch = false)
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
            repository.insertWord(newWord)
            clearAddWordState()
        }
    }

    fun passWord() {
        val current = _memoryState.value.currentWord ?: return
        val isRevisit = _memoryState.value.isRevisit
        val tipWasShown = _memoryState.value.isShowingTip
        viewModelScope.launch {
            if (!isRevisit) {
                repository.incrementPassCount(current.id)
                engine.enqueueIfTipShown(current, tipWasShown)
                forwardWords.clear()
            }
            previousWords.add(current)
            while (previousWords.size > MAX_PREVIOUS) { previousWords.removeAt(0) }

            if (isRevisit && forwardWords.isNotEmpty()) {
                val next = forwardWords.removeAt(forwardWords.lastIndex)
                val stillRevisiting = forwardWords.isNotEmpty()
                val filtered = filterByTimeRange(repository.getAllWordsList())
                val idx = filtered.indexOfFirst { it.id == next.id }.coerceAtLeast(0)
                _memoryState.value = _memoryState.value.copy(
                    currentWord = next, isShowingTip = false,
                    currentIndex = idx, hasPrevious = true, isRevisit = stillRevisiting)
            } else {
                pickNextWord()
            }
            saveState()
        }
    }

    fun showTip() {
        val current = _memoryState.value.currentWord ?: return
        _memoryState.value = _memoryState.value.copy(isShowingTip = true)
        viewModelScope.launch { repository.incrementTipCount(current.id) }
        viewModelScope.launch { saveState() }
    }

    fun showPrevious() {
        if (previousWords.isEmpty()) return
        val prevWord = previousWords.removeAt(previousWords.lastIndex)
        val current = _memoryState.value.currentWord
        if (current != null) forwardWords.add(current)
        _memoryState.value = _memoryState.value.copy(
            currentWord = prevWord, isShowingTip = false,
            isRevisit = true, hasPrevious = previousWords.isNotEmpty())
        viewModelScope.launch { saveState() }
    }

    fun resetAllCounts() {
        viewModelScope.launch {
            repository.resetAllCounts()
            engine.clearQueue()
            forwardWords.clear()
            settingsManager.clearMemoryState()
        }
    }

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
            currentWord = selected, isShowingTip = false,
            currentIndex = idx, hasPrevious = previousWords.isNotEmpty(), isRevisit = false)
    }

    fun deleteWord(word: WordEntry) {
        viewModelScope.launch {
            repository.deleteWord(word)
            engine.removeWord(word.id)
            forwardWords.removeAll { it.id == word.id }
            if (_memoryState.value.currentWord?.id == word.id) { pickNextWord() }
            previousWords.removeAll { it.id == word.id }
            saveState()
        }
    }
}