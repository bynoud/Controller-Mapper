package me.ductran.controllermapper


//import android.annotation.SuppressLint
import android.Manifest
import android.R.attr
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*
import me.ductran.controllermapper.touchEmu.Shell
import java.io.*
import java.util.concurrent.TimeUnit


open class FilelogADB(private val context: Context) {
    companion object {
        const val TAG = "FADB"
        const val MAX_OUTPUT_BUFFER_SIZE = 1024 * 16
        const val OUTPUT_BUFFER_DELAY_MS = 100L
//        @SuppressLint("StaticFieldLeak")
//        @Volatile private var instance: DebugADB? = null
//        fun getInstance(context: Context): DebugADB = instance ?: synchronized(this) {
//            instance ?: DebugADB(context).also { instance = it }
//        }
    }

    private val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

    private val adbPath = "${context.applicationInfo.nativeLibraryDir}/libadb.so"
    private val scriptPath = "${context.getExternalFilesDir(null)}/script.sh"

    /**
     * Is the shell ready to handle commands?
     */
    private val _ready = MutableLiveData<Boolean>()
    val ready: LiveData<Boolean> = _ready

    /**
     * Is the shell closed for any reason?
     */
    private val _closed = MutableLiveData<Boolean>()
    val closed: LiveData<Boolean> = _closed

    /**
     * Where shell output is stored
     */
    val outputBufferFile: File = File.createTempFile("buffer", ".txt").also {
        it.deleteOnExit()
    }

    /**
     * Single shell instance where we can pipe commands to
     */
    private var shellProcess: Process? = null

    init {
        startOutputThread()
    }

//    /**
//     * Decide how to initialize the shellProcess variable
//     */
//    fun initializeClient(applicationId: String) {
//        if (_ready.value == true)
//            return
//
//        val autoShell = sharedPrefs.getBoolean(context.getString(R.string.auto_shell_key), true)
//        val autoPair = sharedPrefs.getBoolean(context.getString(R.string.auto_pair_key), true)
//        val autoWireless = sharedPrefs.getBoolean(context.getString(R.string.auto_wireless_key), true)
//        val startupCommand = sharedPrefs.getString(context.getString(R.string.startup_command_key), "echo 'Success! ※\\(^o^)/※'")!!
//
//        initializeADBShell(applicationId, autoShell, autoPair, autoWireless, startupCommand)
//    }
//
//    /**
//     * Scan and make a connection to a wireless device
//     */
//    private fun initializeADBShell(applicationId: String, autoShell: Boolean, autoPair: Boolean, autoWireless: Boolean, startupCommand: String) {
//        val secureSettingsGranted =
//            context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
//
//        if (autoWireless) {
//            debug("Enabling wireless debugging $secureSettingsGranted")
//            if (secureSettingsGranted) {
//                Settings.Global.putInt(
//                    context.contentResolver,
//                    "adb_wifi_enabled",
//                    1
//                )
//                debug("Waiting a few moments...")
//                Thread.sleep(3_000)
//            } else {
//                debug("NOTE: Secure settings permission not granted yet")
//                debug("NOTE: After first pair, it will auto-grant")
//            }
//        }
//
//        if (autoPair) {
//            debug("Starting ADB client")
//            adb(false, listOf("kill-server"))?.waitFor()
//            adb(false, listOf("start-server"))?.waitFor()
//            adb(false, listOf("devices"))?.waitFor()
//            debug("Waiting for device respond (max 5m)")
//            adb(false, listOf("wait-for-device"))?.waitFor()
//        }
//
//        debug("Shelling into device")
//        val process = if (autoShell && autoPair) {
//            val argList = if (Build.SUPPORTED_ABIS[0] == "arm64-v8a")
//                listOf("-t", "1", "shell")
//            else
//                listOf("shell")
//            adb(true, argList)
//        } else
//            shell(true, listOf("sh", "-l"))
//
//        if (process == null) {
//            debug("Failed to open shell connection")
//            return
//        }
//        shellProcess = process
//
//        sendToShellProcess("alias adb=\"$adbPath\"")
//
//        if (autoWireless && !secureSettingsGranted) {
//            sendToShellProcess("echo 'NOTE: Granting secure settings permission for next time'")
//            sendToShellProcess("pm grant $applicationId android.permission.WRITE_SECURE_SETTINGS &> /dev/null")
//        }
//
//        if (autoShell && autoPair)
//            sendToShellProcess("echo 'NOTE: Dropped into ADB shell automatically'")
//        else
//            sendToShellProcess("echo 'NOTE: In unprivileged shell, not ADB shell'")
//
//        if (startupCommand.isNotEmpty())
//            sendToShellProcess(startupCommand)
//
//        _ready.postValue(true)
//
//        startShellDeathThread(applicationId)
//    }

