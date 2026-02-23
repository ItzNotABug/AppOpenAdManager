@file:Suppress("unused")

package com.lazygeniouz.appopenads

import android.app.Application
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.lazygeniouz.aoa.AppOpenAdManager
import com.lazygeniouz.aoa.configs.Configs
import com.lazygeniouz.aoa.extensions.getAppOpenAdManager
import com.lazygeniouz.aoa.idelay.InitialDelay
import com.lazygeniouz.aoa.listener.AppOpenAdListener

class App : Application() {

    lateinit var adManager: AppOpenAdManager
        private set

    var eventListener: AppOpenAdListener? = null

    private val mainListener = object : AppOpenAdListener() {
        override fun onAdLoaded() {
            eventListener?.onAdLoaded()
        }

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
            eventListener?.onAdFailedToLoad(loadAdError)
        }

        override fun onAdWillShow() {
            eventListener?.onAdWillShow()
        }

        override fun onAdShown() {
            eventListener?.onAdShown()
        }

        override fun onAdDismissed() {
            eventListener?.onAdDismissed()
        }

        override fun onAdShowFailed(error: AdError?) {
            eventListener?.onAdShowFailed(error)
        }
    }

    override fun onCreate() {
        super.onCreate()

        MobileAds.initialize(this)

        adManager = getAppOpenAdManager(
            Configs(initialDelay = InitialDelay.NONE)
        ).apply {
            setAppOpenAdListener(mainListener)
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