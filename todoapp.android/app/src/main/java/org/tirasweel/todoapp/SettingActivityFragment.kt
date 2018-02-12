package org.tirasweel.todoapp

import android.content.Context
import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_setting.*

/**
 * A placeholder fragment containing a simple view.
 */
class SettingActivityFragment : Fragment() {

    var mTodoServerUri: String? = ""
    var mTodoServerApiToken: String? = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val sharedPref = activity!!.getPreferences(Context.MODE_PRIVATE)

        mTodoServerUri = sharedPref.getString(SettingKey.SETTING_SERVER_URI.toString(), "")
        mTodoServerApiToken = sharedPref.getString(SettingKey.SETTING_API_TOKEN.toString(), "")

        return inflater.inflate(R.layout.fragment_setting, container, false)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        text_input_edit_jsonHost.setText(mTodoServerUri)
        text_input_edit_apitoken.setText(mTodoServerApiToken)

        super.onActivityCreated(savedInstanceState)
    }

}