    /**
     * Start a death listener to restart the shell once it dies
     */
    private fun startShellDeathThread(applicationId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            shellProcess?.waitFor()
            _ready.postValue(false)
            debug("Shell is dead, resetting")
            delay(1_000)
            adb(false, listOf("kill-server"))?.waitFor()
            startShell(applicationId)
        }
    }

    /**
     * Completely reset the ADB client
     */
    fun reset() {
        _ready.postValue(false)
//        outputBufferFile.writeText("")
        debug("Destroying shell process")
        shellProcess?.destroyForcibly()
        debug("Disconnecting all clients")
        adb(false, listOf("disconnect"))?.waitFor()
        debug("Killing ADB server")
        adb(false, listOf("kill-server"))?.waitFor()
        debug("Erasing all ADB server files")
        context.filesDir.deleteRecursively()
        context.cacheDir.deleteRecursively()
        _closed.postValue(true)
    }

    /**
     * Ask the device to pair on Android 11 phones
     */
    open fun pair(port: String, code: String) {
        Log.d(AppADB.TAG, "Pairing port=$port code=$code")
        adb(false, listOf("start-server"))?.waitFor()

        val pairShell = adb(true, listOf("pair", "localhost:$port"))

        /* Sleep to allow shell to catch up */
        Log.d(TAG, "sleep start ${if (pairShell == null) 0 else 1}")
        Thread.sleep(2000)
        Log.d(TAG, "sleep done ${if (pairShell == null) 0 else 1}")

        /* Pipe pairing code */
        PrintStream(pairShell?.outputStream).apply {
            println(code)
            flush()
        }

        /* Continue once finished pairing (or 10s elapses) */
        pairShell?.waitFor(2, TimeUnit.SECONDS)
//        val pairShell = adb(true, listOf("pair", "localhost:$port", code))?.waitFor(3, TimeUnit.SECONDS)
        Log.d(TAG, "PairShell now ${if (pairShell == null) 0 else 1}")
    }

    fun startShell(applicationId: String,) {
        debug("Starting ADB client")
//        adb(false, listOf("kill-server"))?.waitFor()
        adb(false, listOf("start-server"))?.waitFor()
        adb(false, listOf("devices"))?.waitFor()
        debug("Waiting for device respond (max 5m)")
        adb(false, listOf("wait-for-device"))?.waitFor(3, TimeUnit.SECONDS)


//        val argList = if (Build.SUPPORTED_ABIS[0] == "arm64-v8a")
//            listOf("-t", "1", "shell")
//        else
//            listOf("shell")
//        shellProcess = adb(true, argList)
        shellProcess = adb(true, listOf("shell"))

        if (shellProcess == null) {
            debug("Failed to open shell connection")
            return
        }

        sendToShellProcess("alias adb=\"$adbPath\"")
        sendToShellProcess("su")

        val secureSettingsGranted =
            context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
        debug("Shelling into device $secureSettingsGranted")
        if (!secureSettingsGranted) {
            debug("NOTE: Granting secure settings permission for next time $applicationId")
            sendToShellProcess("pm grant $applicationId android.permission.WRITE_SECURE_SETTINGS")
        }


        debug("NOTE: Dropped into ADB shell automatically")
        _ready.postValue(true)
        startShellDeathThread(applicationId)
    }

    /**
     * Send a raw ADB command
     */
    private fun adb(redirect: Boolean, command: List<String>): Process? {
        val commandList = command.toMutableList().also {
            it.add(0, adbPath)
        }
        return shell(redirect, commandList)
    }

    /**
     * Continuously update shell output
     */
    private fun startOutputThread() {
        Log.d(TAG, "Output thread started")
        var currentText = ""
        outputBufferFile.writeText("")
        GlobalScope.launch(Dispatchers.IO) {
            while (isActive) {
                val out = readOutputFile(outputBufferFile)
                if (out != currentText) {
                    Log.d(TAG, "CMDOUT: $out")
                    currentText = out
                }
                Thread.sleep(OUTPUT_BUFFER_DELAY_MS)
            }
        }
    }
    /**
     * Read the content of the ABD output file
     */
    private fun readOutputFile(file: File): String {
        val out = ByteArray(MAX_OUTPUT_BUFFER_SIZE)

        synchronized(file) {
            if (!file.exists())
                return ""

            file.inputStream().use {
                val size = it.channel.size()

                if (size <= out.size)
                    return String(it.readBytes())

                val newPos = (it.channel.size() - out.size)
                it.channel.position(newPos)
                it.read(out)
            }

            file.writeText("")
        }

        return String(out)
    }

//    fun getProcessOutput(process: Process) {
//        Log.d(TAG, "Start getting process output")
//        val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
//        var line: String?
//        while (bufferedReader.readLine().also { line = it } != null) {
//            Log.d(TAG, "CMD_OUT: $line")
//        }
//        Log.d(TAG, "Done getting process output")
//    }

    /**
     * Send a raw shell command
     */
    fun shell(redirect: Boolean, command: List<String>): Process? {
        Log.d(TAG, "shell $command")
        val processBuilder = ProcessBuilder(command)
            .directory(context.filesDir)
            .apply {
                redirectErrorStream(true)
                redirectOutput(outputBufferFile)

                environment().apply {
                    put("HOME", context.filesDir.path)
                    put("TMPDIR", context.cacheDir.path)
                }
            }

        return try {
            processBuilder.start()
        } catch (e: IOException) {
            Log.d(TAG,"EEEE")
            e.printStackTrace()
            null
        }
    }



//    /**
//     * Execute a script
//     */
//    fun sendScript(code: String) {
//        /* Store script locally */
//        val internalScript = File(scriptPath).apply {
//            bufferedWriter().use {
//                it.write(code)
//            }
//            deleteOnExit()
//        }
//
//        /* Execute the script here */
//        sendToShellProcess("sh ${internalScript.absolutePath}")
//    }

    /**
     * Send commands directly to the shell process
     */
    fun sendToShellProcess(msg: String) {
        Log.d(TAG, "Running : $msg")
        if (shellProcess == null || shellProcess?.outputStream == null) {
            Log.d(TAG, "ERROR: No shell created yet")
            return
        }
        PrintStream(shellProcess!!.outputStream!!).apply {
            println(msg)
            flush()
        }

    }

    /**
     * Write a debug message to the user
     */
    private fun debug(msg: String) {
        Log.d(TAG, msg)
//        synchronized(outputBufferFile) {
//            if (outputBufferFile.exists())
//                outputBufferFile.appendText(">>> $msg" + System.lineSeparator())
//        }
    }
}