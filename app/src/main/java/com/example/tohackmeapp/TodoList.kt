package com.example.tohackmeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tohackmeapp.FormatConvertion.toMap
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_todo_list.*

class TodoList : AppCompatActivity(), (Todo) -> Unit {
    val levelXpRange = listOf(5, 10, 15, 20)

    companion object {
        val SELECTED_TASK = "com.example.tohackme.SELECTED_TASK"
    }

    var fStore: FirebaseFirestore? = null
    var fAuth: FirebaseAuth? = null
    var userId: Any? = null
    var taskList: List<Todo> = ArrayList()
    val taskListAdapter: TaskListAdapter = TaskListAdapter(taskList, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo_list)

        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
        userId = fAuth!!.currentUser!!.uid

        getTaskList()


        todoList.layoutManager = LinearLayoutManager(this)
        todoList.adapter = taskListAdapter



//        btn_back.setOnClickListener() {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//        }

        btnHome.setOnClickListener() {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }
    fun addTask(view: View) {
        val intent = Intent(this, TodoForm::class.java)
        startActivity(intent)
    }


    override fun onStart() {
        super.onStart()
        fStore!!.collection("users").document(userId.toString()).collection("tasks")
            .addSnapshotListener(this, EventListener { value, error ->
                if (error != null) {
                    return@EventListener
                }
                getTaskList()
            })
    }

    fun getTaskList() {
        val document = fStore!!.collection("users").document(userId.toString()).collection("tasks")
            .orderBy("status", Query.Direction.DESCENDING)
        document.get().addOnCompleteListener {
            if (it.isSuccessful) {
                taskList = it.result!!.toObjects(Todo::class.java)

                for ((index, task) in it.result!!.withIndex()) {
                    taskList[index].id = task.id
                }

                taskListAdapter.taskList = taskList
                taskListAdapter.notifyDataSetChanged()
            }
        }
    }

    fun finishAll(view: View) {
        var physical = 0
        var intelligence = 0
        var lifestyle = 0
        var others = 0
        val batch = FirebaseFirestore.getInstance().batch()
        for (task in taskList) {
            if (!task.status) {
                when (task.tag) {
                    "physical" -> physical += task.level!!
                    "intelligence" -> intelligence += task.level!!
                    "lifestyle" -> lifestyle += task.level!!
                    "others" -> others += task.level!!
                }
                task.status = true
                val documented =
                    fStore!!.collection("users").document(userId.toString()).collection("tasks")
                        .document(task.id.toString())

//                documented.set(task, SetOptions.merge())
                batch.set(documented, task, SetOptions.merge())
//                batch.update(documented, task.toMap())
            }
        }
        batch.commit()

        val totalXp = physical + intelligence + lifestyle + others
        val documentReference = fStore!!.collection("users").document(userId.toString())
        documentReference.get().addOnSuccessListener {
            val user = it.toObject(User::class.java)
            user!!.physical += physical
            user!!.intelligence += intelligence
            user!!.lifestyle += lifestyle
            user!!.others += others
            user!!.ep += totalXp

            while ((user!!.ep >= levelXpRange[user.level - 1]) && user.level < levelXpRange.size) {
                user.ep -= levelXpRange[user.level - 1]
                user.level++
            }

//            if (user!!.level < 4) user.level = (user.ep / 5) + 1

            documentReference.update(user.toMap())
            Toast.makeText(this, "完了", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteAll(view: View) {
        fStore!!.collection("users").document(userId.toString()).collection("tasks").get()
            .addOnSuccessListener {
                val batch = FirebaseFirestore.getInstance().batch()
                val list = it.documents
                for (document in list) {
                    batch.delete(document.reference)
                }
                batch.commit().addOnSuccessListener {
                    taskListAdapter.notifyDataSetChanged()
                    Toast.makeText(this, "削除", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun invoke(task: Todo) {
        val taskId: String = task.id!!
//        println(taskId)
        val intent = Intent(this, editTask::class.java)
        intent.putExtra(SELECTED_TASK, taskId)
        startActivity(intent)
    }
}