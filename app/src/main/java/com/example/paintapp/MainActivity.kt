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
import com.example.paintapp.PaintView.Companion.currentBrush
import com.example.paintapp.PaintView.Companion.displaySelectBox
import com.example.paintapp.PaintView.Companion.eraserMode
import com.example.paintapp.PaintView.Companion.isSelected
import com.example.paintapp.PaintView.Companion.pathList
import com.example.paintapp.PaintView.Companion.selectMode
import com.example.paintapp.PaintView.Companion.selectedStroke

class MainActivity : AppCompatActivity() {
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
    }

    private fun currentColor(color: Int){
        strokePaint.color = color
        path = Path()
    }
}