package org.tirasweel.todoapp.todo

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


import org.tirasweel.todoapp.MainActivityFragment.OnListFragmentInteractionListener

import kotlinx.android.synthetic.main.fragment_main.view.*
import org.tirasweel.todoapp.MyApplication
import org.tirasweel.todoapp.R
import java.text.SimpleDateFormat


class TodoRecyclerViewAdapter(
        private val mValues: List<TodoModel>,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<TodoRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_main, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.mTodoText.text = mValues[position].text

        val dateFormat = SimpleDateFormat("yyyy MMM dd")
        val deadline = mValues[position].deadline
        val deadlineString = if (deadline != null) dateFormat.format(deadline) else MyApplication.mAppContext.getString(R.string.msg_nodeadline)
        holder.mTodoDeadline.text = deadlineString

        holder.mTodoIcon.setColorFilter(
                ContextCompat.getColor(
                        MyApplication.mAppContext,
                        when (mValues[position].priority) {
                            1 -> R.color.colorPri1
                            2 -> R.color.colorPri2
                            3 -> R.color.colorPri3
                            4 -> R.color.colorPri4
                            5 -> R.color.colorPri5
                            else -> R.color.colorPriNone
                        }))
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        var mTodoText = mView.text_todo_text
        var mTodoDeadline = mView.text_todo_deadline
        var mTodoIcon = mView.image_todo_icon
    }
}
