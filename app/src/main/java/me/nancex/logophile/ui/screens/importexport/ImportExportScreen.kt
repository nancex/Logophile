package me.nancex.logophile.ui.screens.importexport

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.nancex.logophile.LogophileApp
import me.nancex.logophile.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as LogophileApp
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        // Force WAL checkpoint so all data is in the main .db file
                        app.repository.checkpointBeforeExport(context)
                        val dbFile = context.getDatabasePath("logophile_database")
                        Log.d("ImportExport", "export: dbFile path=${dbFile.absolutePath}, size=${dbFile.length()}")
                        val walFile = File(dbFile.absolutePath + "-wal")
                        val shmFile = File(dbFile.absolutePath + "-shm")
                        Log.d("ImportExport", "export: wal exists=${walFile.exists()} shm exists=${shmFile.exists()}")
                        context.contentResolver.openOutputStream(it)?.use { output ->
                            FileInputStream(dbFile).use { input -> input.copyTo(output) }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, context.getString(R.string.export_success), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("ImportExport", "export failed: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, context.getString(R.string.export_fail, e.message), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    var importedCount = 0
                    withContext(Dispatchers.IO) {
                        val tempFile = File(context.cacheDir, "import_temp.db")
                        Log.d("ImportExport", "import: copying to ${tempFile.absolutePath}")
                        context.contentResolver.openInputStream(it)?.use { input ->
                            FileOutputStream(tempFile).use { output -> input.copyTo(output) }
                        }
                        Log.d("ImportExport", "import: temp file size=${tempFile.length()}")
                        importedCount = app.repository.importFromFile(context, tempFile.absolutePath)
                        tempFile.delete()
                    }
                    withContext(Dispatchers.Main) {
                        Log.d("ImportExport", "import: complete, imported $importedCount words")
                        Toast.makeText(context,
                            context.getString(R.string.import_success_count, importedCount),
                            Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("ImportExport", "import failed: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context,
                            context.getString(R.string.import_fail, e.message),
                            Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.import_export_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Button(
                        onClick = { exportLauncher.launch("logophile_backup.db") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.FileUpload, contentDescription = null)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(stringResource(R.string.export_db), fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { importLauncher.launch(arrayOf("application/octet-stream", "*/*")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Filled.FileDownload, contentDescription = null)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(stringResource(R.string.import_db), fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
