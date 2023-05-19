package com.example.paintapp.data

import androidx.room.*
import com.example.paintapp.CustomPath


@Entity(tableName="paint_canvas")
data class PaintCanvas(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "canvas_id")
    val canvasId : Long,

    @ColumnInfo(name = "file_name")
    var fileName : String = "",
) { }

@Entity(tableName = "canvas_path",
    foreignKeys = [
        ForeignKey(
            entity = PaintCanvas::class,
            parentColumns = ["canvas_id"],
            childColumns = ["canvas_id"]
        )
    ])
data class CanvasPath(
    @PrimaryKey(autoGenerate = true)
    val pathId : Long,

    @ColumnInfo(name = "canvas_id")
    val canvasId: Long,

    @Embedded
    var canvasPath : CustomPath? = null,
) { }

@Entity(tableName = "model_answer",
    foreignKeys = [
        ForeignKey(
            entity = PaintCanvas::class,
            parentColumns = ["canvas_id"],
            childColumns = ["canvas_id"]
        )
    ])
data class ModelAnswer(
    @PrimaryKey(autoGenerate = true)
    val answerId : Long,

    @ColumnInfo(name = "canvas_id")
    val canvasId: Long,

    var question: String,
    var answer: String,
    var x: Float,
    var y: Float
) { }