package com.example.todo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.todo.databinding.FragmentAddTodoPopUpBinding
import com.example.todo.utils.TodoData
import com.google.android.material.textfield.TextInputEditText

class AddTodoPopUpFragment : DialogFragment() {

    private lateinit var binding: FragmentAddTodoPopUpBinding
    private lateinit var listener: DialogNextBtnClickListener
    private var todoData: TodoData? = null

    fun setListener(listener: TaskPanelFragment) {
        this.listener = listener
    }

    companion object {
        const val TAG = "AddTodoPopUpFragment"

        @JvmStatic
        fun newInstance(taskId: String, task: String) = AddTodoPopUpFragment().apply {
            arguments = Bundle().apply {
                putString("taskId", taskId)
                putString("task", task)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddTodoPopUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments != null) {
            todoData = TodoData(
                arguments?.getString("taskId").toString(),
                arguments?.getString("task").toString()
            )

            binding.taskEt.setText(todoData?.task)
        }
        registerEvents()
    }

    private fun registerEvents() {
        binding.btnAddTask.setOnClickListener {
            val todoTask = binding.taskEt.text.toString()

            if (todoTask.isNotEmpty()) {
                if (todoData == null) {
                    listener.onSaveTask(todoTask, binding.taskEt)
                } else {
                    todoData?.task = todoTask
                    listener.onUpdateTask(todoData!!, binding.taskEt)
                }
            } else {
                Toast.makeText(context, "Please enter task", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    interface DialogNextBtnClickListener {
        fun onSaveTask(todo: String, todoEt: TextInputEditText)
        fun onUpdateTask(todoData: TodoData, todoEt: TextInputEditText)
    }

}