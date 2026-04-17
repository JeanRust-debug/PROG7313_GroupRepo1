package com.example.prog7313

import android.app.Application
import com.example.prog7313.data.AppDatabase

class PROG7313Application : Application() {

    val database by lazy {
        AppDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
    }
}