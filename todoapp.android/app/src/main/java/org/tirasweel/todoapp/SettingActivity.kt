package org.tirasweel.todoapp

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.fragment_setting.*

class SettingActivity : AppCompatActivity() {

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_setting, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.menu_done -> {

                val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
                val editor = sharedPref.edit()

                editor.putString(SettingKey.SETTING_SERVER_URI.toString(),
                        text_input_edit_jsonHost.text.toString())
                editor.putString(SettingKey.SETTING_API_TOKEN.toString(),
                        text_input_edit_apitoken.text.toString())

                editor.commit()

                makeToast(this, getString(R.string.setting_changed))
                finish()

                true
            }
            R.id.menu_cancel -> {
                makeToast(this, getString(R.string.setting_not_changed))
                finish()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        setSupportActionBar(toolbar)
    }

}
