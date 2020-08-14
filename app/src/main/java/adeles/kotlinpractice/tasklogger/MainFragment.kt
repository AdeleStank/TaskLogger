package adeles.kotlinpractice.tasklogger

import android.app.Application
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_main.*

private const val TAG = "MainActivityFragment"
private const val DIALOG_ID_DELETE = 1
private const val DIALOG_ID_DONE = 2
private const val DIALOG_TASK_ID = "task_id"

class MainFragment : Fragment(), CursorRecyclerViewAdapter.OnTaskClickListener,
    AppDialog.DialogEvents{

    interface OnTaskEdit {
        fun onTaskEdit(task: Task)
    }

    private val viewModel by lazy { ViewModelProviders.of(activity!!).get(TaskLoggerViewModel::class.java) }
    private val mAdapter = CursorRecyclerViewAdapter(null, this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context !is OnTaskEdit) {
            throw RuntimeException("$context must implement OnTaskEdit")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.cursor.observe(this, Observer { cursor -> mAdapter.swapCursor(cursor)?.close()})
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        main_task_list.layoutManager = LinearLayoutManager(context)    // <-- set up RecyclerView
        main_task_list.adapter = mAdapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onEditClick(task: Task) {
        (activity as OnTaskEdit?)?.onTaskEdit(task)
    }

    override fun onDeleteClick(task: Task) {
        val args = Bundle().apply {
            putInt(DIALOG_ID, DIALOG_ID_DELETE)
            putString(DIALOG_MESSAGE, getString(R.string.diag_delete_message))
            putInt(DIALOG_POSITIVE_RID, R.string.deldiag_positive_caption)
            putLong(DIALOG_TASK_ID, task.id)   // pass the id in the arguments, so we can retrieve it when we get called back.
        }
        val dialog = AppDialog()
        dialog.arguments = args
        dialog.show(childFragmentManager, null)
    }

    override fun onDoneClick(task: Task) {
        val args = Bundle().apply {
            putInt(DIALOG_ID, DIALOG_ID_DONE)
            putString(DIALOG_MESSAGE, getString(R.string.diag_done_message))
            putLong(DIALOG_TASK_ID, task.id)   // pass the id in the arguments, so we can retrieve it when we get called back.
        }
        val dialog = AppDialog()
        dialog.arguments = args
        dialog.show(childFragmentManager, null)
    }

    override fun onTaskLongClick(task: Task) {
        TODO("Not yet implemented")
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        if (dialogId == DIALOG_ID_DELETE) {
            val taskId = args.getLong(DIALOG_TASK_ID)
            if (BuildConfig.DEBUG && taskId == 0L) throw AssertionError("Task ID is zero")
            viewModel.deleteTask(taskId)
        } else if (dialogId == DIALOG_ID_DONE){
            val taskId = args.getLong(DIALOG_TASK_ID)
            if (BuildConfig.DEBUG && taskId == 0L) throw AssertionError("Task ID is zero")
            viewModel.moveTaskToDone(taskId)
        }
    }
}