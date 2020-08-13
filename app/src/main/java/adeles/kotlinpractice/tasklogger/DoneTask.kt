package adeles.kotlinpractice.tasklogger

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DoneTask(val name: String, val doneDate: String, val description: String, var id: Long = 0) :
    Parcelable {
}