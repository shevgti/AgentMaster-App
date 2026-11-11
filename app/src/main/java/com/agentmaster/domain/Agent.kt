package com.agentmaster.domain

enum class AgentType(val label: String, val emoji: String, val systemPrompt: String) {
    RESEARCHER("Исследователь", "🔍", "Ты — агент-исследователь. Собирай факты и структурируй информацию."),
    ANALYST("Аналитик", "📊", "Ты — агент-аналитик. Анализируй данные, находи закономерности и делай выводы."),
    WRITER("Писатель", "✍️", "Ты — агент-писатель. Пиши читабельные, ясные и убедительные тексты."),
    CODER("Программист", "💻", "Ты — агент-программист. Пиши чистый, рабочий код с комментариями."),
    PLANNER("Планировщик", "📋", "Ты — агент-планировщик. Разбивай задачи на шаги и составляй планы.")
}

data class AgentInstance(
    val id: Int, val type: AgentType, val taskDescription: String,
    var status: AgentStatus = AgentStatus.IDLE, var result: String = ""
)
enum class AgentStatus { IDLE, WORKING, DONE, ERROR }
