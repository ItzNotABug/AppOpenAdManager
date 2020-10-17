package com.lazygeniouz.aoa

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.lazygeniouz.aoa.base.BaseManager
import com.lazygeniouz.aoa.extensions.logDebug
import com.lazygeniouz.aoa.extensions.logError
import com.lazygeniouz.aoa.idelay.DelayType
import com.lazygeniouz.aoa.idelay.InitialDelay

/**
 * @AppOpenManager = A class that handles all of the App Open Ad operations.
 *
 * Constructor arguments:
 * @param application Required to keep a track of App's state.
 * @param adUnitId Pass your created AdUnitId
 * @param initialDelay for Initial Delay
 *
 * @param adRequest = Pass a customised AdRequest if you have any.
 * @see AdRequest
 *
 * @param orientation Ad's Orientation, Can be PORTRAIT or LANDSCAPE (Default is Portrait)
 * @see AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
 * @see AppOpenAd.APP_OPEN_AD_ORIENTATION_LANDSCAPE
 *
 */
class AppOpenManager(
    application: Application,
    initialDelay: InitialDelay,
    override var adUnitId: String = TEST_AD_UNIT_ID,
    override var adRequest: AdRequest = AdRequest.Builder().build(),
    override var orientation: Int = AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
) : BaseManager(application),
    LifecycleObserver {

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        this.initialDelay = initialDelay
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStart() {
        if (initialDelay != InitialDelay.NONE) saveInitialDelayTime()
        showAdIfAvailable()
    }

    // Let's fetch the Ad
    private fun fetchAd() {
        if (isAdAvailable()) return
        loadAd()
        logDebug("A pre-cached Ad was not available, loading one.")
    }

    // Show the Ad if the conditions are met.
    private fun showAdIfAvailable() {
        if (!isShowingAd &&
            isAdAvailable() &&
            isInitialDelayOver()
        ) appOpenAd?.show(
            currentActivity,
            getFullScreenContentCallback()
        )
        else {
            if (!isInitialDelayOver()) logDebug("The Initial Delay period is not over yet.")

            /**
             *If the next session happens after the delay period is over
             * & under 4 Hours, we can show a cached Ad.
             * However the above will only work for DelayType.HOURS.
             */
            if (initialDelay.delayPeriodType != DelayType.DAYS ||
                initialDelay.delayPeriodType == DelayType.DAYS &&
                isInitialDelayOver()
            ) fetchAd()

        }
    }


    private fun loadAd() {
        // this is good for informing the user :)
        if (adUnitId == TEST_AD_UNIT_ID)
            logDebug(
                "Current adUnitId is a Test Ad Unit Id, make sure to replace with yours in Production "
            )

        loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAppOpenAdLoaded(loadedAd: AppOpenAd) {
                this@AppOpenManager.appOpenAd = loadedAd
                this@AppOpenManager.loadTime = getCurrentTime()
                logDebug("Ad Loaded")
            }

            override fun onAppOpenAdFailedToLoad(loadError: LoadAdError) {
                logError("Ad Failed To Load, Reason: ${loadError.responseInfo}")
            }
        }
        AppOpenAd.load(
            getApplication(),
            adUnitId, adRequest,
            orientation,
            loadCallback
        )
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
                isShowingAd = true
            }
        }
    }
}