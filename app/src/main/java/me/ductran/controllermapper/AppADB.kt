package me.ductran.controllermapper

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

class AppADB(
    private val application: Application = App.application!!,
    private val appCtx: Context = application.applicationContext) : FilelogADB(appCtx) {
    private val sharedPreferences = appCtx
        .getSharedPreferences(
            appCtx.getString(R.string.pref_file),
            Context.MODE_PRIVATE
        )

    companion object {
        val TAG = "AppADB"
    }

    fun exe(msg: String) {
        sendToShellProcess(msg)
    }



    fun onRestart(owner: LifecycleOwner, callback: Runnable) {
        Log.d(TAG, "Add restart callback")
        closed.observe(owner, Observer {
            if (it == true) {
                Log.d(TAG, "Closed")
                callback.run()
            }
        })
    }

    fun onReady(owner: LifecycleOwner, callback: Runnable) {
        Log.d(TAG, "Add ready callback")
        ready.observe(owner, Observer {
            if (it == true) {
                Log.d(TAG, "ready")
                callback.run()
            }
        })
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
        Log.d("VIEW", "Should we pair")
        if (!sharedPreferences.getBoolean(appCtx.getString(R.string.paired_key), false)) {
            Log.d("VIEW", "Not ${Build.VERSION.SDK_INT} ${Build.VERSION_CODES.R}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                return true
        }

        return false
    }

    override fun pair(port: String, code: String) {
        super.pair(port, code)
        with(sharedPreferences.edit()) {
            putBoolean(appCtx.getString(R.string.paired_key), true)
            apply()
        }
    }
}