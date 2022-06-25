package me.ductran.controllermapper.evdev

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.hardware.input.InputManager
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
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
import xyz.cp74.evdev.InputDevice
import java.io.PrintStream
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext


class ControllerService: Service() {
    companion object {
        const val TAG = "CTLR"
        const val NOTIFICATION_ID = 102
        private val adb = "${App.context.applicationInfo.nativeLibraryDir}/libadb.so"

        fun shouldWePair(): Boolean {
            Log.d(TAG, "Should we pair")
            if (!App.sharedPref.getBoolean(App.context.getString(R.string.paired_key), false)) {
                Log.d(TAG, "Not ${Build.VERSION.SDK_INT} ${Build.VERSION_CODES.R}")
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

            ShellSimple().start(listOf(adb, "kill-server"), App.context).waitFor()
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
    private var startupThread: Thread? = null
    private var eventParser: EventParser? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "created")
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "Staring")
        startMyOwnForeground()


        eventParser = EventParser.getInst()

        startAdbServer {
            Log.d(TAG, "connected")
        }
//        startAdbServer {
//            val devPath = "/dev/input/event1"
//            startInputShell(intent.action!!, eventParser!!)
//            startInjectShell(devPath) {
//                val metrics: DisplayMetrics =
//                    applicationContext.resources.displayMetrics
//                val width = metrics.widthPixels
//                val height = metrics.heightPixels
//                Log.d(TAG, "Screeen w $width h $height")
//
//                val c2t = ControllerToTouch(
//                    devPath=devPath,
//                    injectShell=injectShell!!,
//                    width=width,
//                    height=height
//                )
//
//                eventParser!!.addEventListener(c2t)
//                eventParser!!.start()
//            }
//        }

//        createOverlay()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startAdbServer(callback: Runnable) {
        startupThread = Thread {
            val sh = ShellSimple()
            try {
                sh.start(listOf(adb, "kill-server"), App.context).waitFor()
                sh.start(listOf(adb, "start-server"), App.context).waitFor()
                sh.start(listOf(adb, "wait-for-device"), App.context).waitFor()
            } catch (e: Exception) {
                return@Thread
            }
                callback.run()
        }
        startupThread!!.start()
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

        startupThread?.interrupt()
        startupThread = null

        inputShell?.stop()
        injectShell?.stop()
        eventParser?.stop()

        val sh = ShellSimple()
        sh.start(listOf(adb,"disconnect"), App.context).waitFor()
        sh.start(listOf(adb,"kill-server"), App.context).waitFor()
        stopSelf()
        return super.onDestroy()
    }



    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d(TAG, "It's landscape")
            eventParser?.setScreenOrientation(true)
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d(TAG, "It's portrait")
            eventParser?.setScreenOrientation(false)
        }
    }

}