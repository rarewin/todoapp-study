package org.tirasweel.todoapp

import android.app.DatePickerDialog
import android.app.Dialog
import android.support.v4.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import java.util.*

class DatePickerDialogFragment: DialogFragment(),
        DatePickerDialog.OnDateSetListener {

    private var mListener: OnDateSetListener? = null

    interface OnDateSetListener {
        fun onDateSelected(dateString: String)
    }

    override fun onAttach(context: Context?) {

        super.onAttach(context)

        if (context is OnDateSetListener) {
            mListener = context
        }

    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(activity, this, year, month, day)

    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {

        val dateString = getDateString(year, month, dayOfMonth)

        mListener?.onDateSelected(dateString)
        fragmentManager?.beginTransaction()?.remove(this)?.commit()

    }

    private fun getDateString(year: Int, month: Int, day: Int)
            = "%04d/%02d/%02d".format(year, month + 1, day)  // date-picker returns month starting from zero...

}
