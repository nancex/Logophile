package me.nancex.logophile.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.nancex.logophile.ui.components.BottomNavTab
import me.nancex.logophile.ui.components.LogophileBottomBar
import me.nancex.logophile.ui.components.LogophileDrawer
import me.nancex.logophile.ui.components.LogophileTopBar
import me.nancex.logophile.ui.theme.AppFont
import me.nancex.logophile.viewmodel.MainViewModel

@Composable
fun MainScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToImportExport: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val memoryState by viewModel.memoryState.collectAsState()
    var selectedTab by remember { mutableStateOf(BottomNavTab.MEMORY) }
    var showAddWordDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val drawerState = remember {
        androidx.compose.material3.DrawerState(androidx.compose.material3.DrawerValue.Closed)
    }
    val repository = (LocalContext.current.applicationContext as me.nancex.logophile.LogophileApp).repository

    LogophileDrawer(
        drawerState = drawerState,
        scope = scope,
        onSettingsClick = onNavigateToSettings,
        onImportExportClick = onNavigateToImportExport,
        onAboutClick = onNavigateToAbout
    ) {
        Scaffold(
            topBar = {
                LogophileTopBar(
                    title = when (selectedTab) {
                        BottomNavTab.MEMORY -> "\u8bb0\u5fc6"
                        BottomNavTab.WORD_BANK -> "\u8bcd\u5e93"
                    },
                    onMenuClick = { scope.launch { drawerState.open() } }
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
                        font = AppFont.DEFAULT,
                        repository = repository
                    )
                    BottomNavTab.WORD_BANK -> WordBankContent(
                        viewModel = viewModel
                    )
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
            onAdd = {
                viewModel.addWord()
                showAddWordDialog = false
            },
            onDismiss = {
                viewModel.clearAddWordState()
                showAddWordDialog = false
            }
        )
    }
}
