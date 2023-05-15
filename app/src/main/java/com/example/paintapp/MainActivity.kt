package com.example.paintapp

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import com.example.paintapp.PaintView.Companion.currentBrush
import com.example.paintapp.PaintView.Companion.displaySelectBox
import com.example.paintapp.PaintView.Companion.isSelected
import com.example.paintapp.PaintView.Companion.pathList
import com.example.paintapp.PaintView.Companion.selectMode
import com.example.paintapp.PaintView.Companion.selectedStroke
import com.google.android.material.navigation.NavigationView
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import android.net.Uri
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.document.download.DownloadRequest
import com.pspdfkit.ui.PdfActivity
//import kotlin.coroutines.jvm.internal.CompletedContinuation.context

const val PICK_PDF_FILE = 1001
class MainActivity : AppCompatActivity() {
//    private lateinit var toolbar : Toolbar

    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navigationView : NavigationView
    private lateinit var btnAddPdf: Button
    private lateinit var tvStorageInfo: TextView

    fun checkInternalStorage() {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            val root = Environment.getExternalStorageDirectory()
            val totalSpace = root.totalSpace / (1024 * 1024)
            val freeSpace = root.freeSpace / (1024 * 1024)
            val usedSpace = totalSpace - freeSpace
            // Do something with the storage information
        } else {
            // Handle the case where external storage is not available
        }
    }
    companion object{
        // accessible throughout the application
        var path = Path()
        var strokePaint = Paint()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ocrapi.OcrTest("KakaoTalk_Photo_2023-05-06-21-59-55.jpg")
        supportActionBar?.hide()

        val selectBrush = findViewById<ImageView>(R.id.selectBrush)
        val brushBtnGroup = findViewById<LinearLayout>(R.id.brushGroup)
        val redBtn = findViewById<ImageButton>(R.id.redColor)
        val blueBtn = findViewById<ImageButton>(R.id.blueColor)
        val blackBtn = findViewById<ImageButton>(R.id.blackColor)
        val clearBtn = findViewById<ImageView>(R.id.clear)
        val checkButton = findViewById<Button>(R.id.btnAddPdf)

        selectBrush.setOnClickListener {
            displaySelectBox = false
            isSelected = false
            selectedStroke.clear()
            if (selectMode) {
                selectMode = false
                brushBtnGroup.visibility = VISIBLE
            }
            else {
                selectMode = true
                brushBtnGroup.visibility = GONE
            }
        }
        redBtn.setOnClickListener {
            if (!selectMode) {
                Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
                currentBrush = Color.RED
                currentColor(currentBrush)
            }
        }
        blueBtn.setOnClickListener {
            if (!selectMode) {
                Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
                currentBrush = Color.BLUE
                currentColor(currentBrush)
            }
        }
        blackBtn.setOnClickListener {
            if (!selectMode) {
                Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
                currentBrush = Color.BLACK
                currentColor(currentBrush)
            }
        }
        clearBtn.setOnClickListener {
            if (!selectMode) {
                Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
                pathList.clear()
                path.reset()
            }
        }
        checkButton.setOnClickListener {
            checkInternalStorage()
        }

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply{
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }


        btnAddPdf = findViewById(R.id.btnAddPdf)
        btnAddPdf.setOnClickListener {
            startActivityForResult(intent, PICK_PDF_FILE)
//        runOnUiThread{
//                val uri = Uri.parse("file://android_asset/sample.pdf")
//                val config = PdfActivityConfiguration.Builder(this).build()
//                PdfActivity.showDocument(this,uri,config)
//            }
        }
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int,resultData:Intent?){
        super.onActivityResult(requestCode, resultCode, resultData)
        if(requestCode == PICK_PDF_FILE && resultCode== Activity.RESULT_OK){
            resultData?.data?.also{uri->
                val documentUri = Uri.parse(uri.toString())
                val config = PdfActivityConfiguration.Builder(this).build()
                PdfActivity.showDocument(this,documentUri,config)
            }
        }
    }
    private fun currentColor(color: Int){
        strokePaint.color = color
        path = Path()
    }

}
