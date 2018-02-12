package org.tirasweel.todoapp

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        val todoAppSetting = TodoAppSetting(activity!!)

        text_input_edit_jsonHost.setText(todoAppSetting.getServerUri())
        text_input_edit_apitoken.setText(todoAppSetting.getApiToken())

        super.onActivityCreated(savedInstanceState)
    }

}
