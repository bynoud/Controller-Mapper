package me.ductran.controllermapper


import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.ductran.controllermapper.evdev.ControllerService
import me.ductran.controllermapper.evdev.ScreenMonitor
import me.ductran.controllermapper.touchEmu.ShellReadyListener
import java.io.*


class MainActivity : AppCompatActivity(), ShellReadyListener {

    companion object {
        val TAG = "MAIN"
    }

//    private lateinit var appAdb: DebugADB

    /* UI components */
    private lateinit var command: TextInputEditText
    private lateinit var progress: ProgressBar

    var debugShell: Process? = null
//    var controller: ControllerService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        command = findViewById(R.id.command)
        progress = findViewById(R.id.progress)

        Log.d(TAG, "Starting")

        findViewById<Button>(R.id.debugBtn).setOnClickListener {
            Log.d(TAG, "debug clicked")
            val intent = Intent(this, SetupActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.startBtn).setOnClickListener {
            Log.d(TAG, "start clicked")
            startEmuService()
        }
        findViewById<Button>(R.id.stopBtn).setOnClickListener {
            Log.d(TAG, "stop clicked")
            stopEmuService()

        }
        findViewById<Button>(R.id.forcePairBtn).setOnClickListener {
            pairingCheck(true)
        }

        findViewById<Button>(R.id.debugBtn).setOnClickListener {
            val mon = ScreenMonitor()
            mon.start()
        }

        /* Send commands to the ADB instance */
        command.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    val text = command.text.toString()
                    command.text = null

                    if (text=="debug") {
//                        lifecycleScope.launch(Dispatchers.IO) {
//                            goTest1()
//                        }
//                        lifecycleScope.launch(Dispatchers.IO) {
//                            goTest2()
//                        }
                    } else {
                        lifecycleScope.launch(Dispatchers.IO) {
                            Log.d(TAG, "input $text")
//                            appAdb.exe(text)
//                            sendToShellProcess(debugShell, text)
                        }
                    }
                }
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        pairingCheck()
    }

    fun askForFileManage() {
        if (Environment.isExternalStorageManager()) {
            return
        } else {
            //request for the permission
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
    }

    // method to check is the user has permitted the accessibility permission
    // if not then prompt user to the system's Settings activity
    fun checkAccessibilityPermission(askFor: Boolean=true): Boolean {
        var accessEnabled = 0
        try {
            accessEnabled =
                Settings.Secure.getInt(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED)
        } catch (e: SettingNotFoundException) {
            e.printStackTrace()
        }
        if (accessEnabled == 0) {
            if (askFor) {
                // if not construct intent to request permission
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                // request permission via start activity for result
                startActivity(intent)
            }
        }
        return accessEnabled==1
    }


    @Suppress("DEPRECATION") // Deprecated for third party Services.
    fun isServiceForegrounded() =
        (getSystemService(ACTIVITY_SERVICE) as? ActivityManager)
            ?.getRunningServices(Integer.MAX_VALUE)
            ?.find { it.service.className == ControllerService::class.java.name }
            ?.foreground == true

    fun startEmuService() {
        Log.d(TAG, "Starting service")
        if (checkAccessibilityPermission()) {
            Toast.makeText(this, "Enable AccessibitilyService first", Toast.LENGTH_SHORT).show();
            return
        }
        ScreenMonitorService.getInst()?.startMonitor()

        if (!isServiceForegrounded()) {
            val i = Intent(this, ControllerService::class.java)
            i.action = "/dev/input/event8"
            startService(i)
        }
    }

    fun stopEmuService() {
        Log.d(TAG, "Stopping service")

        ScreenMonitorService.getInst()?.stopMonitor()

        if (isServiceForegrounded()) {
            val m = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            m.cancel(ControllerService.NOTIFICATION_ID)
            stopService(Intent(this, ControllerService::class.java))
        }
    }

    override fun onShellReady(ready: Boolean) {
        Log.d(TAG, "READYYYYYYYYY $ready")
        runOnUiThread {
            command.isEnabled = ready
            progress.visibility = if (ready) View.INVISIBLE else View.VISIBLE
        }
    }


    /**
     * Ask the user to pair
     */

    fun pairingCheck(force: Boolean=false) {
        if (force || ControllerService.shouldWePair()) {
            askToPair {port, code ->
                ControllerService.pair(port, code)
                Log.d(TAG, "askToPair done")
            }
        }
    }

    private fun askToPair(callback: ((String, String) -> Unit)? = null) {
        val builder = AlertDialog.Builder(this)
        val ll = LinearLayout(this);

        ll.orientation = LinearLayout.VERTICAL

        // Set up the input
        val portInput = EditText(this)
        portInput.inputType = InputType.TYPE_CLASS_NUMBER
        portInput.setHint(R.string.port_hint)
        ll.addView(portInput)
        val codeInput = EditText(this)
        codeInput.inputType = InputType.TYPE_CLASS_NUMBER
        codeInput.setHint(R.string.code_hint)
        ll.addView(codeInput)

        builder.setView(ll)
        builder.setTitle("Set Pairing information")

        // Set up the buttons
        builder.setPositiveButton(
            "OK"
        ) { dialog, which ->
            val port = portInput.text.toString()
            val code = codeInput.text.toString()
            Log.d(TAG, "launch pair here")
            lifecycleScope.launch(Dispatchers.IO) {

                callback?.invoke(port, code)
            }
        }

        builder.show()
    }

}