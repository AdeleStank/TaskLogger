package adeles.kotlinpractice.tasklogger

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

private const val TAG = "AppDatabase"

private const val DATABASE_NAME = "TaskLogger.db"
private const val DATABASE_VERSION = 1

internal class AppDatabase private constructor(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    init {
        Log.d(TAG, "AppDatabase: initialising")
    }

    override fun onCreate(db: SQLiteDatabase) {
        // CREATE TABLE Tasks (_id INTEGER PRIMARY KEY NOT NULL, Name TEXT NOT NULL, Description TEXT, SortOrder INTEGER);
        Log.d(TAG, "onCreate: starts")
        addTasksTable(db)
        Log.d(TAG, "onCreate: Tasks table added")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "onUpgrade: starts")
        when(oldVersion) {
            1 -> {
                //upgrade database
            }
            else -> throw IllegalStateException("onUpgrade() with unknown newVersion: $newVersion")
        }
    }

    private fun addTasksTable(db: SQLiteDatabase){
        val sSQL = """CREATE TABLE ${TasksContract.TABLE_NAME} (
            ${TasksContract.Columns.ID} INTEGER PRIMARY KEY NOT NULL,
            ${TasksContract.Columns.TASK_NAME} TEXT NOT NULL,
            ${TasksContract.Columns.TASK_DESCRIPTION} TEXT,
            ${TasksContract.Columns.TASK_DEADLINE} TEXT);""".replaceIndent(" ")
        Log.d(TAG, sSQL)
        db.execSQL(sSQL)
    }

    companion object : SingletonHolder<AppDatabase, Context>(::AppDatabase)
}