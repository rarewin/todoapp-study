package org.tirasweel.todoapp

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.view.MenuItem
import android.widget.Toast

fun makeToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun tintMenuIcon(menuItem: MenuItem) {
    val icon = menuItem.icon
    icon.mutate()
    icon.setColorFilter(ContextCompat.getColor(MyApplication.mAppContext, R.color.colorMenuIcon),
            PorterDuff.Mode.SRC_IN)
}
