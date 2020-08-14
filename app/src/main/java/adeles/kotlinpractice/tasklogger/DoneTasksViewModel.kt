import adeles.kotlinpractice.tasklogger.DoneTask
import adeles.kotlinpractice.tasklogger.DoneTasksContract
import adeles.kotlinpractice.tasklogger.Task
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.ContentValues
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

private const val TAG = "DTViewModel"

class DoneTasksViewModel (application: Application): AndroidViewModel(application) {
    private val contentObserver = object: ContentObserver(Handler()){
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            loadDoneTasks()
        }
    }
    private val databaseCursor = MutableLiveData<Cursor>()
    val cursor : LiveData<Cursor>
        get() = databaseCursor

    init{
        getApplication<Application>().contentResolver.
        registerContentObserver(DoneTasksContract.CONTENT_URI,true, contentObserver)
        loadDoneTasks()
    }

    private fun loadDoneTasks(){
        val projection = arrayOf(
            DoneTasksContract.Columns.ID,
            DoneTasksContract.Columns.TASK_NAME,
            DoneTasksContract.Columns.TASK_DONE_DATE,
            DoneTasksContract.Columns.TASK_DESCRIPTION)

        val sortOrder = "${DoneTasksContract.Columns.TASK_NAME}"

        GlobalScope.launch{
            val cursor = getApplication<Application>().contentResolver.query(
                DoneTasksContract.CONTENT_URI,
                projection, null, null, sortOrder)
            databaseCursor.postValue(cursor)
        }
    }

    fun deleteDoneTask(doneTaskId: Long){
        GlobalScope.launch{
            getApplication<Application>().contentResolver?.delete(
                DoneTasksContract.buildUriFromId(doneTaskId),
                null, null)}
    }

    fun saveDoneTask(doneTask: DoneTask): DoneTask {
        val values = ContentValues()

            values.put(DoneTasksContract.Columns.TASK_NAME, doneTask.name)
            values.put(DoneTasksContract.Columns.TASK_DONE_DATE, doneTask.doneDate)
            values.put(DoneTasksContract.Columns.TASK_DESCRIPTION, doneTask.description)

        GlobalScope.launch{
                val uri = getApplication<Application>().contentResolver?.insert(DoneTasksContract.CONTENT_URI, values)
                if (uri != null){
                    doneTask.id = DoneTasksContract.getId(uri)
                }
            }

        return doneTask
    }

    override fun onCleared() {
        getApplication<Application>().contentResolver.unregisterContentObserver(contentObserver)
    }
}