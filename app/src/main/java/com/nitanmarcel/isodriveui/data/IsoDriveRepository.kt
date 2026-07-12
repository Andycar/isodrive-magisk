package com.nitanmarcel.isodriveui.data

import com.topjohnwu.superuser.Shell

data class MountOptions(
    val readWrite: Boolean = false,
    val cdrom: Boolean = false,
    val forceConfigfs: Boolean = false,
    val forceUsbGadget: Boolean = false,
)

data class ShellResult(val success: Boolean, val output: List<String>)

/**
 * Talks to the `isodrive` binary installed by the isodrive Magisk module
 * (/system/bin/isodrive) over a root shell via libsu.
 */
class IsoDriveRepository {

    fun isRootAvailable(): Boolean = Shell.getShell().isRoot

    fun isBinaryInstalled(): Boolean = Shell.cmd("command -v isodrive").exec().isSuccess

    fun listDirectory(path: String): ShellResult {
        val result = Shell.cmd("ls -1Ap -- ${shellQuote(path)}").exec()
        return ShellResult(result.isSuccess, result.out)
    }

    fun mount(filePath: String, options: MountOptions): ShellResult {
        val args = mutableListOf("isodrive", shellQuote(filePath))
        if (options.readWrite) args += "-rw"
        if (options.cdrom) args += "-cdrom"
        if (options.forceConfigfs) args += "-configfs"
        if (options.forceUsbGadget) args += "-usbgadget"
        val result = Shell.cmd(args.joinToString(" ")).exec()
        return ShellResult(result.isSuccess, result.out)
    }

    fun unmount(): ShellResult {
        val result = Shell.cmd("isodrive").exec()
        return ShellResult(result.isSuccess, result.out)
    }

    private fun shellQuote(value: String) = "'" + value.replace("'", "'\\''") + "'"
}
