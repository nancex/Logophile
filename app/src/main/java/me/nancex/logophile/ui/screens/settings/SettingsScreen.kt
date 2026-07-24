package me.nancex.logophile.ui.screens.settings

import android.app.Activity
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.launch
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
    val theme by settingsManager.themeFlow.collectAsState(initial = AppTheme.LIGHT)
    val font by settingsManager.fontFlow.collectAsState(initial = AppFont.DEFAULT)
    val language by settingsManager.languageFlow.collectAsState(initial = AppLanguage.CHINESE)
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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

            AppFont.entries.forEach { fontOption ->
                SettingsRadioRow(
                    label = when (fontOption) {
                        AppFont.DEFAULT -> stringResource(R.string.font_default)
                        AppFont.SERIF -> stringResource(R.string.font_serif)
                        AppFont.MONOSPACE -> stringResource(R.string.font_monospace)
                        AppFont.EIGHT_BIT -> stringResource(R.string.font_8bit)
                    },
                    selected = font == fontOption,
                    onClick = { scope.launch { settingsManager.setFont(fontOption) } }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionTitle(title = stringResource(R.string.settings_language))

            AppLanguage.entries.forEach { lang ->
                SettingsRadioRow(
                    label = when (lang) {
                        AppLanguage.CHINESE -> stringResource(R.string.lang_chinese)
                        AppLanguage.ENGLISH -> stringResource(R.string.lang_english)
                    },
                    selected = language == lang,
                    onClick = {
                        scope.launch {
                            settingsManager.setLanguage(lang)
                            val locale = when (lang) {
                                AppLanguage.CHINESE -> LocaleListCompat.forLanguageTags("zh-CN")
                                AppLanguage.ENGLISH -> LocaleListCompat.forLanguageTags("en-US")
                            }
                            AppCompatDelegate.setApplicationLocales(locale)
                            Log.d("SettingsScreen", "language changed to ${lang.code}, recreating activity")
                            activity?.recreate()
                        }
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionTitle(title = stringResource(R.string.settings_theme))

            AppTheme.entries.forEach { themeOption ->
                SettingsRadioRow(
                    label = when (themeOption) {
                        AppTheme.LIGHT -> stringResource(R.string.theme_light)
                        AppTheme.DARK -> stringResource(R.string.theme_dark)
                        AppTheme.OCEAN -> stringResource(R.string.theme_ocean)
                        AppTheme.ROSE -> stringResource(R.string.theme_rose)
                        AppTheme.FOREST -> stringResource(R.string.theme_forest)
                    },
                    selected = theme == themeOption,
                    onClick = { scope.launch { settingsManager.setTheme(themeOption) } }
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
fun SettingsRadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        RadioButton(selected = selected, onClick = onClick)
    }
}
