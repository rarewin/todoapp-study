package org.tirasweel.todoapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.fragment_setting.*


class SettingActivity : AppCompatActivity() {

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_setting, menu)

        // tint menu icons to white
        tintMenuIcon(menu.findItem(R.id.menu_setting_done))
        tintMenuIcon(menu.findItem(R.id.menu_setting_cancel))

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.menu_setting_done -> {

                intent.apply {
                    putExtra(IntentKey.TODO_APP_SETTING_HOST.name,
                            text_input_edit_jsonHost.text.toString())
                    putExtra(IntentKey.TODO_APP_SETTING_API_TOKEN.name,
                            text_input_edit_apitoken.text.toString())
                }

                setResult(REQUEST_NEWSETTING, intent)
                finish()
                true
            }
            R.id.menu_setting_cancel -> {
                setResult(REQUEST_NEWSETTING, intent)
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
