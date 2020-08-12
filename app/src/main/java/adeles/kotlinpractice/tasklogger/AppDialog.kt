package adeles.kotlinpractice.tasklogger

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AppCompatDialogFragment
import android.util.Log

private const val TAG = "AppDialog"

const val DIALOG_ID = "id"
const val DIALOG_MESSAGE = "message"
const val DIALOG_POSITIVE_RID = "positive_rid"
const val DIALOG_NEGATIVE_RID = "negative_rid"

class AppDialog: AppCompatDialogFragment() {
    private var dialogEvents: DialogEvents? = null

    internal interface DialogEvents{
        fun onPositiveDialogResult(dialogId: Int, args: Bundle)
//        fun onNegativeDialogResult(dialogId: Int, args: Bundle)
//        fun onDialogCancelled(dialogId: Int)
    }

    override fun onAttach(context: Context) {
        Log.d(TAG, "onAttach: called, context is $context")
        super.onAttach(context)

        // Checking if fragment/activity implements DialogEvents interface
        dialogEvents = try {
            parentFragment as DialogEvents
        }
        catch (e: TypeCastException) {
            try {
                context as DialogEvents
            }
            catch (e: ClassCastException) {
                throw ClassCastException("Activity $context must implement AppDialog.DialogEvents interface")
            }
        }
        catch (e: ClassCastException) {
            throw ClassCastException("Fragment $parentFragment must implement AppDialog.DialogEvents interface")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(TAG, "onCreateDialog called")

        val builder = AlertDialog.Builder(context!!)

        val arguments = arguments
        val dialogId: Int
        val messageString: String?
        var positiveStringId: Int
        var negativeStringId: Int

        if (arguments != null) {
            dialogId = arguments.getInt(DIALOG_ID)
            messageString = arguments.getString(DIALOG_MESSAGE)

            if (dialogId == 0 || messageString == null) {
                throw IllegalArgumentException("DIALOG_ID and/or DIALOG_MESSAGE not present in the bundle")
            }

            positiveStringId = arguments.getInt(DIALOG_POSITIVE_RID)
            if (positiveStringId == 0) {
                positiveStringId = R.string.ok
            }
            negativeStringId = arguments.getInt(DIALOG_NEGATIVE_RID)
            if (negativeStringId == 0) {
                negativeStringId = R.string.cancel
            }
        } else {
            throw IllegalArgumentException("Must pass DIALOG_ID and DIALOG_MESSAGE in the bundle")
        }

        return builder.setMessage(messageString)
            .setPositiveButton(positiveStringId) { dialogInterface, which ->
                dialogEvents?.onPositiveDialogResult(dialogId, arguments)
            }
            .setNegativeButton(negativeStringId) { dialogInterface, which ->
//                dialogEvents?.onNegativeDialogResult(dialogId, arguments)
            }
            .create()
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach: called")
        super.onDetach()
    }

    override fun onCancel(dialog: DialogInterface) {
        Log.d(TAG, "onCancel called")
        val dialogId = arguments!!.getInt(DIALOG_ID)
//        dialogEvents?.onDialogCancelled(dialogId)
    }

    override fun onDismiss(dialog: DialogInterface?) {
        Log.d(TAG, "onDismiss called")
        super.onDismiss(dialog)
    }
}