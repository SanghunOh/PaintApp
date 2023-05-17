package com.example.paintapp

import android.graphics.Path
import android.graphics.PointF


data class CustomPath(
    val color: Int,
    val path: Path,
    val minX: Float,
    val minY: Float,
    val maxX: Float,
    val maxY: Float,
    val points: List<PointF>) {
}