package adeles.kotlinpractice.tasklogger

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

private const val TAG = "AppDatabase"

private const val DATABASE_NAME = "TaskLogger.db"
private const val DATABASE_VERSION = 2

internal class AppDatabase private constructor(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        //creating tables
        addTasksTable(db)
        addDoneTasksTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        when(oldVersion) {
            1 -> {
                addDoneTasksTable(db)
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
        db.execSQL(sSQL)
    }

    private fun addDoneTasksTable(db: SQLiteDatabase){
        val sSQL = """CREATE TABLE ${DoneTasksContract.TABLE_NAME} (
            ${DoneTasksContract.Columns.ID} INTEGER PRIMARY KEY NOT NULL,
            ${DoneTasksContract.Columns.TASK_NAME} TEXT NOT NULL,
            ${DoneTasksContract.Columns.TASK_DESCRIPTION} TEXT,
            ${DoneTasksContract.Columns.TASK_DONE_DATE} TEXT NOT NULL);""".replaceIndent(" ")
        db.execSQL(sSQL)
    }

    companion object : SingletonHolder<AppDatabase, Context>(::AppDatabase)
}