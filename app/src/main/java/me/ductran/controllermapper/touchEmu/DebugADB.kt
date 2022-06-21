package me.ductran.controllermapper.touchEmu


//import android.annotation.SuppressLint
import android.Manifest
import android.R.attr
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*
import me.ductran.controllermapper.AppADB
import me.ductran.controllermapper.R
import me.ductran.controllermapper.touchEmu.Shell
import java.io.*
import java.lang.Runnable
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

interface ShellReadyListener {
    fun onShellReady(ready: Boolean)
}

open class DebugADB(val context: Context): ShellOutputListener {
    companion object {
        const val TAG = "DADB"
    }

    protected val sharedPreferences = context
        .getSharedPreferences(
            context.getString(R.string.pref_file),
            Context.MODE_PRIVATE
        )

    private val adbPath = "${context.applicationInfo.nativeLibraryDir}/libadb.so"

    protected var adbShell: Shell? = null

    protected var onReady: ShellReadyListener? = null

//    fun onReady(owner: LifecycleOwner,
//                onTrue: Runnable?=null,
//                onFalse: Runnable?=null
//    ) {
//        Log.d(AppADB.TAG, "Add restart callback")
//        adbShell?.ready?.observe(owner, Observer {
//            Log.d(AppADB.TAG, "ready $it")
//            if (it == true) onTrue?.run()
//            if (it == false) onFalse?.run()
//        })
//    }

    private fun adbCmd(cmd: String): Shell {
        return Shell(context, listOf(adbPath, cmd)).start()
    }

    /**
     * Completely reset the ADB client
     */
    fun stop() {
        Log.d(TAG, "Destroying shell process")
        adbShell?.stop()
        adbCmd("disconnect").waitFor()
        adbCmd("kill-server").waitFor()
    }

    private fun setVarBool(name: String, value: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(name, value)
            apply()
        }
    }

    /**
     * Ask the device to pair on Android 11 phones
     */
    fun pair(port: String, code: String): Boolean {
        Log.d(TAG, "Pairing port=$port code=$code")
        setVarBool(context.getString(R.string.paired_key), false)
//        adbCmd("kill-server").waitFor()
        Shell(context,listOf(adbPath,"start-server")).runUtilSucess()
        Log.d(TAG, "Succccc")
        val sh = Shell(context,listOf(adbPath,"pair", "localhost:$port")).start()
        sh.send(code)
        val ok = sh.waitFor(3)
        Log.d(TAG, "PairShell now $ok")
        if (ok) setVarBool(context.getString(R.string.paired_key), true)
        return ok
    }

    fun checkForSupport(): Boolean {
        if (Build.SUPPORTED_64_BIT_ABIS.isNullOrEmpty() &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        ) {
            Log.d(TAG, "Not supported")
            return false
        }
        return true
    }

    fun shouldWePair(): Boolean {
        Log.d(TAG, "Should we pair")
        if (!sharedPreferences.getBoolean(context.getString(R.string.paired_key), false)) {
            Log.d(TAG, "Not ${Build.VERSION.SDK_INT} ${Build.VERSION_CODES.R}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                return true
        }
        return false
    }

    override fun onShellOutput(msg: String) {
        Log.d(TAG, "This output")
    }
    override fun onShellError(msg: String) {

    }

    override fun onShellReady(ready: Boolean) {
        onReady?.onShellReady(ready)
    }

    open fun startShell(onReady: ShellReadyListener? = null) {
        Log.d(TAG, "Starting ADB client")
        this.onReady = onReady

//        adbCmd("kill-server").waitFor()
        Shell(context,listOf(adbPath,"start-server")).runUtilSucess()
//        Shell(context,listOf(adbPath,"wait-for-device")).runUtilSucess(1000)
        adbCmd("wait-for-device").waitFor()

//        adbShell = Shell(context,listOf(adbPath,"shell")).runUtilSucess(1000, 2000)
//        adbShell!!.startShellDeathThread()
        adbShell = Shell(context,
            listOf(adbPath, "shell"),
            listOf("alias adb=\"$adbPath\"", "su")
        )
//        adbShell!!.outputListener = this
        adbShell!!.start(true)

        val secureSettingsGranted =
            context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "Shelling into device $secureSettingsGranted")
        if (!secureSettingsGranted) {
            exe("pm grant me.ductran.controllermapper android.permission.WRITE_SECURE_SETTINGS")
        }

//        val secureSettingsGranted =
//            context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
//        debug("Shelling into device $secureSettingsGranted")
//        if (!secureSettingsGranted) {
//            debug("NOTE: Granting secure settings permission for next time $applicationId")
//            sendToShellProcess("pm grant $applicationId android.permission.WRITE_SECURE_SETTINGS")
//        }

        Log.d(TAG, "NOTE: Dropped into ADB shell automatically")
    }

    fun exe(cmd: String) {
        adbShell?.send(cmd)
    }

}