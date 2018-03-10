package org.tirasweel.todoapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.webkit.URLUtil
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder

import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.tirasweel.todoapp.todo.TodoAppSetting
import org.tirasweel.todoapp.todo.TodoClient
import org.tirasweel.todoapp.todo.TodoModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private var mTodoAppSetting: TodoAppSetting? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mTodoAppSetting = TodoAppSetting(this)

        supportFragmentManager.beginTransaction()
                .add(R.id.container_main,
                        MainActivityFragment.newInstance(
                                mTodoAppSetting!!.getServerUri(),
                                mTodoAppSetting!!.getApiToken()),
                        "MAIN")
                .commit()

        fab.setOnClickListener {
            run {
                val intent = Intent(this, EditActivity::class.java).apply {
                    putExtra(IntentKey.TODO_APP_EDIT_MODE.name, EditMode.EDIT_NEW)
                }
                startActivityForResult(intent, REQUEST_NEWTODO)
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
                startActivityForResult(intent, REQUEST_NEWSETTING)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode) {

            REQUEST_NEWSETTING -> {

                val host = data?.getStringExtra(IntentKey.TODO_APP_SETTING_HOST.name)
                val apitoken = data?.getStringExtra(IntentKey.TODO_APP_SETTING_API_TOKEN.name)

                if (host == null && apitoken == null) {
                    makeToast(this, getString(R.string.setting_not_changed))
                } else {

                    mTodoAppSetting!!.setServerUri(host ?: "")
                    mTodoAppSetting!!.setApiToken(apitoken ?: "")

                    makeToast(this, getString(R.string.setting_changed))
                }
            }
            REQUEST_NEWTODO -> {

                val todo = data?.getSerializableExtra(IntentKey.TODO_APP_EDIT_MODE_RESULT.name) as TodoModel

                val host = mTodoAppSetting!!.getServerUri()
                val apitoken = mTodoAppSetting!!.getApiToken()

                // if json_host is invalidTODO_APP_SETTING
                if (!URLUtil.isValidUrl(host)) {
                    makeToast(MyApplication.mAppContext, getString(R.string.msg_invalid_url))
                }

                val client = OkHttpClient.Builder()
                        .addInterceptor(Interceptor { chain ->

                            val orig = chain.request()
                            val request = orig.newBuilder()
                                    .header("Authorization", "Token " + apitoken)
                                    .method(orig.method(), orig.body())
                                    .build()

                            chain.proceed(request)

                        }).build()

                val gson = GsonBuilder()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .create()

                val retrofit = Retrofit.Builder()
                        .baseUrl(host)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .client(client)
                        .build()

                val todoClient = retrofit.create(TodoClient::class.java)

                todoClient.addTodo(todo).enqueue(object: Callback<TodoModel> {
                    override fun onResponse(call: Call<TodoModel>?, response: Response<TodoModel>?) {
                        makeToast(MyApplication.mAppContext, getString(R.string.msg_new_todo_registerd))
                    }
                    override fun onFailure(call: Call<TodoModel>?, t: Throwable?) {
                        makeToast(MyApplication.mAppContext, getString(R.string.msg_fail_new_todo_registered))
                    }
                })
            }
        }
    }
}
