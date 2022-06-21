package me.ductran.controllermapper


import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import me.ductran.controllermapper.evdev.ControllerService
import me.ductran.controllermapper.touchEmu.DebugADB
import me.ductran.controllermapper.touchEmu.EmuService
import me.ductran.controllermapper.touchEmu.ShellReadyListener
import java.io.*
import kotlin.reflect.KClass


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

        findViewById<Button>(R.id.startBtn).setOnClickListener {
            Log.d(TAG, "start clicked")
            startEmuService()
//            controller = ControllerService()
//            controller!!.startInputShell("/dev/input/event1")
        }
        findViewById<Button>(R.id.stopBtn).setOnClickListener {
            stopEmuService()
//            controller?.stop()

        }
        findViewById<Button>(R.id.forcePairBtn).setOnClickListener {
            pairingCheck(true)
        }

//        debugShell = shell2()

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

//        command.isEnabled = true
//        progress.visibility = View.INVISIBLE
//        getInput2()

        command.isEnabled = true
        progress.visibility = View.INVISIBLE
        pairingCheck()
    }

//    fun getSu() {
//        appAdb = AppADB()
//        val shell = appAdb.shell(false, listOf("su"))
//        if (shell == null) {
//            Log.d(TAG, "Cannot get su")
//        } else {
//            PrintStream(shell.outputStream!!).apply {
//                println("exit")
//                flush()
//            }
//        }
//        Log.d(TAG, "done")
//        lifecycleScope.launch(Dispatchers.IO) {
//            goTest3()
//        }
//    }

    fun getInput() {
        Log.d(TAG, "start INput")
        val path = "/dev/input/event8"
        val file = File(path)
        file.setReadOnly()
        val dev = DataInputStream(FileInputStream(file))
        Log.d(TAG, "opened")
        val timeval = ByteArray(24)
        var type: Short
        var code: Short
        var value: Int
        while (true) {
            dev.readFully(timeval)
            type = dev.readShort()
            code = dev.readShort()
            value = dev.readInt()
            Log.d(TAG, "$path $type $code $value")
        }
    }

    @Suppress("DEPRECATION") // Deprecated for third party Services.
    fun isServiceForegrounded() =
        (getSystemService(ACTIVITY_SERVICE) as? ActivityManager)
            ?.getRunningServices(Integer.MAX_VALUE)
            ?.find { it.service.className == ControllerService::class.java.name }
            ?.foreground == true

    fun startEmuService() {
        if (isServiceForegrounded()) return
        Log.d(TAG, "Starting service")
        val i = Intent(this, ControllerService::class.java)
        i.action = "/dev/input/event8"
        startService(i)
    }

    fun stopEmuService() {
        if (!isServiceForegrounded()) return
        Log.d(TAG, "Stopping service")
        val m = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        m.cancel(ControllerService.NOTIFICATION_ID)
        stopService(Intent(this, ControllerService::class.java))
    }

    fun shell2(): Process {
        val command = listOf("sh")
        Log.d(TAG, "shell $command")
        val processBuilder = ProcessBuilder(command)
            .directory(applicationContext.filesDir)
            .apply {
                environment().apply {
                    put("HOME", applicationContext.filesDir.path)
                    put("TMPDIR", applicationContext.cacheDir.path)
                }
            }
        val process = processBuilder.start()
        val outbuf = BufferedReader(InputStreamReader(process.inputStream))
        val errbuf = BufferedReader(InputStreamReader(process.errorStream))

        lifecycleScope.launch(Dispatchers.Default) {
            outMonitor(outbuf, "OUT")
        }
        lifecycleScope.launch(Dispatchers.Default) {
            outMonitor(errbuf, "ERR")
        }
        return process
    }
    fun outMonitor(buf: BufferedReader, pref: String) {
        Log.d(TAG, "Start monitor $pref")
        var line: String? = null
        while (true) {
            Log.d(TAG, "$pref - restarted")
            while (buf.readLine().also { line = it } != null) {
                Log.d(TAG, "$pref: $line")
            }
            Thread.sleep(200)
        }
    }
    fun sendToShellProcess(shell: Process?, msg: String) {
        Log.d(FilelogADB.TAG, "Running : $msg")
        if (shell == null || shell?.outputStream == null) {
            Log.d(FilelogADB.TAG, "ERROR: No shell created yet")
            return
        }
        PrintStream(shell!!.outputStream!!).apply {
            println(msg)
            flush()
        }

    }

    override fun onShellReady(ready: Boolean) {
        Log.d(TAG, "READYYYYYYYYY $ready")
        runOnUiThread {
            command.isEnabled = ready
            progress.visibility = if (ready) View.INVISIBLE else View.VISIBLE
        }
    }

    fun pairingCheck(force: Boolean=false) {
        if (force || ControllerService.shouldWePair()) {
            askToPair {port, code ->
                ControllerService.pair(port, code)
                Log.d(TAG, "askToPair done")
            }
        }
    }

