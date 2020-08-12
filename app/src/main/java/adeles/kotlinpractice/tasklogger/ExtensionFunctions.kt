package adeles.kotlinpractice.tasklogger

import android.os.Bundle
import android.support.v4.app.FragmentActivity

fun FragmentActivity.showConfirmationDialog(id: Int,
                                            message: String,
                                            positiveCaption: Int = R.string.ok,
                                            negativeCaption: Int = R.string.cancel) {
    val args = Bundle().apply {
        putInt(DIALOG_ID, id)
        putString(DIALOG_MESSAGE, message)
        putInt(DIALOG_POSITIVE_RID, positiveCaption)
        putInt(DIALOG_NEGATIVE_RID, negativeCaption)
    }
    val dialog = AppDialog()
    dialog.arguments = args
    dialog.show(this.supportFragmentManager, null)
}