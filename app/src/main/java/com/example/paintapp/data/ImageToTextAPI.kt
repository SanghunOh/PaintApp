package com.example.paintapp.data

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ImageToTextAPI {

    companion object {
        fun imageToText(file: File): String? {
            // OkHttp 클라이언트 생성
            val client = OkHttpClient()

            // 사진 파일을 요청 바디로 변환
            val requestFile = file.asRequestBody("image/*".toMediaType())

            // Multipart 요청 생성
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("photo", file.name, requestFile)
                .build()

            // OkHttp 요청 생성
            val request = Request.Builder()
                .url("http://your-api-url/upload")
                .post(requestBody)
                .build()

            // 요청 보내기
            val response = client.newCall(request).execute()

            // 응답 처리
            if (response.isSuccessful) {
                // 업로드 성공
                return response.body?.string()

                // 응답 데이터 처리
            } else {
                // 업로드 실패
                return "FAILED"
            }
        }
    }
}