package org.tirasweel.todoapp

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import kotlinx.android.synthetic.main.fragment_edit.*

/**
 * A placeholder fragment containing a simple view.
 */
class EditActivityFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null

    private var mMode = EditMode.EDIT_EDIT

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        arguments?.let {
            mMode = it.getSerializable(ARG_editMode) as EditMode
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit, container, false)

    }

    override fun onAttach(context: Context?) {

        super.onAttach(context)

        if (context is OnFragmentInteractionListener) {
            mListener = context
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)

        // disable for new TODO
        check_edit_done.isEnabled = when (arguments?.getSerializable(ARG_editMode) as EditMode) {
            EditMode.EDIT_NEW -> false
            else -> true
        }

        imagebutton_todo_deadline.setOnClickListener {
            mListener?.onDatePickerLaunched()
        }
    }

    interface OnFragmentInteractionListener {
        fun onDatePickerLaunched()
    }

    companion object {

        private val ARG_editMode = IntentKey.TODO_APP_EDIT_MODE.name

        @JvmStatic
        fun newInstance(edit_mode: EditMode) =
                EditActivityFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(ARG_editMode, edit_mode)
                    }
                }
    }

}
