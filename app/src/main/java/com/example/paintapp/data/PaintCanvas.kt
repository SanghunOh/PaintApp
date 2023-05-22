package com.example.paintapp.data

import androidx.room.*
import com.example.paintapp.CustomPath


@Entity(tableName="paint_canvas")
data class PaintCanvas(
    @ColumnInfo(name = "file_name")
    var fileName : String = "",
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "canvas_id")
    var canvasId: Long = 0
}

@Entity(tableName = "canvas_path",
    foreignKeys = [
        ForeignKey(
            entity = PaintCanvas::class,
            parentColumns = ["canvas_id"],
            childColumns = ["canvas_id"]
        )
    ])
data class CanvasPath(
    @ColumnInfo(name = "canvas_id")
    val canvasId: Long,

    @Embedded
    var canvasPath : CustomPath? = null,
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "path_id")
    var pathId: Long = 0
}

@Entity(tableName = "model_answer",
    foreignKeys = [
        ForeignKey(
            entity = PaintCanvas::class,
            parentColumns = ["canvas_id"],
            childColumns = ["canvas_id"]
        )
    ])
data class ModelAnswer(
    @ColumnInfo(name = "canvas_id")
    val canvasId: Long,

    var question: String,
    var answer: String,
    var x: Float,
    var y: Float
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "answer_id")
    var answerId: Long = 0
}