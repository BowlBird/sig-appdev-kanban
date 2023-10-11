package com.github.bowlbird.kanban

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class AddUiState(val text: String = "")

class AddViewModel(
    private val repository: AppRepository
) : ViewModel() {
    private var _addUiState = MutableStateFlow(AddUiState())
    val addUiState: StateFlow<AddUiState> = _addUiState

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val appRepository = (this[APPLICATION_KEY] as KanbanApplication).appRepository
                AddViewModel(
                    repository = appRepository,
                )
            }
        }
    }


    fun updateAddUiState(state: AddUiState) {
        _addUiState.update { _ ->
            state
        }
    }

    suspend fun addEntry(list: ListType, entry: Entry): Boolean {
        val exists = repository.getList(list).contains(entry)
        return if (!exists) {
            repository.createEntry(list, entry)
            true
        }
        else false
    }
}