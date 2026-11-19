package com.dragondojo.app.domain

object TechniqueSearch {
    fun filter(techniques: List<Technique>, query: String): List<Technique> {
        val normalized = query.trim().lowercase()
        if (normalized.isBlank()) return techniques
        return techniques.filter {
            listOf(it.name, it.japanese, it.description, it.category, it.belt.label)
                .any { value -> value.lowercase().contains(normalized) }
        }
    }
}

object TrainingActions {
    fun toggleBooking(state: DojoState, sessionId: String): DojoState {
        if (sessionId in state.attendedSessionIds) return state
        if (sessionId in state.bookedSessionIds) {
            return state.copy(bookedSessionIds = state.bookedSessionIds - sessionId)
        }
        val session = state.sessions.firstOrNull { it.id == sessionId } ?: return state
        if (session.spacesLeft <= 0) return state
        return state.copy(bookedSessionIds = state.bookedSessionIds + sessionId)
    }

    fun markAttendance(state: DojoState, sessionId: String): DojoState {
        if (sessionId !in state.bookedSessionIds || sessionId in state.attendedSessionIds) return state
        return state.copy(
            attendedSessionIds = state.attendedSessionIds + sessionId,
            progress = state.progress.copy(
                sessionsCompleted = (state.progress.sessionsCompleted + 1)
                    .coerceAtMost(state.progress.sessionsForNextBelt)
            )
        )
    }
}
