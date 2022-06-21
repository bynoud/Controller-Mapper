package me.ductran.controllermapper.touchEmu

import me.ductran.controllermapper.AppADB

data class TouchPoint(var x: Int, var y: Int) {
    fun move(tx: Int, ty: Int) {
        x=tx
        y=ty
    }
    fun rmove(dx: Int, dy: Int) {
        x += dx
        y += dy
    }
}

class TouchManager(private val devicePath: String,
                   private val adb: AppADB) {
    companion object {
        const val TAG = "TEMU"
        const val MAX_ID = 10

        const val TOUCH_MAJOR = 9 // not sure, this is from getevent, seem they around 8/9/10
        const val PRESSURE = 1000

        private var me: TouchManager? = null
        fun getInst(devicePath: String, adb: AppADB): TouchManager {
            if (me == null) me = TouchManager(devicePath, adb)
            return me!!
        }
    }

    private var trackingId = 1
    private var activeCnt = 0
    private var curSlotId = -1
    private val slotIdUsed = BooleanArray(MAX_ID){false}

    private fun consumeSlotId(): Int {
        val id = slotIdUsed.indexOfFirst {!it}
        assert(id in 0 until MAX_ID && activeCnt < MAX_ID)
        slotIdUsed[id] = true
        activeCnt++
        return id
    }
    private fun releaseSlotId(id: Int) {
        assert(id in 0 until MAX_ID && activeCnt > 0)
        slotIdUsed[id] = false
        activeCnt--
    }

    private fun sendev(msg: String) {
        adb.exe("sendevent $devicePath $msg")
    }

    fun newTouch(): Touch {
        return Touch(this, trackingId++)
    }

    private fun changeSlotId(id: Int) {
        if (id != curSlotId)
            sendev("$EV_ABS $ABS_MT_SLOT $id")
        curSlotId = id
    }

    @Synchronized private fun startTouch(t: Touch, p: TouchPoint) {
        t.slotId = consumeSlotId()

        changeSlotId(t.slotId)

        sendev("$EV_ABS $ABS_MT_TRACKING_ID ${t.trackingId}")
        sendev("$EV_ABS $ABS_MT_POSITION_X ${p.x}")
        sendev("$EV_ABS $ABS_MT_POSITION_Y ${p.y}")
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

        releaseSlotId(t.slotId)
        t.slotId = -1
    }

    @Synchronized private fun moveTouch(t: Touch, p: TouchPoint, noX: Boolean=false, noY: Boolean=false) {
        changeSlotId(t.slotId)
        if (!noX)
            sendev("$EV_ABS $ABS_MT_POSITION_X ${p.x}")
        if (!noY)
            sendev("$EV_ABS $ABS_MT_POSITION_Y ${p.y}")
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

    class Touch(private val M: TouchManager, val trackingId: Int) {
        var slotId = -1
        val curPos = TouchPoint(0,0)

        private val isActive: Boolean
            get() = slotId>=0

        fun start(x: Int, y: Int) {
            curPos.move(x,y)
            if (isActive) M.endTouch(this)
            M.startTouch(this, curPos)
        }

        fun rmove(dx: Int=0, dy: Int=0) {
            curPos.rmove(dx, dy)
            M.moveTouch(this, curPos, noX=dx==0, noY=dy==0)
        }

        fun move(x: Int, y: Int) {
            curPos.move(x, y)
            M.moveTouch(this, curPos)
        }

        fun end() {
            if (isActive) M.endTouch(this)
        }
    }
}

