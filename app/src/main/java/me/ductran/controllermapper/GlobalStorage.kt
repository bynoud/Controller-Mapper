package me.ductran.controllermapper

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log

class GlobalStorage private constructor() {
    companion object {
        private const val TAG = "GVAR"

        fun getScreenSize(): List<Int> {
            val metrics: DisplayMetrics =
                App.context.resources.displayMetrics
            return listOf(metrics.widthPixels, metrics.heightPixels)
        }

        fun setBool(resId: Int, value: Boolean) = with(App.sharedPref.edit()) {
            putBoolean(App.context.getString(resId), value)
            apply()
        }
        fun getBool(resId: Int, default: Boolean=false) = App.sharedPref.getBoolean(App.context.getString(resId), default)

        fun setString(resId: Int, value: String) = with(App.sharedPref.edit()) {
            putString(App.context.getString(resId), value)
            apply()
        }
        fun getString(resId: Int, default: String="") = App.sharedPref.getString(App.context.getString(resId), default)!!

        private var me: GlobalStorage? = null
        fun getInst(): GlobalStorage {
            if (me==null) me = GlobalStorage()
            return me!!
        }
    }

    var setupBrgUri: Uri? = null

    // game dependence setup
    val screenDetectPollingMs = 1000L // dont set too low, it will through ERROR_TAKE_SCREENSHOT_INTERVAL_TIME_SHORT
    private val screenDetectX = 1877
    private val screenDetectY = 619
    private val screenDetectWidth = 10
    private val screenDetectHeight = 4
    private val grayAttackThreshold = 7000
    var isAttacking = false
        get() = field
        private set

    private fun rgb2gray(p: Int): Int {
        val r = Color.red(p)
        val g = Color.green(p)
        val b = Color.blue(p)
        val gray = 0.2126*r + 0.7152*g + 0.0722*b
        return gray.toInt()
    }

    fun detectAttacking(bitmap: Bitmap) {
        val bits = Bitmap.createBitmap(bitmap,
            screenDetectX,
            screenDetectY,
            screenDetectWidth,
            screenDetectHeight)
            .copy(Bitmap.Config.ARGB_8888, true)
        var sum = 0
        for (row in 0 until bits.height) {
            for (col in 0 until bits.width) {
                sum += rgb2gray(bits.getPixel(col, row))
            }
        }
        isAttacking = (sum >= grayAttackThreshold)
        Log.d(TAG, "Attacking $isAttacking")
    }

}