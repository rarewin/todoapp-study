package org.tirasweel.todoapp

import org.tirasweel.todoapp.model.TodoModel
import retrofit2.Call
import retrofit2.http.GET

interface TodoClient {

    @GET("/todos/?format=json")
    fun getJson(): Call<ArrayList<TodoModel>>

}