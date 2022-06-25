package me.ductran.controllermapper.evdev

import android.content.Context
import android.os.SystemClock
import android.util.Log


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

//data class SimpleInputEvent(
//    val type: Int,
//    val code: Int,
//    val value: Int
//)

const val DOUBLE_CLICK_THRESH = 500

enum class ControllerAxisType {
    AXIS_LEFT, AXIS_RIGHT, AXIS_DPAD
}

interface EventListener {
    fun onKey(keyCode: Int, value: Boolean)
    fun onMotion(type: ControllerAxisType, x: Int, y: Int)
    fun setScreenOrientation(isLandscape: Boolean)
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

class StateStorage private constructor() {

    companion object {
        private var me: StateStorage? = null
        fun getInst(): StateStorage {
            if (me == null) me = StateStorage()
            return me!!
        }
    }

    private data class ButtonState(
        var down: Boolean,
        var timeMs: Long,
        var doubleClicked: Boolean
    )

    private val axisState = mutableMapOf(
        ControllerAxisType.AXIS_RIGHT to listOf(0,0),
        ControllerAxisType.AXIS_LEFT to listOf(0,0),
        ControllerAxisType.AXIS_DPAD to listOf(0,0)
    )
    private val axisMax = mutableMapOf(
        ControllerAxisType.AXIS_RIGHT to listOf(0,0),
        ControllerAxisType.AXIS_LEFT to listOf(0,0),
        ControllerAxisType.AXIS_DPAD to listOf(0,0)
    )
    private val lastAxisState = axisState.toMutableMap()

    private val btnState = mutableMapOf<Int,ButtonState>()
    private val lastBtnState = mutableMapOf<Int,Boolean>()

    fun setAxis(code: Int, value: Int) {
        if (!axisTypeMap.containsKey(code)) return
        val (key, isY) = axisTypeMap[code]!!
        val cur = axisState[key]!!
        val max = axisMax[key]!!
        axisState[key] = if (isY==1) listOf(cur[0],value) else listOf(value,cur[1])
        axisMax[key] = if (isY==1 && value>max[1]) listOf(max[0],value) else
            if (isY==0 && value>max[0]) listOf(value,max[1]) else max
    }
    fun getAxisMax(type: ControllerAxisType) = if (axisMax.containsKey(type)) axisMax[type]!! else listOf(0,0)

    fun setBtn(code: Int, value: Boolean) {
        val timeMs = SystemClock.uptimeMillis()
        val doubleClicked = if (!btnState.containsKey(code)) false else
            (timeMs - btnState[code]!!.timeMs) <= DOUBLE_CLICK_THRESH
        btnState[code] = ButtonState(
            down = value,
            timeMs = timeMs,
            doubleClicked = doubleClicked)
    }
    fun getBtn(code: Int) = if (btnState.containsKey(code)) btnState[code]!!.down else false
    fun isBtnDoubleClicked(code: Int) = if (btnState.containsKey(code)) btnState[code]!!.doubleClicked else false

    fun getUpdateAxis() = sequence {
        for ((key,value) in axisState) {
            if (lastAxisState[key] != value) {
                lastAxisState[key] = value
                yield(Triple(key, value[0], value[1]))
            }
        }
    }

//    fun getUpdateBtn() = sequence<Pair<Int,Boolean>> {
//        val last = lastBtnState.toMap()
//        val cur = btnState.toMap()
//        for ((key,value) in cur) {
//            if (!last.containsKey(key) || last[key] != value) {
//                lastBtnState[key] = value
//                yield(Pair(key,value))
//            }
//        }
//    }
}



class EventParser private constructor() : DefaultShellOutputListener() {
    companion object {
        const val TAG = "EvParser"


        private var me: EventParser? = null
        fun getInst(): EventParser {
            if (me == null) me = EventParser()
            return me!!
        }
    }

//    private val axisStorage = mapOf(
//        ControllerAxisType.AXIS_RIGHT to mutableListOf(0,0),
//        ControllerAxisType.AXIS_LEFT to mutableListOf(0,0),
//        ControllerAxisType.AXIS_DPAD to mutableListOf(0,0)
//    )
//    private val btnStorage = mutableMapOf<Int,Boolean>()

