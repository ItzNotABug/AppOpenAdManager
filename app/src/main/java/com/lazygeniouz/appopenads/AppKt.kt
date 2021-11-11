@file:Suppress("unused")

package com.lazygeniouz.appopenads

import android.app.Application
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.MobileAds
import com.lazygeniouz.aoa.configs.Configs
import com.lazygeniouz.aoa.extensions.getAppOpenAdManager
import com.lazygeniouz.aoa.idelay.InitialDelay
import com.lazygeniouz.aoa.listener.AppOpenAdListener
import com.lazygeniouz.appopenads.activities.SplashActivity

/**
 * Sample App's Main Application
 * Kotlin Version, registered in the Manifest.
 * */
class AppKt : Application() {

    override fun onCreate() {
        super.onCreate()

        MobileAds.initialize(this)
        getAppOpenAdManager(
            Configs(
                InitialDelay.NONE,
                showInActivities = arrayListOf(SplashActivity::class.java),
            )
        ).loadAppOpenAd(object : AppOpenAdListener() {
            override fun onAdLoaded() = println("AppOpenAdListener#onAdLoaded")

            override fun onAdWillShow() = println("AppOpenAdListener#onAdWillShow")

            override fun onAdShown() = println("AppOpenAdListener#onAdShown")

            override fun onAdDismissed() = println("AppOpenAdListener#onAdDismissed")

            override fun onAdShowFailed(error: AdError?) =
                println("AppOpenAdListener#onAdShowFailed(${error?.message})")

        })
    }
}