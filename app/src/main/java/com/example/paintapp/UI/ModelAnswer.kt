package com.example.paintapp.UI

import android.content.Context
import android.view.ViewGroup
import android.widget.*
import com.example.paintapp.R

class ModelAnswer(context: Context) : LinearLayout(context) {
    var params : ViewGroup.LayoutParams? = null
    private lateinit var closeBtn: ImageView
    private lateinit var minimizeBtn: ImageView
    private lateinit var questionTextView: TextView
    private lateinit var answerTextView: TextView
    var isMinimized: Boolean = false
    init {
        params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        inflate(context, R.layout.model_answer_view, this)
//        closeBtn.setOnClickListener{ closeView() }
//        minimizeBtn.setOnClickListener{ minimizeView() }
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