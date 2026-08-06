package me.nancex.logophile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class BottomNavTab(val icon: ImageVector, val label: String) {
    MEMORY(Icons.Filled.Psychology, "Memory"),
    WORD_BANK(Icons.Filled.MenuBook, "WordBank")
}

@Composable
fun LogophileBottomBar(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    onAddWord: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth().height(64.dp)) {
        BottomAppBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            contentPadding = PaddingValues(horizontal = 48.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabCapsule(
                    icon = BottomNavTab.MEMORY.icon,
                    label = BottomNavTab.MEMORY.label,
                    selected = selectedTab == BottomNavTab.MEMORY,
                    onClick = { onTabSelected(BottomNavTab.MEMORY) }
                )

                Spacer(modifier = Modifier.width(48.dp))

                TabCapsule(
                    icon = BottomNavTab.WORD_BANK.icon,
                    label = BottomNavTab.WORD_BANK.label,
                    selected = selectedTab == BottomNavTab.WORD_BANK,
                    onClick = { onTabSelected(BottomNavTab.WORD_BANK) }
                )
            }
        }

        FloatingActionButton(
            onClick = onAddWord,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-28).dp)
                .size(56.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 4.dp
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Word",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun TabCapsule(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else Color.Transparent
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.width(64.dp).height(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
