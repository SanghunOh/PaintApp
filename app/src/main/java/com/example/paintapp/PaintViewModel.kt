package com.example.paintapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.paintapp.Data.PaintCanvas
import com.example.paintapp.Data.PaintCanvasRepository

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