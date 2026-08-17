package com.lazygeniouz.aoa

import android.app.Activity
import android.app.Application
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdPreloader
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo
import com.lazygeniouz.aoa.AppOpenAdManager.Companion.get
import com.lazygeniouz.aoa.base.BaseAdManager
import com.lazygeniouz.aoa.configs.Configs
import com.lazygeniouz.aoa.extensions.logDebug
import com.lazygeniouz.aoa.listener.AppOpenAdListener

/**
 * [AppOpenAdManager]: A class that handles all the App Open Ad operations.
 * @param application Required to keep a track of App's state.
 * @param configs A Data class to pass required arguments.
 */
@Suppress("unused")
class AppOpenAdManager private constructor(
    application: Application,
    private val configs: Configs
) : BaseAdManager(application) {

    private val adRequest = configs.adRequest ?: AdRequest.Builder(configs.adUnitId).build()
    private val preloadId = "${adRequest.adUnitId}:${System.identityHashCode(this)}"
    private val preloadConfiguration = PreloadConfiguration(adRequest)
    private var appOpenAdInstance: AppOpenAd? = null
    private var pendingShow: Runnable? = null

    init {
        initialDelay = configs.initialDelay
        registerProcessObserver()
    }

    /**
     * Returns true if an **AppOpenAd** is available
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun isAdAvailable(): Boolean {
        return isPreloadingStarted &&
            !isShowingAd &&
            isInitialDelayOver() &&
            AppOpenAdPreloader.isAdAvailable(preloadId)
    }

    /**
     * Stops preloading and destroys any cached or pending Ad.
     */
    @Synchronized
    fun clearAdInstance() {
        isPreloadingStarted = false
        AppOpenAdPreloader.destroy(preloadId)
        runOnMainThread {
            cancelPendingShow()
            appOpenAdInstance?.destroy()
            appOpenAdInstance = null
            isShowingAd = false
        }
    }

    /**
     * Starts SDK-managed preloading. Repeated calls for the same Ad request are ignored.
     */
    @Synchronized
    fun loadAppOpenAd() {
        if (isPreloadingStarted) {
            logDebug("App Open Ad preloading is already active.")
            return
        }
        isPreloadingStarted = true
        if (adRequest.adUnitId == TEST_AD_UNIT_ID) {
            logDebug("Current adUnitId is a Test Ad Unit Id, make sure to replace with yours in Production.")
        }

        val started = AppOpenAdPreloader.start(
            preloadId,
            preloadConfiguration,
            preloadCallback,
        )
        logDebug(if (started) "Started App Open Ad preloading." else "App Open Ad preloading is already active.")
    }

    /**
     * Assign a listener to observe AppOpenAd events.
     * @param adListener An optional listener if you want to listen to the Ad's visibility events
     */
    fun setAppOpenAdListener(adListener: AppOpenAdListener?) = apply {
        this.listener = adListener
    }

    /**
     * Assign a listener to observe if the AppOpenAd earned any money.
     * @param paidListener An optional listener if you want to observe Ad's monetary values.
     */
    fun setOnPaidEventListener(paidListener: ((AdValue) -> Unit)?) = apply {
        this.adPaidEventListener = paidListener
    }

    /**
     * Sets a flag that controls if this app open ad object will be displayed in immersive mode.
     *
     * During show time, if this flag is on and immersive mode is supported,
     * `SYSTEM_UI_FLAG_IMMERSIVE_STICKY` & `SYSTEM_UI_FLAG_HIDE_NAVIGATION` will be turned on for the app open ad.
     */
    fun setImmersiveMode(isImmersiveMode: Boolean) {
        this.isImmersive = isImmersiveMode
        this.appOpenAdInstance?.setImmersiveMode(isImmersiveMode)
    }

    /**
     * Delays showing the AppOpenAd by 1 second. [AppOpenAdListener.onAdWillShow] is invoked after
     * the delay, immediately before the SDK show call.
     *
     * @param useDelay Use a delay of 1 second for showing the Ad if true
     */
    fun showAdWithDelay(useDelay: Boolean) {
        // 1 second
        if (useDelay) this.adShowDelayPeriod = 1000
        else adShowDelayPeriod = 0
    }

    /**
     * Sets a custom delay before showing the AppOpenAd. [AppOpenAdListener.onAdWillShow] is invoked
     * after the delay, immediately before the SDK show call.
     *
     * @param timeInMillis Use a custom delay in milliseconds for delaying the Ad showing.
     */
    fun showAdWithDelay(timeInMillis: Long) {
        this.adShowDelayPeriod = timeInMillis
    }

    /**
     * Returns the currently set Ad Listener, can be **null**.
     * @return [AppOpenAdListener]
     */
    fun getAdListener(): AppOpenAdListener? {
        return this.listener
    }

    /**
     * Returns the currently set Ad's **PaidEventListener**, can be **null**.
     * @return A callback receiving the Next-Gen SDK's [AdValue].
     */
    fun getPaidEventListener(): ((AdValue) -> Unit)? {
        return this.adPaidEventListener
    }

    override fun onAppResume() {
        if (isAdAvailable()) showAd()
    }

    private fun showAd(additionalShowCondition: (() -> Boolean)? = null) {
        if (isShowingAd) return

        val activity = currentActivity ?: return
        if (!isActivityReady(activity)) return
        val allowedActivities = configs.showInActivities
        if (allowedActivities != null && activity.javaClass !in allowedActivities) {
            logDebug("Current Activity (${activity.javaClass.simpleName}) not included in Configs.showInActivities")
            return
        }
        isShowingAd = true

        val showAction = Runnable {
            pendingShow = null
            if (!isPreloadingStarted || !isActivityReady(activity)) {
                isShowingAd = false
                return@Runnable
            }
            if (!canShowAd(additionalShowCondition)) {
                isShowingAd = false
                logDebug("An Ad show condition returned false, Ad will not be shown.")
                return@Runnable
            }

            val ad = AppOpenAdPreloader.pollAd(preloadId)
            if (ad == null) {
                isShowingAd = false
                return@Runnable
            }

            ad.setImmersiveMode(isImmersive)
            ad.adEventCallback = getAdEventCallback(ad)
            appOpenAdInstance = ad
            listener?.onAdWillShow()
            if (!isPreloadingStarted ||
                appOpenAdInstance !== ad ||
                !isActivityReady(activity) ||
                !canShowAd(additionalShowCondition)
            ) {
                releaseAd(ad)
                return@Runnable
            }

            try {
                ad.show(activity)
            } catch (error: RuntimeException) {
                releaseAd(ad)
                logDebug("App Open Ad failed to show: ${error.message}")
            }
        }

        if (adShowDelayPeriod > 0L) {
            pendingShow = showAction
            mainHandler.postDelayed(showAction, adShowDelayPeriod)
        } else {
            showAction.run()
        }
    }

    private fun getAdEventCallback(ad: AppOpenAd): AppOpenAdEventCallback {
        return object : AppOpenAdEventCallback {
            override fun onAdDismissedFullScreenContent() {
                runOnMainThread {
                    if (appOpenAdInstance !== ad) return@runOnMainThread
                    releaseAd(ad)
                    listener?.onAdDismissed()
                }
            }

            override fun onAdFailedToShowFullScreenContent(
                fullScreenContentError: FullScreenContentError
            ) {
                runOnMainThread {
                    if (appOpenAdInstance !== ad) return@runOnMainThread
                    releaseAd(ad)
                    listener?.onAdShowFailed(fullScreenContentError)
                }
            }

            override fun onAdShowedFullScreenContent() {
                runOnMainThread {
                    if (appOpenAdInstance === ad) listener?.onAdShown()
                }
            }

            override fun onAdPaid(value: AdValue) {
                runOnMainThread {
                    adPaidEventListener?.invoke(value)
                }
            }
        }
    }

    private val preloadCallback = object : PreloadCallback {
        override fun onAdPreloaded(preloadId: String, responseInfo: ResponseInfo) {
            mainHandler.post {
                if (!isPreloadingStarted || preloadId != this@AppOpenAdManager.preloadId) {
                    return@post
                }
                listener?.onAdLoaded()
                if (!coldStartHandled) {
                    coldStartHandled = true
                    if (isInitialDelayOver() && configs.showOnColdStart?.invoke() == true) {
                        showAd(configs.showOnColdStart)
                    }
                }
            }
        }

        override fun onAdFailedToPreload(preloadId: String, adError: LoadAdError) {
            mainHandler.post {
                if (!isPreloadingStarted || preloadId != this@AppOpenAdManager.preloadId) {
                    return@post
                }
                listener?.onAdFailedToLoad(adError)
            }
        }

        override fun onAdsExhausted(preloadId: String) {
            if (isPreloadingStarted && preloadId == this@AppOpenAdManager.preloadId) {
                logDebug("App Open Ad preload cache is refilling.")
            }
        }
    }

    private fun isActivityReady(activity: Activity): Boolean {
        return currentActivity === activity && !activity.isFinishing && !activity.isDestroyed
    }

    private fun canShowAd(additionalShowCondition: (() -> Boolean)?): Boolean {
        return configs.showOnCondition?.invoke() != false &&
            additionalShowCondition?.invoke() != false
    }

    private fun cancelPendingShow() {
        pendingShow?.let(mainHandler::removeCallbacks)
        pendingShow = null
    }

    private fun releaseAd(ad: AppOpenAd) {
        if (appOpenAdInstance !== ad) return
        ad.destroy()
        appOpenAdInstance = null
        isShowingAd = false
    }

    companion object {
        /**
         * The ad unit used for testing.
         */
        const val TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"

        /**
         * [get]: A static function that returns an instance of [AppOpenAdManager].
         * @param application To initialize the AppOpenAd & keep a track of App's state.
         * @param configs A Data class to pass required arguments.
         * @return A new manager. Create it once and retain it for the Application lifetime.
         */
        @JvmStatic
        @JvmOverloads
        fun get(
            application: Application,
            configs: Configs = Configs.DEFAULT,
        ): AppOpenAdManager {
            return AppOpenAdManager(application, configs)
        }
    }
}