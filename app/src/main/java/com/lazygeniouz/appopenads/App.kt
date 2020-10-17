@file:Suppress("unused")

package com.lazygeniouz.appopenads

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.lazygeniouz.aoa.AppOpenManager
import com.lazygeniouz.aoa.idelay.DelayType
import com.lazygeniouz.aoa.idelay.InitialDelay

/** Sample App's Main Application */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)

        //InitialDelay to ZERO for testing
        AppOpenManager(this, InitialDelay(1, DelayType.HOUR))
    }
}