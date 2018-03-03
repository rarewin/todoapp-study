package org.tirasweel.todoapp

import org.tirasweel.todoapp.model.TodoModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TodoClient {

    @GET("/todos/?format=json")
    fun getTodos(): Call<ArrayList<TodoModel>>

    @POST("/todo/")
    fun addTodo(@Body todo: TodoModel): Call<TodoModel>

}
