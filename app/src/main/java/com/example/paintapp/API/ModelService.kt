package com.example.paintapp.API

import com.example.paintapp.API.response.Message
import com.example.paintapp.API.response.ModelResponse
import com.example.paintapp.BuildConfig
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

object RetrofitInstance {
    private const val BASE_URL = "https://api.openai.com/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api : ModelService by lazy {
        retrofit.create(ModelService::class.java)
    }
}

interface ModelService {

    @Headers("Content-Type: application/json", "Authorization: ${BuildConfig.API_KEY}")
    @POST("/v1/chat/completions")
    fun query(
        @Body model: String,
        @Body messages: List<Message>
    ): ModelResponse
}