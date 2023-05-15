package com.example.paintapp

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

//        toolbar = findViewById<Toolbar>(R.id.toolbar)
//        setSupportActionBar(toolbar)

//      drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

//        navigationView = findViewById<NavigationView>(R.id.navigationView)

//        navigationView.setNavigationItemSelectedListener { menuItem ->
//            // Handle navigation item click here
//            when (menuItem.itemId) {
//                R.id.nav_photo -> {
//                    // Handle the gallery action
//                    drawerLayout.closeDrawers()
//                    true
//                }
//                R.id.nav_slideShow -> {
//                    // Handle the slideshow action
//                    drawerLayout.closeDrawers()
//                    true
//                }
//                else -> false
//            }
//
//            true // Return true to indicate that the item was handled
//        }
//        btnAddPdf = findViewById(R.id.btnAddPdf)
//        tvStorageInfo = findViewById(R.id.tvStorageInfo)
//
//        btnAddPdf.setOnClickListener {
//            val internalStoragePath = Environment.getExternalStorageDirectory().absolutePath
//            tvStorageInfo.text = "Internal Storage: $internalStoragePath"
//        }
    }

    private fun currentColor(color: Int){
        strokePaint.color = color
        path = Path()
    }
}