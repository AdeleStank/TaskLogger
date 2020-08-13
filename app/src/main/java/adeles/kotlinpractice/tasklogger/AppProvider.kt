package adeles.kotlinpractice.tasklogger

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.util.Log

private const val TAG = "AppProvider"

const val CONTENT_AUTHORITY = "adeles.kotlinpractice.tasklogger.provider"

private const val TASKS = 100
private const val TASKS_ID = 101
private const val DONE_TASKS = 200
private const val DONE_TASKS_ID = 201

val CONTENT_AUTHORITY_URI: Uri = Uri.parse("content://$CONTENT_AUTHORITY")

class AppProvider: ContentProvider() {
    private val uriMatcher by lazy { buildUriMatcher() }

    private fun buildUriMatcher() : UriMatcher {
        Log.d(TAG, "buildUriMatcher: starts")
        val matcher = UriMatcher(UriMatcher.NO_MATCH)

        matcher.addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME, TASKS)
        matcher.addURI(CONTENT_AUTHORITY, "${TasksContract.TABLE_NAME}/#", TASKS_ID)
        matcher.addURI(CONTENT_AUTHORITY, DoneTasksContract.TABLE_NAME, DONE_TASKS)
        matcher.addURI(CONTENT_AUTHORITY, "${DoneTasksContract.TABLE_NAME}/#", DONE_TASKS_ID)

