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
        check_edit_done.isEnabled = false

        imagebutton_todo_deadline.setOnClickListener {
            mListener?.onDatePickerLaunched()
        }
    }

    // override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    //
    //     if (item!!.itemId == R.id.menu_edit_done) {
    //
    //         if (text_edit_task.text.isNullOrEmpty()) {
    //             text_edit_task.error = getString(R.string.msg_empty_todo)
    //             return false
    //         }
    //
    //         val todo = TodoModel(
    //                 text_edit_task.text.toString(),
    //                 //SimpleDateFormat.getDateInstance()
    //                 //        .parse(text_edit_deadline.text.toString()),
    //                 null,
    //                 null, false, "")
    //
    //         return true
    //
    //     }
    //
    //     return super.onOptionsItemSelected(item)
    // }

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
