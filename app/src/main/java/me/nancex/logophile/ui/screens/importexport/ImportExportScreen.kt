package me.nancex.logophile.ui.screens.importexport

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val dbFile = context.getDatabasePath("logophile_database")
                        context.contentResolver.openOutputStream(it)?.use { output ->
                            FileInputStream(dbFile).use { input ->
                                input.copyTo(output)
                            }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "\u5bfc\u51fa\u6210\u529f", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "\u5bfc\u51fa\u5931\u8d25: ${e.message}", Toast.LENGTH_SHORT).show()
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
                    withContext(Dispatchers.IO) {
                        val dbFile = context.getDatabasePath("logophile_database")
                        context.contentResolver.openInputStream(it)?.use { input ->
                            FileOutputStream(dbFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "\u5bfc\u5165\u6210\u529f\uff0c\u8bf7\u91cd\u542f\u5e94\u7528", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "\u5bfc\u5165\u5931\u8d25: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("\u5bfc\u5165\u5bfc\u51fa") },
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
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Button(
                        onClick = { exportLauncher.launch("logophile_backup.db") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.FileUpload, contentDescription = null)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("\u5bfc\u51fa\u8bcd\u5e93 (.db)", fontWeight = FontWeight.Medium)
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
                        Text("\u5bfc\u5165\u8bcd\u5e93 (.db)", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
