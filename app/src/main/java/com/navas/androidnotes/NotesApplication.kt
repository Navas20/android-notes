package com.navas.androidnotes

import android.app.Application
import com.navas.androidnotes.data.AppDatabase
import com.navas.androidnotes.data.NoteRepository

class NotesApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: NoteRepository by lazy { NoteRepository(database.noteDao()) }
}
