package me.ductran.controllermapper

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SetupActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SETUP"

        private const val ACT_SELECT_IMG = 1
    }

    lateinit var imgView: ImageView
    lateinit var zoomImgView: ImageView
    var selX = 1877f
    var selY = 619f

    val getBgrImg = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        Log.d(TAG, "Content result $uri")
//        imgView.setImageURI(uri)
        if (uri!=null) {
//            GlobalStorage.getInst().setupBrgUri = uri
//            setBackground()
            imgView.setImageURI(uri)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        imgView = findViewById(R.id.setupBgrImg)
        zoomImgView = findViewById(R.id.setupZoomImage)

        imgView.setOnTouchListener(OnTouchListener { _, ev ->
//            Log.d(TAG, "Touch ${ev.action} ${ev.x} ${ev.y}")
            selX = ev.x
            selY = ev.y
            if (ev.action == MotionEvent.ACTION_UP) {
                Log.d(TAG, "Up ${ev.x} ${ev.y}")
            }
            true
        })

        hideSystemBars()

        setBackground()

        findViewById<Button>(R.id.setupStartBtn).setOnClickListener {
            Log.d(TAG, "syaty clicked")
            getBgrImg.launch("image/*")
        }
        findViewById<Button>(R.id.setupScreenshotBtn).setOnClickListener {
            Log.d(TAG, "ss clicked")
            ScreenMonitorService.getInst()?.captureScreen()
        }

        startUpdatingZoom()

    }

    fun setBackground() {
//        val bgrImg = GlobalStorage.getString(R.string.setup_bgr_img, "")
//        val uri = GlobalStorage.getInst().setupBrgUri
//        Log.d(TAG, "image ${uri}")
//        if (uri==null) {
            imgView.setImageResource(R.drawable.default_setup_background)
//        } else {
//            imgView.setImageURI(uri)
//        }

//        val m = Matrix()
//        m.postTranslate(155f,515f)
////        m.postScale(3f,3f)
//        val zoomView = findViewById<ImageView>(R.id.setupZoomImage)
//        zoomView.scaleType = ImageView.ScaleType.MATRIX
//        zoomView.imageMatrix = m
//        zoomView.setImageDrawable(imgView.drawable)
//        drawZoomImg()
    }

    fun startUpdatingZoom() = lifecycleScope.launch(Dispatchers.IO) {
        Log.d(TAG, "zoom update start here")
        while(true) {
//            Log.d(TAG, "zoom at $selX $selY")
            runOnUiThread {
                val org = Bitmap.createBitmap(
                    imgView.drawable.toBitmap(),
                    selX.toInt(), selY.toInt(), 10, 4
                )
                zoomImgView.setImageBitmap(org)
            }
            delay(500)
        }
    }

    fun drawZoomImg() {
        val org = Bitmap.createBitmap(imgView.drawable.toBitmap(),
            130, 430, 200, 200)
        findViewById<ImageView>(R.id.setupZoomImage).setImageBitmap(org)

//        val scaleBitmap = Bitmap.createBitmap(
//            (org.width * 4),
//            (org.height * 4),
//            org.config
//        )
//
//        val scaleCanvas = Canvas(scaleBitmap)
//        val scaleMatrix = Matrix()
//        scaleMatrix.setScale(3f, 3f)
//
//        val paint = Paint()
//        scaleCanvas.drawBitmap(org, scaleMatrix, paint)
//        findViewById<ImageView>(R.id.setupZoomImage).setImageBitmap(scaleBitmap)
    }


    fun hideSystemBars() {
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}