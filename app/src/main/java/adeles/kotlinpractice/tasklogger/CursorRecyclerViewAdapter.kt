package adeles.kotlinpractice.tasklogger

import android.annotation.SuppressLint
import android.database.Cursor
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.task_summary.*
import java.lang.IllegalStateException
import java.util.concurrent.CyclicBarrier

private const val TAG = "cursorRVAdapter"

class TaskViewHolder(override val containerView: View) :
    RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bind(task: Task, listener: CursorRecyclerViewAdapter.OnTaskClickListener) {
        ts_name.text = task.name
        ts_deadline.text = task.deadline
        ts_description.text = task.description
        ts_edit.visibility = View.VISIBLE
        ts_delete.visibility = View.VISIBLE
        ts_done.visibility = View.VISIBLE

        ts_edit.setOnClickListener {
            Log.d(TAG, "Edit button tapped")
            listener.onEditClick(task)
        }

        ts_delete.setOnClickListener {
            Log.d(TAG, "Delete button tapped")
            listener.onDeleteClick(task)
        }

        ts_done.setOnClickListener {
            Log.d(TAG, "Done button tapped")
            listener.onDoneClick(task)
        }

        containerView.setOnLongClickListener {
            Log.d(TAG, "onLongClick tapped")
            listener.onTaskLongClick(task)
            true
        }
    }

    @SuppressLint("SetTextI18n")
    fun bindDone(doneTask: DoneTask, listener: DoneTaskCursorRVAdapter.OnDoneTaskClickListener) {
        ts_name.text = doneTask.name
        ts_deadline.text = "Finished on: ${doneTask.doneDate}"
        ts_description.text = doneTask.description
        ts_edit.visibility = View.GONE
        ts_delete.visibility = View.VISIBLE
        ts_done.visibility = View.GONE

        ts_delete.setOnClickListener {
            Log.d(TAG, "Delete button tapped")
            listener.onDeleteDoneClick(doneTask)
        }

//        containerView.setOnLongClickListener {
//            Log.d(TAG, "onLongClick tapped")
//            listener.onTaskLongClick(task)
//            true
//        }
    }
}

class CursorRecyclerViewAdapter (private var cursor : Cursor?, private val listener: OnTaskClickListener)
    : RecyclerView.Adapter<TaskViewHolder>() {

    interface OnTaskClickListener{
        fun onEditClick(task: Task)
        fun onDeleteClick(task: Task)
        fun onDoneClick(task: Task)
        fun onTaskLongClick(task: Task)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        Log.d(TAG, "onCreateViewHolder: starts")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_summary, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: starts")
        val cursor = cursor

        Log.d(TAG, "cursor: $cursor")

        if (cursor == null || cursor.count == 0){
            Log.d(TAG, "OnBindViewHolder: providing an example")
            holder.ts_name.setText("An example")
            holder.ts_deadline.setText("No deadline")
            holder.ts_description.setText("This task is an example. It will disappear once you add at least one real task.")
            holder.ts_done.visibility = View.GONE
            holder.ts_edit.visibility = View.GONE
            holder.ts_delete.visibility = View.GONE
        } else{
            if(!cursor.moveToPosition((position))){
                throw IllegalStateException("Couldn't move cursor to the position $position")
            }
            val task = Task(
                cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_NAME)),
                cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_DEADLINE)),
                cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_DESCRIPTION))
            )
            task.id = cursor.getLong(cursor.getColumnIndex(TasksContract.Columns.ID))

            holder.bind(task, listener)
        }
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: starts")
        Log.d(TAG, "cursor: $cursor")
        val cursor = cursor
        val count = if (cursor == null || cursor.count == 0) {
            1   //fib, because we populate a single ViewHolder with instructions
        } else {
            cursor.count
        }
        Log.d(TAG, "returning $count")
        return count
    }

    fun swapCursor(newCursor: Cursor?): Cursor? {
        if (newCursor === cursor) {
            return null
        }
        val numItems = itemCount

        val oldCursor = cursor
        cursor = newCursor
        if (newCursor != null) {
            // notify the observers about the new cursor
            notifyDataSetChanged()
        } else {
            // notify the observers about the lack of a data set
            notifyItemRangeRemoved(0, numItems)
        }
        return oldCursor
    }

}