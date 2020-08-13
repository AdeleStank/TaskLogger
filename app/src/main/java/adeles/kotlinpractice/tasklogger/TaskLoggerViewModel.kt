package adeles.kotlinpractice.tasklogger

import DoneTasksViewModel
import android.annotation.SuppressLint
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
import java.lang.IllegalStateException
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.concurrent.thread

private const val TAG = "TLViewModel"

class TaskLoggerViewModel (application: Application): AndroidViewModel(application) {
    private val contentObserver = object: ContentObserver(Handler()){
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            Log.d(TAG, "contentObserver: onChange: called with uri $uri")
            loadTasks()
        }
    }
    private val databaseCursor = MutableLiveData<Cursor>()
    val cursor : LiveData<Cursor>
        get() = databaseCursor

    init{
        Log.d(TAG, "TaskLoggerViewModel: created")

        getApplication<Application>().contentResolver.
            registerContentObserver(TasksContract.CONTENT_URI,true, contentObserver)
        loadTasks()
    }

    private fun loadTasks(){
        Log.d(TAG, "LoadTasks: started")
        val projection = arrayOf(TasksContract.Columns.ID,
        TasksContract.Columns.TASK_NAME,
        TasksContract.Columns.TASK_DEADLINE,
        TasksContract.Columns.TASK_DESCRIPTION)

        val sortOrder = "${TasksContract.Columns.TASK_NAME}"

        thread{
            val cursor = getApplication<Application>().contentResolver.query(TasksContract.CONTENT_URI,
                projection, null, null, sortOrder)
            databaseCursor.postValue(cursor)
        }

        Log.d(TAG, "databaseCursor: $databaseCursor")
    }

    fun deleteTask(taskId: Long){
        thread{
            getApplication<Application>().contentResolver?.delete(TasksContract.buildUriFromId(taskId),
                null, null)}
    }

    fun saveTask(task: Task): Task{
        val values = ContentValues()

        if(task.name.isNotEmpty()){
            values.put(TasksContract.Columns.TASK_NAME, task.name)
            values.put(TasksContract.Columns.TASK_DEADLINE, task.deadline)
            values.put(TasksContract.Columns.TASK_DESCRIPTION, task.description)
        }

        if(task.id == 0L){
            thread{
                Log.d(TAG, "saveTask: adding new task")
                val uri = getApplication<Application>().contentResolver?.insert(TasksContract.CONTENT_URI, values)
                if (uri != null){
                    task.id = TasksContract.getId(uri)
                    Log.d(TAG, "new id is $task.id")
                }
            }
        } else{
            thread {
                Log.d(TAG, "saveTask: updating existing task")
                getApplication<Application>().contentResolver?.update(TasksContract.buildUriFromId(task.id), values, null, null)
            }

        }
        return task
    }

    @SuppressLint("NewApi")
    fun moveTaskToDone(taskId: Long): DoneTask{
        val uri = TasksContract.buildUriFromId(taskId)
        val cursor = getApplication<Application>().contentResolver?.query(TasksContract.buildUriFromId(taskId), null, null,null,null)
        cursor!!.moveToNext()
            val task = Task(
                cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_NAME)),
                cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_DEADLINE)),
                cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_DESCRIPTION))
            )

            val doneTask = DoneTask(task.name, LocalDate.now().toString(), task.description)
            saveDoneTask(doneTask)
            Log.d(TAG, "saving done task")
            deleteTask(taskId)
            Log.d(TAG, "deleting a task")
            return doneTask
    }

    override fun onCleared() {
        Log.d(TAG, "OnCleared: called")
        getApplication<Application>().contentResolver.unregisterContentObserver(contentObserver)
    }

    // TODO find a way to make it work without copy pasting code
    fun saveDoneTask(doneTask: DoneTask): DoneTask {
        val values = ContentValues()

        values.put(DoneTasksContract.Columns.TASK_NAME, doneTask.name)
        values.put(DoneTasksContract.Columns.TASK_DONE_DATE, doneTask.doneDate)
        values.put(DoneTasksContract.Columns.TASK_DESCRIPTION, doneTask.description)

        if(doneTask.id == 0L){
            thread{
                Log.d(TAG, "saveTask: adding new task to done tasks")
                val uri = getApplication<Application>().contentResolver?.insert(DoneTasksContract.CONTENT_URI, values)
                if (uri != null){
                    doneTask.id = DoneTasksContract.getId(uri)
                    Log.d(TAG, "new id is $doneTask.id")
                }
            }
        } else{
            thread {
                Log.d(TAG, "saveTask: updating existing task")
                getApplication<Application>().contentResolver?.update(DoneTasksContract.buildUriFromId(doneTask.id), values, null, null)
            }
        }
        return doneTask
    }
}