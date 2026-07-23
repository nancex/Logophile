package me.nancex.logophile.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.nancex.logophile.ui.theme.AppFont
import me.nancex.logophile.ui.theme.AppLanguage
import me.nancex.logophile.ui.theme.AppTheme
import me.nancex.logophile.ui.theme.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val theme by settingsManager.themeFlow.collectAsState(initial = AppTheme.LIGHT)
    val font by settingsManager.fontFlow.collectAsState(initial = AppFont.DEFAULT)
    val language by settingsManager.languageFlow.collectAsState(initial = AppLanguage.CHINESE)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("\u8bbe\u7f6e") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "\u8fd4\u56de")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SettingsSectionTitle(title = "\u5355\u8bcd\u5b57\u4f53")

            AppFont.entries.forEach { fontOption ->
                SettingsRadioRow(
                    label = fontOption.displayName,
                    selected = font == fontOption,
                    onClick = {
                        scope.launch { settingsManager.setFont(fontOption) }
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionTitle(title = "\u754c\u9762\u8bed\u8a00")

            AppLanguage.entries.forEach { lang ->
                SettingsRadioRow(
                    label = when (lang) {
                        AppLanguage.CHINESE -> "\u4e2d\u6587"
                        AppLanguage.ENGLISH -> "English"
                    },
                    selected = language == lang,
                    onClick = {
                        scope.launch { settingsManager.setLanguage(lang) }
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionTitle(title = "\u4e3b\u9898")

            AppTheme.entries.forEach { themeOption ->
                SettingsRadioRow(
                    label = when (themeOption) {
                        AppTheme.LIGHT -> "\u767d\u5929"
                        AppTheme.DARK -> "\u9ed1\u591c"
                        AppTheme.OCEAN -> "\u6d77\u6d0b\u84dd"
                        AppTheme.ROSE -> "\u73ab\u7470\u7c89"
                        AppTheme.FOREST -> "\u68ee\u6797\u7eff"
                    },
                    selected = theme == themeOption,
                    onClick = {
                        scope.launch { settingsManager.setTheme(themeOption) }
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

@Composable
fun SettingsRadioRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        RadioButton(selected = selected, onClick = onClick)
    }
}
