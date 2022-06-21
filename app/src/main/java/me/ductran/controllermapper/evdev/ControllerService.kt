package me.ductran.controllermapper.evdev

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.input.InputManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.ductran.controllermapper.App
import me.ductran.controllermapper.AppADB
import me.ductran.controllermapper.FilelogADB
import me.ductran.controllermapper.R
import me.ductran.controllermapper.touchEmu.ShellOutputListener
import xyz.cp74.evdev.InputDevice
import java.io.PrintStream
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

// ---- const val EV_ABS = 0x03
// const val ABS_HAT0X = 0x10 // dpad left=-1, right=1
// const val ABS_HAT0Y = 0x11 // dpad up=-1, down=1
//const val ABS_Z = 0x02 // right analog left<80, right>80
//const val ABS_RZ = 0x05 // right up<80, down>80
//const val ABS_X = 0x00 // right > 90, left<80
//const val ABS_Y = 0x01 // up < 80, down>80

// ---- const val EV_KEY = 0x01---
//const val BTN_A = BTN_SOUTH
//const val BTN_B = BTN_EAST
//const val BTN_X = BTN_NORTH
//const val BTN_Y = BTN_WEST
//const val BTN_TL = 0x136 // trigger left
//const val BTN_TR = 0x137 // trigger right
//const val BTN_TL2 = 0x138
//const val BTN_TR2 = 0x139 // can also as ABS event, but just ignore now

data class SimpleInputEvent(
    val type: Int,
    val code: Int,
    val value: Int
)

enum class ControllerAxisType {
    AXIS_LEFT, AXIS_RIGHT, AXIS_DPAD
}

interface EventListener {
    fun onKeyDown(keyCode: Int)
    fun onKeyUp(keyCode: Int)
    fun onKey(keyCode: Int, value: Int)

    fun onMotion(type: ControllerAxisType, x: Int, y: Int)
//    fun onMotion(keyCode: Int, value: Int)
}


private val absSupportList = listOf(ABS_X,ABS_Y,ABS_Z, ABS_RZ, ABS_HAT0X, ABS_HAT0Y)
private val absCorrectionList = listOf(ABS_X,ABS_Y,ABS_Z, ABS_RZ)
private val absCorrectionCenter = 0x80


private val axisTypeMap = mapOf(
    ABS_Z to Pair(ControllerAxisType.AXIS_RIGHT,0),
    ABS_RZ to Pair(ControllerAxisType.AXIS_RIGHT,1),
    ABS_X to Pair(ControllerAxisType.AXIS_LEFT,0),
    ABS_Y to Pair(ControllerAxisType.AXIS_LEFT,1),
    ABS_HAT0X to Pair(ControllerAxisType.AXIS_DPAD,0),
    ABS_HAT0Y to Pair(ControllerAxisType.AXIS_DPAD,1),
)

class EventParser private constructor() : DefaultShellOutputListener() {
    companion object {
        const val TAG = "EvParser"


        private var me: EventParser? = null
        fun getInst(): EventParser {
            if (me == null) me = EventParser()
            return me!!
        }
    }

    private val axisStorage = mutableMapOf(
        ControllerAxisType.AXIS_RIGHT to mutableListOf(0,0),
        ControllerAxisType.AXIS_LEFT to mutableListOf(0,0),
        ControllerAxisType.AXIS_DPAD to mutableListOf(0,0)
    )

    private fun setAxisStorage(code: Int, value: Int): ControllerAxisType {
        val axis = axisTypeMap[code]!!
        axisStorage[axis.first]!![axis.second] = value
        return axis.first
    }

    private val listeners: MutableList<EventListener> = mutableListOf()
    fun addEventListener(listener: EventListener) {
        listeners.add(listener)
    }

