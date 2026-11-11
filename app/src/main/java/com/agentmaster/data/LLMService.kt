package com.agentmaster.data
import com.google.gson.annotations.SerializedName
import retrofit2.http.*

interface LLMService {
    @POST("chat/completions")
    suspend fun chatCompletion(@Header("Authorization") auth: String, @Body request: ChatRequest): ChatResponse
}
data class ChatRequest(val model: String, val messages: List<ChatMessage>, val temperature: Float = 0.7f, @SerializedName("max_tokens") val maxTokens: Int = 1024)
data class ChatMessage(val role: String, val content: String)
data class ChatResponse(val choices: List<Choice>?)
data class Choice(val message: ChatMessage?)
