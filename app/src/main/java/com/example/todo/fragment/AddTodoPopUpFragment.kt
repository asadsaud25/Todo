package com.example.todo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
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
        fun newInstance(taskId: String, task: String, label: Int) = AddTodoPopUpFragment().apply {
            arguments = Bundle().apply {
                putString("taskId", taskId)
                putString("task", task)
                putInt("label", label)
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
                arguments?.getString("task").toString(),
                arguments?.getInt("label") ?: -1
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
                    val label: Int = when (selectedRadioButton?.id) {
                        binding.op1.id -> 2131 // Urgent and important
                        binding.op2.id -> 3120 // Important but not urgent
                        binding.op3.id -> 3021 // Not important but urgent
                        else -> 3020 // Not important and not urgent
                    }

                    if (todoData == null) {
                        listener.onSaveTask(todoTask, label, binding.taskEt)
                    } else {
                        todoData?.task = todoTask
                        todoData?.label = label
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
        fun onSaveTask(todo: String, lable: Int, todoEt: TextInputEditText)
        fun onUpdateTask(todoData: TodoData, todoEt: TextInputEditText)
    }

}