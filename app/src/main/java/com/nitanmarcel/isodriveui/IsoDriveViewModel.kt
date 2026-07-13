package com.nitanmarcel.isodriveui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitanmarcel.isodriveui.data.IsoDriveRepository
import com.nitanmarcel.isodriveui.data.MountOptions
import com.nitanmarcel.isodriveui.data.ShellResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class IsoDriveUiState(
    val rootAvailable: Boolean? = null,
    val binaryInstalled: Boolean? = null,
    val selectedFile: String? = null,
    val options: MountOptions = MountOptions(),
    val busy: Boolean = false,
    val log: List<String> = emptyList(),
)

class IsoDriveViewModel : ViewModel() {

    val repository = IsoDriveRepository()

    private val _uiState = MutableStateFlow(IsoDriveUiState())
    val uiState: StateFlow<IsoDriveUiState> = _uiState.asStateFlow()

    init {
        checkEnvironment()
    }

    fun checkEnvironment() {
        viewModelScope.launch {
            val root = withContext(Dispatchers.IO) { repository.isRootAvailable() }
            val binary = withContext(Dispatchers.IO) { repository.isBinaryInstalled() }
            _uiState.update { it.copy(rootAvailable = root, binaryInstalled = binary) }
        }
    }

    fun selectFile(path: String) {
        _uiState.update { it.copy(selectedFile = path) }
    }

    fun updateOptions(options: MountOptions) {
        _uiState.update { it.copy(options = options) }
    }

    fun mount() {
        val file = _uiState.value.selectedFile ?: return
        val options = _uiState.value.options
        runCommand { repository.mount(file, options) }
    }

    fun unmount() {
        runCommand { repository.unmount() }
    }

    fun clearLog() {
        _uiState.update { it.copy(log = emptyList()) }
    }

    private fun runCommand(block: suspend () -> ShellResult) {
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true) }
            val result = withContext(Dispatchers.IO) { block() }
            _uiState.update { it.copy(busy = false, log = result.output) }
        }
    }
}
