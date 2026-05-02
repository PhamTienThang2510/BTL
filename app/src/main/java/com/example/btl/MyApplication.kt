package com.example.btl

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.btl.Api.RetrofitClient

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        RetrofitClient.init(this)
    }
}
