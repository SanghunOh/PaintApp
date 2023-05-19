package com.example.paintapp.API.response

import com.google.gson.annotations.SerializedName


data class Message (
    val role: String = "user",
    val content: String
)

data class Choice(
    val index: Int,
    val message: Message,
    val finish_reason: String
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

data class ModelResponse(
    val id: String,
    @SerializedName("object")
    val _object: String,
    val created: Int,
    val choices: List<Choice>,
    val usage: Usage
)