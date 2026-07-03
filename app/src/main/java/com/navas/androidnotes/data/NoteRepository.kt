package com.navas.androidnotes.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

    val allActiveNotes: Flow<List<Note>> = noteDao.getAllActiveNotes()
    val allArchivedNotes: Flow<List<Note>> = noteDao.getAllArchivedNotes()
    val noteCount: Flow<Int> = noteDao.getNoteCount()

    suspend fun getNoteById(id: Long): Note? = noteDao.getNoteById(id)

    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: Note) = noteDao.updateNote(note)

    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)

    suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)

    suspend fun clearArchived() = noteDao.clearArchived()
}
