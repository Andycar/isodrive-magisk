package com.nitanmarcel.isodriveui.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nitanmarcel.isodriveui.data.IsoDriveRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val ROOT_PATH = "/storage/emulated/0"

@Composable
fun FileBrowserDialog(
    repository: IsoDriveRepository,
    onFileSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var currentPath by remember { mutableStateOf(ROOT_PATH) }
    var entries by remember { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentPath) {
        loading = true
        error = null
        val result = withContext(Dispatchers.IO) { repository.listDirectory(currentPath) }
        if (result.success) {
            val listed = result.output.sortedBy { it.lowercase() }
            entries = if (currentPath.trimEnd('/').isNotEmpty()) listOf("../") + listed else listed
        } else {
            entries = emptyList()
            error = "Could not read $currentPath. Is root access granted?"
        }
        loading = false
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Column(Modifier.padding(16.dp).heightIn(max = 480.dp)) {
                Text(currentPath, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))

                when {
                    loading -> CircularProgressIndicator()
                    error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
                    else -> LazyColumn {
                        items(entries) { entry ->
                            val isDir = entry.endsWith("/")
                            val name = entry.removeSuffix("/")
                            val displayName = if (name == "..") ".." else name
                            ListItem(
                                headlineContent = { Text(displayName) },
                                leadingContent = {
                                    Icon(
                                        imageVector = if (isDir) Icons.Filled.Folder else Icons.Filled.InsertDriveFile,
                                        contentDescription = null,
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isDir) {
                                            currentPath = when (name) {
                                                ".." -> currentPath.trimEnd('/')
                                                    .substringBeforeLast('/', "")
                                                    .ifEmpty { "/" }
                                                else -> "${currentPath.trimEnd('/')}/$name"
                                            }
                                        } else {
                                            onFileSelected("${currentPath.trimEnd('/')}/$name")
                                        }
                                    },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    }
}
