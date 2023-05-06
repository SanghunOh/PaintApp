package com.example.paintapp

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.example.paintapp.PaintView.Companion.colorList
import com.example.paintapp.PaintView.Companion.currentBrush
import com.example.paintapp.PaintView.Companion.pathList
import com.google.android.material.navigation.NavigationView
import android.os.Environment
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var toolbar : Toolbar

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
        var paintBrush = Paint()
    }
    //hi poki
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val checkButton = findViewById<Button>(R.id.btnAddPdf)
        checkButton.setOnClickListener {
            checkInternalStorage()
        }

        supportActionBar?.hide()

        val redBtn = findViewById<ImageButton>(R.id.redColor)
        val blueBtn = findViewById<ImageButton>(R.id.blueColor)
        val blackBtn = findViewById<ImageButton>(R.id.blackColor)
        val eraser = findViewById<ImageButton>(R.id.whiteColor)

        redBtn.setOnClickListener {
            Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
            paintBrush.color = Color.RED
            currentColor(paintBrush.color)
        }
        blueBtn.setOnClickListener {
            Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
            paintBrush.color = Color.BLUE
            currentColor(paintBrush.color)
        }
        blackBtn.setOnClickListener {
            Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
            paintBrush.color = Color.BLACK
            currentColor(paintBrush.color)
        }
        eraser.setOnClickListener {
            Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
            pathList.clear()
            colorList.clear()
            path.reset()
        }

        toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

//      drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        navigationView = findViewById<NavigationView>(R.id.navigationView)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            // Handle navigation item click here
            when (menuItem.itemId) {
//                R.id.nav_camera -> {
//                    // Handle the home action
//                    drawerLayout.closeDrawers()
//                    true
//                }
                R.id.nav_photo -> {
                    // Handle the gallery action
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_slideShow -> {
                    // Handle the slideshow action
                    drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }

            true // Return true to indicate that the item was handled
        }
        btnAddPdf = findViewById(R.id.btnAddPdf)
        tvStorageInfo = findViewById(R.id.tvStorageInfo)

        btnAddPdf.setOnClickListener {
            val internalStoragePath = Environment.getExternalStorageDirectory().absolutePath
            tvStorageInfo.text = "Internal Storage: $internalStoragePath"
        }
    }

    private fun currentColor(color: Int){
        currentBrush = color
        path = Path()
    }
}