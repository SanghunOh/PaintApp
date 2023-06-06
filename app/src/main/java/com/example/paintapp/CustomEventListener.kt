package com.example.paintapp

import android.graphics.PointF
import com.example.paintapp.data.CustomPath

interface CustomEventListener {
    fun onPathAdded(path: CustomPath)
    fun onStrokeSelected(pos: PointF)

    fun onStrokeDeselected()
}