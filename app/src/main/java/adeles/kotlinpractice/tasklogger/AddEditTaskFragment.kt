package adeles.kotlinpractice.tasklogger

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import kotlinx.android.synthetic.main.fragment_add_edit_task.*

private const val TAG = "AddEditTaskFragment"

private const val ARG_TASK = "task"

class AddEditTaskFragment : Fragment() {
    private var task: Task? = null
    private var listener: OnSaveClicked? = null
    private val viewModel by lazy { ViewModelProviders.of(activity!!).get(TaskLoggerViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            task = it.getParcelable(ARG_TASK)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_edit_task, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: called")
        if (savedInstanceState == null) {
            val task = task
            if (task != null) {
                Log.d(TAG, "onViewCreated: Task details found, editing task ${task.id}")
                add_edit_name.setText(task.name)
                add_edit_description.setText(task.description)
                add_edit_deadline.setText(task.deadline)
            } else {
                // No task, so we must be adding a new task, and NOT editing an existing one
                Log.d(TAG, "onViewCreated: No arguments, adding new record")
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated: starts")
        super.onActivityCreated(savedInstanceState)

        if (listener is AppCompatActivity) {
            val actionBar = (listener as AppCompatActivity).supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }

        add_edit_save.setOnClickListener {
            saveTask()
            listener?.onSaveClicked()
        }
    }

    override fun onAttach(context: Context?) {
        Log.d(TAG, "onAttach: started")
        super.onAttach(context)
        if (context is OnSaveClicked) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnSaveClicked")
        }
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach: starts")
        super.onDetach()
        listener = null
    }

    fun taskFromUI(): Task{
        val deadline = if (add_edit_deadline.text.isNotEmpty()){
            add_edit_deadline.text.toString()
        } else{
            "no deadline"
        }

        val newTask = Task(add_edit_name.text.toString(), deadline, add_edit_description.text.toString())
        newTask.id = task?.id ?: 0
        return newTask
    }

    fun hasUnsavedData(): Boolean {
        val newTask = taskFromUI()
        return ((newTask != task) &&
                (newTask.name.isNotBlank()
                        || newTask.description.isNotBlank()
                        || newTask.deadline.isNotBlank())
                )
    }

    private fun saveTask() {
        val newTask = taskFromUI()
        if(newTask != task){
            Log.d(TAG, "Saving task, task id is ${newTask.id}")
            task = viewModel.saveTask(newTask)
            Log.d(TAG, "Saving task, task id is ${task?.id}")
        }
    }

    interface OnSaveClicked {
        fun onSaveClicked()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddEditTaskFragment.
         */
        @JvmStatic
        fun newInstance(task: Task?) =
            AddEditTaskFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TASK, task)
                }
            }
    }
}