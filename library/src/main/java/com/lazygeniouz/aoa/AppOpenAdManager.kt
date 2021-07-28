package com.lazygeniouz.aoa

import android.app.Application
import android.os.Handler
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.appopen.AppOpenAd
import com.lazygeniouz.aoa.base.BaseManager
import com.lazygeniouz.aoa.configs.Configs
import com.lazygeniouz.aoa.extensions.logDebug
import com.lazygeniouz.aoa.extensions.logError
import com.lazygeniouz.aoa.idelay.DelayType
import com.lazygeniouz.aoa.idelay.InitialDelay
import com.lazygeniouz.aoa.listener.AppOpenAdListener

/**
 * [AppOpenAdManager]: A class that handles all of the App Open Ad operations.
 * @param application Required to keep a track of App's state.
 * @param configs A Data class to pass required arguments.
 * @param listener An optional listener if you want to listen to the Ad's visibility events
 */
class AppOpenAdManager private constructor(
    @NonNull application: Application,
    @NonNull private val configs: Configs,
    @Nullable private val listener: AppOpenAdListener?
) : BaseManager(application, configs),
    LifecycleObserver {

    init {
        addObserver()
        unpackConfigs()
        attachColdStartListener()
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

    private fun attachColdStartListener() = apply { coldShowListener = { showAd() } }

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
            if (configs.showInActivities != null) {
                if (currentActivity != null) {
                    if (currentActivity!!.javaClass in configs.showInActivities) showAd()
                    else logDebug("Current Activity not included in the Activity List provided in Configs.showInActivities")
                } else logDebug("Current Activity is @null, strange! *_*")
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
        currentActivity?.let { activity ->
            if (listener != null) {
                listener.onAdWillShow().also {
                    Handler(activity.mainLooper)
                        .postDelayed({ openAd.show(activity) }, 750)
                }
            } else openAd.show(activity)
        }
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
    private fun getFullScreenContentCallback() =
        object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                listener?.onAdDismissed()
                appOpenAd = null
                isShowingAd = false
                fetchAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                listener?.onAdShowFailed(adError)
                logError("AppOpenAd failed To Show Full-Screen Content: ${adError?.message}")
            }

            override fun onAdShowedFullScreenContent() {
                listener?.onAdShown()
                logDebug("AppOpenAd Shown")
                isShowingAd = true
            }
        }

    companion object {
        const val TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

        /**
         * [loadAppOpenAds]: A static function that handles all of the App Open Ad operations.
         * @param application Required to keep a track of App's state.
         * @param configs A Data class to pass required arguments.
         * @param listener An optional listener if you want to listen to the Ad's visibility events
         */
        @JvmStatic
        @JvmOverloads
        fun loadAppOpenAds(
            @NonNull application: Application,
            @NonNull configs: Configs,
            @Nullable listener: AppOpenAdListener? = null
        ) = AppOpenAdManager(application, configs, listener)
    }
}