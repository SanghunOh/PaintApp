package com.example.paintapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PaintCanvasDao {
    @Query("SELECT * from paint_canvas")
    fun getAll() : LiveData<PaintCanvas>

    @Query("SELECT * from paint_canvas WHERE file_name = :fileName")
    fun getCanvas(fileName: String) : PaintCanvas

    @Insert()
    fun insert(p : PaintCanvas) : Long

    @Delete()
    fun delete(p : PaintCanvas)
}

@Dao
interface CanvasPathDao {
    @Query("SELECT * FROM canvas_path WHERE canvas_id = :canvasId")
    fun getPaths(canvasId: Long): List<CanvasPath>

    @Query("DELETE FROM canvas_path WHERE canvas_id = :canvasId")
    fun deletePaths(canvasId: Int)

    @Insert()
    fun insertPath(canvasPath: CanvasPath) : Long
}

@Dao
interface ModelAnswerDao {
    @Query("SELECT * FROM model_answer WHERE canvas_id = :canvasId")
    fun getModelAnswers(canvasId: Long) : List<ModelAnswer>

    @Insert()
    fun insert(modelAnswer: ModelAnswer)
}