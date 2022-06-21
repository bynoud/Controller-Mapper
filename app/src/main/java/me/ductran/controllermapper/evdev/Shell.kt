package me.ductran.controllermapper.evdev

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.ductran.controllermapper.FilelogADB
import me.ductran.controllermapper.touchEmu.ShellOutputListener
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PrintStream

open class DefaultShellOutputListener: ShellOutputListener {
    override fun onShellOutput(msg: String) {
        Log.d("SH", "[OUT] $msg")
    }

    override fun onShellError(msg: String) {
        Log.d("SH", "[ERR] $msg")
    }

    override fun onShellReady(ready: Boolean) {
        Log.d("SH", "[Ready] $ready")
    }

}

class ShellSimple(): ViewModel() {
    companion object {
        const val TAG = "SHS"
    }

    private var process: Process? = null
    private var listener: ShellOutputListener = DefaultShellOutputListener()
    private var resumeThread: Thread? = null

    fun start(cmds: List<String>, context: Context, listener: ShellOutputListener? = null): Process {
        Log.d(FilelogADB.TAG, "startShell $cmds")
        if (listener!=null) this.listener = listener

        val processBuilder = ProcessBuilder(cmds)
            .directory(context.filesDir)
            .apply {
                environment().apply {
                    put("HOME", context.filesDir.path)
                    put("TMPDIR", context.cacheDir.path)
                }
            }
        val p = processBuilder.start()

        startOutputMonitor(p.inputStream, false)
        startOutputMonitor(p.errorStream, true)
        process = p

        Log.d(TAG, "Init succeed")

        return p
    }

    fun waitFor() = process?.waitFor()

    fun send(msg: String) {
        Log.d(TAG, "exe : $msg")
        if (process == null || process?.outputStream == null) {
            Log.d(FilelogADB.TAG, "ERROR: No shell created yet")
            return
        }
        PrintStream(process!!.outputStream!!).apply {
            println(msg)
            flush()
        }
    }

    private fun startOutputMonitor(inStream: InputStream,
                                   isError: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        val buf = BufferedReader(InputStreamReader(inStream))
        Log.d(TAG, "Start monitor $isError")
        var line: String? = null
        try {
            while (buf.readLine().also { line = it } != null) {
                if (isError) {
                    listener.onShellError(line!!)
                } else {
                    listener.onShellOutput(line!!)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error during output monitor: $e")
        }
        Log.d(TAG, "Done monitor $isError")
    }

    fun startAdbInterative(cmds: List<String>, context: Context,
                           outputListener: ShellOutputListener?=null, onReady: ((ShellSimple?) -> Unit)?=null) {
        Log.d(TAG, "Running interative")
        resumeThread = Thread {
            var p: Process? = null
            while (true) {
                try {
                    p = start(cmds, context, outputListener)
                    Thread.sleep(100)
                    if (p.isAlive) break
                    Log.d(TAG, "It dead, rerun")
                    Thread.sleep(2000)
                } catch (e: Exception) {
                    if (e is InterruptedException) {
                        Log.d(TAG, "during startup Interrupted exception. Exit")
                        return@Thread
                    }
                    Log.d(TAG, "startup Other exception?: $e")
                }
            }

            onReady?.invoke(this)

            try {
                p?.waitFor()
            } catch (e: Exception) {
                if (e is InterruptedException) {
                    Log.d(TAG, "Interrupted exception. Exit")
                    return@Thread
                }
                Log.d(TAG, "Other exception?: $e")
            }
            onReady?.invoke(null)
            Log.d(TAG, "Shell is dead")
            startAdbInterative(cmds, context, outputListener, onReady)
        }
        resumeThread!!.start()
    }


    fun stop() {
        Log.d(TAG, "Stoping Shell")
        resumeThread?.interrupt()
        resumeThread = null
        Thread.sleep(100)
        process?.destroyForcibly()
    }


}