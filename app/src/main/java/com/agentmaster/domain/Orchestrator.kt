package com.agentmaster.domain
class Orchestrator {
    fun planAgents(userTask: String): List<AgentInstance> {
        val agents = mutableListOf<AgentInstance>()
        var id = 1
        val t = userTask.lowercase()
        if ("найди" in t or "узнай" in t) agents.add(AgentInstance(id++, AgentType.RESEARCHER, "Собери информацию: $userTask"))
        if ("анализ" in t or "сравни" in t) agents.add(AgentInstance(id++, AgentType.ANALYST, "Проанализируй: $userTask"))
        if ("напиши" in t or "текст" in t) agents.add(AgentInstance(id++, AgentType.WRITER, "Напиши текст: $userTask"))
        if ("код" in t or "скрипт" in t) agents.add(AgentInstance(id++, AgentType.CODER, "Напиши код: $userTask"))
        if (agents.isEmpty()) agents.add(AgentInstance(id++, AgentType.RESEARCHER, "Разбери задачу: $userTask"))
        return agents
    }
    fun assembleResult(task: Task): String {
        val sb = StringBuilder("📋 Задача: ${task.userPrompt}\n\n")
        for (agent in task.agents) sb.appendLine("${agent.type.emoji} ${agent.type.label}:\n${agent.result.ifEmpty { "(в работе...)" }}\n")
        return sb.toString()
    }
}