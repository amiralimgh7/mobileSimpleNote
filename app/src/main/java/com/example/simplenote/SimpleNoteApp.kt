package com.example.simplenote

import android.app.Application

class SimpleNoteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppGraph.init(this)
    }
}
