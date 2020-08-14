package adeles.kotlinpractice.tasklogger

import DoneTasksViewModel
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.content_done_tasks.*

private const val TAG = "DoneTasksActivity"
private const val DIALOG_ID_DELETE = 1
private const val DIALOG_TASK_ID = "done_task_id"

class DoneTasksActivity : AppCompatActivity(),
    DoneTaskCursorRVAdapter.OnDoneTaskClickListener, AppDialog.DialogEvents{

    private val viewModel by lazy { ViewModelProviders.of(this).get(DoneTasksViewModel::class.java) }
    private val mAdapter = DoneTaskCursorRVAdapter(null, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_done_tasks)
        setSupportActionBar(findViewById(R.id.toolbar)) //TODO: fix the toolbar (no menu showing)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.cursor.observe(this, Observer { cursor -> mAdapter.swapCursor(cursor)?.close()})
        dt_list.layoutManager = LinearLayoutManager(this)    // <-- set up RecyclerView
        dt_list.adapter = mAdapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->{
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDeleteDoneClick(doneTask: DoneTask) {
        val args = Bundle().apply {
            putInt(DIALOG_ID, DIALOG_ID_DELETE)
            putString(DIALOG_MESSAGE, getString(R.string.diag_delete_message))
            putInt(DIALOG_POSITIVE_RID, R.string.deldiag_positive_caption)
            putLong(DIALOG_TASK_ID, doneTask.id)   // pass the id in the arguments, so we can retrieve it when we get called back.
        }
        val dialog = AppDialog()
        dialog.arguments = args
        dialog.show(this.supportFragmentManager, null)
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        if (dialogId == DIALOG_ID_DELETE) {
            val doneTaskId = args.getLong(DIALOG_TASK_ID)
            if (BuildConfig.DEBUG && doneTaskId == 0L) throw AssertionError("Task ID is zero")
            viewModel.deleteDoneTask(doneTaskId)
        }
    }
}