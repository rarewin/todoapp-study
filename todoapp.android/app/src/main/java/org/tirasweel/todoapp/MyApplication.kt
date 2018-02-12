package org.tirasweel.todoapp

import android.app.Application
import android.content.Context

class MyApplication: Application() {

    override fun onCreate() {
        mAppContext = this
        super.onCreate()
    }

    companion object {
        lateinit var mAppContext: Context
    }

}