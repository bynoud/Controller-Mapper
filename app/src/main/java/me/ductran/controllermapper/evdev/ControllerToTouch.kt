package me.ductran.controllermapper.evdev

import android.content.Context
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.Log
import me.ductran.controllermapper.App
import me.ductran.controllermapper.GlobalStorage
import kotlin.coroutines.coroutineContext

private const val FLAG_TR = 0x1
private const val FLAG_TR2 = 0x1 shl 1
private const val FLAG_TL = 0x1 shl 2
private const val FLAG_TL2 = 0x1 shl 3


class ControllerToTouch(
    devPath: String,
    injectShell: ShellSimple,
    width: Int,
    height: Int
) : EventListener {

    companion object {
        const val TAG = "C2T"
    }

    private val touchManager = TouchManager.getInst(devPath, injectShell, width=width, height=height)
    private val stateStorage = StateStorage.getInst()
    private val globalVar = GlobalStorage.getInst()

    private var stopMove = false
    private val moveStick = touchManager.newTouch(startAtCenter=true).setCenterPosition(436,680)
    private val btnDash = touchManager.newTouch().setCenterPosition(2150,850)
    // attacking
    private val btnPass = touchManager.newTouch().setCenterPosition(1875,870)
    private val btnThrough = touchManager.newTouch().setCenterPosition(1935,630)
    private val btnShot = touchManager.newTouch().setCenterPosition(2175,570)
    // defending
    private val btnPress = btnThrough
    private val btnSwitch = btnPass
    private val btnTackle = btnShot

    init {
        Log.d(TAG, "Started")
    }

    override fun setScreenOrientation(isLandscape: Boolean) {
        Log.d(TAG, "set orientation $isLandscape")
        touchManager.screenIsLandscape = isLandscape
    }

//    private val isStunning: Boolean
//        get() = stateStorage.getBtn(BTN_TR2)

    private fun setBtnFlag(t: TouchManager.Touch) {
        t.clearFlag()
        if (stateStorage.getBtn(BTN_TR)) t.setFlag(FLAG_TR)
        if (stateStorage.getBtn(BTN_TR2)) t.setFlag(FLAG_TR2)
        if (stateStorage.getBtn(BTN_TL)) t.setFlag(FLAG_TL)
        if (stateStorage.getBtn(BTN_TL2)) t.setFlag(FLAG_TL2)
    }
    private fun getBtnFlg(t: TouchManager.Touch): List<Boolean> {
        val flags = listOf(
            t.getFlag(FLAG_TR) || stateStorage.getBtn(BTN_TR),
            t.getFlag(FLAG_TR2) || stateStorage.getBtn(BTN_TR2),
            t.getFlag(FLAG_TL) || stateStorage.getBtn(BTN_TL),
            t.getFlag(FLAG_TL2) || stateStorage.getBtn(BTN_TL2),
        )
        return flags
    }


    private fun doPass(press: Boolean, isLoft: Boolean) {
        if (press) {
            // FIXME: add kick feint if previous press shot
            setBtnFlag(btnPass)
            btnPass.start(0,0)
        } else {
            val (r1, r2, l1, l2) = getBtnFlg(btnPass)
            if (r2) {
                if (isLoft) btnPass.right(200) // stunning cross
                else btnPass.left(200) // stunning pass
            } else if (isLoft) {
                btnPass.up(200) // cross
            }
            btnPass.end()
            if (l1) {
                // pass and run
                doLeftFlick()
            } else if (l2) {
                // pass-run crossover
                moveStick.start()
                moveStick.down(200)
                moveStick.up(300)
                moveStick.end()
            }
        }
    }

    private fun doThrough(press: Boolean) {
        if (press) {
            setBtnFlag(btnThrough)
            btnThrough.start()
        } else {
            val (r1, r2,l1) = getBtnFlg(btnThrough)
            if (r2) {
                if (l1) btnThrough.right(200) // stunning loft through
                else btnThrough.left(200) // stunning through
            } else if (l1) {
                btnThrough.up(200) // loft through
            }
            btnThrough.end()
        }
    }

    private fun doShoot(press: Boolean) {
        if (press) {
            setBtnFlag(btnShot)
            btnShot.start(0,0)
        } else {
            val (r1,r2,l1) = getBtnFlg(btnShot)
            if (l1) {
                btnShot.up(200) // chiped shot
            } else if (r1) {
                btnShot.down(200) // controlled shot
            } else if (r2) {
                btnShot.right(200) // stunning
            }
            btnShot.end()
        }
    }

    private fun doShield(press: Boolean) {
        if (!press) return
        doLeftDoubleTap()
    }

    private fun doLeftDoubleTap() {
        stopMove = true
        moveStick.start()
        moveStick.start()
        moveStick.end()
        stopMove = false
    }

    private fun doLeftFlick() {
        val curPos = moveStick.curPos
        stopMove = true
        moveStick.start()
        moveStick.move(curPos.x, curPos.y)
        moveStick.end()
        stopMove = false
    }

    private fun doDash(press: Boolean) {
        if (!press) return btnPass.end()
        // FIXME: Add quick stop if leftstick is zero
        btnDash.start()
        val dc = stateStorage.isBtnDoubleClicked(BTN_TR)
//        Log.d(TAG, "Dash $dc")
        if (dc) doLeftFlick()
    }

    private fun doMove(x: Int, y: Int) {
        if (stopMove) return
        if (x==0 && y==0) moveStick.end()
        else moveStick.move(x,y)
    }

    private fun doSkill(x: Int, y: Int) {
        Log.d(TAG, "Do skil $x $y")
        if (x==0 && y==0) {
            stopMove = true
            moveStick.end()
            btnDash.start()
            btnDash.end()
            doLeftFlick()
        }
    }

    // defending
    private fun doMatchup(press: Boolean) {
        if (!press) return btnPass.end()
        if (stateStorage.isBtnDoubleClicked(BTN_A)) {
            // do shouder charge
            doLeftDoubleTap()
        } else {
            btnPress.start(0,0)
            btnPress.left(200)
        }
    }
    private fun doCallGK(press: Boolean) {
        if (!press) return btnPress.end()
        btnPress.start()
        btnPress.down(200)
    }
    private fun doCallTeammate(press: Boolean) {
        if (press) {
            btnPress.start()
            btnPress.up(200)
            btnPress.end()
        }
    }
    private fun doPressure(press: Boolean) {
        if (press) btnPress.start()
        else btnPress.end()
    }

    private fun doSlidingTackle(press: Boolean) {
        if (press) {
            btnTackle.start()
            btnTackle.end()
        }
    }

    private fun doManualSwitch(x: Int, y: Int) {
        if (x==0 && y==0) {
            val (mx, my) = stateStorage.getAxisMax(ControllerAxisType.AXIS_RIGHT)
            Log.d(TAG, "manual switch $mx $my")
            btnSwitch.start()
            btnSwitch.move(mx,my)
            btnSwitch.end()
        }
    }

    override fun onKey(keyCode: Int, value: Boolean) {
        val attacking = globalVar.isAttacking
        Log.d(TAG, "Key $keyCode $value ${SystemClock.uptimeMillis()} $attacking")
        when (keyCode) {
            BTN_A -> if (attacking) doPass(value, false) else doMatchup(value)
            BTN_B -> if (attacking) doPass(value, true) else doSlidingTackle(value)
            BTN_Y -> if (attacking) doThrough(value) else doCallGK(value)
            BTN_X -> if (attacking) doShoot(value) else doCallTeammate(value)
            BTN_TR -> doDash(value)
            BTN_TL2 -> if (attacking) doShield(value) else doPressure(value)
        }
    }

    override fun onMotion(type: ControllerAxisType, x: Int, y: Int) {
        Log.d(TAG, "Motion $type $x $y")
        if (type==ControllerAxisType.AXIS_LEFT) {
            doMove(x,y)
        } else if (type==ControllerAxisType.AXIS_RIGHT) {
            if (globalVar.isAttacking) doSkill(x, y) else doManualSwitch(x, y)
        } else {
            Log.d(TAG, "Unsupported motion $type")
        }

//        else if (type == ControllerAxisType.AXIS_RIGHT) {
//            if (x==0 && y==0) touch1.end()
//            else touch1.move(x,y)
//        }

    }

}