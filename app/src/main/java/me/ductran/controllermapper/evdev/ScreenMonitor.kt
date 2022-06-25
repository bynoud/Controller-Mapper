package me.ductran.controllermapper.evdev

import android.os.SystemClock
import android.util.Log
import me.ductran.controllermapper.App
import java.io.BufferedInputStream
import java.io.InputStream

class ScreenMonitor : ShellStreamHandler {

    companion object {
        private const val TAG = "SMON"
        private val adb = "${App.context.applicationInfo.nativeLibraryDir}/libadb.so"
    }

    private var monShell: ShellSimple? = null

    fun start() {
        val sh = ShellSimple()
        monShell = sh

        sh.start(
            listOf(adb, "shell"),
            App.context,
            streamHandler = this
        )

        sh.send("screencap")
        sh.send("exit")
    }

    override fun handleOutputStream(inStream: InputStream) {
        Log.d(TAG, "start output stream handler ${SystemClock.uptimeMillis()}")
        val bs = BufferedInputStream(inStream)
        val ba = ByteArray(100)
        var printCnt = 0
        while (true) {
            var out = ""
            val cnt = bs.read(ba,0,100)
            if (cnt<0) break
            for (i in 0 until cnt) out += " ${ba[i].toString(16)}"
            if (printCnt < 300) Log.d(TAG, "Read: $cnt = $out")
            printCnt += cnt
        }
        Log.d(TAG, "output stream done: $printCnt ${SystemClock.uptimeMillis()}")
    }

    override fun handleErrorStream(inStream: InputStream) {
        Log.d(TAG, "start Error stream handler")
        val bs = BufferedInputStream(inStream)
        val ba = ByteArray(100)
        while (true) {
            var out = ""
            val cnt = bs.read(ba,0,100)
            if (cnt<0) break
            for (i in 0 until cnt) out += " ${ba[i].toString(16)}"
            Log.d(TAG, "Err: $cnt = $out")
        }
        Log.d(TAG, "Err stream done")
    }
}