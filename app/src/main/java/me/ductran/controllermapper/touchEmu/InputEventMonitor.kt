package me.ductran.controllermapper.touchEmu

import android.content.Context
import android.util.Log
import xyz.cp74.evdev.InputDevice

data class EmuInputEvent(
    val type: Int,
    val code: Int,
    val value: Int
)

interface EmuInputEventListener: ShellReadyListener {
    fun onInputEvent(ev: EmuInputEvent)
}

class InputEventMonitor(val devPath: String,
                        val eventListener: EmuInputEventListener,
                        context: Context
): DebugADB(context) {
    companion object {
        const val TAG = "INDEV"
    }

    fun startMonitor() {
        startShell(onReady)
        adbShell!!.outputListener = this
        exe("getevent $devPath")
    }

//    override fun startShell(onReady: ShellReadyListener?) {
//        super.startShell(onReady)
//        adbShell!!.outputListener = this
//        exe("getevent $devPath")
//    }

    override fun onShellOutput(msg: String) {
        try {
            val txt = msg.split(" ").map { it.toInt() }
            val ev = EmuInputEvent(type=txt[0], code=txt[1], value=txt[2])
            eventListener.onInputEvent(ev)
        } catch (e: Exception) {
            Log.d(TAG, "output parsing error: $e")
        }
    }
}

class InputEventMonitor2() {
    companion object {
        const val TAG = "INDEV"
    }

    val device = InputDevice("/dev/input/event1");

    fun start() {
        Log.d(TAG, "Starting")
        device.onAttach {
            Log.d(TAG, "Attached")
        }
        device.onDetach() {
            Log.d(TAG, "Detached")
        }
        device.onEvent() {
            Log.d(TAG, "Event: $it")
        }
        device.start()
    }
}