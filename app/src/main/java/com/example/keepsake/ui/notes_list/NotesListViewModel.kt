package com.example.keepsake.ui.notes_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.keepsake.data.local.ChecklistItem
import com.example.keepsake.data.local.NoteEntity
import com.example.keepsake.data.repository.NoteRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NoteUiModel(
    val id: Int,
    val title: String,
    val content: String,
    val color: Long,
    val isPinned: Boolean,
    val isListMode: Boolean,
    val firstMedia: String?,
    val checklistPreview: List<String>,
    val hasAudio: Boolean,
    val originalEntity: NoteEntity
)

class NotesListViewModel(private val repository: NoteRepository) : ViewModel() {

    private val gson = Gson()
    private val stringListType = object : TypeToken<List<String>>() {}.type
    private val checklistType = object : TypeToken<List<ChecklistItem>>() {}.type

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedNoteIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedNoteIds = _selectedNoteIds.asStateFlow()
    private fun mapToUiModel(note: NoteEntity): NoteUiModel {
        val images: List<String> = try {
            if (note.imagePaths.isEmpty()) emptyList() else gson.fromJson(
                note.imagePaths,
                stringListType
            )
        } catch (e: Exception) {
            emptyList()
        }

        val drawings: List<String> = try {
            if (note.drawingPaths.isEmpty()) emptyList() else gson.fromJson(
                note.drawingPaths,
                stringListType
            )
        } catch (e: Exception) {
            emptyList()
        }

        val checklist: List<ChecklistItem> = try {
            if (note.isListMode && note.checklistData.isNotEmpty()) gson.fromJson(
                note.checklistData,
                checklistType
            ) else emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        return NoteUiModel(
            id = note.id,
            title = note.title,
            content = note.content,
            color = note.color,
            isPinned = note.isPinned,
            isListMode = note.isListMode,
            firstMedia = images.firstOrNull() ?: drawings.firstOrNull(),
            checklistPreview = checklist.filter { !it.isChecked }.take(3).map { it.text },
            hasAudio = note.audioPath != null,
            originalEntity = note
        )
    }

    val pinnedNotes: StateFlow<List<NoteUiModel>> = combine(
        repository.getAllNotes(),
        _searchQuery
    ) { notesList, query ->
        notesList.filter { note ->
            note.isPinned && !note.isArchived && !note.isTrashed &&
                    (note.title.contains(query, ignoreCase = true) ||
                            note.content.contains(query, ignoreCase = true))
        }.map { mapToUiModel(it) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val otherNotes: StateFlow<List<NoteUiModel>> = combine(
        repository.getAllNotes(),
        _searchQuery
    ) { notesList, query ->
        notesList.filter { note ->
            !note.isPinned && !note.isArchived && !note.isTrashed &&
                    (note.title.contains(query, ignoreCase = true) ||
                            note.content.contains(query, ignoreCase = true))
        }.map { mapToUiModel(it) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val archivedNotes: StateFlow<List<NoteUiModel>> = repository.getAllNotes()
        .map { notesList ->
            notesList.filter { it.isArchived && !it.isTrashed }.map { mapToUiModel(it) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val trashedNotes: StateFlow<List<NoteUiModel>> = repository.getAllNotes()
        .map { notesList ->
            notesList.filter { it.isTrashed }.map { mapToUiModel(it) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleSelection(noteId: Int) {
        val current = _selectedNoteIds.value
        if (current.contains(noteId)) {
            _selectedNoteIds.value = current - noteId
        } else {
            _selectedNoteIds.value = current + noteId
        }
    }

    fun clearSelection() {
        _selectedNoteIds.value = emptySet()
    }

    fun deleteSelectedNotes() {
        viewModelScope.launch {
            val selectedIds = _selectedNoteIds.value.toList()
            if (selectedIds.isNotEmpty()) repository.trashNotes(selectedIds)
            clearSelection()
        }
    }

    fun archiveSelectedNotes() {
        viewModelScope.launch {
            val selectedIds = _selectedNoteIds.value.toList()
            if (selectedIds.isNotEmpty()) repository.archiveNotes(selectedIds, true)
            clearSelection()
        }
    }

    fun pinSelectedNotes(pin: Boolean) {
        viewModelScope.launch {
            val selectedIds = _selectedNoteIds.value.toList()
            if (selectedIds.isNotEmpty()) repository.pinNotes(selectedIds, pin)
            clearSelection()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun moveToTrash(note: NoteEntity) {
        viewModelScope.launch { repository.updateNote(note.copy(isTrashed = true)) }
    }

    fun restoreFromTrash(note: NoteUiModel) {
        viewModelScope.launch { repository.updateNote(note.originalEntity.copy(isTrashed = false)) }
    }

    fun togglePin(note: NoteUiModel) {
        viewModelScope.launch { repository.updateNote(note.originalEntity.copy(isPinned = !note.isPinned)) }
    }

    fun deleteNotePermanently(note: NoteUiModel) {
        viewModelScope.launch { repository.deleteNote(note.originalEntity) }
    }

    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotesListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NotesListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}