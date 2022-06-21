package me.ductran.controllermapper.touchEmu

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import me.ductran.controllermapper.FilelogADB
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PrintStream
import java.util.concurrent.TimeUnit

interface ShellOutputListener {
    fun onShellOutput(msg: String)
    fun onShellError(msg: String)
    fun onShellReady(ready: Boolean)
}

class Shell(private val context: Context,
            private val startCmd: List<String>,
            private val initCmds: List<String>? = null): ViewModel() {
    companion object {
        const val TAG = "SH"

//        fun runAndWait(cmds: List<String>, timeout: Long=0): Boolean {
//            val sh = Shell(cmds).start()
//            return sh.waitFor(timeout)
//        }
//        fun runCmdList(cmds: List<String>, pref: String="", timeout: Long=0): Boolean {
//            val _pref = if (pref=="") "" else "$pref "
//            val sh = Shell(listOf("sh")).start()
//            for (cmd in cmds) sh.send("$_pref$cmd")
//            return sh.waitFor(timeout)
//        }
    }

    private var process: Process? = null

    private val jobList: MutableList<Job> = mutableListOf()
    private var active = true

//    val ready = MutableLiveData<Boolean>()

    var outputListener: ShellOutputListener? = null
    var hasError = false
        private set

    val alive: Boolean
        get() = if (process==null) false else process!!.isAlive

//    init {
//        startReadyMonitor()
//    }

//    private fun startReadyMonitor() = viewModelScope.launch(Dispatchers.IO) {
//        ready.observeForever {
//            Log.d(TAG, "ready $it")
//            outputListener?.onShellReady(it)
//        }
//    }


    fun start(restartOnExit: Boolean = false, sleepTime: Long=5000): Shell {
        Log.d(FilelogADB.TAG, "startShell $startCmd -- $initCmds")

        hasError = false
        val processBuilder = ProcessBuilder(startCmd)
            .directory(context.filesDir)
            .apply {
                environment().apply {
                    put("HOME", context.filesDir.path)
                    put("TMPDIR", context.cacheDir.path)
                }
            }
        process = processBuilder.start()

        jobList.removeIf { !it.isActive }
        jobList.add(startOutputMonitor(process!!.inputStream, false))
        jobList.add(startOutputMonitor(process!!.errorStream, true))
        jobList.add(startShellDeathThread(restartOnExit, sleepTime))


        if (initCmds != null) {
            for (cmd in initCmds) {
                send(cmd)
            }
        }

        Log.d(TAG, "Init succeed")

//        ready.postValue(true)
        outputListener?.onShellReady(true)

        return this
    }

    fun runUtilSucess(timeout: Long=0, sleepTime: Long=500): Shell {
        while (active) {
            Log.d(TAG, "Run until sucess")
            start(false)
            waitFor(timeout)
            if (!hasError) break
            Log.d(TAG, "hasError, reruning")
            Thread.sleep(sleepTime)
        }
        return this
    }

//    private fun startInitShell(timeout: Long): Boolean {
//        Log.d(FilelogADB.TAG, "startInitShell $startCmd")
//        hasError = false
//        val processBuilder = ProcessBuilder(startCmd)
////            .directory(context.filesDir)
////            .apply {
////                environment().apply {
////                    put("HOME", context.filesDir.path)
////                    put("TMPDIR", context.cacheDir.path)
////                }
////            }
//        process = processBuilder.start()
//
//        startOutputMonitor(process!!.inputStream, false, onOutput)
//        startOutputMonitor(process!!.errorStream, true, onError)
//
//        return waitFor(timeout)
//    }

    fun waitFor(timeout: Long=0): Boolean {
        if (process==null) return false
        if (timeout == 0L) {
            process!!.waitFor()
        } else {
            try {
                val ok = process!!.waitFor(timeout, TimeUnit.SECONDS)
                if (!ok) {
                    process!!.destroyForcibly()
                    hasError = true
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error during waitFor: $e")
            }
        }
        return !hasError
    }

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

    fun stop() {
        Log.d(TAG, "Stopping")
        active = false
        Thread.sleep(200)
        for (j in jobList) j.cancel()
        jobList.clear()
        process?.destroyForcibly()
        context.filesDir.deleteRecursively()
        context.cacheDir.deleteRecursively()
    }


    private fun startOutputMonitor(inStream: InputStream,
                                   isError: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        val buf = BufferedReader(InputStreamReader(inStream))
        Log.d(TAG, "Start monitor $isError")
        var line: String? = null
        try {
            while (buf.readLine().also { line = it } != null) {
                Log.d(TAG, "[${if (isError) "ERR" else "OUT"}]: $line")
                if (isError) {
                    hasError = true
                    outputListener?.onShellError(line!!)
                } else {
                    outputListener?.onShellOutput(line!!)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error during output monitor: $e")
        }
         Log.d(TAG, "Done monitor $isError")
    }

    fun startShellDeathThread(restart: Boolean=true, sleepTime: Long=1000) = viewModelScope.launch(Dispatchers.IO) {
        process?.waitFor()
//        ready.postValue(false)
        outputListener?.onShellReady(false)
        Log.d(TAG, "Shell is dead $restart")
        if (restart) {
            delay(sleepTime)
            start(true)
        }
    }

//    fun onReady(owner: LifecycleOwner,
//                onTrue: Runnable?=null,
//                onFalse: Runnable?=null
//    ) {
//        Log.d(TAG, "Add restart callback")
//        ready.observe(owner, Observer {
//            Log.d(TAG, "ready $it")
//            if (it == true) onTrue?.run()
//            if (it == false) onFalse?.run()
//        })
//    }

}

class ShellSimple(): ViewModel() {
    companion object {
        const val TAG = "SHS"
    }

    private var process: Process? = null
    private var listener: ShellOutputListener? = null
    private var resumeThread: Thread? = null

    fun start(cmds: List<String>, context: Context, listener: ShellOutputListener? = null): Process {
        Log.d(FilelogADB.TAG, "startShell $cmds")
        this.listener = listener

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
                Log.d(TAG, "[${if (isError) "ERR" else "OUT"}]: $line")
                if (isError) {
                    listener?.onShellError(line!!)
                } else {
                    listener?.onShellOutput(line!!)
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