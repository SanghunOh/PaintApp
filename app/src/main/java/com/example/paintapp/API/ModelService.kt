package com.example.paintapp.API

import com.example.paintapp.API.response.ModelResponse
import com.example.paintapp.API.response.Request
import com.example.paintapp.BuildConfig
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

object RetrofitInstance {
    private const val BASE_URL = "https://api.openai.com"

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
    @POST("/v1/chat/completions/")
    suspend fun query(
        @Body req: Request
    ): Call<ModelResponse>
}