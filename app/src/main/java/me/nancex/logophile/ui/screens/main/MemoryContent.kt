package me.nancex.logophile.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import me.nancex.logophile.R
import me.nancex.logophile.data.repository.WordRepository
import me.nancex.logophile.ui.theme.AppFont
import me.nancex.logophile.ui.theme.getWordFontFamily
import me.nancex.logophile.viewmodel.MemoryState

@Composable
fun MemoryContent(
    state: MemoryState,
    onPass: () -> Unit,
    onShowTip: () -> Unit,
    onPrevious: () -> Unit,
    font: AppFont,
    repository: WordRepository
) {
    val wordFont = getWordFontFamily(font)

    if (state.currentWord == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(R.string.no_words),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(R.string.tap_to_add),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    val word = state.currentWord

    Box(
        modifier = Modifier.fillMaxSize()
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onPass() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            AutoSizeWord(
                text = word.word,
                fontFamily = wordFont,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (state.isShowingTip) {
                Spacer(modifier = Modifier.height(32.dp))
                if (!word.phonetic.isNullOrEmpty()) {
                    Text(text = word.phonetic,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                val defDisplay = repository.parseDefinitionToDisplayText(word.definition)
                defDisplay.forEach { (part, means) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = part, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = means, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f))
                    }
                }
                if (!word.audioUrl.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Icon(imageVector = Icons.Filled.VolumeUp,
                        contentDescription = stringResource(R.string.play_audio),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp))
                }
            }
        }

        Row(
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(bottom = 80.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onPrevious, enabled = state.hasPrevious,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    contentColor = MaterialTheme.colorScheme.onSurface),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.previous),
                    modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(48.dp))
            Button(
                onClick = onShowTip, shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    contentColor = MaterialTheme.colorScheme.onSurface),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(imageVector = Icons.Filled.Help,
                    contentDescription = stringResource(R.string.tip),
                    modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun AutoSizeWord(
    text: String,
    fontFamily: FontFamily,
    style: androidx.compose.ui.text.TextStyle,
    color: Color
) {
    val textMeasurer = rememberTextMeasurer()
    val targetStyle = style.copy(fontFamily = fontFamily, color = color, fontWeight = FontWeight.Bold)

    BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val availableWidthPx = constraints.maxWidth.toFloat()
        val maxFontSize = targetStyle.fontSize

        var fontSize = maxFontSize
        val measured = textMeasurer.measure(
            text = text,
            style = targetStyle.copy(fontSize = fontSize),
            maxLines = 1,
            overflow = TextOverflow.Clip,
            constraints = Constraints(maxWidth = Constraints.Infinity)
        )
        if (measured.size.width > availableWidthPx && availableWidthPx > 0f) {
            fontSize = maxFontSize * (availableWidthPx / measured.size.width.toFloat())
        }

        Text(
            text = text,
            style = targetStyle.copy(fontSize = fontSize),
            maxLines = 1,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Center
        )
    }
}
