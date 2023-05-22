package com.example.paintapp.data

import android.app.Application
import android.graphics.PointF
import com.example.paintapp.API.RetrofitInstance
import com.example.paintapp.API.response.Message
import com.example.paintapp.CustomPath
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException


class PaintCanvasRepository(application: Application) {
    private val app = application
    private val paintDatabase = PaintDatabase.getDatabase(application)
    private val paintCanvasDao: PaintCanvasDao = paintDatabase.PaintCanvasDao()
    private val canvasPathDao: CanvasPathDao = paintDatabase.CanvasPathDao()
    private val modelAnswerDao: ModelAnswerDao = paintDatabase.ModelAnswerDao()

    private val answerLiveData: MutableLiveData<String> = MutableLiveData<String>()

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
                canvasPathDao.insertPath(CanvasPath(canvasId, customPath))
            }
            thread.start()
        } catch (e: Exception) { }
    }

    private fun addModelAnswer(modelAnswer: ModelAnswer) {
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

    fun queryGPT(canvasId: Long, question: String, position: PointF, modelAnswer: MutableLiveData<String>) {
        var gptAnswer: String = ""

        val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
        val okHttpClient = OkHttpClient()
        var json = "{" +
                "  \"model\": \"gpt-3.5-turbo\"," +
                "  \"messages\": [{\"role\": \"user\"," +
                "  \"content\": \"Hello!!\"}]" +
                "}"

        val gptAnswer =  RetrofitInstance.api
            .query("gpt-3.5-turbo", listOf(Message("user", question)))
            .choices[0]
            .message
            .content

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val json_obj = JSONObject(response.body?.string())
                Log.d("gpt", json_obj.toString())
                val json_array = json_obj.optJSONArray("choices")

        return gptAnswer
    }
}