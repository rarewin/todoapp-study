package org.tirasweel.todoapp

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.*
import org.tirasweel.todoapp.todo.TodoClient

import org.tirasweel.todoapp.todo.TodoModel
import org.tirasweel.todoapp.todo.TodoRecyclerViewAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivityFragment : Fragment() {

    private var mListener: OnListFragmentInteractionListener? = null
    private var mTodoClient: TodoClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val host = arguments!!.getString(ARG_host)
        val apitoken = arguments!!.getString(ARG_apitoken)

        // if json_host is invalidTODO_APP_SETTING
        if (!URLUtil.isValidUrl(host)) {
            makeToast(MyApplication.mAppContext, getString(R.string.msg_invalid_url))
            return
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

        mTodoClient = retrofit.create(TodoClient::class.java)

    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_main_list, container, false)

        val call = mTodoClient!!.getTodos()


        // val p_client = OkHttpClient()
        // val p_mimetype = MediaType.parse("application/json; charset=utf-8")
        // val p_body = RequestBody.create(p_mimetype, "{}")
        // val p_request = Request.Builder().url("http://shielded-plateau-95764.herokuapp.com/todos/").post(p_body).build()
        // val p_response = p_client.newCall(p_request).execute()

        // val todo = TodoModel("てすとさん", null, 0, false, "ほげほげ")
        // mTodoClient!!.addTodo(todo).execute()

        // val test = TodoConnect()
        // test.execute(null)

        call.enqueue(object : Callback<ArrayList<TodoModel>> {

            override fun onResponse(call: Call<ArrayList<TodoModel>>?,
                                    response: Response<ArrayList<TodoModel>>?) {

                val todoResponse = response!!.body()

                // Set the adapter
                if (view is RecyclerView) {

                    view.apply {
                        setHasFixedSize(true)
                        layoutManager = LinearLayoutManager(context)
                        adapter = TodoRecyclerViewAdapter(
                                todoResponse!!,
                                mListener
                        )
                    }
                }
            }

            override fun onFailure(call: Call<ArrayList<TodoModel>>?, t: Throwable?) {

                // Set the adapter
                if (view is RecyclerView) {

                    view.apply {
                        setHasFixedSize(true)
                        layoutManager = LinearLayoutManager(context)
                        adapter = TodoRecyclerViewAdapter(
                                arrayListOf(),
                                mListener
                        )
                    }
                }

            }

        })

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // if (context is OnListFragmentInteractionListener) {
        //     listener = context
        // } else {
        //     throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        // }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnListFragmentInteractionListener {

        fun onListFragmentInteraction(item: TodoModel)

    }

    companion object {

        private val ARG_host = IntentKey.TODO_APP_SETTING_HOST.name
        private val ARG_apitoken = IntentKey.TODO_APP_SETTING_API_TOKEN.name

        @JvmStatic
        fun newInstance(host: String, apitoken: String) =
                MainActivityFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_host, host)
                        putString(ARG_apitoken, apitoken)
                    }
                }
    }
}