//    fun goTest3() {
//        val ctrl = Controller("/dev/input/event1")
//        ctrl.start()
//    }

//    fun goTest1() {
//        Log.d(TAG, "Start test")
//        val emu = TouchManager.getInst("/dev/input/event1", appAdb)
//        val t1 = emu.newTouch()
//
//        t1.start(300,400)
//        repeat(10) {
//            t1.rmove(dx=50)
//            Thread.sleep(500)
//        }
////        repeat(20) {
////            t1.rmove(dy=20)
////            Thread.sleep(200)
////        }
//        t1.end()
//
//    }
//    fun goTest2() {
//        Log.d(TAG, "Start test2")
//        val emu = TouchManager.getInst("/dev/input/event1", appAdb)
//        val t2 = emu.newTouch()
//
//        Thread.sleep(500)
//        t2.start(800,1300)
//        repeat(10) {
//            t2.rmove(dy=-50)
//            Thread.sleep(300)
//        }
////        repeat(20) {
////            t2.rmove(dx=-20)
////            Thread.sleep(100)
////        }
//        t2.end()
//
//    }


//        /* Check if we need to pair with the device on Android 11 */
//        Log.d(TAG, "Starting here")
//        with(PreferenceManager.getDefaultSharedPreferences(this)) {
//            if (viewModel.shouldWePair(this)) {
//                pairingLatch = CountDownLatch(1)
//                viewModel.adb.debug("Requesting pairing information")
//                askToPair {
//                    with(edit()) {
//                        putBoolean(getString(R.string.paired_key), true)
//                        apply()
//                    }
//                    pairingLatch.countDown()
//                }
//            }
//        }
//
//        viewModel.abiUnsupportedDialog(badAbiDialog)
//        viewModel.piracyCheck(this)
//    }


    /**
     * Ask the user to pair
     */

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
//        builder.setNegativeButton(
//            "Cancel"
//        ) { dialog, which -> dialog.cancel() }

        builder.show()
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        Log.d(TAG, "Not implemented yet")
//        return when (item.itemId) {
//            R.id.bookmarks -> {
//                val intent = Intent(this, BookmarksActivity::class.java)
//                    .putExtra(Intent.EXTRA_TEXT, command.text.toString())
//                bookmarkGetResult.launch(intent)
//                true
//            }
//            R.id.last_command -> {
//                command.setText(lastCommand)
//                command.setSelection(lastCommand.length)
//                true
//            }
//            R.id.help -> {
//                val intent = Intent(this, HelpActivity::class.java)
//                startActivity(intent)
//                true
//            }
//            R.id.share -> {
//                try {
//                    val uri = FileProvider.getUriForFile(
//                        this,
//                        BuildConfig.APPLICATION_ID + ".provider",
//                        viewModel.adb.outputBufferFile
//                    )
//                    val intent = Intent(Intent.ACTION_SEND)
//                    with(intent) {
//                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                        putExtra(Intent.EXTRA_STREAM, uri)
//                        type = "file/*"
//                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    }
//                    startActivity(intent)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    Snackbar.make(output, getString(R.string.snackbar_intent_failed), Snackbar.LENGTH_SHORT)
//                        .setAction(getString(R.string.dismiss)) {}
//                        .show()
//                }
//                true
//            }
//            R.id.clear -> {
//                viewModel.clearOutputText()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.main, menu)
//        return true
//    }
}