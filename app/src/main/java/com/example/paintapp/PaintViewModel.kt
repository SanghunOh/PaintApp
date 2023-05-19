package com.example.paintapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.paintapp.data.PaintCanvas
import com.example.paintapp.data.PaintCanvasRepository

class PaintViewModel(application: Application) : AndroidViewModel(application) {
    private val paintCanvasRepository = PaintCanvasRepository(application)

    private val _pathList = MutableLiveData<ArrayList<CustomPath>>()
    val pathList: LiveData<ArrayList<CustomPath>>
        get() = _pathList

    fun addPath(path : CustomPath) {
        _pathList.value?.add(path)
    }

    fun clearPath() {
        _pathList.value?.clear()
    }

    fun addFile(paintCanvas: PaintCanvas) {
        paintCanvasRepository.addFile(paintCanvas)
    }
}