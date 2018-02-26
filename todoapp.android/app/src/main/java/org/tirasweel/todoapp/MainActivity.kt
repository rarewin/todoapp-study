package org.tirasweel.todoapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var mTodoAppSetting: TodoAppSetting? = null
    // private var mRecyclerView: RecyclerView? = null
    // private var mAdapter: RecyclerView.Adapter? = null
    // private var mLayoutManager: RecyclerView.LayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mTodoAppSetting = TodoAppSetting(this)

        supportFragmentManager.beginTransaction()
                .add(R.id.container_main,
                        MainActivityFragment.newInstance(1),
                        "MAIN")
                .commit()

        fab.setOnClickListener {
            run {
                val intent = Intent(this, EditActivity::class.java)
                startActivity(intent)
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_main, menu)
        tintMenuIcon(menu.findItem(R.id.menu_main_setting))

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.menu_main_setting -> {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
