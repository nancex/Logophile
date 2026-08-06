package me.nancex.logophile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.nancex.logophile.R

@Composable
fun LogophileDrawer(
    drawerState: DrawerState,
    scope: kotlinx.coroutines.CoroutineScope,
    onSettingsClick: () -> Unit,
    onImportExportClick: () -> Unit,
    onAboutClick: () -> Unit,
    versionError: String? = null,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.fillMaxHeight().width(300.dp)) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Logophile",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                        label = { Text(stringResource(R.string.drawer_settings)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close(); onSettingsClick() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.IosShare, contentDescription = null) },
                        label = { Text(stringResource(R.string.drawer_import_export)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close(); onImportExportClick() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Info, contentDescription = null) },
                        label = { Text(stringResource(R.string.drawer_about)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close(); onAboutClick() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (versionError != null) {
                        Text(
                            text = versionError,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFEF5350),
                            modifier = Modifier.align(Alignment.End).padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "v0.1.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End).padding(horizontal = 16.dp, vertical = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        content = content
    )
}