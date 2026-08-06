package me.nancex.logophile.ui.screens.settings

import android.app.Activity
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.launch
import me.nancex.logophile.LogophileApp
import me.nancex.logophile.R
import me.nancex.logophile.ui.theme.AppFont
import me.nancex.logophile.ui.theme.AppLanguage
import me.nancex.logophile.ui.theme.AppTheme
import me.nancex.logophile.ui.theme.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val settingsManager = remember { SettingsManager(context) }
    val repository = (context.applicationContext as LogophileApp).repository
    val theme by settingsManager.themeFlow.collectAsState(initial = AppTheme.LIGHT)
    val font by settingsManager.fontFlow.collectAsState(initial = AppFont.DEFAULT)
    val language by settingsManager.languageFlow.collectAsState(initial = AppLanguage.CHINESE)
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var showResetDialog by remember { mutableStateOf(false) }
    var hasNavigatedBack by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!hasNavigatedBack) {
                            hasNavigatedBack = true
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(scrollState)
        ) {
            SettingsSectionTitle(title = stringResource(R.string.settings_font))
            SettingsDropdown(
                selectedValue = font,
                values = AppFont.entries,
                valueToLabel = { option ->
                    when (option) {
                        AppFont.DEFAULT -> stringResource(R.string.font_default)
                        AppFont.SERIF -> stringResource(R.string.font_serif)
                        AppFont.MONOSPACE -> stringResource(R.string.font_monospace)
                        AppFont.EIGHT_BIT -> stringResource(R.string.font_8bit)
                    }
                },
                onSelect = { scope.launch { settingsManager.setFont(it) } }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionTitle(title = stringResource(R.string.settings_language))
            SettingsDropdown(
                selectedValue = language,
                values = AppLanguage.entries,
                valueToLabel = { option ->
                    when (option) {
                        AppLanguage.CHINESE -> stringResource(R.string.lang_chinese)
                        AppLanguage.ENGLISH -> stringResource(R.string.lang_english)
                    }
                },
                onSelect = { lang ->
                    scope.launch {
                        settingsManager.setLanguage(lang)
                        val locale = when (lang) {
                            AppLanguage.CHINESE -> LocaleListCompat.forLanguageTags("zh-CN")
                            AppLanguage.ENGLISH -> LocaleListCompat.forLanguageTags("en-US")
                        }
                        AppCompatDelegate.setApplicationLocales(locale)
                        activity?.recreate()
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionTitle(title = stringResource(R.string.settings_theme))
            SettingsDropdown(
                selectedValue = theme,
                values = AppTheme.entries,
                valueToLabel = { option ->
                    when (option) {
                        AppTheme.LIGHT -> stringResource(R.string.theme_light)
                        AppTheme.DARK -> stringResource(R.string.theme_dark)
                        AppTheme.OCEAN -> stringResource(R.string.theme_ocean)
                        AppTheme.ROSE -> stringResource(R.string.theme_rose)
                        AppTheme.FOREST -> stringResource(R.string.theme_forest)
                    }
                },
                onSelect = { scope.launch { settingsManager.setTheme(it) } }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(16.dp))

            SettingsSectionTitle(title = stringResource(R.string.settings_dev_mode))
            val devMode by settingsManager.devModeFlow.collectAsState(initial = false)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.settings_dev_mode),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = devMode,
                    onCheckedChange = { enabled -> scope.launch { settingsManager.setDevMode(enabled) } }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(16.dp))

            SettingsSectionTitle(title = stringResource(R.string.settings_data))
            Button(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Filled.DeleteSweep, contentDescription = null)
                Spacer(modifier = Modifier.weight(1f))
                Text(stringResource(R.string.reset_counts), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.reset_counts_title)) },
            text = { Text(stringResource(R.string.reset_counts_message)) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        repository.resetAllCounts()
                        Toast.makeText(context, context.getString(R.string.reset_counts_done), Toast.LENGTH_SHORT).show()
                    }
                    showResetDialog = false
                }) {
                    Text(stringResource(R.string.reset))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SettingsDropdown(
    selectedValue: T,
    values: List<T>,
    valueToLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        OutlinedTextField(
            value = valueToLabel(selectedValue),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            values.forEach { value ->
                DropdownMenuItem(
                    text = { Text(valueToLabel(value)) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}
