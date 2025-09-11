package com.example.simplenote.ui.screens.note

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.simplenote.AppGraph
import com.example.simplenote.data.remote.NoteDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val TAG_NOTE_VM = "NoteVM"

data class NoteUiState(
    val isLoading: Boolean = false,
    val id: Int? = null,
    val title: String = "",
    val content: String = "",
    val lastEditedText: String = "",
    val showDeleteSheet: Boolean = false,
    val error: String? = null
)

class NoteViewModel(
    appContext: Context,
    private val noteId: Int?
) : ViewModel() {

    private val repo = AppGraph.notesRepository

    private val _state = MutableStateFlow(
        NoteUiState(isLoading = noteId != null, id = noteId)
    )
    val state: StateFlow<NoteUiState> = _state

    init {
        if (noteId != null) {
            // 1) اول از کش
            viewModelScope.launch {
                val local: NoteDto? = repo.getLocalById(noteId)
                if (local != null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        title = local.title.orEmpty(),
                        content = local.description.orEmpty(),
                        error = null
                    )
                    updateLastEditedNow()
                } else {
                    // 2) اگر در کش نبود، تلاش برای فچ از سرور و سپس کش
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    val res = repo.fetchAndCacheById(noteId)
                    if (res.isSuccess) {
                        val n = res.getOrThrow()
                        _state.value = _state.value.copy(
                            isLoading = false,
                            title = n.title.orEmpty(),
                            content = n.description.orEmpty(),
                            error = null
                        )
                        updateLastEditedNow()
                    } else {
                        Log.e(TAG_NOTE_VM, "fetchAndCacheById failed", res.exceptionOrNull())
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "این یادداشت در کش نیست و از سرور هم دریافت نشد."
                        )
                    }
                }
            }
        } else {
            updateLastEditedNow()
        }
    }

    private fun updateLastEditedNow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val now = LocalDateTime.now()
            val text = "Last edited on " + now.format(DateTimeFormatter.ofPattern("HH.mm"))
            _state.value = _state.value.copy(lastEditedText = text)
        } else {
            _state.value = _state.value.copy(lastEditedText = "Last edited")
        }
    }

    fun onTitleChange(v: String) {
        _state.value = _state.value.copy(title = v)
    }

    fun onContentChange(v: String) {
        _state.value = _state.value.copy(content = v)
    }

    fun toggleDeleteSheet(show: Boolean) {
        _state.value = _state.value.copy(showDeleteSheet = show)
    }

    fun save(onSaved: () -> Unit) {
        viewModelScope.launch {
            val s = state.value
            try {
                if (s.id == null) {
                    // CREATE → Repository (Local-first + Outbox)
                    val res = repo.create(title = s.title, description = s.content)
                    if (res.isSuccess) {
                        val tempId = res.getOrThrow() // id منفی موقت
                        _state.value = _state.value.copy(id = tempId, error = null)
                        updateLastEditedNow()
                        onSaved()  // برگرد به Home؛ HomeVM خودش refresh می‌کند
                    } else {
                        _state.value = _state.value.copy(error = res.exceptionOrNull()?.message ?: "Create failed")
                    }
                } else {
                    // UPDATE → Repository (Local-first + Outbox)
                    val res = repo.update(id = s.id, title = s.title, description = s.content)
                    if (res.isSuccess) {
                        _state.value = _state.value.copy(error = null)
                        updateLastEditedNow()
                        onSaved()
                    } else {
                        _state.value = _state.value.copy(error = res.exceptionOrNull()?.message ?: "Update failed")
                    }
                }
            } catch (t: Throwable) {
                Log.e(TAG_NOTE_VM, "save exception", t)
                _state.value = _state.value.copy(error = t.message ?: "Error")
            }
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val id = state.value.id ?: return onDeleted()
        viewModelScope.launch {
            try {
                val res = repo.delete(id)
                if (!res.isSuccess) {
                    Log.e(TAG_NOTE_VM, "delete failed: ${res.exceptionOrNull()?.message}")
                }
            } catch (t: Throwable) {
                Log.e(TAG_NOTE_VM, "delete exception", t)
            } finally {
                onDeleted()
            }
        }
    }

    companion object {
        fun factory(appContext: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return NoteViewModel(appContext.applicationContext, null) as T
                }
            }

        fun factory(appContext: Context, noteId: Int?): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return NoteViewModel(appContext.applicationContext, noteId) as T
                }
            }
    }
}
