package me.nancex.logophile.ui.screens.main

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.nancex.logophile.R
import me.nancex.logophile.data.remote.VersionCheckResult
import me.nancex.logophile.data.remote.VersionChecker
import me.nancex.logophile.ui.components.BottomNavTab
import me.nancex.logophile.ui.components.LogophileBottomBar
import me.nancex.logophile.ui.components.LogophileDrawer
import me.nancex.logophile.ui.components.LogophileTopBar
import me.nancex.logophile.ui.theme.AppFont
import me.nancex.logophile.ui.theme.SettingsManager
import me.nancex.logophile.ui.theme.TimeRange
import me.nancex.logophile.viewmodel.MainViewModel

@Composable
fun MainScreen(
    font: AppFont = AppFont.DEFAULT,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToImportExport: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val memoryState by viewModel.memoryState.collectAsState()
    val toastEvent by viewModel.toastEvent.collectAsState()
    var selectedTab by remember { mutableStateOf(BottomNavTab.MEMORY) }
    var showAddWordDialog by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = remember {
        androidx.compose.material3.DrawerState(androidx.compose.material3.DrawerValue.Closed)
    }
    val context = LocalContext.current
    val repository = (context.applicationContext as me.nancex.logophile.LogophileApp).repository
    val settingsManager = remember { SettingsManager(context) }
    val devMode by settingsManager.devModeFlow.collectAsState(initial = false)

    var versionError by remember { mutableStateOf<String?>(null) }
    var showUpdateDialog by remember { mutableStateOf<VersionCheckResult.UpdateAvailable?>(null) }
    val versionName = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0" }
        catch (e: Exception) { "0.0.0" }
    }

    val versionErrorText = stringResource(R.string.version_error)

    LaunchedEffect(Unit) {
        when (val result = VersionChecker.check(context)) {
            is VersionCheckResult.UpdateAvailable -> showUpdateDialog = result
            is VersionCheckResult.Error -> versionError = versionErrorText
            else -> {}
        }
    }

    LaunchedEffect(toastEvent) {
        val event = toastEvent ?: return@LaunchedEffect
        if (devMode) {
            val message = if (event.countType == "pass") {
                context.getString(R.string.toast_pass, event.word)
            } else {
                context.getString(R.string.toast_tip, event.word)
            }
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
        viewModel.clearToast()
    }

    LogophileDrawer(
        drawerState = drawerState, scope = scope,
        onSettingsClick = onNavigateToSettings,
        onImportExportClick = onNavigateToImportExport,
        onAboutClick = onNavigateToAbout,
        versionName = versionName,
        versionError = versionError
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                LogophileTopBar(
                    title = when (selectedTab) {
                        BottomNavTab.MEMORY -> stringResource(R.string.tab_memory)
                        BottomNavTab.WORD_BANK -> stringResource(R.string.tab_word_bank)
                    },
                    onMenuClick = {
                        if (drawerState.isClosed) {
                            scope.launch { drawerState.open() }
                        }
                    },
                    actions = {
                        if (selectedTab == BottomNavTab.MEMORY) {
                            Box {
                                IconButton(onClick = { showFilterMenu = true }) {
                                    Icon(Icons.Filled.FilterList,
                                        contentDescription = stringResource(R.string.filter_words),
                                        tint = if (memoryState.timeRange != TimeRange.ALL)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                DropdownMenu(
                                    expanded = showFilterMenu,
                                    onDismissRequest = { showFilterMenu = false }
                                ) {
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
                                                viewModel.setTimeRange(range)
                                                scope.launch { settingsManager.setMemoryTimeRange(range) }
                                                showFilterMenu = false
                                            },
                                            trailingIcon = {
                                                if (range == memoryState.timeRange) {
                                                    Icon(Icons.Filled.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            },
            bottomBar = {
                LogophileBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onAddWord = { showAddWordDialog = true }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (selectedTab) {
                    BottomNavTab.MEMORY -> MemoryContent(
                        state = memoryState,
                        onPass = { viewModel.passWord() },
                        onShowTip = { viewModel.showTip() },
                        onPrevious = { viewModel.showPrevious() },
                        font = font,
                        repository = repository
                    )
                    BottomNavTab.WORD_BANK -> WordBankContent(viewModel = viewModel, font = font)
                }
            }
        }
    }

    if (showAddWordDialog) {
        val addWordState by viewModel.addWordState.collectAsState()
        AddWordDialog(
            state = addWordState,
            onInputChange = { viewModel.updateAddWordInput(it) },
            onSearch = { viewModel.fetchWordDefinition() },
            onAdd = { viewModel.addWord(); showAddWordDialog = false },
            onDismiss = { viewModel.clearAddWordState(); showAddWordDialog = false }
        )
    }

    showUpdateDialog?.let { update ->
        AlertDialog(
            onDismissRequest = { showUpdateDialog = null },
            title = { Text(stringResource(R.string.update_available_title)) },
            text = { Text(stringResource(R.string.update_available_message, update.latestVersion, versionName)) },
            confirmButton = {
                TextButton(onClick = {
                    showUpdateDialog = null
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(update.downloadUrl))
                    context.startActivity(intent)
                }) {
                    Text(stringResource(R.string.update_download))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
