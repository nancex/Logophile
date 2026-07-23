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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun LogophileDrawer(
    drawerState: DrawerState,
    scope: CoroutineScope,
    onSettingsClick: () -> Unit,
    onImportExportClick: () -> Unit,
    onAboutClick: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(300.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Logophile",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    NavigationDrawerItem(
                        icon = {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        },
                        label = { Text("Settings") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onSettingsClick()
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = {
                            Icon(Icons.Filled.IosShare, contentDescription = "Import/Export")
                        },
                        label = { Text("Import/Export") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onImportExportClick()
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = {
                            Icon(Icons.Filled.Info, contentDescription = "About")
                        },
                        label = { Text("About") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onAboutClick()
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "v1.0.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(16.dp)
                    )
                }
            }
        },
        content = content
    )
}
