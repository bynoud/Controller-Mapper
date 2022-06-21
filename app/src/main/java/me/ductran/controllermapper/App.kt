package me.ductran.controllermapper

import android.app.Application
import android.content.Context
import android.content.SharedPreferences


class App : Application() {
    override fun onCreate() {
        super.onCreate()
        application = this
    }

    companion object {
        var application: Application? = null
            private set
        val context: Context
            get() = application!!.applicationContext

        val sharedPref: SharedPreferences
            get() = context
            .getSharedPreferences(
                context.getString(R.string.pref_file),
                Context.MODE_PRIVATE
            )
    }
}