package org.tirasweel.todoapp

enum class IntentKey {
    TODO_APP_SETTING_HOST,
    TODO_APP_SETTING_API_TOKEN,
    TODO_APP_TODOCLIENT,
    TODO_APP_EDIT_MODE,
    TODO_APP_EDIT_MODE_RESULT
}

enum class EditMode {
    EDIT_NEW,
    EDIT_EDIT,
}


const val REQUEST_NEWTODO = 100
const val REQUEST_NEWSETTING = 200
