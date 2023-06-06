package com.example.paintapp.data

import android.app.Application
import android.graphics.PointF
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.paintapp.BuildConfig
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
        val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
        val okHttpClient = OkHttpClient()
        val json = "{" +
                "  \"model\": \"gpt-3.5-turbo\"," +
                "  \"messages\": [{\"role\": \"user\"," +
                "  \"content\": \"$question\"}]" +
                "}"

        val requestBody: RequestBody = json.toRequestBody(mediaType)
        val request: Request =
            Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader(
                    "Authorization",
                    BuildConfig.API_KEY
                )
                .post(requestBody)
                .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val json_obj = JSONObject(response.body?.string())
                val json_array = json_obj.optJSONArray("choices")

                val json_text = json_array!!.getJSONObject(0).getString("message")
                val json_obj2 = JSONObject(json_text)
                val json_text2 = json_obj2.getString("content")

                println(json_text2)
                modelAnswer.postValue(json_text2)
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.i("gpt", "onFailure: ")
            }
        })
    }
}