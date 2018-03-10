package org.tirasweel.todoapp.todo

import java.io.Serializable
import java.util.*

data class TodoModel(val text: String,
                     val deadline: String?,
                     val priority: Int?,
                     val done: Boolean,
                     val memo: String?): Serializable
