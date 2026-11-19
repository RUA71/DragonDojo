package com.dragondojo.app

import com.dragondojo.app.domain.Belt
import com.dragondojo.app.domain.DojoState
import com.dragondojo.app.domain.MemberProgress
import com.dragondojo.app.domain.Technique
import com.dragondojo.app.domain.TechniqueSearch
import com.dragondojo.app.domain.TrainingActions
import com.dragondojo.app.domain.TrainingSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class DomainUseCasesTest {
    private val session = TrainingSession("one", "Basics", DayOfWeek.MONDAY, LocalTime.of(18, 0), 60, "Maya", 12, 11)
    private val fullSession = session.copy(id = "full", capacity = 1, spacesUsed = 1)

    @Test
    fun `search matches technique names japanese and category case insensitively`() {
        val techniques = listOf(
            Technique("1", "Lunge punch", "Oi-zuki", "Advancing strike", "Punches", Belt.WHITE),
            Technique("2", "Front kick", "Mae-geri", "Fast kick", "Kicks", Belt.YELLOW)
        )
        assertEquals(listOf(techniques[0]), TechniqueSearch.filter(techniques, "OI-ZUKI"))
        assertEquals(listOf(techniques[1]), TechniqueSearch.filter(techniques, "kicks"))
    }

    @Test
    fun `booking is reversible and cannot overbook`() {
        val state = DojoState(sessions = listOf(session, fullSession))
        val booked = TrainingActions.toggleBooking(state, session.id)
        assertEquals(setOf(session.id), booked.bookedSessionIds)
        assertEquals(emptySet<String>(), TrainingActions.toggleBooking(booked, session.id).bookedSessionIds)
        assertSame(state, TrainingActions.toggleBooking(state, fullSession.id))
    }

    @Test
    fun `attended session cannot have its booking cancelled`() {
        val attended = DojoState(
            sessions = listOf(session),
            bookedSessionIds = setOf(session.id),
            attendedSessionIds = setOf(session.id)
        )
        assertSame(attended, TrainingActions.toggleBooking(attended, session.id))
    }

    @Test
    fun `attendance requires booking and advances progress only once`() {
        val state = DojoState(progress = MemberProgress(Belt.GREEN, 23), sessions = listOf(session))
        assertSame(state, TrainingActions.markAttendance(state, session.id))
        val attended = TrainingActions.markAttendance(
            state.copy(bookedSessionIds = setOf(session.id)), session.id
        )
        assertEquals(24, attended.progress.sessionsCompleted)
        assertTrue(session.id in attended.attendedSessionIds)
        assertSame(attended, TrainingActions.markAttendance(attended, session.id))
    }

    @Test
    fun `belt progress is bounded`() {
        val progress = MemberProgress(Belt.GREEN, 30)
        assertEquals(1f, progress.fraction)
        assertEquals(0, progress.sessionsRemaining)
    }
}