    private var mThread: Thread? = null

    private var listener: EventListener? = null
    fun addEventListener(listener: EventListener) {
        this.listener = listener
    }

    private val stateStorage = StateStorage.getInst()

    fun start() {
        mThread = Thread {
            Log.d(EventParser.TAG, "Thread starting")
//            val lastBtnStorage = mutableMapOf<Int, Boolean>()
//            val lastAxis = mapOf(
//                ControllerAxisType.AXIS_RIGHT to mutableListOf(0, 0),
//                ControllerAxisType.AXIS_LEFT to mutableListOf(0, 0),
//                ControllerAxisType.AXIS_DPAD to mutableListOf(0, 0)
//            )

            try {
                while (true) {
                    stateStorage.getUpdateAxis().forEach {
                        listener?.onMotion(it.first, it.second, it.third)
                    }
//                    stateStorage.getUpdateBtn().forEach {
//                        listener?.onKey(it.first, it.second)
//                    }
////                Log.d(TAG, "Checking thread")
//                    val cp = btnStorage.toMap()
//                    for ((key, value) in cp) {
////                        Log.d(TAG, "Key $key ${lastBtnStorage.containsKey(key)} ${lastBtnStorage[key]} $value")
//                        if (!lastBtnStorage.containsKey(key) || lastBtnStorage[key] != value) {
//                            listener?.onKey(key, value)
//                            lastBtnStorage[key] = value
//                        }
//                    }
//                    for ((key, value) in lastAxis) {
//                        val newVal = axisStorage[key]!!
//                        if (value != newVal) {
//                            listener?.onMotion(key, newVal[0], newVal[1])
//                            value[0] = newVal[0]
//                            value[1] = newVal[1]
//                        }
//                    }
                    Thread.sleep(10)
                }
            } catch (e: Exception) {
                if (e is InterruptedException) {
                    Log.d(EventParser.TAG, "Thread interrupted")
                    return@Thread
                }
                Log.d(EventParser.TAG, "Thread not interrupted?")
                return@Thread
            }
        }
        mThread!!.start()
    }

    fun stop() {
        Log.d(TAG, "Stoping")
        mThread?.interrupt()
    }


//    private fun setAxisStorage(code: Int, value: Int): ControllerAxisType {
//        val axis = axisTypeMap[code]!!
////        axisStorage[axis.first]!![axis.second] = value
//        stateStorage.setAxis(code, value)
//        return axis.first
//    }

    override fun onShellOutput(msg: String) {
        try {
//            Log.d(TAG, "Raw $msg")
            val txt = msg.split(" ").map { it.toLong(radix=16) }.map{it.toInt()}
            var (type, code, value) = txt

            if (type==EV_ABS) {
                if (!absSupportList.contains(code)) return
                if (absCorrectionList.contains(code)) value -= absCorrectionCenter // adjust the value to center
//                setAxisStorage(code, value)
                stateStorage.setAxis(code, value)
//                val (x,y) = axisStorage[axis]!!
//                for (l in listeners)  l.onMotion(axis, x, y)
            }
            else if (type==EV_KEY) {
//                btnStorage[code] = value!=0
                val down = value!=0
                stateStorage.setBtn(code, down)
                listener?.onKey(code, down)
//                for (l in listeners) {
//                    l.onKey(code, value)
//                    if (isDown) l.onKeyDown(code)
//                    else l.onKeyUp(code)
//                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "output parsing error: $e")
        }
    }

    fun setScreenOrientation(isLandscape: Boolean) {
        listener?.setScreenOrientation(isLandscape)
    }
}
