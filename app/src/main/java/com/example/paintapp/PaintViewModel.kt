package com.example.paintapp

import android.app.Application
import android.graphics.PointF
import androidx.lifecycle.*
import com.example.paintapp.data.CustomPath
import com.example.paintapp.data.ModelAnswer
import com.example.paintapp.data.PaintCanvas
import com.example.paintapp.data.PaintCanvasRepository
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PaintViewModel(application: Application) : AndroidViewModel(application) {
    private val paintCanvasRepository = PaintCanvasRepository(application)

    private val _pathList = MutableLiveData<ArrayList<CustomPath>>()
    val pathList: LiveData<ArrayList<CustomPath>>
        get() = _pathList

    private val _modelAnswer = MutableLiveData<String>()
    val modelAnswer: LiveData<String>
        get() = _modelAnswer

    private val _gptQuestion = MutableLiveData<String>()
    val gptQuestion: LiveData<String>
        get() = _gptQuestion

    private val _maPos = MutableLiveData<PointF>()
    val maPos: LiveData<PointF>
        get() = _maPos


    init {
        // data 가져오기
    }
    fun addPath(path : CustomPath) {
        _pathList.value?.add(path)
    }

    fun clearPath() {
        _pathList.value?.clear()
    }

    fun addFile(paintCanvas: PaintCanvas) {
        paintCanvasRepository.addFile(paintCanvas)
    }

    fun queryGPT(canvasId: Long, question: String, position: PointF) {
        viewModelScope.launch {
            withContext(Main){
                paintCanvasRepository.queryGPT(canvasId, question, position, _modelAnswer)
                maPos
                gptQuestion
            }
        }
    }
}