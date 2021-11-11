package com.lazygeniouz.aoa

import android.app.Application
import android.os.Handler
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.lifecycle.*
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.appopen.AppOpenAd
import com.lazygeniouz.aoa.base.BaseAdManager
import com.lazygeniouz.aoa.configs.Configs
import com.lazygeniouz.aoa.extensions.logDebug
import com.lazygeniouz.aoa.extensions.logError
import com.lazygeniouz.aoa.idelay.DelayType
import com.lazygeniouz.aoa.listener.AppOpenAdListener

/**
 * [AppOpenAdManager]: A class that handles all of the App Open Ad operations.
 * @param application Required to keep a track of App's state.
 * @param configs A Data class to pass required arguments.
 */
class AppOpenAdManager private constructor(
    @NonNull application: Application,
    @NonNull private val configs: Configs
) : BaseAdManager(application, configs) {

    init {
        unpackConfigs()
        attachColdStartListener()
    }

    /**
     * Returns true if an **AppOpenAd** is available
     */
    override fun isAdAvailable(): Boolean {
        return !isShowingAd && super.isAdAvailable() && isInitialDelayOver()
    }

    /**
     * Load Ad & optionally attach a listener.
     */
    fun loadAppOpenAd() { fetchAd() }

    /**
     * Assign a listener tp observe AppOpenAd events.
     * @param adListener An optional listener if you want to listen to the Ad's visibility events
     */
    fun setAppOpenAdListener(@NonNull adListener: AppOpenAdListener) {
        if (listener == null) this.listener = adListener
    }

    /**
     * Returns the [AppOpenAd] instance, can be **null** if it is not loaded yet.
     * @return [AppOpenAd]
     */
    @Nullable
    fun getAppOpenAd(): AppOpenAd? {
        return appOpenAdInstance
    }

    /**
     * Returns the currently set Ad Listener, can be **null**.
     * @return [AppOpenAdListener]
     */
    @Nullable
    fun getAdListener(): AppOpenAdListener? {
        return this.listener
    }

    override fun onResume() = showAdIfAvailable()

    private fun unpackConfigs() = apply {
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
        if (isAdAvailable()) {
            // Show Ad Conditionally,
            // If the passed activity class equals to the current activity, then show the Ad.
            if (configs.showInActivities != null) {
                if (currentActivity != null) {
                    if (currentActivity!!.javaClass in configs.showInActivities) showAd()
                    else logDebug("Current Activity (${currentActivity!!.javaClass.simpleName}) not included in the Activity List provided in Configs.showInActivities")
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

    private fun showAd() = appOpenAdInstance?.let { openAd ->
        if (configs.showOnCondition?.invoke() == false) {
            logDebug("Configs.showOnCondition lambda returned false, Ad will not be shown")
            return@let
        }

        openAd.fullScreenContentCallback = getFullScreenContentCallback()
        currentActivity?.let { activity ->
            if (listener != null) {
                listener?.onAdWillShow().also {
                    Handler(activity.mainLooper)
                        .postDelayed({ openAd.show(activity) }, 1000L)
                }
            } else openAd.show(activity)
        }
    }

    @Synchronized
    private fun loadAd() {
        // this is good for informing the user :)
        if (adUnitId == TEST_AD_UNIT_ID)
            logDebug("Current adUnitId is a Test Ad Unit Id, make sure to replace with yours in Production.")

        AppOpenAd.load(
            getApplication(),
            adUnitId, adRequest,
            orientation, loadCallback
        ).also { logDebug("A pre-cached Ad was not available, loading one.") }
    }

    // Handling the visibility of App Open Ad
    private fun getFullScreenContentCallback() =
        object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                listener?.onAdDismissed()
                appOpenAdInstance = null
                isShowingAd = false
                fetchAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                listener?.onAdShowFailed(adError)
                logError("AppOpenAd failed To show Full-Screen Content: ${adError?.message}")
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
         * [get]: A static function that returns an instance of [AppOpenAdManager].
         * @param application To initialize the AppOpenAd & keep a track of App's state.
         * @param configs A Data class to pass required arguments.
         */
        @JvmStatic
        @JvmOverloads
        fun get(
            @NonNull application: Application,
            configs: Configs = Configs.DEFAULT,
        ): AppOpenAdManager {
            return AppOpenAdManager(application, configs)
        }
    }
}