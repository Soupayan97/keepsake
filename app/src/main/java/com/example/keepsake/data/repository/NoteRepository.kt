package com.example.keepsake.data.repository

import com.example.keepsake.data.local.NoteDao
import com.example.keepsake.data.local.NoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NoteRepository(
    private val dao: NoteDao
) {
    fun getAllNotes(): Flow<List<NoteEntity>> {
        return dao.getAllNotes()
    }

    suspend fun insertNote(note: NoteEntity): Long = withContext(Dispatchers.IO) {
        dao.insertNote(note)
    }

    suspend fun updateNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        dao.insertNote(note)
    }

    suspend fun trashNotes(ids: List<Int>) = withContext(Dispatchers.IO) {
        dao.trashNotes(ids)
    }

    suspend fun archiveNotes(ids: List<Int>, archive: Boolean) = withContext(Dispatchers.IO) {
        dao.archiveNotes(ids, archive)
    }

    suspend fun pinNotes(ids: List<Int>, pin: Boolean) = withContext(Dispatchers.IO) {
        dao.pinNotes(ids, pin)
    }

    suspend fun deleteNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        dao.deleteNote(note)
    }

    suspend fun getNoteById(id: Int): NoteEntity? = withContext(Dispatchers.IO) {
        dao.getNoteById(id)
    }
}