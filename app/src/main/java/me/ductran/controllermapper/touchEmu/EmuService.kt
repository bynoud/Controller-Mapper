package me.ductran.controllermapper.touchEmu

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.view.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import me.ductran.controllermapper.R

class EmuService() : Service(), EmuInputEventListener {
    private lateinit var mView: View
    private lateinit var inputDev: InputEventMonitor
    private lateinit var mThread: Thread

    companion object {
        val TAG = "EmuService"
        const val NOTIFICATION_ID = 101
    }

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "created")
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startMyOwnForeground()
        startMonitor(intent.action!!)
//        createOverlay()
        return super.onStartCommand(intent, flags, startId)
    }

    fun startMonitor(devPath: String) {
        Log.d(TAG, "Starting $devPath")
        mThread = Thread {
            inputDev = InputEventMonitor(devPath, this, this)
            inputDev.startShell(this)
//        createOverlay()
        }
        mThread.start()
    }

    override fun onShellReady(ready: Boolean) {
        Log.d(TAG, "Ready $ready")
    }

    override fun onInputEvent(ev: EmuInputEvent) {
        Log.d(TAG, "Event $ev")
    }

//    class CloseButtonReceiver: BroadcastReceiver() {
//        override fun onReceive(p0: Context?, p1: Intent?) {
//            Log.d(TAG, "close clicked")
//            val manager = p0?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//            manager.cancel(NOTIFICATION_ID)
//
//        }
//    }

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

//    fun createOverlay() {
//        // set the layout parameters of the window
//        val mParams = WindowManager.LayoutParams( // Shrink the window to wrap the content rather
//            // than filling the screen
//            WindowManager.LayoutParams.WRAP_CONTENT,
//            WindowManager.LayoutParams.WRAP_CONTENT,  // Display it on top of other application windows
//            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,  // Don't let it grab the input focus
//            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
//                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, //FLAG_NOT_FOCUSABLE,  // Make the underlying application window visible
//            // through any transparent parts
//            PixelFormat.TRANSLUCENT
//        )
//
//        // getting a LayoutInflater
//        val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        // inflating the view with the custom layout we created
//        mView = layoutInflater.inflate(R.layout.popup_window, null)
//        // set onClickListener on the remove button, which removes
//        // the view from the window
//        mView.findViewById<View>(R.id.window_close).setOnClickListener { stopSelf() }
//
//        mView.findViewById<View>(R.id.simulate_touch).setOnClickListener {
//            Log.d("WINDOW", "Clickme")
//            val accService = AccService.getInstance()!!
//            accService.callMe("From Window")
////            accService.doRightThenDownDrag()
//            accService.gesture2()
//        }
//
//        // Define the position of the
//        // window within the screen
//        mParams.gravity = Gravity.CENTER
//        (getSystemService(Context.WINDOW_SERVICE) as WindowManager).addView(mView, mParams)
//    }
//
//    fun closeOverlay() {
//        try {
//            // remove the view from the window
//            (getSystemService(Context.WINDOW_SERVICE) as WindowManager).removeView(mView)
//            // invalidate the view
//            mView.invalidate()
//            // remove all views
//            (mView.parent as ViewGroup).removeAllViews()
//
//            // the above steps are necessary when you are adding and removing
//            // the view simultaneously, it might give some exceptions
//        } catch (e: Exception) {
//            Log.d(TAG, e.toString())
//        }
//    }
//
    override fun onDestroy() {
        Log.d(TAG, "destroying")
        inputDev.stop()
        mThread.interrupt()
        super.onDestroy()
    }

}