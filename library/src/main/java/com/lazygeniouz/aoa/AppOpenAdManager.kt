package com.lazygeniouz.aoa

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Handler
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.appopen.AppOpenAd
import com.lazygeniouz.aoa.AppOpenAdManager.Companion.get
import com.lazygeniouz.aoa.base.BaseAdManager
import com.lazygeniouz.aoa.configs.Configs
import com.lazygeniouz.aoa.extensions.logDebug
import com.lazygeniouz.aoa.idelay.DelayType
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
) : BaseAdManager(application, configs) {

    init {
        unpackConfigs()
        attachColdStartListener()
    }

    /**
     * Returns true if an **AppOpenAd** is available
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun isAdAvailable(): Boolean {
        return !isShowingAd && isAdAvailableInternal() && isInitialDelayOver()
    }

    /**
     * Can be used to manually remove the Ad if you cannot directly use the [Configs.showOnCondition]
     */
    fun clearAdInstance() {
        appOpenAdInstance = null
    }

    /**
     * Load Ad & optionally attach a listener.
     */
    fun loadAppOpenAd() {
        logDebug("AppOpenAdManager#loadAppOpenAd, Is InitialDelay Over: ${isInitialDelayOver()}")
        if (!isInitialDelayOver()) return
        fetchAd()
        isManuallyCalled = true
    }

    /**
     * Assign a listener to observe AppOpenAd events.
     * @param adListener An optional listener if you want to listen to the Ad's visibility events
     */
    fun setAppOpenAdListener(adListener: AppOpenAdListener) = apply {
        this.listener = adListener
    }

    /**
     * Assign a listener to observe if the AppOpenAd earned any money.
     * @param paidListener An optional listener if you want to observe Ad's monetary values.
     */
    fun setOnPaidEventListener(paidListener: OnPaidEventListener) = apply {
        this.adPaidEventListener = paidListener
        this.appOpenAdInstance?.onPaidEventListener = paidListener
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
     * Use an activity animation when the Ad is shown,
     * similar to [Activity.overridePendingTransition] or [Activity.overrideActivityTransition].
     */
    fun setAdTransition(enterAnim: Int, exitAnim: Int) {
        this.adEnterTransition = enterAnim to exitAnim
    }

    /**
     * Set a delay for showing the AppOpenAd.
     *
     * The [AppOpenAdListener.onAdWillShow] will be invoked
     * before [AppOpenAdListener.onAdShown] with a default delay of 1 second.
     *
     * @param useDelay Use a delay of 1 second for showing the Ad if true
     */
    fun showAdWithDelay(useDelay: Boolean) {
        // 1 second
        if (useDelay) this.adShowDelayPeriod = 1000
        else adShowDelayPeriod = 0
    }

    /**
     * Set a custom delay for showing the AppOpenAd.
     *
     * The [AppOpenAdListener.onAdWillShow] will be invoked
     * before [AppOpenAdListener.onAdShown] with a delay of provided time in milliseconds.
     *
     * @param timeInMillis Use a custom delay in milliseconds for delaying the Ad showing.
     */
    fun showAdWithDelay(timeInMillis: Long) {
        this.adShowDelayPeriod = timeInMillis
    }

    /**
     * Returns the [AppOpenAd] instance, can be **null** if it is not loaded yet.
     *
     * **Note:** If you manually use `getAppOpenAd().show(activity)`,
     * then the [setAdTransition] will not work, but you may also use
     * [Activity.overridePendingTransition] or [Activity.overrideActivityTransition].
     * @return [AppOpenAd]
     */
    fun getAppOpenAd(): AppOpenAd? {
        return appOpenAdInstance
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
     * @return [OnPaidEventListener]
     */
    fun getPaidEventListener(): OnPaidEventListener? {
        return this.adPaidEventListener
    }

    override fun onResume() {
        if (!isManuallyCalled) return
        else {
            isManuallyCalled = true
            showAdIfAvailable()
        }
    }

    private fun unpackConfigs() = apply {
        adUnitId = configs.adUnitId
        adRequest = configs.adRequest
        initialDelay = configs.initialDelay
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
                    val activity = currentActivity!!
                    if (activity.javaClass in configs.showInActivities) showAd()
                    else logDebug("Current Activity (${activity.javaClass.simpleName}) not included in the Activity List provided in Configs.showInActivities")
                } else logDebug("Current Activity is @null, strange! *_*")
            } else showAd()
        } else {
            if (!isInitialDelayOver()) logDebug("The Initial Delay period is not over yet.")
            else {

                /**
                 * If the next session happens after the delay period is over
                 * & under 4 Hours, we can show a cached Ad.
                 * However, this will only work for DelayType.HOURS.
                 */
                if (initialDelay.delayPeriodType != DelayType.DAYS ||
                    initialDelay.delayPeriodType == DelayType.DAYS &&
                    isInitialDelayOver()
                ) fetchAd()
            }
        }
    }

    private fun showAd() {
        appOpenAdInstance?.let { appOpenAd ->
            if (configs.showOnCondition?.invoke() == false) {
                logDebug("Configs.showOnCondition lambda returned false, Ad will not be shown")
                return@let
            }

            currentActivity?.let { activity ->
                if (adShowDelayPeriod > 0L) {
                    listener?.onAdWillShow()
                    Handler(activity.mainLooper).postDelayed({
                        appOpenAd.show(activity)
                        applyAdEnterTransition(activity)
                    }, this.adShowDelayPeriod)
                } else {
                    appOpenAd.show(activity)
                    applyAdEnterTransition(activity)
                }
            }
        }
    }

    private fun applyAdEnterTransition(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(
                Activity.OVERRIDE_TRANSITION_OPEN,
                adEnterTransition.first, adEnterTransition.second
            )
        } else {
            @Suppress("deprecation")
            activity.overridePendingTransition(adEnterTransition.first, adEnterTransition.second)
        }
    }

    private fun loadAd() {
        if (isLoading) return
        else isLoading = true

        // this is good for informing the user :)
        if (adUnitId == TEST_AD_UNIT_ID)
            logDebug("Current adUnitId is a Test Ad Unit Id, make sure to replace with yours in Production.")

        AppOpenAd.load(getApplication(), adUnitId, adRequest, loadCallback).also {
            logDebug("A pre-cached Ad was not available, loading one.")
        }
    }

    // Handling the visibility of the AppOpenAd
    override fun getFullScreenCallback(): FullScreenContentCallback {
        return object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                listener?.onAdDismissed()
                appOpenAdInstance = null
                isShowingAd = false
                fetchAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                listener?.onAdShowFailed(adError)
            }

            override fun onAdShowedFullScreenContent() {
                listener?.onAdShown()
                isShowingAd = true
            }
        }
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