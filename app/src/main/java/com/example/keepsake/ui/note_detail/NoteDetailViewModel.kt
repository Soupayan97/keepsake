package com.example.keepsake.ui.note_detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.keepsake.data.local.ChecklistItem
import com.example.keepsake.data.local.NoteEntity
import com.example.keepsake.data.repository.NoteRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteDetailViewModel(private val repository: NoteRepository) : ViewModel() {

    private val gson = Gson()
    var currentNoteId by mutableStateOf(-1)
        private set
    var noteTitle by mutableStateOf("")
        private set
    var noteContent by mutableStateOf("")
        private set
    var noteColor by mutableLongStateOf(0xFF202124)
        private set
    var isPinned by mutableStateOf(false)
        private set
    var isArchived by mutableStateOf(false)
        private set
    var isTrashed by mutableStateOf(false)
        private set
    var isListMode by mutableStateOf(false)
        private set

    val checklistItems = mutableStateListOf<ChecklistItem>()

    val imagePaths = mutableStateListOf<String>()
    val drawingPaths = mutableStateListOf<String>()
    var audioPath by mutableStateOf<String?>(null)

    private var isDataLoaded = false
    private var isSaving = false
    fun loadNote(noteId: Int, isListModeInitial: Boolean = false) {
        if (isDataLoaded) return
        isDataLoaded = true

        if (noteId != -1) {
            viewModelScope.launch {
                val note = repository.getNoteById(noteId)
                note?.let {
                    currentNoteId = it.id
                    noteTitle = it.title
                    noteContent = it.content
                    noteColor = it.color
                    isPinned = it.isPinned
                    isArchived = it.isArchived
                    isTrashed = it.isTrashed
                    isListMode = it.isListMode

                    checklistItems.clear()
                    if (it.checklistData.isNotEmpty()) {
                        val type = object : TypeToken<List<ChecklistItem>>() {}.type
                        val items: List<ChecklistItem> = gson.fromJson(it.checklistData, type)
                        checklistItems.addAll(items)
                    }

                    imagePaths.clear()
                    if (it.imagePaths.isNotEmpty()) {
                        val type = object : TypeToken<List<String>>() {}.type
                        val paths: List<String> = gson.fromJson(it.imagePaths, type)
                        imagePaths.addAll(paths)
                    }
                    drawingPaths.clear()
                    if (it.drawingPaths.isNotEmpty()) {
                        val type = object : TypeToken<List<String>>() {}.type
                        val paths: List<String> = gson.fromJson(it.drawingPaths, type)
                        drawingPaths.addAll(paths)
                    }
                    audioPath = it.audioPath
                }
            }
        } else {
            resetState(isListModeInitial)
        }
    }

    private fun resetState(isListModeInitial: Boolean) {
        currentNoteId = -1
        noteTitle = ""
        noteContent = ""
        noteColor = 0xFF202124
        isPinned = false
        isArchived = false
        isTrashed = false
        isListMode = isListModeInitial
        checklistItems.clear()
        imagePaths.clear()
        drawingPaths.clear()
        audioPath = null
    }

    fun updateTitle(title: String) {
        noteTitle = title
    }

    fun updateContent(content: String) {
        noteContent = content
    }

    fun updateColor(color: Long) {
        noteColor = color
    }

    fun togglePin() {
        isPinned = !isPinned
    }

    fun toggleArchive() {
        isArchived = !isArchived
    }

    fun toggleListMode() {
        isListMode = !isListMode
    }

    fun addImagePath(path: String) {
        imagePaths.add(path)
    }

    fun removeImagePath(path: String) {
        imagePaths.remove(path)
    }

    fun addDrawingPath(path: String) {
        drawingPaths.add(path)
    }

    fun removeDrawingPath(path: String) {
        drawingPaths.remove(path)
    }

    fun updateAudioPath(path: String?) {
        audioPath = path
    }
    fun addChecklistItem(text: String) {
        val newItem =
            ChecklistItem(id = System.currentTimeMillis().toInt(), text = text, isChecked = false)
        checklistItems.add(newItem)
    }

    fun updateChecklistItem(itemId: Int, newText: String, isChecked: Boolean) {
        val index = checklistItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            checklistItems[index] =
                checklistItems[index].copy(text = newText, isChecked = isChecked)
        }
    }

    fun removeChecklistItem(itemId: Int) {
        checklistItems.removeAll { it.id == itemId }
    }

    fun moveToTrash() {
        if (currentNoteId == -1 && noteTitle.isBlank() && noteContent.isBlank() && checklistItems.isEmpty()) return
        isTrashed = true
        saveNote()
    }
    fun saveNote() {
        if (isSaving) return
        if (noteTitle.isBlank() && noteContent.isBlank() && checklistItems.isEmpty() &&
            imagePaths.isEmpty() && drawingPaths.isEmpty() && audioPath == null
        ) return

        isSaving = true
        val checklistJson = if (isListMode) gson.toJson(checklistItems.toList()) else ""
        val imagesJson = gson.toJson(imagePaths.toList())
        val drawingsJson = gson.toJson(drawingPaths.toList())

        val noteToSave = NoteEntity(
            id = if (currentNoteId == -1) 0 else currentNoteId,
            title = noteTitle,
            content = noteContent,
            color = noteColor,
            isPinned = isPinned,
            isArchived = isArchived,
            isTrashed = isTrashed,
            isListMode = isListMode,
            checklistData = checklistJson,
            imagePaths = imagesJson,
            drawingPaths = drawingsJson,
            audioPath = audioPath,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            withContext(NonCancellable) {
                try {
                    val newId = repository.insertNote(noteToSave)
                    if (currentNoteId == -1) {
                        currentNoteId = newId.toInt()
                    }
                } finally {
                    isSaving = false
                }
            }
        }
    }

    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NoteDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NoteDetailViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