        return matcher
    }

    override fun onCreate(): Boolean {
        Log.d(TAG, "onCreate: starts")
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?,
        selection: String?, selectionArgs: Array<out String>?, sortOrder: String?
    ): Cursor {
        Log.d(TAG, "query: called with uri $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "query: match is $match")

        val queryBuilder = SQLiteQueryBuilder()

        when(match){
            TASKS -> queryBuilder.tables = TasksContract.TABLE_NAME

            TASKS_ID -> {
                queryBuilder.tables = TasksContract.TABLE_NAME
                val taskId = TasksContract.getId(uri)
                queryBuilder.appendWhere("${TasksContract.Columns.ID} = ")
                queryBuilder.appendWhereEscapeString("$taskId")
            }

            DONE_TASKS -> queryBuilder.tables = DoneTasksContract.TABLE_NAME

            DONE_TASKS_ID -> {
                queryBuilder.tables = DoneTasksContract.TABLE_NAME
                val doneTaskId = DoneTasksContract.getId(uri)
                queryBuilder.appendWhere("${DoneTasksContract.Columns.ID} = ")
                queryBuilder.appendWhereEscapeString("$doneTaskId")
            }

            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }

        val db = AppDatabase.getInstance(context!!).readableDatabase //TODO: why is context nullable?
        val cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        Log.d(TAG, "query: cursor = $cursor")
        Log.d(TAG, "query: rows in returned cursor = ${cursor.count}") // TODO remove this line

        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        Log.d(TAG, "insert: called with uri $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "insert: match is $match")

        val recordId: Long
        val returnUri: Uri

        when(match) {
            TASKS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase //TODO: fix the nullable context problem
                recordId = db.insert(TasksContract.TABLE_NAME, null, values)
                if(recordId != -1L) {
                    returnUri = TasksContract.buildUriFromId(recordId)
                } else {
                    throw SQLException("Failed to insert, Uri was $uri")
                }
            }

            DONE_TASKS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase //TODO: fix the nullable context problem
                recordId = db.insert(DoneTasksContract.TABLE_NAME, null, values)
                if(recordId != -1L) {
                    returnUri = DoneTasksContract.buildUriFromId(recordId)
                } else {
                    throw SQLException("Failed to insert, Uri was $uri")
                }
            }

            else -> throw IllegalArgumentException("Unknown uri: $uri")
        }

        if (recordId > 0) {
            Log.d(TAG, "insert: Setting notifyChange with $uri")
            context?.contentResolver?.notifyChange(uri, null)
        }

        Log.d(TAG, "Exiting insert, returning $returnUri")
        return returnUri
    }

    override fun update(uri: Uri, values: ContentValues?,
        selection: String?, selectionArgs: Array<out String>?
    ): Int {
        Log.d(TAG, "update: called with uri $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "update: match is $match")

        val count: Int
        var selectionCriteria: String

        when(match) {

            TASKS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase //TODO: fix the nullable context problem
                count = db.update(TasksContract.TABLE_NAME, values, selection, selectionArgs)
            }

            TASKS_ID -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase //TODO: fix the nullable context problem
                val id = TasksContract.getId(uri)
                selectionCriteria = "${TasksContract.Columns.ID} = $id"

                if(selection != null && selection.isNotEmpty()) {
                    selectionCriteria += " AND ($selection)"
                }

                count = db.update(TasksContract.TABLE_NAME, values, selectionCriteria, selectionArgs)
            }

            DONE_TASKS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase //TODO: fix the nullable context problem
                count = db.update(DoneTasksContract.TABLE_NAME, values, selection, selectionArgs)
            }

            DONE_TASKS_ID -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase //TODO: fix the nullable context problem
                val id = DoneTasksContract.getId(uri)
                selectionCriteria = "${DoneTasksContract.Columns.ID} = $id"

                if(selection != null && selection.isNotEmpty()) {
                    selectionCriteria += " AND ($selection)"
                }

                count = db.update(DoneTasksContract.TABLE_NAME, values, selectionCriteria, selectionArgs)
            }

            else -> throw IllegalArgumentException("Unknown uri: $uri")
        }

        if (count > 0) {
            Log.d(TAG, "update: Setting notifyChange with $uri")
            context?.contentResolver?.notifyChange(uri, null)
        }

        Log.d(TAG, "Exiting update, returning $count")
        return count
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.d(TAG, "delete: called with uri $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "delete: match is $match")

        val count: Int
        var selectionCriteria: String

        when(match) {

            TASKS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase //TODO: fix the nullable context problem
                count = db.delete(TasksContract.TABLE_NAME, selection, selectionArgs)
            }

            TASKS_ID -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase //TODO: fix the nullable context problem
                val id = TasksContract.getId(uri)
                selectionCriteria = "${TasksContract.Columns.ID} = $id"

                if(selection != null && selection.isNotEmpty()) {
                    selectionCriteria += " AND ($selection)"
                }

                count = db.delete(TasksContract.TABLE_NAME, selectionCriteria, selectionArgs)
            }

            DONE_TASKS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase //TODO: fix the nullable context problem
                count = db.delete(DoneTasksContract.TABLE_NAME, selection, selectionArgs)
            }

            DONE_TASKS_ID -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase //TODO: fix the nullable context problem
                val id = DoneTasksContract.getId(uri)
                selectionCriteria = "${DoneTasksContract.Columns.ID} = $id"

                if(selection != null && selection.isNotEmpty()) {
                    selectionCriteria += " AND ($selection)"
                }

                count = db.delete(DoneTasksContract.TABLE_NAME, selectionCriteria, selectionArgs)
            }

            else -> throw IllegalArgumentException("Unknown uri: $uri")
        }

        if (count > 0) {
            // something was deleted
            Log.d(TAG, "delete: Setting notifyChange with $uri")

            context?.contentResolver?.notifyChange(uri, null)
        }

        Log.d(TAG, "Exiting delete, returning $count")
        return count
    }


    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            TASKS -> TasksContract.CONTENT_TYPE

            TASKS_ID -> TasksContract.CONTENT_ITEM_TYPE

            DONE_TASKS -> DoneTasksContract.CONTENT_TYPE

            DONE_TASKS_ID -> DoneTasksContract.CONTENT_ITEM_TYPE

            else -> throw IllegalArgumentException("unknown Uri: $uri")
        }
    }
}