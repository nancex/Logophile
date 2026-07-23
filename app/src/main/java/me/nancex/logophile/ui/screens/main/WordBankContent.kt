package me.nancex.logophile.ui.screens.main

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import me.nancex.logophile.data.local.WordEntry
import me.nancex.logophile.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordBankContent(viewModel: MainViewModel) {
    val app = LocalContext.current.applicationContext as me.nancex.logophile.LogophileApp
    val repository = app.repository
    val words by repository.allWords.collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }
    var alphaOrder by remember { mutableStateOf(true) }
    var selectedWord by remember { mutableStateOf<WordEntry?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("\u641c\u7d22\u5355\u8bcd...") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(
                        Icons.Filled.Sort,
                        contentDescription = "\u6392\u5e8f",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("\u5b57\u6bcd\u8868\u987a\u5e8f") },
                        onClick = { alphaOrder = true; showSortMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("\u6dfb\u52a0\u65e5\u671f") },
                        onClick = { alphaOrder = false; showSortMenu = false }
                    )
                }
            }
        }

        val filteredWords = if (searchQuery.isBlank()) {
            words
        } else {
            words.filter { it.word.startsWith(searchQuery, ignoreCase = true) }
        }

        val sortedWords = if (alphaOrder) {
            filteredWords.sortedBy { it.word.lowercase() }
        } else {
            filteredWords.sortedByDescending { it.addedTime }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(sortedWords, key = { it.id }) { word ->
                WordListItem(
                    word = word,
                    onClick = { selectedWord = word },
                    onPlayAudio = { /* TODO: audio playback */ }
                )
            }
        }
    }

    if (selectedWord != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedWord = null },
            sheetState = sheetState
        ) {
            WordDetailSheet(
                word = selectedWord!!,
                repository = repository,
                onDelete = {
                    showDeleteConfirm = true
                }
            )
        }
    }

    if (showDeleteConfirm && selectedWord != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("\u786e\u8ba4\u5220\u9664") },
            text = { Text("\u786e\u5b9a\u8981\u5220\u9664\u5355\u8bcd\u300c\u300d\u5417\uff1f") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteWord(selectedWord!!)
                    selectedWord = null
                    showDeleteConfirm = false
                }) {
                    Text("\u5220\u9664", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("\u53d6\u6d88")
                }
            }
        )
    }
}

@Composable
fun WordListItem(
    word: WordEntry,
    onClick: () -> Unit,
    onPlayAudio: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = word.word,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!word.audioUrl.isNullOrEmpty()) {
                    IconButton(onClick = onPlayAudio, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Filled.VolumeUp,
                            contentDescription = "\u64ad\u653e\u53d1\u97f3",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "\u66f4\u591a",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailSheet(
    word: WordEntry,
    repository: me.nancex.logophile.data.repository.WordRepository,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = word.word,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        if (!word.phonetic.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = word.phonetic,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!word.audioUrl.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Filled.VolumeUp,
                        contentDescription = "\u64ad\u653e\u53d1\u97f3",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        val defDisplay = repository.parseDefinitionToDisplayText(word.definition)
        if (defDisplay.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            defDisplay.forEach { (part, means) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = part,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = means,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDelete,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("\u5220\u9664\u5355\u8bcd")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}


