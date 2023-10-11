package com.github.bowlbird.kanban

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking


data class KanbanUiState(
    val todoEntries: List<Entry> = listOf(),
    val doingEntries: List<Entry> = listOf(),
    val doneEntries: List<Entry> = listOf()
)

class KanbanViewModel(
    private val repository: AppRepository
) : ViewModel() {
    private var _kanbanUiState: MutableStateFlow<KanbanUiState> = runBlocking {
        MutableStateFlow(
            KanbanUiState(
                todoEntries = repository.getList(ListType.Todo),
                doingEntries = repository.getList(ListType.Doing),
                doneEntries = repository.getList(ListType.Done)
            )
        )
    }

    val kanbanUiState: StateFlow<KanbanUiState> = _kanbanUiState

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val appRepository = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as KanbanApplication).appRepository
                KanbanViewModel(
                    repository = appRepository,
                )
            }
        }
    }

    suspend fun deleteEntry(list: ListType, entry: Entry) {
        repository.deleteEntry(list, entry)
        updateKanbanUiState(
            when(list) {
                ListType.Todo -> kanbanUiState.value.copy(todoEntries = kanbanUiState.value.todoEntries.filter {it != entry})
                ListType.Doing -> kanbanUiState.value.copy(doingEntries = kanbanUiState.value.doingEntries.filter {it != entry})
                ListType.Done -> kanbanUiState.value.copy(doneEntries = kanbanUiState.value.doneEntries.filter {it != entry})
            }
        )
    }

    suspend fun addEntry(list: ListType, entry: Entry): Boolean {
        val exists = repository.getList(list).contains(entry)
        return if (!exists) {
            repository.createEntry(list, entry)
            updateKanbanUiState(
                when(list) {
                    ListType.Todo -> kanbanUiState.value.copy(todoEntries = kanbanUiState.value.todoEntries + entry)
                    ListType.Doing -> kanbanUiState.value.copy(doingEntries = kanbanUiState.value.doingEntries + entry)
                    ListType.Done -> kanbanUiState.value.copy(doneEntries = kanbanUiState.value.doneEntries + entry)
                }
            )
            true
        }
        else false
    }

    private fun updateKanbanUiState(state: KanbanUiState) {
        _kanbanUiState.update { _ ->
            state
        }
    }
}