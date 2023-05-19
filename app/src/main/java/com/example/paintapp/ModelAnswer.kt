package com.example.paintapp

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView

class ModelAnswer(context: Context, attributeSet: AttributeSet) : LinearLayout(context, attributeSet) {
    var params : ViewGroup.LayoutParams? = null
    private var closeBtn = findViewById<ImageView>(R.id.close)
    private var minimizeBtn = findViewById<ImageView>(R.id.minimize)
    private var isMinimized: Boolean = false
    init {
        params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        closeBtn.setOnClickListener{ closeView() }
        minimizeBtn.setOnClickListener{ minimizeView() }
    }

    private fun closeView() {
        //이벤트 호출

    }

    private fun minimizeView() {
        var scrollView = findViewById<ScrollView>(R.id.answer_field)
        if (isMinimized) {
            scrollView.visibility = VISIBLE
        }
        else {
            scrollView.visibility = GONE
        }
    }
}