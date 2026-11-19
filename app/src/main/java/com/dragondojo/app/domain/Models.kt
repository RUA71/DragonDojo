package com.dragondojo.app.domain

import java.time.DayOfWeek
import java.time.LocalTime

enum class Role { STUDENT, PARENT, INSTRUCTOR, ADMIN }

enum class Belt(val label: String, val color: Long) {
    WHITE("White", 0xFFFFFFFF),
    YELLOW("Yellow", 0xFFFFD54FL),
    ORANGE("Orange", 0xFFFF9800L),
    GREEN("Green", 0xFF4CAF50L),
    BLUE("Blue", 0xFF42A5F5L),
    BROWN("Brown", 0xFF795548L),
    BLACK("Black", 0xFF212121L)
}

data class TrainingSession(
    val id: String,
    val title: String,
    val day: DayOfWeek,
    val time: LocalTime,
    val durationMinutes: Int,
    val instructor: String,
    val capacity: Int,
    val spacesUsed: Int
) {
    val spacesLeft: Int get() = capacity - spacesUsed
}

data class Technique(
    val id: String,
    val name: String,
    val japanese: String,
    val description: String,
    val category: String,
    val belt: Belt
)

data class MemberProgress(
    val belt: Belt,
    val sessionsCompleted: Int,
    val sessionsForNextBelt: Int = 24,
    val skills: Int = 12
) {
    val fraction: Float
        get() = (sessionsCompleted.toFloat() / sessionsForNextBelt).coerceIn(0f, 1f)
    val sessionsRemaining: Int
        get() = (sessionsForNextBelt - sessionsCompleted).coerceAtLeast(0)
}

data class DojoState(
    val role: Role? = null,
    val darkMode: Boolean = false,
    val sessions: List<TrainingSession> = emptyList(),
    val techniques: List<Technique> = emptyList(),
    val bookedSessionIds: Set<String> = emptySet(),
    val attendedSessionIds: Set<String> = emptySet(),
    val progress: MemberProgress = MemberProgress(Belt.GREEN, 18),
    val query: String = ""
)
