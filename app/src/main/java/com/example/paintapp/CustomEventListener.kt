package com.example.paintapp

import android.graphics.PointF

interface CustomEventListener {
    fun onPathAdded(path: CustomPath)
    fun onStrokeSelected(pos: PointF)
}