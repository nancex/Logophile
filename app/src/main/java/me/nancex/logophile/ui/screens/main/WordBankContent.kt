package me.nancex.logophile.ui.screens.main

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.nancex.logophile.R
import me.nancex.logophile.data.local.WordEntry
import me.nancex.logophile.data.repository.WordRepository
import me.nancex.logophile.ui.theme.AppFont
import me.nancex.logophile.ui.theme.SettingsManager
import me.nancex.logophile.ui.theme.TimeRange
import me.nancex.logophile.ui.theme.getWordFontFamily
import me.nancex.logophile.ui.theme.getWordFontSizeMultiplier
import me.nancex.logophile.viewmodel.MainViewModel

private const val TAG = "WordBankContent"

private enum class SearchMode { BY_WORD, BY_DEFINITION }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordBankContent(
    viewModel: MainViewModel,
    font: AppFont = AppFont.DEFAULT
) {
    val context = LocalContext.current
    val app = context.applicationContext as me.nancex.logophile.LogophileApp
    val repository = app.repository
    val settingsManager = remember { SettingsManager(context) }
    val words by repository.allWords.collectAsState(initial = emptyList())
    val wordFont = getWordFontFamily(font)
    val fontSizeMul = getWordFontSizeMultiplier(font)
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var searchQuery by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }
    val order by settingsManager.orderFlow.collectAsState(initial = "alpha")
    val mode by settingsManager.modeFlow.collectAsState(initial = "word")
    val sortDir by settingsManager.wordSortDirFlow.collectAsState(initial = "asc")
    val wordTimeRange by settingsManager.wordTimeRangeFlow.collectAsState(initial = TimeRange.ALL)
    val devMode by settingsManager.devModeFlow.collectAsState(initial = false)
    val alphaOrder = order == "alpha"
    val sortAsc = sortDir == "asc"
    val searchMode = if (mode == "word") SearchMode.BY_WORD else SearchMode.BY_DEFINITION
    var sheetWord by remember { mutableStateOf<WordEntry?>(null) }
    var wordToDelete by remember { mutableStateOf<WordEntry?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val listState = rememberLazyListState()

    LaunchedEffect(sheetWord) {
        val w = sheetWord ?: return@LaunchedEffect
        val delta = w.passCount - w.tipCount
        val newTip = if (delta > 2) (w.passCount - 2).coerceAtLeast(0) else (w.tipCount + 1).coerceAtMost(w.passCount)
        if (newTip != w.tipCount) {
            app.repository.setTipCount(w.id, newTip)
        }
    }

    LaunchedEffect(order, sortDir, mode, wordTimeRange) {
        listState.scrollToItem(0)
    }

    val displayedWords by remember(searchQuery, words, searchMode, alphaOrder, sortAsc, wordTimeRange) {
        derivedStateOf {
            val filtered = if (searchQuery.isBlank()) words
            else if (searchMode == SearchMode.BY_WORD)
                words.filter { it.word.startsWith(searchQuery, ignoreCase = true) }
            else words.filter { it.definition?.contains(searchQuery, ignoreCase = true) == true }

            val timeCutoff = wordTimeRange.days?.let { System.currentTimeMillis() - it * 86_400_000L }
            val timeFiltered = if (timeCutoff != null) filtered.filter { it.addedTime >= timeCutoff } else filtered

            when {
                alphaOrder && sortAsc -> timeFiltered.sortedBy { it.word.lowercase() }
                alphaOrder && !sortAsc -> timeFiltered.sortedByDescending { it.word.lowercase() }
                !alphaOrder && sortAsc -> timeFiltered.sortedBy { it.addedTime }
                else -> timeFiltered.sortedByDescending { it.addedTime }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = ""; focusManager.clearFocus() }) {
                            Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.clear))
                        }
                    } else {
                        Icon(Icons.Filled.Search, contentDescription = null)
                    }
                },
                trailingIcon = {
                    Text(
                        text = displayedWords.size.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.Filled.Sort, contentDescription = stringResource(R.string.sort),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                    Column(
                        modifier = Modifier.heightIn(max = 340.dp).verticalScroll(rememberScrollState())
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_alpha)) },
                            onClick = {
                                scope.launch { settingsManager.setOrder("alpha") }
                                showSortMenu = false
                            },
                            trailingIcon = { if (alphaOrder) Icon(Icons.Filled.Check, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_date)) },
                            onClick = {
                                scope.launch { settingsManager.setOrder("date") }
                                showSortMenu = false
                            },
                            trailingIcon = { if (!alphaOrder) Icon(Icons.Filled.Check, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_asc)) },
                            onClick = {
                                scope.launch { settingsManager.setWordSortDir("asc") }
                                showSortMenu = false
                            },
                            trailingIcon = { if (sortAsc) Icon(Icons.Filled.Check, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_desc)) },
                            onClick = {
                                scope.launch { settingsManager.setWordSortDir("desc") }
                                showSortMenu = false
                            },
                            trailingIcon = { if (!sortAsc) Icon(Icons.Filled.Check, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.search_mode_word)) },
                            onClick = {
                                scope.launch { settingsManager.setMode("word") }
                                showSortMenu = false
                            },
                            trailingIcon = { if (searchMode == SearchMode.BY_WORD)
                                Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.search_mode_definition)) },
                            onClick = {
                                scope.launch { settingsManager.setMode("meaning") }
                                showSortMenu = false
                            },
                            trailingIcon = { if (searchMode == SearchMode.BY_DEFINITION)
                                Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        )
                        HorizontalDivider()
                        TimeRange.entries.forEach { range ->
                            DropdownMenuItem(
                                text = {
                                    Text(when (range) {
                                        TimeRange.ONE_DAY -> stringResource(R.string.time_1d)
                                        TimeRange.THREE_DAYS -> stringResource(R.string.time_3d)
                                        TimeRange.ONE_WEEK -> stringResource(R.string.time_1w)
                                        TimeRange.ONE_MONTH -> stringResource(R.string.time_1m)
                                        TimeRange.THREE_MONTHS -> stringResource(R.string.time_3m)
                                        TimeRange.ALL -> stringResource(R.string.time_all)
                                    })
                                },
                                onClick = {
                                    scope.launch { settingsManager.setWordTimeRange(range) }
                                    showSortMenu = false
                                },
                                trailingIcon = {
                                    if (range == wordTimeRange) Icon(Icons.Filled.Check,
                                        contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(displayedWords, key = { it.id }) { word ->
                WordListItem(
                    word = word, fontFamily = wordFont, fontSizeMultiplier = fontSizeMul,
                    showDevInfo = devMode,
                    onClick = { sheetWord = word },
                    onPlayAudio = { playAudio(word.audioUrl) }
                )
            }
        }
    }

    if (sheetWord != null) {
        ModalBottomSheet(onDismissRequest = { sheetWord = null }, sheetState = sheetState) {
            WordDetailSheet(word = sheetWord!!, repository = repository,
                onPlayAudio = { playAudio(sheetWord!!.audioUrl) },
                onDelete = { wordToDelete = sheetWord })
        }
    }

    if (wordToDelete != null) {
        AlertDialog(
            onDismissRequest = { wordToDelete = null },
            title = { Text(stringResource(R.string.confirm_delete_title)) },
            text = { Text(stringResource(R.string.confirm_delete_text, wordToDelete!!.word)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteWord(wordToDelete!!); sheetWord = null; wordToDelete = null
                }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { wordToDelete = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

private fun playAudio(audioUrl: String?) {
    if (audioUrl.isNullOrEmpty()) { Log.d(TAG, "playAudio: audioUrl is null, skipping"); return }
    Log.d(TAG, "playAudio: attempting $audioUrl")
    try {
        val mp = MediaPlayer()
        mp.setDataSource(audioUrl)
        mp.setOnPreparedListener { it.start() }
        mp.setOnErrorListener { _, what, extra -> Log.e(TAG, "playAudio: error what=$what extra=$extra"); false }
        mp.setOnCompletionListener { it.release() }
        mp.prepareAsync()
    } catch (e: Exception) { Log.e(TAG, "playAudio: exception: ${e.message}", e) }
}

@Composable
fun WordListItem(
    word: WordEntry,
    fontFamily: FontFamily = FontFamily.Default,
    fontSizeMultiplier: Float = 1f,
    showDevInfo: Boolean = false,
    onClick: () -> Unit,
    onPlayAudio: () -> Unit
) {
    val typography = MaterialTheme.typography
    val wordStyle = remember(fontFamily, fontSizeMultiplier, typography) {
        typography.bodyLarge.copy(
            fontFamily = fontFamily,
            fontSize = typography.bodyLarge.fontSize * fontSizeMultiplier
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = word.word,
                style = wordStyle,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showDevInfo) {
                    Text(
                        text = word.passCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = word.tipCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
                IconButton(onClick = onPlayAudio, enabled = !word.audioUrl.isNullOrEmpty(),
                    modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = stringResource(R.string.play_audio),
                        modifier = Modifier.size(22.dp),
                        tint = if (!word.audioUrl.isNullOrEmpty()) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                }
                Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.more),
                    modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailSheet(
    word: WordEntry, repository: WordRepository,
    onPlayAudio: () -> Unit, onDelete: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Text(text = word.word, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        if (!word.phonetic.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = word.phonetic, style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!word.audioUrl.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onPlayAudio, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.VolumeUp, contentDescription = stringResource(R.string.play_audio),
                            modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        val defDisplay = repository.parseDefinitionToDisplayText(word.definition)
        if (defDisplay.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            defDisplay.forEach { (part, means) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                    Box(modifier = Modifier.padding(top = 2.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text(text = part, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = means, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onDelete, colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.delete_word))
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
