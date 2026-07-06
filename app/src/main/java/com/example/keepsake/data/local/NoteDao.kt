package com.example.keepsake.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNote(note: NoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNotes(notes: List<NoteEntity>)

    @Query("UPDATE notes SET isTrashed = 1 WHERE id IN (:ids)")
    fun trashNotes(ids: List<Int>)

    @Query("UPDATE notes SET isArchived = :archive WHERE id IN (:ids)")
    fun archiveNotes(ids: List<Int>, archive: Boolean)

    @Query("UPDATE notes SET isPinned = :pin WHERE id IN (:ids)")
    fun pinNotes(ids: List<Int>, pin: Boolean)

    @Delete
    fun deleteNote(note: NoteEntity): Int

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Int): NoteEntity?
}