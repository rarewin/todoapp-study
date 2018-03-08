package org.tirasweel.todoapp

import android.support.v4.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_setting.*
import org.tirasweel.todoapp.todo.TodoAppSetting

/**
 * A placeholder fragment containing a simple view.
 */
class SettingActivityFragment : Fragment(), TextWatcher {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        val todoAppSetting = TodoAppSetting(activity!!)

        text_input_edit_jsonHost.setText(todoAppSetting.getServerUri())
        text_input_edit_apitoken.setText(todoAppSetting.getApiToken())

        // register listeners
        // refer to <https://stackoverflow.com/questions/2763022/android-how-can-i-validate-edittext-input>
        // text_input_edit_jsonHost.addTextChangedListener(this)
        // text_input_edit_apitoken.addTextChangedListener(this)

        super.onActivityCreated(savedInstanceState)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun afterTextChanged(s: Editable?) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

}
