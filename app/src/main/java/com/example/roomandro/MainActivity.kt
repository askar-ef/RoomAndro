package com.example.roomandro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.example.roomandro.database.Note
import com.example.roomandro.database.NoteDao
import com.example.roomandro.database.NoteRoomDatabase
import com.example.roomandro.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mNoteDao: NoteDao
    private lateinit var executorService: ExecutorService
    private var updateId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        executorService = Executors.newSingleThreadExecutor()
        val db = NoteRoomDatabase.getDatabase(this)
        mNoteDao = db!!.noteDao()!!


        with(binding) {
            btnPost.setOnClickListener(
                View.OnClickListener {
                    insert(
                        Note(
                            title = txtTitle.text.toString(),
                            description = txtDescription.text.toString(),
                            date = txtDate.text.toString()
                        )
                    )
                    setEmptyField()
                }
            )
            btnUpdate.setOnClickListener{
                update(
                    Note(
                        id = updateId,
                        title = txtTitle.getText().toString(),
                        description = txtDescription.getText().toString(),
                        date = txtDate.getText().toString()
                    )
                )
                updateId = 0
                setEmptyField()
            }

            lvName.setOnItemClickListener { adapterView, _, i, _ ->
                val item = adapterView.adapter.getItem(i) as Note
                updateId = item.id
                binding.txtTitle.setText(item.title)
                binding.txtDescription.setText(item.description)
                binding.txtDate.setText(item.date)
            }

            lvName.setOnItemLongClickListener { adapterView, _, i, _ ->
                val item = adapterView.adapter.getItem(i) as Note
                delete(item)
                true
            }
        }
    }

    override fun onResume(){
        super.onResume()
        getAllNotes()
    }

    private fun setEmptyField(){
        with(binding){
            txtTitle.setText("")
            txtDescription.setText("")
            txtDate.setText("")
        }
    }


    private fun getAllNotes() {
        mNoteDao.allNotes.observe(this){
            notes->
            val adapter:ArrayAdapter<Note> = ArrayAdapter<Note>(
                this,
                android.R.layout.simple_list_item_1, notes
            )
            binding.lvName.adapter = adapter
        }
    }

    private fun insert(note: Note) {
        executorService.execute {mNoteDao.insert(note)}
    }

    private fun delete(note: Note) {
        executorService.execute {mNoteDao.delete(note)}
    }

    private fun update(note: Note) {
        executorService.execute {mNoteDao.update(note)}
    }



}