package org.tirasweel.todoapp.model

import java.util.*

data class TodoModel(val text: String,
                     val deadline: Date,
                     val priority: Int,
                     val done: Boolean,
                     val memo: String)
