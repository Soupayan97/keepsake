package com.example.keepsake

import android.app.Application
import com.example.keepsake.data.local.NoteDatabase
import com.example.keepsake.data.repository.NoteRepository

class KeepsakeApp : Application() {
    val database: NoteDatabase by lazy {
        NoteDatabase.getDatabase(this)
    }
    val repository: NoteRepository by lazy {
        NoteRepository(database.noteDao)
    }
}