package org.tirasweel.todoapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.fragment_setting.*

class SettingActivity : AppCompatActivity() {

    lateinit var mTodoAppSetting: TodoAppSetting

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_setting, menu)

        tintMenuIcon(menu.findItem(R.id.menu_setting_done))
        tintMenuIcon(menu.findItem(R.id.menu_setting_cancel))

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.menu_setting_done -> {

                mTodoAppSetting.setServerUri(text_input_edit_jsonHost.text.toString())
                mTodoAppSetting.setApiToken(text_input_edit_apitoken.text.toString())
                makeToast(this, getString(R.string.setting_changed))
                finish()
                true
            }
            R.id.menu_setting_cancel -> {
                makeToast(this, getString(R.string.setting_not_changed))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        mTodoAppSetting = TodoAppSetting(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        setSupportActionBar(toolbar)

    }

}
