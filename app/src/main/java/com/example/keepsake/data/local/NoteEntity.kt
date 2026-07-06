package com.example.keepsake.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

data class ChecklistItem(
    val id: Int,
    val text: String,
    val isChecked: Boolean
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val color: Long,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isTrashed: Boolean = false,
    val isListMode: Boolean = false,
    val checklistData: String = "", // JSON
    val imagePaths: String = "",    // JSON List of file paths
    val drawingPaths: String = "",  // JSON List of file paths
    val audioPath: String? = null,  // Single audio file path
    val reminderTime: Long? = null, // Reminder timestamp
    val timestamp: Long
)