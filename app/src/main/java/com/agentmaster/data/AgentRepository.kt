package com.agentmaster.data
import com.agentmaster.domain.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AgentRepository(private val apiKey: String, private val apiUrl: String, private val model: String) {
    private val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }).build()
    private val service: LLMService = Retrofit.Builder().baseUrl(apiUrl.trimEnd('/') + "/").client(client)
        .addConverterFactory(GsonConverterFactory.create()).build().create(LLMService::class.java)

    suspend fun runAgent(agent: AgentInstance): AgentInstance {
        return try {
            val req = ChatRequest(model = model, messages = listOf(ChatMessage("system", agent.type.systemPrompt), ChatMessage("user", agent.taskDescription)))
            val resp = service.chatCompletion("Bearer $apiKey", req)
            agent.copy(status = AgentStatus.DONE, result = resp.choices?.firstOrNull()?.message?.content ?: "(пустой ответ)")
        } catch (e: Exception) {
            agent.copy(status = AgentStatus.ERROR, result = "Ошибка: ${e.localizedMessage}")
        }
    }
    suspend fun runAllAgents(task: Task): Task {
        return task.copy(agents = task.agents.map { runAgent(it.copy(status = AgentStatus.WORKING)) })
    }
}
