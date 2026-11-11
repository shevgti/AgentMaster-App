package com.agentmaster.data

import com.agentmaster.domain.AgentInstance
import com.agentmaster.domain.Task
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class AgentRepository(
    private val apiKey: String,
    private val apiUrl: String,
    private val model: String
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val service: LLMService = Retrofit.Builder()
        .baseUrl(apiUrl.trim().trimEnd('/') + "/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(LLMService::class.java)

    suspend fun runAgent(agent: AgentInstance): AgentInstance {
        return try {
            val request = ChatRequest(
                model = model,
                messages = listOf(
                    ChatMessage("system", agent.type.systemPrompt),
                    ChatMessage("user", agent.taskDescription)
                )
            )
            val response = service.chatCompletion("Bearer $apiKey", request)
            val answer = response.choices?.firstOrNull()?.message?.content?.trim()
            if (answer.isNullOrEmpty()) {
                agent.copy(status = "ERROR", result = "Ошибка: сервер вернул пустой ответ.")
            } else {
                agent.copy(status = "DONE", result = answer)
            }
        } catch (e: HttpException) {
            val detail = when (e.code()) {
                401 -> "Неверный API-ключ (HTTP 401). Проверь ключ в настройках."
                403 -> "Сервер запретил доступ (HTTP 403). Проверь права ключа."
                404 -> "Модель или адрес API не найдены (HTTP 404). Проверь URL и модель."
                429 -> "Превышен лимит запросов (HTTP 429). Попробуй позже."
                in 500..599 -> "Ошибка сервера ИИ (HTTP ${e.code()}). Попробуй позже."
                else -> "Ошибка HTTP ${e.code()}."
            }
            agent.copy(status = "ERROR", result = detail)
        } catch (e: IOException) {
            agent.copy(status = "ERROR", result = "Нет соединения с сервером. Проверь интернет и адрес API.")
        } catch (e: Exception) {
            agent.copy(
                status = "ERROR",
                result = "Ошибка агента: ${e.localizedMessage ?: "неизвестная ошибка"}"
            )
        }
    }

    suspend fun runAllAgents(task: Task): Task {
        val results = task.agents.map { agent -> runAgent(agent.copy(status = "WORKING")) }
        return task.copy(agents = results)
    }
}
