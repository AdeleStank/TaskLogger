package adeles.kotlinpractice.tasklogger

import android.content.res.Configuration
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.content_main.*

private const val TAG = "MainActivity"
private const val DIALOG_ID_CANCEL_EDIT = 1

class MainActivity : AppCompatActivity(), AddEditTaskFragment.OnSaveClicked,
    MainFragment.OnTaskEdit, AppDialog.DialogEvents{

    //checking for landscape mode
    private var mTwoPane = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        mTwoPane = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        var fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
        if (fragment != null){
            showEditPane()
        } else{
            hideEditPane()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
     when (item.itemId) {
            R.id.mnu_add_task -> taskEditRequest(null)
        //    R.id.mnu_settings -> true
         android.R.id.home -> {
             Log.d(TAG, "onOptionsItemSelected: home button pressed")
             val fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
//                removeEditPane(fragment)
             if ((fragment is AddEditTaskFragment) && fragment.hasUnsavedData()) {
                 showConfirmationDialog(DIALOG_ID_CANCEL_EDIT,
                     getString(R.string.cancelEditDiag_message),
                     R.string.cancelEditDiag_positive_caption,
                     R.string.cancelEditDiag_negative_caption)
             } else {
                 removeEditPane(fragment)
             }
         }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun taskEditRequest(task: Task?) {
        Log.d(TAG, "taskEditRequest: starts")

        // Create a new fragment to edit the task
//        val newFragment = AddEditFragment.newInstance(task)
//        supportFragmentManager.beginTransaction()
//                .replace(R.id.task_details_container, newFragment)
//                .commit()

        val newFragment = AddEditTaskFragment.newInstance(task)
        supportFragmentManager.beginTransaction().replace(R.id.task_details_container, newFragment).commit()

        showEditPane()

        Log.d(TAG, "Exiting taskEditRequest")
    }

    override fun onSaveClicked() {
        Log.d(TAG, "onSaveClicked: started")
        val fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
        removeEditPane(fragment)
        Log.d(TAG, "onSaveClicked: finished")
    }

    private fun removeEditPane(fragment: Fragment? = null) {
        Log.d(TAG, "removeEditPane called")
        if(fragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
            //removeFragment(fragment)
        }
        // Make edit fragment invisible and main visible
        hideEditPane()
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun showEditPane(){
        task_details_container.visibility = View.VISIBLE
        mainFragment.view?.visibility = if (mTwoPane) View.VISIBLE else View.GONE
    }

    private fun hideEditPane(){
        task_details_container.visibility = if(mTwoPane) View.INVISIBLE else View.GONE
        mainFragment.view?.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
        if (fragment == null) {
            super.onBackPressed()
        } else {
            if ((fragment is AddEditTaskFragment) && fragment.hasUnsavedData()) {
                showConfirmationDialog(DIALOG_ID_CANCEL_EDIT,
                    getString(R.string.cancelEditDiag_message),
                    R.string.cancelEditDiag_positive_caption,
                    R.string.cancelEditDiag_negative_caption)
            } else {
                removeEditPane(fragment)
            }
        }
    }

    override fun onTaskEdit(task: Task) {
        taskEditRequest(task)
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG, "onPositiveDialogResult: called with dialogId $dialogId")
        if (dialogId == DIALOG_ID_CANCEL_EDIT) {
            val fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
            removeEditPane(fragment)
        }
    }
}