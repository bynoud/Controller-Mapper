package me.ductran.controllermapper.eventinj

import android.util.Log
import me.ductran.controllermapper.App

// Kind of tricky, but you first need to build project to generate the execution file (add .so for file ext)
// then copy below file to app\src\main\jniLibs
// app\build\intermediates\cmake\debug\obj
class EventInject {
    companion object {
        const val TAG = "INJ"
        private val injlib = "${App.context.applicationInfo.nativeLibraryDir}/event_inj.so"
    }

    var shell: Shell? = null

    fun start(devNum: Int) {
        Log.d(TAG, "starting")
        shell = Shell()
        shell!!.start(listOf("sh"), App.context)
        shell!!.send("su")
        shell!!.send(injlib)

        testing(devNum)
    }

    fun testing(devId: Int) {
        val th = Thread {
            Thread.sleep(200)
            shell!!.send("pollonly event$devId")
        }
        th.start()
    }

    fun stop() {
        Log.d(TAG, "Stopping")
        shell?.send("exit_inj")
        Thread.sleep(100)
        shell?.stop()
    }
}