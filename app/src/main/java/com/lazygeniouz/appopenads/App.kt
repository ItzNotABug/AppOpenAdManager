@file:Suppress("unused")

package com.lazygeniouz.appopenads

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.lazygeniouz.aoa.AppOpenManager

/** Sample App's Main Application */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        AppOpenManager(this)
    }
}