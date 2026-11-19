package com.dragondojo.app.data

import android.content.Context
import androidx.lifecycle.ViewModel
import com.dragondojo.app.domain.Belt
import com.dragondojo.app.domain.DojoState
import com.dragondojo.app.domain.MemberProgress
import com.dragondojo.app.domain.Role
import com.dragondojo.app.domain.Technique
import com.dragondojo.app.domain.TrainingActions
import com.dragondojo.app.domain.TrainingSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalTime

class OfflineDojoRepository(context: Context) {
    private val preferences = context.getSharedPreferences("dragon_dojo", Context.MODE_PRIVATE)
    private val mutableState = MutableStateFlow(loadState())
    val state: StateFlow<DojoState> = mutableState.asStateFlow()

    fun selectRole(role: Role?) = update { copy(role = role) }
    fun toggleDarkMode(enabled: Boolean) = update { copy(darkMode = enabled) }
    fun search(query: String) = mutableState.update { it.copy(query = query) }

    fun toggleBooking(sessionId: String): Boolean {
        val before = mutableState.value
        val after = TrainingActions.toggleBooking(before, sessionId)
        if (after === before) return false
        update { after }
        return true
    }

    fun markAttendance(sessionId: String) {
        val after = TrainingActions.markAttendance(mutableState.value, sessionId)
        if (after !== mutableState.value) update { after }
    }

    private inline fun update(transform: DojoState.() -> DojoState) {
        mutableState.update(transform)
        persist(mutableState.value)
    }

    private fun persist(state: DojoState) {
        preferences.edit()
            .putString("role", state.role?.name)
            .putBoolean("dark", state.darkMode)
            .putStringSet("bookings", state.bookedSessionIds)
            .putStringSet("attendance", state.attendedSessionIds)
            .putInt("completed", state.progress.sessionsCompleted)
            .apply()
    }

    private fun loadState() = DojoState(
        role = preferences.getString("role", null)?.let { Role.valueOf(it) },
        darkMode = preferences.getBoolean("dark", false),
        sessions = sessions,
        techniques = techniques,
        bookedSessionIds = preferences.getStringSet("bookings", emptySet()).orEmpty(),
        attendedSessionIds = preferences.getStringSet("attendance", emptySet()).orEmpty(),
        progress = MemberProgress(Belt.GREEN, preferences.getInt("completed", 18))
    )

    companion object {
        val sessions = listOf(
            TrainingSession("mon-beg", "Foundations & Kihon", DayOfWeek.MONDAY, LocalTime.of(17, 30), 60, "Sensei Maya", 16, 11),
            TrainingSession("wed-all", "Kata & Technique", DayOfWeek.WEDNESDAY, LocalTime.of(18, 30), 75, "Sensei Ken", 20, 14),
            TrainingSession("fri-adv", "Sparring Lab", DayOfWeek.FRIDAY, LocalTime.of(19, 0), 60, "Sensei Maya", 12, 9),
            TrainingSession("sat-kids", "Junior Dragons", DayOfWeek.SATURDAY, LocalTime.of(10, 0), 45, "Sensei Ren", 18, 17)
        )
        val techniques = listOf(
            Technique("oi-zuki", "Lunge punch", "Oi-zuki", "A straight punch delivered while advancing with a long stance.", "Punches", Belt.WHITE),
            Technique("age-uke", "Rising block", "Age-uke", "An upward forearm block used against high attacks.", "Blocks", Belt.WHITE),
            Technique("mae-geri", "Front kick", "Mae-geri", "A direct kick with the ball of the foot and a controlled snap.", "Kicks", Belt.YELLOW),
            Technique("heian-shodan", "Peaceful mind, first form", "Heian Shodan", "A foundational kata connecting basic blocks, strikes and turns.", "Kata", Belt.ORANGE),
            Technique("gyaku-zuki", "Reverse punch", "Gyaku-zuki", "A powerful straight punch coordinated with hip rotation.", "Punches", Belt.GREEN),
            Technique("yoko-geri", "Side kick", "Yoko-geri", "A penetrating side kick driven from a chambered knee position.", "Kicks", Belt.BLUE)
        )
    }
}

class DragonDojoViewModel(private val repository: OfflineDojoRepository) : ViewModel() {
    val state = repository.state
    fun selectRole(role: Role?) = repository.selectRole(role)
    fun setDarkMode(enabled: Boolean) = repository.toggleDarkMode(enabled)
    fun search(query: String) = repository.search(query)
    fun toggleBooking(id: String) = repository.toggleBooking(id)
    fun markAttendance(id: String) = repository.markAttendance(id)
}
