package org.tirasweel.todoapp.todo

import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TodoClient {

    @GET("/todos/")
    fun getTodos(): Call<ArrayList<TodoModel>>

    @POST("/todos/")
    fun addTodo(@Body todo: TodoModel): Call<TodoModel>

}