    override fun onShellOutput(msg: String) {
        try {
            Log.d(TAG, "Raw $msg")
            val txt = msg.split(" ").map { it.toLong(radix=16) }.map{it.toInt()}
            var (type, code, value) = txt

            if (type==EV_ABS) {
                if (!absSupportList.contains(code)) return
                if (absCorrectionList.contains(code)) value -= absCorrectionCenter // adjust the value to center
                val axis = setAxisStorage(code, value)
                val (x,y) = axisStorage[axis]!!
                for (l in listeners)  l.onMotion(axis, x, y)
            }
            else if (type==EV_KEY) {
                val isDown = value != 0
                for (l in listeners) {
                    l.onKey(code, value)
                    if (isDown) l.onKeyDown(code)
                    else l.onKeyUp(code)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "output parsing error: $e")
        }
    }
}

class ControllerService: Service() {
    companion object {
        const val TAG = "CTLR"
        const val NOTIFICATION_ID = 102
        private val adb = "${App.context.applicationInfo.nativeLibraryDir}/libadb.so"

        fun shouldWePair(): Boolean {
            Log.d(TAG, "Should we pair")
            if (!App.sharedPref.getBoolean(App.context.getString(R.string.paired_key), false)) {
                Log.d(TAG, "Not ${Build.VERSION.SDK_INT} ${Build.VERSION_CODES.R}")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    return true
            }
            return false
        }

        private fun setVarBool(resId: Int, value: Boolean) {
            with(App.sharedPref.edit()) {
                putBoolean(App.context.getString(resId), value)
                apply()
            }
        }

        fun pair(port: String, code: String): Boolean {
            Log.d(TAG, "Pairing port=$port code=$code")
            setVarBool(R.string.paired_key, false)

            ShellSimple().start(listOf(adb, "start-server"), App.context).waitFor()

            val sh = ShellSimple()
            var error = false
            sh.start(listOf(adb, "pair", "localhost:$port"), App.context, object : ShellOutputListener {
                override fun onShellError(msg: String) {
                    error = true
                }

                override fun onShellOutput(msg: String) {
                    Log.d(TAG, "OOO $msg")
                }

                override fun onShellReady(ready: Boolean) {

                }
            })
            Thread.sleep(200)
            sh.send(code)
            sh.waitFor()

            Log.d(TAG, "Wait $error")
            if (!error) {
                setVarBool(R.string.paired_key, true)
            }
            Log.d(TAG, "Done")
            return !error

        }


    }
//    val dev = InputDevice(path)

//    private var shell: ShellSimple? = null

//    private var inputThread: Thread? = null
//    private var injectThread: Thread? = null
    private var inputShell: ShellSimple? = null
    private var injectShell: ShellSimple? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "created")
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "Staring")
        startMyOwnForeground()


        val parser = EventParser.getInst()

        startAdbServer {
            val devPath = "/dev/input/event1"
            startInputShell(intent.action!!, parser)
            startInjectShell(devPath) {
                val c2t = ControllerToTouch(devPath, injectShell!!)
                parser.addEventListener(c2t)
            }
        }
//        createOverlay()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startAdbServer(callback: Runnable) {
        val th = Thread {
            val sh = ShellSimple()
            sh.start(listOf(adb, "start-server"), App.context)
            sh.start(listOf(adb, "wait-for-device"), App.context).waitFor()
            callback.run()
        }
        th.start()
    }

    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "me.ductt.permanence"
        val channelName = "Emu Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_MIN
        )
        val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)

//        //Create an Intent for the BroadcastReceiver
//        val buttonIntent = Intent(context, CloseButtonReceiver::class.java)
//        //Create the PendingIntent
//        val btPendingIntent = PendingIntent.getBroadcast(context, 0, buttonIntent, 0)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("TouchEmu Service")
            .setContentText("Input event monitoring & touch emulating") // this is important, otherwise the notification will show the way
            // you want i.e. it will show some default notification
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }


//    private fun startAdbInterative(outputListener: ShellOutputListener, onReady: ((Thread, ShellSimple?) -> Unit)?=null) {
//        Log.d(TAG, "Running interative")
//        val mThread = Thread {
//            val sh = ShellSimple()
//            var process: Process? = null
//            while (true) {
//                process = sh.start(listOf(adb, "shell"), App.context, outputListener)
//                Thread.sleep(100)
//                if (process.isAlive) break
//                Log.d(TAG, "It dead, rerun")
//                Thread.sleep(2000)
//            }
//
//            onReady?.invoke(this, sh)
//
//            process?.waitFor()
//            onReady?.invoke(null)
//            Log.d(Shell.TAG, "Shell is dead")
//            startAdbInterative(outputListener, onReady)
//        }
//        mThread.start()
//    }

    fun startInjectShell(devPath: String, callback: Runnable) {
        Log.d(TAG, "starting inject shell")

        injectShell = ShellSimple()
        injectShell!!.startAdbInterative(
            listOf(adb, "shell"),
            App.context) {
            Log.d(TAG, "inject shell on ready ${if(it==null) "NULL" else "OK"}")
            it?.send("su")
//            it?.send("alias sendev sendevent $devPath")

            callback.run()
        }
    }

    fun startInputShell(devPath: String, parser: EventParser) {
        Log.d(TAG, "starting input event")

        inputShell = ShellSimple()
        inputShell!!.startAdbInterative(
            listOf(adb, "shell"),
            App.context,
            parser) {
            Log.d(TAG, "Input event on ready ${if(it==null) "NULL" else "OK"}")
            it?.send("getevent $devPath")
        }
//        val sh = ShellSimple()
//        shell = sh
//        sh.start(listOf(adb, "shell"), App.context, parser)
//        sh.send("su")
//        sh.send("getevent $devPath")
    }

    override fun onDestroy() {
//        Log.d(TAG, "stoping ${if(shell==null) "NULL" else "OK"}")
        Log.d(TAG, "Destroying")
        inputShell?.stop()
        injectShell?.stop()

        val sh = ShellSimple()
        sh.start(listOf(adb,"disconnect"), App.context).waitFor()
        sh.start(listOf(adb,"kill-server"), App.context).waitFor()
        stopSelf()
        return super.onDestroy()
    }



    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

}