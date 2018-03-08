package org.tirasweel.todoapp.todo

import android.content.Context
import android.webkit.URLUtil

class TodoAppSetting(context: Context) {

    // constants for TodoAppSetting
    companion object {
        private const val TODO_APP_SETTING = "SettingForTodoApp"
    }

    enum class SettingKey {
        SETTING_SERVER_URI,
        SETTING_API_TOKEN
    }

    private val sharedPref = context.getSharedPreferences(TODO_APP_SETTING, Context.MODE_PRIVATE)

    fun setServerUri(uri: String): Boolean {

        // fail on invalid url (empty string is allowed)
        if (!URLUtil.isValidUrl(uri) && uri.isNotEmpty())
            return false

        // store uri
        val editor = sharedPref.edit()
        editor.putString(SettingKey.SETTING_SERVER_URI.toString(), uri)

        return editor.commit()
    }

    fun getServerUri() = sharedPref.getString(SettingKey.SETTING_SERVER_URI.toString(), "")

    fun setApiToken(token: String): Boolean {

        // validate token if required

        // store API token
        val editor = sharedPref.edit()
        editor.putString(SettingKey.SETTING_API_TOKEN.toString(), token)

        return editor.commit()
    }

    fun getApiToken() = sharedPref.getString(SettingKey.SETTING_API_TOKEN.toString(), "")

}
