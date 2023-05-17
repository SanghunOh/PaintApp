package com.example.paintapp.Data

import android.app.Application
import android.view.Display.Mode
import androidx.lifecycle.LiveData
import com.example.paintapp.CustomPath

class PaintCanvasRepository(application: Application) {
    private val paintDatabase = PaintDatabase.getDatabase(application)
    private val paintCanvasDao: PaintCanvasDao = paintDatabase.PaintCanvasDao()
    private val canvasPathDao: CanvasPathDao = paintDatabase.CanvasPathDao()
    private val modelAnswerDao: ModelAnswerDao = paintDatabase.ModelAnswerDao()

    fun addFile(paintCanvas : PaintCanvas) : Long {
        var id : Long = 0
        try {
            val thread = Thread {
                id = paintCanvasDao.insert(paintCanvas)
            }
            thread.start()
        } catch (e: Exception) { }
        return id
    }

    fun delete(paintCanvas: PaintCanvas) {
        try {
            val thread = Thread {
                paintCanvasDao.delete(paintCanvas)
            }
            thread.start()
        } catch (e: Exception) { }
    }

    fun getPaths(canvasId: Long) : ArrayList<CanvasPath> {
        lateinit var canvasPathList: List<CanvasPath>
        lateinit var ret : ArrayList<CanvasPath>
        try {
            val thread = Thread {
                canvasPathList = canvasPathDao.getPaths(canvasId)
                ret = ArrayList(canvasPathList)
            }
        } catch (e: Exception) { }
        return ret
    }

    fun addCustomPath(canvasId: Long, customPath: CustomPath) {
        try {
            val thread = Thread {
                canvasPathDao.insertPath(CanvasPath(0, canvasId, customPath))
            }
            thread.start()
        } catch (e: Exception) { }
    }

    fun addModelAnswer(modelAnswer: ModelAnswer) {
        try {
            val thread = Thread {
                modelAnswerDao.insert(modelAnswer)
            }
            thread.start()
        } catch (e: Exception) { }
    }

    fun getModelAnswers(canvasId : Long) : ArrayList<ModelAnswer> {
        lateinit var modelAnswerList : List<ModelAnswer>
        lateinit var ret : ArrayList<ModelAnswer>
        try {
            val thread = Thread {
                modelAnswerList = modelAnswerDao.getModelAnswers(canvasId)
                ret = ArrayList(modelAnswerList)
            }
            thread.start()
        } catch (e: Exception) { }
        return ret
    }
}