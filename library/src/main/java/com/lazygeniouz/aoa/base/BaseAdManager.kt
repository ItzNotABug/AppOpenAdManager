package com.lazygeniouz.aoa.base

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.appopen.AppOpenAd
import com.lazygeniouz.aoa.AppOpenAdManager
import com.lazygeniouz.aoa.AppOpenAdManager.Companion.TEST_AD_UNIT_ID
import com.lazygeniouz.aoa.configs.Configs
import com.lazygeniouz.aoa.idelay.InitialDelay
import com.lazygeniouz.aoa.listener.AppOpenAdListener
import java.time.Instant
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * A Base class that extends [BaseObserver] to handle Activity Lifecycle.
 *
 * This class is created to declare a few helper methods and variables,
 * so that our main usable class does not have a lot of methods and variables.
 * @see com.lazygeniouz.aoa.AppOpenAdManager
 */
abstract class BaseAdManager(
    private val application: Application,
    private val configs: Configs
) : BaseObserver(application),
    LifecycleEventObserver {

    init {
        initPrefs()
        addProcessObserver()
    }

    /**
     * Why is this required?
     * Example: When the user initially starts the app, it will hit **ON_RESUME**,
     * and suppose the app then asks for some sort of Runtime Permission then after that,
     * the app will hit the **ON_RESUME** again, which will trigger the lifecycle callback
     * & that will **load** / **show** the Ad which is not what a user would like.
     *
     * So we limit the ad loading in the **ON_RESUME** callback until the user has manually,
     * at-least once, has called the [AppOpenAdManager.loadAppOpenAd] method.
     */
    protected var isManuallyCalled = false
    private lateinit var sharedPreferences: SharedPreferences

    // Callbacks
    protected var listener: AppOpenAdListener? = null
    protected var adPaidEventListener: OnPaidEventListener? = null

    protected var isImmersive: Boolean = false
    protected var adShowDelayPeriod: Long = 1000
    protected var coldShowListener: (() -> Unit)? = null

    protected var isLoading = false
    protected var isShowingAd = false
    protected var coldStartShown = false

    @Volatile
    protected var appOpenAdInstance: AppOpenAd? = null
    protected val loadCallback: AppOpenAd.AppOpenAdLoadCallback =
        object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(loadedAd: AppOpenAd) {
                loadTime = getCurrentTime()

                // set listeners and other options
                loadedAd.setImmersiveMode(isImmersive)
                loadedAd.onPaidEventListener = adPaidEventListener
                loadedAd.fullScreenContentCallback = getFullScreenCallback()
                appOpenAdInstance = loadedAd

                if (!coldStartShown
                    && isInitialDelayOver()
                    && configs.showOnColdStart?.invoke() == true
                ) {
                    coldShowListener?.invoke()
                    coldStartShown = true
                }

                listener?.onAdLoaded()
                isLoading = false
            }

            override fun onAdFailedToLoad(loadError: LoadAdError) {
                isLoading = false
                listener?.onAdFailedToLoad(loadError)
            }
        }

    protected var adUnitId: String = TEST_AD_UNIT_ID
    protected var initialDelay: InitialDelay = InitialDelay()
    protected var adRequest: AdRequest = AdRequest.Builder().build()

    protected fun getApplication(): Application = application

    // SharedPreferences keep a better track
    protected var loadTime: Long
        get() = sharedPreferences.getLong("lastTime", 0)
        set(value) = sharedPreferences.edit()
            .putLong("lastTime", value)
            .apply()

    private fun addProcessObserver() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this@BaseAdManager)
    }

    private fun initPrefs() {
        val settingsName = "appOpenAdsManager"
        // A few cases had multi-threading in use, so lazy getValue always returned null.
        sharedPreferences = application.getSharedPreferences(settingsName, Context.MODE_PRIVATE)
    }

    /**
     * `onResume` will be fired when the lifecycle event for **ON_RESUME** is triggered.
     */
    abstract fun onResume()

    abstract fun getFullScreenCallback(): FullScreenContentCallback

    /**
     * State observer callback to handle relevant operations.
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_RESUME) onResume()
        if (event == Lifecycle.Event.ON_START && initialDelay != InitialDelay.NONE) saveInitialDelayTime()
    }

    /**
     * There's a platform issue on Android 8/8.1 when using `Date().time`
     *
     * Details: [https://itznotabug.dev/blog/short_standard_assertion_error/]
     */
    protected fun getCurrentTime(): Long {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) Date().time
        else Instant.now().toEpochMilli()
    }

    private fun saveInitialDelayTime() {
        val initialDelayKey = "savedDelay"
        val savedDelay = sharedPreferences.getLong(initialDelayKey, 0L)
        if (savedDelay != 0L) return
        sharedPreferences.edit()
            .putLong(initialDelayKey, getCurrentTime())
            .apply()
    }

    /**
     * The documentation says that the Ads are only cached for 4 Hours.
     *
     * [https://developers.google.com/admob/android/app-open#expiration]
     */
    private fun notLongerThanFourHours(): Boolean {
        val dateDifference = getCurrentTime() - loadTime
        val fourHours: Long = TimeUnit.HOURS.toMillis(4)
        return dateDifference < fourHours
    }

    /**
     * Returns `true` if an Ad is available & valid, `false` otherwise.
     */
    protected fun isAdAvailableInternal(): Boolean {
        return (appOpenAdInstance != null) && notLongerThanFourHours()
    }

    /**
     * Difference = Current Time `minus` Saved Time,
     *
     * therefore difference >= duration.getTime()
     */
    protected fun isInitialDelayOver(): Boolean {
        val savedDelay = sharedPreferences.getLong("savedDelay", 0L)
        // Zero means first load & it shouldn't be marked as ad available,
        // so we flag that as well as a false value boolean for this specific condition.
        return if (initialDelay == InitialDelay.NONE) (getCurrentTime() - savedDelay) >= initialDelay.getTime()
        else savedDelay != 0L && (getCurrentTime() - savedDelay) >= initialDelay.getTime()
    }
}