package org.tirasweel.todoapp.todo

import android.os.AsyncTask
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class TodoConnect: AsyncTask<Void, Void, String>() {

    override fun doInBackground(vararg params: Void?): String? {

        val p_client = OkHttpClient()
        val p_mimetype = MediaType.parse("application/json; charset=utf-8")
        val p_body = RequestBody.create(p_mimetype, "{}")
        val p_request = Request.Builder().url("http://shielded-plateau-95764.herokuapp.com/todos/").post(p_body).build()
        val p_response = p_client.newCall(p_request).execute()

        // val todo = TodoModel("てすとさん", null, 0, false, "ほげほげ")
        // mTodoClient!!.addTodo(todo)

        return null

    }

}