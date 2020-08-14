package adeles.kotlinpractice.tasklogger

import android.database.Cursor
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.task_summary.*
import java.lang.IllegalStateException

private const val TAG = "DTCursorRVAdapter"

//TODO try to make CRViewAdapter usable for both cases so this class is not needed anymore

class DoneTaskCursorRVAdapter(private var cursor : Cursor?, private val listener: OnDoneTaskClickListener)
    : RecyclerView.Adapter<TaskViewHolder>() {

    interface OnDoneTaskClickListener{
        fun onDeleteDoneClick(doneTask: DoneTask)
//        fun onDoneTaskLongClick(task: Task)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_summary, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val cursor = cursor

        if (cursor == null || cursor.count == 0){
            Log.d(TAG, "OnBindViewHolder: providing an example")
            holder.ts_name.setText("No done tasks")
            holder.ts_deadline.setText("")
            holder.ts_description.setText("You have no done tasks saved.")
            holder.ts_done.visibility = View.GONE
            holder.ts_edit.visibility = View.GONE
            holder.ts_delete.visibility = View.GONE
        } else{
            if(!cursor.moveToPosition((position))){
                throw IllegalStateException("Couldn't move cursor to the position $position")
            }
            val doneTask = DoneTask(
                cursor.getString(cursor.getColumnIndex(DoneTasksContract.Columns.TASK_NAME)),
                cursor.getString(cursor.getColumnIndex(DoneTasksContract.Columns.TASK_DONE_DATE)),
                cursor.getString(cursor.getColumnIndex(DoneTasksContract.Columns.TASK_DESCRIPTION))
            )
            doneTask.id = cursor.getLong(cursor.getColumnIndex(DoneTasksContract.Columns.ID))

            holder.bindDone(doneTask, listener)
        }
    }

    override fun getItemCount(): Int {
        val cursor = cursor
        return if (cursor == null || cursor.count == 0) {
            1   //fib, because we populate a single ViewHolder with instructions
        } else {
            cursor.count
        }
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