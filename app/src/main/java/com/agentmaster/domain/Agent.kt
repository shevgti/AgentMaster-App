package com.agentmaster.domain
enum class AgentType(val label: String, val emoji: String, val systemPrompt: String) {
    RESEARCHER("Исследователь", "🔍", "Собирай факты."),
    ANALYST("Аналитик", "📊", "Анализируй данные."),
    WRITER("Писатель", "✍️", "Пиши тексты."),
    CODER("Программист", "💻", "Пиши код."),
    PLANNER("Планировщик", "📋", "Составляй планы.")
}
data class AgentInstance(val id: Int, val type: AgentType, val taskDescription: String, var status: String = "IDLE", var result: String = "")