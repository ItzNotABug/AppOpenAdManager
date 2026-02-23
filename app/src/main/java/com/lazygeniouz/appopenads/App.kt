@file:Suppress("unused")

package com.lazygeniouz.appopenads

import android.app.Application
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.lazygeniouz.aoa.AppOpenAdManager
import com.lazygeniouz.aoa.configs.Configs
import com.lazygeniouz.aoa.extensions.getAppOpenAdManager
import com.lazygeniouz.aoa.idelay.InitialDelay
import com.lazygeniouz.appopenads.activities.SplashActivity

/**
 * Sample App's Main Application
 * Kotlin Version, registered in the Manifest.
 */
class App : Application() {

    lateinit var adManager: AppOpenAdManager
        private set

    override fun onCreate() {
        super.onCreate()

        MobileAds.initialize(this)

        adManager = getAppOpenAdManager(
            Configs(
                initialDelay = InitialDelay.NONE,
                showInActivities = arrayListOf(SplashActivity::class.java),
            )
        ).apply {
            setOnPaidEventListener { adValue ->
                val revenue = adValue.valueMicros / 1_000_000.0
                Log.d(TAG, "Ad earned: $revenue ${adValue.currencyCode}")
            }

            loadAppOpenAd()
        }
    }

    companion object {
        private const val TAG = "AppOpenAds"
    }
}