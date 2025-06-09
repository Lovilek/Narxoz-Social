package com.narxoz.social.ui.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.narxoz.social.repository.EventsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/* ───────── модели (MVP) ───────── */
data class EventUi(
    val id: Int,
    val title: String,
    val startsAt: LocalDateTime,
    val description: String?
)

/* ───────── ViewModel-заглушка ───────── */
class EventsViewModel(
    private val repo: EventsRepository = EventsRepository()
) : ViewModel() {
    private val _items = MutableStateFlow<List<EventUi>>(emptyList())
    val items: StateFlow<List<EventUi>> = _items

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        repo.load().onSuccess { events ->
            _items.value = events.map { dto ->
                EventUi(
                    id = dto.id,
                    title = dto.title,
                    startsAt = runCatching { LocalDateTime.parse(dto.startsAt) }.getOrElse { LocalDateTime.now() },
                    description = dto.description
                )
            }
        }
    }
}

/* ───────── UI ───────── */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(vm: EventsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val list by vm.items.collectAsState()

    Scaffold(
        topBar = { SmallTopAppBar(title = { Text("Events") }) }
    ) { inner ->
        if (list.isEmpty())
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                Text("Нет событий")
            }
        else
            LazyColumn(contentPadding = PaddingValues(12.dp), modifier = Modifier.padding(inner)) {
                items(list) { ev ->
                    Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(ev.title, style = MaterialTheme.typography.titleMedium)
                            Text(ev.startsAt.toString(), style = MaterialTheme.typography.bodySmall)
                            if (!ev.description.isNullOrBlank())
                                Text(ev.description!!, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
    }
}