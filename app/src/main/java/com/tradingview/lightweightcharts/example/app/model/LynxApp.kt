package com.tradingview.lightweightcharts.example.app.model

import android.app.Application
import android.content.Context
import com.jakewharton.threetenabp.AndroidThreeTen

class LynxApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        LynxApp.applicationContext = this
    }

    companion object {
        var applicationContext: Context? = null
    }

}