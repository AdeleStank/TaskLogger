package adeles.kotlinpractice.tasklogger

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Task(val name: String, val deadline: String, val description: String, var id: Long = 0) :
    Parcelable {
}