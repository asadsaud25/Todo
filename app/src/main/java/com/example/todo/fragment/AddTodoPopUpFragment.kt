package com.example.todo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.compose.material3.RadioButton
import androidx.fragment.app.DialogFragment
import com.example.todo.databinding.FragmentAddTodoPopUpBinding
import com.example.todo.utils.TodoData
import com.google.android.material.textfield.TextInputEditText

class AddTodoPopUpFragment : DialogFragment() {

    private lateinit var binding: FragmentAddTodoPopUpBinding
    private lateinit var listener: DialogNextBtnClickListener
    private var selectedRadioButton: RadioButton? = null
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
        setupRadioButtons()
        registerEvents()
    }



    // Setup the RadioButtons with listeners when the view is created or during setup
    private fun setupRadioButtons() {
        for (i in 0 until binding.radioGroup.childCount) {
            val child = binding.radioGroup.getChildAt(i)
            if (child is LinearLayout) {
                for (j in 0 until child.childCount) {
                    val innerChild = child.getChildAt(j)
                    if (innerChild is RadioButton) {
                        innerChild.setOnClickListener {
                            // Deselect all RadioButtons first
                            deselectAllRadioButtons()

                            // Select the clicked RadioButton and update the selectedRadioButton variable
                            innerChild.isChecked = true
                            selectedRadioButton = innerChild
                        }
                    }
                }
            }
        }
    }

    private fun registerEvents() {
        binding.btnAddTask.setOnClickListener {

            val todoTask = binding.taskEt.text.toString()

            if (todoTask.isNotEmpty()) {
                if (selectedRadioButton != null) {
                    val label = selectedRadioButton?.text.toString()

                    Toast.makeText(context, "Selected: $label", Toast.LENGTH_SHORT).show()

                    if (todoData == null) {
                        listener.onSaveTask(todoTask, label, binding.taskEt)
                    } else {
                        todoData?.task = todoTask
                        listener.onUpdateTask(todoData!!, binding.taskEt)
                    }
                } else {
                    Toast.makeText(context, "Please choose an option", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Please enter task", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deselectAllRadioButtons() {
        for (i in 0 until binding.radioGroup.childCount) {
            val child = binding.radioGroup.getChildAt(i)
            if (child is LinearLayout) {
                // Inside the LinearLayout, find and deselect all RadioButtons
                for (j in 0 until child.childCount) {
                    val innerChild = child.getChildAt(j)
                    if (innerChild is RadioButton) {
                        innerChild.isChecked = false
                    }
                }
            }
        }
    }

    interface DialogNextBtnClickListener {
        fun onSaveTask(todo: String, lable: String, todoEt: TextInputEditText)
        fun onUpdateTask(todoData: TodoData, todoEt: TextInputEditText)
    }

}