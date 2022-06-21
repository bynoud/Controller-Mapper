package me.ductran.controllermapper.evdev

import android.util.Log

class ControllerToTouch(devPath: String, injectShell: ShellSimple) : EventListener {

    companion object {
        const val TAG = "C2T"
    }

    private val touchManager = TouchManager(devPath, injectShell)

    val touch0 = touchManager.newTouch().setCenterPosition(500,800)
    val touch1 = touchManager.newTouch().setCenterPosition(500,1500)

    init {
        Log.d(TAG, "Started")
    }

    override fun onKeyDown(keyCode: Int) {
        Log.d(TAG, "Down $keyCode")
    }

    override fun onKeyUp(keyCode: Int) {
        Log.d(TAG, "Up $keyCode")
    }

    override fun onKey(keyCode: Int, value: Int) {
        Log.d(TAG, "Key $keyCode $value")
    }

    override fun onMotion(type: ControllerAxisType, x: Int, y: Int) {
        Log.d(TAG, "Motion $type $x $y")
        if (type == ControllerAxisType.AXIS_LEFT) {
            if (x==0 && y==0) touch0.end()
            else touch0.move(x,y)
        } else if (type == ControllerAxisType.AXIS_RIGHT) {
            if (x==0 && y==0) touch1.end()
            else touch1.move(x,y)
        }

//        if (value < 2) touch0.end()
//        else touch0.move()
    }

}