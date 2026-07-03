package com.navas.androidnotes

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.navas.androidnotes.data.Note
import com.navas.androidnotes.ui.NotesAdapter
import com.navas.androidnotes.ui.NotesViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: NotesViewModel
    private lateinit var adapter: NotesAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: android.widget.TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[NotesViewModel::class.java]

        recyclerView = findViewById(R.id.recycler_notes)
        emptyView = findViewById(R.id.empty_view)
        val fabAdd: FloatingActionButton = findViewById(R.id.fab_add)

        adapter = NotesAdapter(
            onNoteClick = { note -> showNoteDialog(note) },
            onNoteLongClick = { note -> showDeleteConfirmation(note) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel.notes.observe(this) { notes ->
            adapter.submitList(notes)
            emptyView.visibility = if (notes.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }

        fabAdd.setOnClickListener { showNoteDialog(null) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.search(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.search(newText ?: "")
                return true
            }
        })

        searchView.setOnCloseListener {
            viewModel.search("")
            false
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_archived -> {
                viewModel.toggleArchivedView()
                item.title = if (viewModel.showArchived.value) {
                    "Ver activas"
                } else {
                    "Ver archivadas"
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showNoteDialog(note: Note?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_note, null)
        val titleInput = dialogView.findViewById<TextInputEditText>(R.id.input_title)
        val contentInput = dialogView.findViewById<TextInputEditText>(R.id.input_content)

        val isEditing = note != null
        if (isEditing) {
            titleInput.setText(note.title)
            contentInput.setText(note.content)
        }

        AlertDialog.Builder(this)
            .setTitle(if (isEditing) "Editar nota" else "Nueva nota")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val title = titleInput.text?.toString()?.trim() ?: ""
                val content = contentInput.text?.toString()?.trim() ?: ""

                if (title.isEmpty()) {
                    Toast.makeText(this, "El título es requerido", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (isEditing) {
                    viewModel.updateNote(note.copy(title = title, content = content))
                } else {
                    viewModel.addNote(title, content)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteConfirmation(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar nota")
            .setMessage("¿Estás seguro de eliminar \"${note.title}\"?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteNote(note)
                Toast.makeText(this, "Nota eliminada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
