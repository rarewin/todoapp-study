package org.tirasweel.todoapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.fragment_edit.*
import org.tirasweel.todoapp.model.TodoModel

class EditActivity : AppCompatActivity(),
        EditActivityFragment.OnFragmentInteractionListener,
        DatePickerDialogFragment.OnDateSetListener {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        setSupportActionBar(toolbar)

        val bundle = intent.extras
        val mode = bundle.getSerializable(IntentKey.TODO_APP_EDIT_MODE.name)

        supportFragmentManager.beginTransaction()
                .add(R.id.container_edit,
                        EditActivityFragment.newInstance(EditMode.EDIT_NEW),
                        "EDIT")
                .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        tintMenuIcon(menu.findItem(R.id.menu_edit_done))
        tintMenuIcon(menu.findItem(R.id.menu_edit_cancel))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.menu_edit_done -> {

                /*
                var todo = TodoModel(text_edit_task,
                        text_edit_deadline,
                        0,
                        false,
                        text_edit_memo
                )
                */

                true
            }
            R.id.menu_edit_cancel -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDatePickerLaunched() {
        DatePickerDialogFragment().show(supportFragmentManager, "datepickr")
    }

    override fun onDateSelected(dateString: String) {
        text_edit_deadline.setText(dateString)
    }

}
