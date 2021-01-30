@file:Suppress("unused")

package com.lazygeniouz.appopenads

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.lazygeniouz.aoa.AppOpenManager
import com.lazygeniouz.aoa.Configs
import com.lazygeniouz.aoa.idelay.InitialDelay
import com.lazygeniouz.appopenads.activities.SplashActivity

/**
 * Sample App's Main Application
 * Kotlin Version, registered in the Manifest.
 * */
class AppKt : Application() {

    override fun onCreate() {
        super.onCreate()

        MobileAds.initialize(this)
        AppOpenManager.loadAppOpenAds(
            this,
            Configs(InitialDelay.NONE, showInActivity = SplashActivity::class.java)
        )
    }
}