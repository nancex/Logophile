package me.nancex.logophile.ui.screens.about

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.nancex.logophile.R
import me.nancex.logophile.data.remote.VersionCheckResult
import me.nancex.logophile.data.remote.VersionChecker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var hasNavigatedBack by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf<VersionCheckResult.UpdateAvailable?>(null) }
    var isChecking by remember { mutableStateOf(false) }

    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
        } catch (e: Exception) { "0.0.0" }
    }

    val density = LocalDensity.current
    val icon = remember {
        val drawable = context.resources.getDrawable(R.mipmap.ic_launcher, null)
        val size = with(density) { 96.dp.toPx().toInt() }
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap.asImageBitmap()
    }

    val toastLatest = stringResource(R.string.update_latest)
    val toastError = stringResource(R.string.version_error)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!hasNavigatedBack) { hasNavigatedBack = true; onNavigateBack() }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Image(
                bitmap = icon,
                contentDescription = null,
                modifier = Modifier.size(96.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "v$versionName",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = stringResource(R.string.about_github),
                style = MaterialTheme.typography.bodyMedium.copy(
                    textDecoration = TextDecoration.Underline,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/nancex/Logophile"))
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    isChecking = true
                    scope.launch {
                        val result = VersionChecker.check(context)
                        isChecking = false
                        when (result) {
                            is VersionCheckResult.UpdateAvailable -> showUpdateDialog = result
                            is VersionCheckResult.UpToDate -> Toast.makeText(context, toastLatest, Toast.LENGTH_SHORT).show()
                            is VersionCheckResult.Error -> Toast.makeText(context, toastError, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = !isChecking,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.about_check_update))
            }
        }
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