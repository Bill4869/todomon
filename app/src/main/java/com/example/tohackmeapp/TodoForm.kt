package com.example.tohackmeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import com.example.tohackmeapp.FormatConvertion.toMap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_todo_form.*

class TodoForm : AppCompatActivity() {
    var fStore : FirebaseFirestore? = null
    var fAuth : FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo_form)

        fStore = FirebaseFirestore.getInstance()
        fAuth = FirebaseAuth.getInstance()


        btn_back.setOnClickListener(){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    fun onCreateTask(view: View) {
        val title = tvTitle.text.toString()
        val descrip = tvExplanation.text.toString()
        val level = tvLevel.text.toString().toInt()

        val selectedId = radioGroup.checkedRadioButtonId
        val radiobtn = findViewById<RadioButton>(selectedId)
        val userID = fAuth!!.currentUser!!.uid

        // add task
        fStore!!.collection("users").document(userID).collection("tasks").get().addOnSuccessListener {
            var taskId = it.size() + 1
            var collectionReference: CollectionReference = fStore!!.collection("users").document(userID).collection("tasks")
            val task : Todo = Todo(taskId, title, descrip, radiobtn.text.toString(), level)
            collectionReference.add(task.toMap())
        }

        Toast.makeText(this, "Task Created", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, TodoList::class.java)
        startActivity(intent)
        finish()

    }
}