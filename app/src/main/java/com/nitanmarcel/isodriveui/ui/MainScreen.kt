package com.nitanmarcel.isodriveui.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nitanmarcel.isodriveui.IsoDriveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: IsoDriveViewModel) {
    val state by viewModel.uiState.collectAsState()
    var showBrowser by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("IsoDriveUI") }) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            StatusRow("Root access", state.rootAvailable)
            StatusRow("isodrive binary installed", state.binaryInstalled)

            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = { showBrowser = true }, modifier = Modifier.fillMaxWidth()) {
                Text(state.selectedFile ?: "Select ISO / IMG file")
            }

            Spacer(Modifier.height(16.dp))
            Text("Options", style = MaterialTheme.typography.titleSmall)
            OptionCheckbox("Read/write (-rw)", state.options.readWrite) {
                viewModel.updateOptions(
                    state.options.copy(
                        readWrite = it,
                        cdrom = if (it) false else state.options.cdrom,
                    )
                )
            }
            OptionCheckbox("CD-ROM (-cdrom)", state.options.cdrom) {
                viewModel.updateOptions(
                    state.options.copy(
                        cdrom = it,
                        readWrite = if (it) false else state.options.readWrite,
                    )
                )
            }
            OptionCheckbox("Force configfs", state.options.forceConfigfs) {
                viewModel.updateOptions(
                    state.options.copy(
                        forceConfigfs = it,
                        forceUsbGadget = if (it) false else state.options.forceUsbGadget,
                    )
                )
            }
            OptionCheckbox("Force USB gadget", state.options.forceUsbGadget) {
                viewModel.updateOptions(
                    state.options.copy(
                        forceUsbGadget = it,
                        forceConfigfs = if (it) false else state.options.forceConfigfs,
                    )
                )
            }

            Spacer(Modifier.height(16.dp))
            Row {
                Button(
                    onClick = viewModel::mount,
                    enabled = !state.busy && state.selectedFile != null && state.rootAvailable == true,
                ) { Text("Mount") }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = viewModel::unmount,
                    enabled = !state.busy && state.rootAvailable == true,
                ) { Text("Unmount") }
            }

            if (state.busy) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(16.dp))
            Text("Log", style = MaterialTheme.typography.titleSmall)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
            ) {
                LazyColumn(Modifier.padding(8.dp)) {
                    items(state.log) { line -> Text(line, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
    }

    if (showBrowser) {
        FileBrowserDialog(
            repository = viewModel.repository,
            onFileSelected = {
                viewModel.selectFile(it)
                showBrowser = false
            },
            onDismiss = { showBrowser = false },
        )
    }
}

@Composable
private fun StatusRow(label: String, ok: Boolean?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val (icon, tint) = when (ok) {
            true -> Icons.Filled.CheckCircle to Color(0xFF2E7D32)
            false -> Icons.Filled.Cancel to MaterialTheme.colorScheme.error
            null -> Icons.Filled.HourglassEmpty to MaterialTheme.colorScheme.onSurfaceVariant
        }
        Icon(icon, contentDescription = null, tint = tint)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
private fun OptionCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}
