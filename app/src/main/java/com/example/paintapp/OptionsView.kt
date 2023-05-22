package com.example.paintapp

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout

class OptionsView(context: Context) : View(context) {
    var params : ViewGroup.LayoutParams? = null
    private var gptBtn = findViewById<ImageButton>(R.id.request_gpt)

    init {

    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        gptBtn?.setOnClickListener{ gptRequest() }
    }

    private fun gptRequest() {
        // change to image file
        // http request
    }
}