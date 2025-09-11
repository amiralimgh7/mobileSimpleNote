package com.example.simplenote.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.simplenote.AppGraph
import com.example.simplenote.data.notes.NotesPage
import com.example.simplenote.data.remote.NoteDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val notes: List<NoteDto> = emptyList(),
    val error: String? = null,
    val page: Int = 1,
    val pageSize: Int = 6,
    val totalPages: Int = 1,
    val count: Int = 0,
    val hasNext: Boolean = false,
    val hasPrev: Boolean = false
)

class HomeViewModel(appContext: Context) : ViewModel() {
    private val repo = AppGraph.notesRepository

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    private var searchJob: Job? = null

    init {
        // بارگیری اولیه
        refresh()
        // هر تغییری در داده‌ها (ساخت/ویرایش/حذف) بیاید → خودکار ریفرش
        viewModelScope.launch {
            repo.events.collect { refresh() }
        }
    }

    fun onQueryChange(v: String) {
        _state.update { it.copy(query = v) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            goToPage(1)
        }
    }

    fun nextPage() = goToPage(_state.value.page + 1)
    fun prevPage() = goToPage(_state.value.page - 1)

    private fun goToPage(p: Int) {
        val s = _state.value
        val target = when {
            p < 1 -> 1
            p > s.totalPages -> s.totalPages
            else -> p
        }
        load(target, s.pageSize, s.query.ifBlank { null })
    }

    fun refresh() {
        val s = _state.value
        load(s.page, s.pageSize, s.query.ifBlank { null })
    }

    private fun load(page: Int, pageSize: Int, query: String?) {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val res = repo.load(page, pageSize, query)
            if (res.isSuccess) {
                val pg: NotesPage = res.getOrThrow()
                _state.update {
                    it.copy(
                        isLoading = false,
                        notes = pg.items,
                        page = pg.page,
                        pageSize = pg.pageSize,
                        totalPages = pg.totalPages,
                        count = pg.count,
                        hasNext = pg.hasNext,
                        hasPrev = pg.hasPrev,
                        error = null
                    )
                }
            } else {
                val err = res.exceptionOrNull()
                _state.update { it.copy(isLoading = false, notes = emptyList(), error = err?.message ?: "Request failed") }
            }
        }
    }

    companion object {
        fun factory(appContext: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return HomeViewModel(appContext.applicationContext) as T
                }
            }
    }
}
