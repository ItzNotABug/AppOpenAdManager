@file:Suppress("unused")

package com.lazygeniouz.appopenads

import android.app.Application
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.lazygeniouz.aoa.AppOpenAdManager
import com.lazygeniouz.aoa.configs.Configs
import com.lazygeniouz.aoa.extensions.getAppOpenAdManager
import com.lazygeniouz.aoa.idelay.InitialDelay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {

    lateinit var adManager: AppOpenAdManager
        private set

    override fun onCreate() {
        super.onCreate()

        adManager = getAppOpenAdManager(
            Configs(initialDelay = InitialDelay.NONE)
        ).apply {
            setOnPaidEventListener { adValue ->
                val revenue = adValue.valueMicros / 1_000_000.0
                Log.d(TAG, "Ad earned: $revenue ${adValue.currencyCode}")
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(
                this@App,
                InitializationConfig
                    .Builder(TEST_APP_ID)
                    .build(),
            )
            adManager.loadAppOpenAd()
        }
    }

    companion object {
        private const val TAG = "AppOpenAds"
        private const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    }
}