package me.ductran.controllermapper

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ColorSpace
import android.os.AsyncTask
import android.os.SystemClock
import android.util.Log
import android.view.Display
import android.view.accessibility.AccessibilityEvent

class ScreenMonitorService : AccessibilityService(), AccessibilityService.TakeScreenshotCallback {
    companion object {
        val TAG = "SMON"
        private var me: ScreenMonitorService? = null
        fun getInst() = me
    }

    private var monThread: Thread? = null
    private val globalVar = GlobalStorage.getInst()

    fun startMonitor() {
        Log.d(TAG, "starting")
        monThread = Thread {
            while(true) {
                captureScreen()
                Thread.sleep(globalVar.screenDetectPollingMs)
            }
        }
        monThread!!.start()
    }
    fun stopMonitor() {
        Log.d(TAG, "stopping")
        monThread?.interrupt()
        monThread = null
    }

    fun captureScreen() {
//        Log.d(TAG, "start takeSS ${SystemClock.uptimeMillis()}")
        takeScreenshot(
            Display.DEFAULT_DISPLAY,
            applicationContext.mainExecutor,
            this
        )
    }

    override fun onFailure(p0: Int) {
        Log.d(TAG, "Screenshot failed $p0")
    }

    override fun onSuccess(ss: ScreenshotResult) {
//        Log.d(TAG, "Screenshot result ${ss.hardwareBuffer.width}")
        if (ss.hardwareBuffer.width < 2000) return
        val bitmap = Bitmap.wrapHardwareBuffer(ss.hardwareBuffer, ss.colorSpace)!!
        globalVar.detectAttacking(bitmap)
        ss.hardwareBuffer.close()
//        Log.d(TAG, "done takeSS ${SystemClock.uptimeMillis()}")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "connected")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "created")
        me = this
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "destroy")
        me = null
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
//        Log.d(TAG, "event")
//        captureScreen()

    }

    override fun onInterrupt() {
        Log.d(TAG, "interrupt")
    }


}