package me.ductran.controllermapper.evdev

import android.util.Log

data class TouchPoint(var x: Int, var y: Int) {

    fun move(tx: Int, ty: Int) {
        x=tx
        y=ty
    }
    fun rmove(dx: Int, dy: Int) {
        x += dx
        y += dy
    }
    fun offset(center: TouchPoint) = TouchPoint(x+center.x, y+center.y)
}

class TouchManager private constructor(private val devicePath: String,
                   private val adb: ShellSimple,
                   private val screenWidth: Int,
                   private val screenHeight: Int
) {
    companion object {
        const val TAG = "TOUCH"
        const val MAX_ID = 10

        const val TOUCH_MAJOR = 9 // not sure, this is from getevent, seem they around 8/9/10
        const val PRESSURE = 1000

        private var me: TouchManager? = null
        fun getInst(devicePath: String, adb: ShellSimple, width: Int, height: Int): TouchManager {
            if (me == null) me = TouchManager(devicePath, adb, width, height)
            return me!!
        }
    }

    var screenIsLandscape = false

    private var trackingId = 111L
    private var activeCnt = 0
    private var curSlotId = -1
    private val slotIdUsed = BooleanArray(MAX_ID){false}

    private fun consumeSlotId(t: Touch) {
        val id = slotIdUsed.indexOfFirst {!it}
        assert(id in 0 until MAX_ID && activeCnt < MAX_ID)
        slotIdUsed[id] = true
        activeCnt++
        t.trackingId = trackingId++
        t.slotId = id
    }
    private fun releaseSlotId(t: Touch) {
        if (t.slotId in 0 until MAX_ID && activeCnt > 0) {
            slotIdUsed[t.slotId] = false
            activeCnt--
            if (activeCnt<0) activeCnt=0
        }
        t.slotId = -1
    }

    private fun sendev(msg: String) {
        adb.send("sendevent $devicePath $msg")
    }

    fun newTouch(startAtCenter: Boolean=false): Touch {
        return Touch(this, startAtCenter)
    }

    private fun changeSlotId(id: Int) {
        if (id != curSlotId)
            sendev("$EV_ABS $ABS_MT_SLOT $id")
        curSlotId = id
    }

    private fun screenCoord(p: TouchPoint): List<Int> {
        val x = if (screenIsLandscape) screenWidth - p.y else p.x
        val y = if (screenIsLandscape) p.x else p.y
        Log.d(TAG, "screenCoord org $screenIsLandscape $screenWidth ${p.x}-${p.y} -> $x-$y")
        return listOf(x,y)
    }

    @Synchronized private fun startTouch(t: Touch, p: TouchPoint) {
        consumeSlotId(t)
        val (x,y) = screenCoord(p)

        changeSlotId(t.slotId)

        sendev("$EV_ABS $ABS_MT_TRACKING_ID ${t.trackingId}")
        sendev("$EV_ABS $ABS_MT_POSITION_X $x")
        sendev("$EV_ABS $ABS_MT_POSITION_Y $y")
        sendev("$EV_ABS $ABS_MT_TOUCH_MAJOR $TOUCH_MAJOR")
        sendev("$EV_ABS $ABS_MT_PRESSURE $PRESSURE")

        if (activeCnt==1)
            sendev("$EV_KEY $BTN_TOUCH $DOWN")

//        // try keep other touch active
//        for (id in 0 until MAX_ID) {
//            var resync = false
//            if (slotIdUsed[id] && id != t.slotId) {
//                changeSlotId(id)
//                sendev("$EV_ABS $ABS_MT_PRESSURE $PRESSURE")
//                resync = true
//            }
//            if (resync) sendev("$EV_SYN $SYN_REPORT 0")
//        }

        sendev("$EV_SYN $SYN_REPORT 0")
    }

    @Synchronized private fun endTouch(t: Touch) {
        changeSlotId(t.slotId)
        sendev("$EV_ABS $ABS_MT_TOUCH_MAJOR 0")
        sendev("$EV_ABS $ABS_MT_PRESSURE 0")
        sendev("$EV_ABS $ABS_MT_TRACKING_ID -1")

        if (activeCnt==1) {
            sendev("$EV_KEY $BTN_TOUCH $UP")
            curSlotId = -1
        }
        sendev("$EV_SYN $SYN_REPORT 0")

        releaseSlotId(t)
    }

    @Synchronized private fun moveTouch(t: Touch, p: TouchPoint, noX: Boolean=false, noY: Boolean=false) {
        val (x,y) = screenCoord(p)
        changeSlotId(t.slotId)
        if (!noX)
            sendev("$EV_ABS $ABS_MT_POSITION_X $x")
        if (!noY)
            sendev("$EV_ABS $ABS_MT_POSITION_Y $y")
        sendev("$EV_SYN $SYN_REPORT 0")
//        // try keep other touch active
//        for (id in 0 until MAX_ID) {
//            var resync = false
//            if (slotIdUsed[id] && id != t.slotId) {
//                changeSlotId(id)
//                sendev("$EV_ABS $ABS_MT_PRESSURE $PRESSURE")
//                resync = true
//            }
//            if (resync) sendev("$EV_SYN $SYN_REPORT 0")
//        }
    }

    class Touch(private val M: TouchManager,
                private val defaultStartAtCenter: Boolean = false
    ) {
        var trackingId = 0L
        var slotId = -1
        val curPos = TouchPoint(0,0)
        private val centerPos = TouchPoint(0,0)

        private var flag = 0

        val isActive: Boolean
            get() = slotId>=0

        fun setCenterPosition(x: Int, y: Int): Touch {
            centerPos.move(x,y)
            return this
        }

        fun setFlag(setBits: Int) {
            flag = flag or setBits
        }
        fun clearFlag() {
            flag = 0
        }
        fun getFlag(bits: Int): Boolean {
            val s = flag and bits
            return (s!=0)
        }

        private fun updatePos(x: Int, y: Int): TouchPoint {
            curPos.move(x,y)
            return curPos.offset(centerPos)
        }

        fun start(x: Int=0, y: Int=0) {
            if (isActive) M.endTouch(this)
            M.startTouch(this, updatePos(x,y))
        }


        fun move(x: Int, y: Int, noX: Boolean=false, noY: Boolean=false) {
//            val noX = x==curPos.x
//            val noY = y==curPos.y
            if (!isActive) {
                if (defaultStartAtCenter) {
                    M.startTouch(this, centerPos)
                    M.moveTouch(this, updatePos(x,y), noX, noY)
                } else {
                    M.startTouch(this, updatePos(x,y))
                }
            }
            else M.moveTouch(this, updatePos(x,y), noX, noY)
        }

        fun rmove(dx: Int=0, dy: Int=0) {
            move(curPos.x+dx, curPos.y+dy)
        }

        fun left(dx: Int) = move(curPos.x-dx, curPos.y, noY=true)
        fun right(dx: Int) = move(curPos.x+dx, curPos.y, noY=true)
        fun up(dy: Int) = move(curPos.x, curPos.y-dy, noX=true)
        fun down(dy: Int) = move(curPos.x, curPos.y+dy, noX=true)


        fun end() {
            if (isActive) M.endTouch(this)
            curPos.move(0, 0)
        }
    }
}

