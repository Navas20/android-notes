package com.navas.androidnotes.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.navas.androidnotes.NotesApplication
import com.navas.androidnotes.data.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as NotesApplication).repository

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showArchived = MutableStateFlow(false)
    val showArchived: StateFlow<Boolean> = _showArchived.asStateFlow()

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            val source = if (_showArchived.value) {
                repository.allArchivedNotes
            } else {
                repository.allActiveNotes
            }
            source.collect { noteList ->
                _notes.value = noteList
                _isLoading.value = false
            }
        }
    }

    fun toggleArchivedView() {
        _showArchived.value = !_showArchived.value
        loadNotes()
    }

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            val note = Note(
                title = title,
                content = content
            )
            repository.insertNote(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun archiveNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isArchived = true, updatedAt = System.currentTimeMillis()))
        }
    }

    fun unarchiveNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isArchived = false, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun deleteNoteById(id: Long) {
        viewModelScope.launch {
            repository.deleteNoteById(id)
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isBlank()) {
                loadNotes()
            } else {
                repository.searchNotes(query).collect { noteList ->
                    _notes.value = noteList
                }
            }
        }
    }
}
