package com.lazygeniouz.aoa.base

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.Nullable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.lazygeniouz.aoa.AppOpenAdManager.Companion.TEST_AD_UNIT_ID
import com.lazygeniouz.aoa.configs.Configs
import com.lazygeniouz.aoa.extensions.logDebug
import com.lazygeniouz.aoa.extensions.logError
import com.lazygeniouz.aoa.idelay.InitialDelay
import com.lazygeniouz.aoa.listener.AppOpenAdListener
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A Base class that extends
 * @see BaseObserver to handle Activity Lifecycle,
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

    private var isFirst = true
    private var isLifecycleAttached = false
    private val processLifecycle by lazy { ProcessLifecycleOwner.get().lifecycle }

    private val sharedPreferences by lazy {
        application.getSharedPreferences("appOpenAdsManager", Context.MODE_PRIVATE)
    }

    // Callbacks
    @Nullable
    protected var listener: AppOpenAdListener? = null

    @Nullable
    protected var coldShowListener: (() -> Unit)? = null

    protected var isShowingAd = false
    protected var coldStartShown = false

    @Volatile
    protected var appOpenAdInstance: AppOpenAd? = null
    protected val loadCallback: AppOpenAd.AppOpenAdLoadCallback =
        object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(loadedAd: AppOpenAd) {
                appOpenAdInstance = loadedAd
                loadTime = getCurrentTime()
                logDebug("Ad Loaded")

                if (!coldStartShown && configs.showAdOnFirstColdStart) {
                    coldShowListener?.invoke()
                    coldStartShown = true
                }

                if (!isLifecycleAttached) {
                    isLifecycleAttached = true
                    processLifecycle.addObserver(this@BaseAdManager)
                }

                listener?.onAdLoaded()
            }

            override fun onAdFailedToLoad(loadError: LoadAdError) {
                listener?.onAdFailedToLoad(loadError)
                logError("Ad Failed To Load, Reason: ${loadError.responseInfo}")
            }
        }

    protected var adUnitId: String = TEST_AD_UNIT_ID
    protected var initialDelay: InitialDelay = InitialDelay()
    protected var adRequest: AdRequest = AdRequest.Builder().build()
    protected var orientation = AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT

    protected fun getApplication(): Application = application

    // SharedPreferences keep a better track
    protected var loadTime: Long
        get() = sharedPreferences.getLong("lastTime", 0)
        set(value) = sharedPreferences.edit()
            .putLong("lastTime", value)
            .apply()

    /**
     * `onResume` will be fired when the lifecycle event for **ON_RESUME** is triggered.
     *
     * This is required to avoid the first trigger to **ON_RESUME** which might show / load the ad,
     * when it is not supposed to.
     */
    abstract fun onResume()

    /**
     * State observer callback to handle relevant operations.
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_RESUME) {
            if (initialDelay != InitialDelay.NONE) saveInitialDelayTime()
            if (!isFirst) onResume()
            else isFirst = false
        }
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
    open fun isAdAvailable(): Boolean {
        return (appOpenAdInstance != null) && notLongerThanFourHours()
    }

    /**
     * Difference = Current Time `minus` Saved Time,
     *
     * therefore difference >= duration.getTime()
     */
    protected fun isInitialDelayOver(): Boolean {
        val savedDelay = sharedPreferences.getLong("savedDelay", 0L)
        return (getCurrentTime() - savedDelay) >= initialDelay.getTime()
    }
}