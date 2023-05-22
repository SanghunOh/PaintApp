package com.example.paintapp

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.*

class ModelAnswer(context: Context) : LinearLayout(context) {
    var params : ViewGroup.LayoutParams? = null
    private lateinit var closeBtn: ImageView
    private lateinit var minimizeBtn: ImageView
    private lateinit var questionTextView: TextView
    private lateinit var answerTextView: TextView
    var isMinimized: Boolean = false
    init {
        inflate(context, R.layout.model_answer_view, this)
    }

    fun inflate() {

    }

    private fun closeView() {

    }

    private fun minimizeView() {
        val scrollView = findViewById<ScrollView>(R.id.answer_field)
        if (isMinimized) {
            scrollView.visibility = VISIBLE
        }
        else {
            scrollView.visibility = GONE
        }
    }
}