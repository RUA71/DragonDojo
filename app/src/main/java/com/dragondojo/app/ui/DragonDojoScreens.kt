package com.dragondojo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dragondojo.app.R
import com.dragondojo.app.data.DragonDojoViewModel
import com.dragondojo.app.domain.DojoState
import com.dragondojo.app.domain.Role
import com.dragondojo.app.domain.Technique
import com.dragondojo.app.domain.TechniqueSearch
import com.dragondojo.app.domain.TrainingSession
import java.util.Locale

private enum class Destination { DASHBOARD, TRAINING, LIBRARY, PROFILE }

@Composable
fun DragonDojoApp(state: DojoState, viewModel: DragonDojoViewModel) {
    if (state.role == null) {
        RoleScreen(onSelect = viewModel::selectRole)
        return
    }
    var destination by rememberSaveable { mutableStateOf(Destination.DASHBOARD) }
    val items = listOf(
        Destination.DASHBOARD to (stringResource(R.string.dashboard) to Icons.Default.Home),
        Destination.TRAINING to (stringResource(R.string.training) to Icons.Default.FitnessCenter),
        Destination.LIBRARY to (stringResource(R.string.library) to Icons.Default.Book),
        Destination.PROFILE to (stringResource(R.string.profile) to Icons.Default.Person)
    )
    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { (item, labelIcon) ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = { destination = item },
                        icon = { Icon(labelIcon.second, contentDescription = null) },
                        label = { Text(labelIcon.first) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (destination) {
                Destination.DASHBOARD -> DashboardScreen(state) { destination = Destination.TRAINING }
                Destination.TRAINING -> TrainingScreen(state, viewModel)
                Destination.LIBRARY -> LibraryScreen(state, viewModel)
                Destination.PROFILE -> ProfileScreen(state, viewModel)
            }
        }
    }
}

@Composable
private fun RoleScreen(onSelect: (Role) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.choose_role), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))
        listOf(
            Role.STUDENT to R.string.student,
            Role.PARENT to R.string.parent,
            Role.INSTRUCTOR to R.string.instructor,
            Role.ADMIN to R.string.admin
        ).forEach { (role, label) ->
            Card(Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable { onSelect(role) }) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(Modifier.width(14.dp))
                    Text(stringResource(label), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun DashboardScreen(state: DojoState, onTraining: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text(stringResource(R.string.welcome), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(roleHint(state.role), color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            AssistChip(onClick = {}, label = { Text(stringResource(R.string.offline_ready)) })
        }
        item {
            SectionTitle(R.string.belt_progress)
            Card {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(34.dp).clip(CircleShape).background(Color(state.progress.belt.color)))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(stringResource(R.string.current_belt), style = MaterialTheme.typography.labelLarge)
                            Text("${state.progress.belt.label} · ${state.progress.sessionsCompleted}/${state.progress.sessionsForNextBelt}", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    LinearProgressIndicator(progress = { state.progress.fraction }, modifier = Modifier.fillMaxWidth().height(8.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.remaining, state.progress.sessionsRemaining))
                }
            }
        }
        item { SectionTitle(R.string.next_class) }
        item {
            state.sessions.firstOrNull()?.let { session ->
                SessionCard(session, session.id in state.bookedSessionIds, false, onBook = onTraining, onAttendance = {})
            }
        }
    }
}

@Composable
private fun TrainingScreen(state: DojoState, viewModel: DragonDojoViewModel) {
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text(stringResource(R.string.training), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        items(state.sessions, key = { it.id }) { session ->
            val booked = session.id in state.bookedSessionIds
            SessionCard(session, booked, session.id in state.attendedSessionIds,
                onBook = { viewModel.toggleBooking(session.id) },
                onAttendance = { viewModel.markAttendance(session.id) })
        }
    }
}

@Composable
private fun SessionCard(
    session: TrainingSession,
    booked: Boolean,
    attended: Boolean,
    onBook: () -> Unit,
    onAttendance: () -> Unit
) {
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(session.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("${session.day.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }} · ${session.time}")
            Text("${session.durationMinutes} min · ${session.instructor} · ${session.spacesLeft} spots", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (booked) {
                    Button(onClick = onBook) { Text(stringResource(R.string.cancel_booking)) }
                    Spacer(Modifier.width(8.dp))
                    if (attended) {
                        AssistChip(onClick = {}, leadingIcon = { Icon(Icons.Default.CheckCircle, null) }, label = { Text(stringResource(R.string.checked_in)) })
                    } else {
                        OutlinedButton(onClick = onAttendance) { Text(stringResource(R.string.check_in)) }
                    }
                } else {
                    Button(onClick = onBook, enabled = session.spacesLeft > 0) { Text(stringResource(R.string.book)) }
                }
            }
        }
    }
}

@Composable
private fun LibraryScreen(state: DojoState, viewModel: DragonDojoViewModel) {
    val techniques = TechniqueSearch.filter(state.techniques, state.query)
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(stringResource(R.string.library), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::search,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(R.string.search_techniques)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
        )
        Spacer(Modifier.height(12.dp))
        if (techniques.isEmpty()) {
            Text(stringResource(R.string.no_results), Modifier.padding(8.dp))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(techniques, key = Technique::id) { technique ->
                    Card {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(technique.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                FilterChip(selected = true, onClick = {}, label = { Text(technique.belt.label) })
                            }
                            Text(technique.japanese, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(5.dp))
                            Text(technique.description)
                            Spacer(Modifier.height(5.dp))
                            Text(technique.category, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(state: DojoState, viewModel: DragonDojoViewModel) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.profile), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Card {
            Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, Modifier.size(52.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Alex Morgan", style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(roleLabel(state.role ?: Role.STUDENT)))
                }
            }
        }
        Card {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.dark_mode))
                    Switch(checked = state.darkMode, onCheckedChange = viewModel::setDarkMode)
                }
                Text(stringResource(R.string.language_note), style = MaterialTheme.typography.bodySmall)
            }
        }
        TextButton(onClick = { viewModel.selectRole(null) }) { Text(stringResource(R.string.change_role)) }
        Text(stringResource(R.string.offline_ready), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun SectionTitle(text: Int) = Text(stringResource(text), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

private fun roleLabel(role: Role) = when (role) {
    Role.STUDENT -> R.string.student
    Role.PARENT -> R.string.parent
    Role.INSTRUCTOR -> R.string.instructor
    Role.ADMIN -> R.string.admin
}

@Composable
private fun roleHint(role: Role?) = stringResource(when (role) {
    Role.PARENT -> R.string.parent_hint
    Role.INSTRUCTOR -> R.string.instructor_hint
    Role.ADMIN -> R.string.admin_hint
    else -> R.string.offline_ready
})
