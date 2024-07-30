package com.example.todo.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.R
import com.example.todo.databinding.FragmentTaskPanelBinding
import com.example.todo.utils.TodoAdapter
import com.example.todo.utils.TodoData
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class TaskPanelFragment : Fragment(), AddTodoPopUpFragment.DialogNextBtnClickListener,
    TodoAdapter.TodoAdapterClicksInterface {

    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var binding: FragmentTaskPanelBinding
    private lateinit var databaseRef: DatabaseReference
    private lateinit var db: FirebaseFirestore
    private var username: String? = null
    private var popUpFragment: AddTodoPopUpFragment? = null
    private lateinit var adapter: TodoAdapter
    private lateinit var mList: MutableList<TodoData>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskPanelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        getDataFromFirebase()
        registerEvent()
    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        navController = Navigation.findNavController(view)
        databaseRef = FirebaseDatabase.getInstance()
            .reference.child("Tasks")
            .child(auth.currentUser?.uid.toString())

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        mList = mutableListOf()

        adapter = TodoAdapter(mList)
        adapter.setListener(this)
        binding.recyclerView.adapter = adapter
    }


    private fun registerEvent() {
        binding.btnDrawer.setOnClickListener {
            auth.signOut()
            navController.navigate(R.id.action_taskPannelFragment_to_loginFragment)
        }

        binding.btnAddTask.setOnClickListener {
            if (popUpFragment != null) {
                childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()
            }
            popUpFragment = AddTodoPopUpFragment()
            popUpFragment!!.setListener(this)
            popUpFragment!!.show(
                childFragmentManager,
                AddTodoPopUpFragment.TAG
            )
        }
    }


    private fun getDataFromFirebase() {

        binding.progressBar.visibility = View.VISIBLE

        val uid = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(uid)
        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    username =
                        document.getString("username")?.replaceFirstChar { it.uppercase() } ?: ""
                    binding.textName.text = buildString {
                        append("Hey ")
                        append(username)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error fetching user data: $exception")
            }

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                for (taskSnapShot in snapshot.children) {
                    val todoTask = taskSnapShot.key?.let {
                        TodoData(it, taskSnapShot.value.toString())
                    }

                    if (todoTask != null) {
                        mList.add(todoTask)
                    }
                }
                binding.textTaskNo.text = buildString {
                    append("Today you have ")
                    append(mList.size)
                    if (mList.size < 2) {
                        append(" task to do")
                    } else {
                        append(" tasks to do")
                    }
                }
                adapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }

        })

    }

    override fun onSaveTask(todo: String, todoEt: TextInputEditText) {
        databaseRef.push().setValue(todo).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Todo saved successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Error!", Toast.LENGTH_SHORT).show()
            }
            todoEt.text = null
            popUpFragment?.dismiss()
        }
    }

    override fun onUpdateTask(todoData: TodoData, todoEt: TextInputEditText) {
        val map = HashMap<String, Any>()
        map[todoData.taskId] = todoData.task
        databaseRef.updateChildren(map).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Task updated successfully", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(context, "Error!!", Toast.LENGTH_SHORT).show()
            }
            todoEt.text = null
            popUpFragment!!.dismiss()
        }
    }

    override fun onDelTask(todoData: TodoData) {
        databaseRef.child(todoData.taskId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Task deleted successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Error!!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onEditTask(todoData: TodoData) {
        if (popUpFragment != null) {
            childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()
        }

        popUpFragment = AddTodoPopUpFragment.newInstance(todoData.taskId, todoData.task)
        popUpFragment!!.setListener(this)
        popUpFragment!!.show(childFragmentManager, AddTodoPopUpFragment.TAG)

    }

}