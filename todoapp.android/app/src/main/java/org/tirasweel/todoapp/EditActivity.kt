package org.tirasweel.todoapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.fragment_edit.*
import org.tirasweel.todoapp.todo.TodoModel
import java.text.SimpleDateFormat

class EditActivity : AppCompatActivity(),
        EditActivityFragment.OnFragmentInteractionListener,
        DatePickerDialogFragment.OnDateSetListener {

    private var mEditFragment: EditActivityFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        setSupportActionBar(toolbar)

        val bundle = intent.extras
        val mode = bundle.getSerializable(IntentKey.TODO_APP_EDIT_MODE.name) as EditMode

        mEditFragment = EditActivityFragment.newInstance(mode)

        supportFragmentManager.beginTransaction()
                .add(R.id.container_edit,
                        mEditFragment,
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

        when (item.itemId) {

            R.id.menu_edit_done -> {

                if (text_edit_task.text.isNullOrBlank()) {
                    text_edit_task.error = getString(R.string.msg_empty_todo)
                    return false
                }

                val priority = when {
                    radio_priority_1.isChecked -> 1
                    radio_priority_2.isChecked -> 2
                    radio_priority_3.isChecked -> 3
                    radio_priority_4.isChecked -> 4
                    radio_priority_5.isChecked -> 5
                    else -> null
                }

                val date = try {
                    SimpleDateFormat.getDateInstance()
                            .parse(text_edit_deadline.text.toString())
                } catch (e: Exception) {
                    null
                }

                val todo = TodoModel(
                        text_edit_task.text.toString(),
                        date,
                        priority,
                        check_edit_done.isChecked,
                        text_edit_memo.text.toString())

                intent.putExtra(IntentKey.TODO_APP_EDIT_MODE_RESULT.name, todo)
                setResult(REQUEST_NEWTODO, intent)
                finish()
            }
            R.id.menu_edit_cancel -> {
                finish()
            }
            else -> {return super.onOptionsItemSelected(item)}
        }

        return true
    }

    override fun onDatePickerLaunched() {
        DatePickerDialogFragment().show(supportFragmentManager, "datepickr")
    }

    override fun onDateSelected(dateString: String) {
        text_edit_deadline.setText(dateString)
    }

}
