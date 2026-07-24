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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.nancex.logophile.R
import me.nancex.logophile.data.local.WordEntry
import me.nancex.logophile.data.repository.WordRepository
import me.nancex.logophile.ui.theme.AppFont
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
    val app = LocalContext.current.applicationContext as me.nancex.logophile.LogophileApp
    val repository = app.repository
    val words by repository.allWords.collectAsState(initial = emptyList())
    val wordFont = getWordFontFamily(font)
    val fontSizeMul = getWordFontSizeMultiplier(font)

    var searchQuery by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }
    var alphaOrder by remember { mutableStateOf(true) }
    var searchMode by remember { mutableStateOf(SearchMode.BY_WORD) }
    var sheetWord by remember { mutableStateOf<WordEntry?>(null) }
    var wordToDelete by remember { mutableStateOf<WordEntry?>(null) }
    val sheetState = rememberModalBottomSheetState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.Filled.Sort, contentDescription = stringResource(R.string.sort),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sort_alpha)) },
                        onClick = { alphaOrder = true; showSortMenu = false },
                        trailingIcon = { if (alphaOrder) Icon(Icons.Filled.Check, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sort_date)) },
                        onClick = { alphaOrder = false; showSortMenu = false },
                        trailingIcon = { if (!alphaOrder) Icon(Icons.Filled.Check, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary) }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.search_mode_word)) },
                        onClick = { searchMode = SearchMode.BY_WORD; showSortMenu = false },
                        trailingIcon = { if (searchMode == SearchMode.BY_WORD)
                            Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.search_mode_definition)) },
                        onClick = { searchMode = SearchMode.BY_DEFINITION; showSortMenu = false },
                        trailingIcon = { if (searchMode == SearchMode.BY_DEFINITION)
                            Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    )
                }
            }
        }

        val filteredWords = if (searchQuery.isBlank()) words
        else if (searchMode == SearchMode.BY_WORD)
            words.filter { it.word.startsWith(searchQuery, ignoreCase = true) }
        else words.filter { it.definition?.contains(searchQuery, ignoreCase = true) == true }

        val sortedWords = if (alphaOrder) filteredWords.sortedBy { it.word.lowercase() }
            else filteredWords.sortedByDescending { it.addedTime }

        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(sortedWords, key = { it.id }) { word ->
                WordListItem(
                    word = word, fontFamily = wordFont, fontSizeMultiplier = fontSizeMul,
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
        mp.setOnPreparedListener { Log.d(TAG, "playAudio: prepared, starting"); it.start() }
        mp.setOnErrorListener { _, what, extra -> Log.e(TAG, "playAudio: error what=$what extra=$extra"); false }
        mp.setOnCompletionListener { Log.d(TAG, "playAudio: completed"); it.release() }
        mp.prepareAsync()
    } catch (e: Exception) { Log.e(TAG, "playAudio: exception: ${e.message}", e) }
}

@Composable
fun WordListItem(
    word: WordEntry,
    fontFamily: FontFamily = FontFamily.Default,
    fontSizeMultiplier: Float = 1f,
    onClick: () -> Unit,
    onPlayAudio: () -> Unit
) {
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
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = fontFamily,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize * fontSizeMultiplier
                ),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
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
