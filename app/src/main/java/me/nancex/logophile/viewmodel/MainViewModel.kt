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
    val isRevisit: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "LogophileVM"
    }

    private val repository = (application as LogophileApp).repository

    private val _addWordState = MutableStateFlow(AddWordState())
    val addWordState: StateFlow<AddWordState> = _addWordState.asStateFlow()

    private val _memoryState = MutableStateFlow(MemoryState())
    val memoryState: StateFlow<MemoryState> = _memoryState.asStateFlow()

    private var wordList: List<WordEntry> = emptyList()
    private var previousWords = mutableListOf<WordEntry>()
    private var revisitIndex = -1

    init {
        viewModelScope.launch {
            repository.allWords.collect { words ->
                wordList = words
                if (_memoryState.value.currentWord == null && words.isNotEmpty()) {
                    _memoryState.value = _memoryState.value.copy(
                        currentWord = words.firstOrNull(),
                        wordCount = words.size,
                        currentIndex = 0,
                        hasPrevious = previousWords.isNotEmpty()
                    )
                } else {
                    _memoryState.value = _memoryState.value.copy(wordCount = words.size)
                }
            }
        }
    }

    fun updateAddWordInput(input: String) {
        val wasModified = _addWordState.value.hasResult &&
                input != _addWordState.value.searchedWord
        _addWordState.value = _addWordState.value.copy(
            input = input,
            alreadyExists = false,
            errorMessage = null,
            wasModifiedAfterSearch = wasModified
        )
    }

    fun clearAddWordState() {
        _addWordState.value = AddWordState()
    }

    fun fetchWordDefinition() {
        val word = _addWordState.value.input.trim()
        if (word.isBlank()) return

        viewModelScope.launch {
            _addWordState.value = _addWordState.value.copy(isLoading = true, errorMessage = null)

            val existing = repository.findByWordAndLanguage(word.lowercase(), "eng")
            if (existing != null) {
                _addWordState.value = _addWordState.value.copy(
                    isLoading = false,
                    alreadyExists = true,
                    hasResult = false,
                    phonetic = null,
                    definitionJson = null,
                    definitionDisplay = emptyList(),
                    audioUrl = null,
                    searchedWord = word,
                    wasModifiedAfterSearch = false
                )
                return@launch
            }

            val result = repository.fetchWordDefinition(word.lowercase())
            if (result != null) {
                val (phonetic, defJson, audioUrl) = result
                val defDisplay = repository.parseDefinitionToDisplayText(defJson)
                val hasResult = phonetic != null || !defDisplay.isNullOrEmpty()

                _addWordState.value = _addWordState.value.copy(
                    isLoading = false,
                    phonetic = phonetic,
                    definitionJson = defJson,
                    definitionDisplay = defDisplay,
                    audioUrl = audioUrl,
                    hasResult = hasResult,
                    alreadyExists = false,
                    searchedWord = word,
                    wasModifiedAfterSearch = false
                )
            } else {
                _addWordState.value = _addWordState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to fetch word definition. Check network.",
                    searchedWord = "",
                    wasModifiedAfterSearch = false
                )
            }
        }
    }

    fun addWord() {
        val state = _addWordState.value
        val word = state.input.trim()
        if (word.isBlank() || !state.hasResult || state.alreadyExists || state.wasModifiedAfterSearch) return

        viewModelScope.launch {
            val newWord = WordEntry(
                word = word.lowercase(),
                language = "eng",
                phonetic = state.phonetic,
                definition = state.definitionJson,
                audioUrl = state.audioUrl,
                addedTime = System.currentTimeMillis()
            )
            val id = repository.insertWord(newWord)
            Log.d(TAG, "=== Word Added to DB ===")
            Log.d(TAG, "  id          = $id")
            Log.d(TAG, "  language    = ${newWord.language}")
            Log.d(TAG, "  word        = ${newWord.word}")
            Log.d(TAG, "  phonetic    = ${newWord.phonetic}")
            Log.d(TAG, "  definition  = ${newWord.definition}")
            Log.d(TAG, "  passCount   = ${newWord.passCount}")
            Log.d(TAG, "  tipCount    = ${newWord.tipCount}")
            Log.d(TAG, "  addedTime   = ${newWord.addedTime}")
            Log.d(TAG, "  audioUrl    = ${newWord.audioUrl}")
            Log.d(TAG, "=======================")
            clearAddWordState()
        }
    }

    fun passWord() {
        val current = _memoryState.value.currentWord ?: return
        val isRevisit = _memoryState.value.isRevisit
        viewModelScope.launch {
            if (!isRevisit) {
                repository.incrementPassCount(current.id)
            }
            previousWords.add(current)
            showNextWord()
        }
    }

    fun showTip() {
        val current = _memoryState.value.currentWord ?: return
        _memoryState.value = _memoryState.value.copy(isShowingTip = true)
        viewModelScope.launch {
            repository.incrementTipCount(current.id)
        }
    }

    fun showPrevious() {
        if (previousWords.isEmpty()) return
        revisitIndex = previousWords.lastIndex
        val prevWord = previousWords.removeAt(revisitIndex)
        _memoryState.value = _memoryState.value.copy(
            currentWord = prevWord,
            isShowingTip = false,
            isRevisit = true,
            hasPrevious = previousWords.isNotEmpty()
        )
    }

    private fun showNextWord() {
        if (wordList.isEmpty()) {
            _memoryState.value = _memoryState.value.copy(
                currentWord = null,
                isShowingTip = false,
                currentIndex = 0,
                hasPrevious = previousWords.isNotEmpty(),
                isRevisit = false
            )
            return
        }
        val currentIndex = _memoryState.value.currentIndex
        val nextIndex = (currentIndex + 1) % wordList.size.coerceAtLeast(1)
        _memoryState.value = _memoryState.value.copy(
            currentWord = wordList[nextIndex.coerceIn(0, wordList.lastIndex)],
            isShowingTip = false,
            currentIndex = nextIndex.coerceIn(0, wordList.lastIndex),
            hasPrevious = previousWords.isNotEmpty(),
            isRevisit = false
        )
    }

    fun deleteWord(word: WordEntry) {
        viewModelScope.launch {
            repository.deleteWord(word)
            if (_memoryState.value.currentWord?.id == word.id) {
                showNextWord()
            }
            previousWords.removeAll { it.id == word.id }
        }
    }
}
