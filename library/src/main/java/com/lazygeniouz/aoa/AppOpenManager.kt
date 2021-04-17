package com.lazygeniouz.aoa

import android.app.Application
import androidx.annotation.NonNull
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.appopen.AppOpenAd
import com.lazygeniouz.aoa.base.BaseManager
import com.lazygeniouz.aoa.extensions.logDebug
import com.lazygeniouz.aoa.extensions.logError
import com.lazygeniouz.aoa.idelay.DelayType
import com.lazygeniouz.aoa.idelay.InitialDelay

/**
 * [AppOpenManager]: A class that handles all of the App Open Ad operations.
 * @param application Required to keep a track of App's state.
 * @param configs A Data class to pass required arguments.
 */
class AppOpenManager private constructor(
    @NonNull application: Application,
    @NonNull private val configs: Configs
) : BaseManager(application),
    LifecycleObserver {

    init {
        addObserver()
        unpackConfigs()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onStart() {
        if (initialDelay != InitialDelay.NONE) saveInitialDelayTime()
        showAdIfAvailable()
    }

    private fun addObserver() = ProcessLifecycleOwner.get().lifecycle.addObserver(this)

    private fun unpackConfigs() =
        apply {
            initialDelay = configs.initialDelay
            adRequest = configs.adRequest
            adUnitId = configs.adUnitId
            orientation = configs.orientation
        }

    // Let's fetch the Ad
    private fun fetchAd() {
        if (isAdAvailable()) return
        loadAd()
    }

    // Show the Ad if the conditions are met.
    private fun showAdIfAvailable() {
        if (!isShowingAd &&
            isAdAvailable() &&
            isInitialDelayOver()
        ) {
            // Show Ad Conditionally,
            // If the passed activity class equals to the current activity, then show the Ad.
            if (configs.showInActivity != null) {
                if (currentActivity?.javaClass?.simpleName == configs.showInActivity.simpleName) showAd()
                else logDebug("Current Activity does not match the Activity provided in Configs.showInActivity (${configs.showInActivity.simpleName})")
            } else showAd()
        } else {
            if (!isInitialDelayOver()) logDebug("The Initial Delay period is not over yet.")

            /**
             * If the next session happens after the delay period is over
             * & under 4 Hours, we can show a cached Ad.
             * However this will only work for DelayType.HOURS.
             */
            if (initialDelay.delayPeriodType != DelayType.DAYS ||
                initialDelay.delayPeriodType == DelayType.DAYS &&
                isInitialDelayOver()
            ) fetchAd()

        }
    }

    private fun showAd() = appOpenAd?.let { openAd ->
        openAd.fullScreenContentCallback = getFullScreenContentCallback()
        currentActivity?.let { activity -> openAd.show(activity) }
    }

    @Synchronized
    private fun loadAd() {
        // this is good for informing the user :)
        if (adUnitId == TEST_AD_UNIT_ID)
            logDebug("Current adUnitId is a Test Ad Unit Id, make sure to replace with yours in Production")

        AppOpenAd.load(
            getApplication(),
            adUnitId, adRequest,
            orientation, loadCallback
        )

        logDebug("A pre-cached Ad was not available, loading one.")
    }

    // Handling the visibility of App Open Ad
    private fun getFullScreenContentCallback(): FullScreenContentCallback {
        return object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                fetchAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                logError("Ad Failed To Show Full-Screen Content: ${adError?.message}")
            }

            override fun onAdShowedFullScreenContent() {
                logDebug("Ad Shown")
                isShowingAd = true
            }
        }
    }

    companion object {
        const val TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

        /**
         * [loadAppOpenAds]: A static function that handles all of the App Open Ad operations.
         * @param application Required to keep a track of App's state.
         * @param configs A Data class to pass required arguments.
         */
        @JvmStatic
        fun loadAppOpenAds(
            @NonNull application: Application,
            @NonNull configs: Configs
        ) {
            AppOpenManager(application, configs)
        }
    }
}