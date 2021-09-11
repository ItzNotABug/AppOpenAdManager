package com.lazygeniouz.aoa.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.Nullable
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.lazygeniouz.aoa.AppOpenAdManager.Companion.TEST_AD_UNIT_ID
import com.lazygeniouz.aoa.configs.Configs
import com.lazygeniouz.aoa.extensions.logDebug
import com.lazygeniouz.aoa.extensions.logError
import com.lazygeniouz.aoa.idelay.InitialDelay
import java.time.Instant
import java.util.*

/**
 * A Base class that extends
 * @see BaseObserver to handle Activity Lifecycle,
 *
 * This class is created to declare a few helper methods and variables,
 * so that our main usable class does not have a lot of methods and variables.
 * @see com.lazygeniouz.aoa.AppOpenAdManager
 */
open class BaseAdManager(
    private val application: Application,
    private val configs: Configs
) : BaseObserver(application) {

    private val sharedPreferences =
        application.getSharedPreferences("appOpenAdsManager", Context.MODE_PRIVATE)

    @Nullable
    protected var coldShowListener: (() -> Unit)? = null

    protected var isShowingAd = false
    protected var coldStartShown = false
    protected var appOpenAd: AppOpenAd? = null
    protected val loadCallback: AppOpenAd.AppOpenAdLoadCallback =
        object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(loadedAd: AppOpenAd) {
                appOpenAd = loadedAd
                loadTime = getCurrentTime()
                logDebug("Ad Loaded")

                if (!coldStartShown && configs.showAdOnFirstColdStart) {
                    coldShowListener?.invoke()
                    coldStartShown = true
                }
            }

            override fun onAdFailedToLoad(loadError: LoadAdError) {
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
        @SuppressLint("CommitPrefEdits")
        set(value) = sharedPreferences.edit()
            .putLong("lastTime", value)
            .apply()

    /**
     * There's a platform issue on Android 8/8.1 when using Date().time
     * Details: https://itznotabug.dev/blog/short_standard_assertion_error/
     */
    protected fun getCurrentTime(): Long {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) Date().time
        else Instant.now().toEpochMilli()
    }

    @SuppressLint("CommitPrefEdits")
    protected fun saveInitialDelayTime() {
        val initialDelayKey = "savedDelay"
        val savedDelay = sharedPreferences.getLong(initialDelayKey, 0L)
        if (savedDelay == 0L)
            sharedPreferences.edit()
                .putLong(initialDelayKey, getCurrentTime())
                .apply()
    }

    /**
     * The documentation says that the Ads are only cached for 4 Hours.
     * https://developers.google.com/admob/android/app-open#expiration
     */
    private fun notLongerThanFourHours(): Boolean {
        val dateDifference = getCurrentTime() - loadTime
        val fourHours: Long = (3600000 * 4)
        return dateDifference < fourHours
    }

    // Returns `true` if an Ad is available & valid, `false` otherwise.
    protected fun isAdAvailable(): Boolean = (appOpenAd != null) && notLongerThanFourHours()

    // Difference = Current Time `minus` Saved Time,
    // therefore difference >= duration.getTime()
    protected fun isInitialDelayOver(): Boolean {
        val savedDelay = sharedPreferences.getLong("savedDelay", 0L)
        return (getCurrentTime() - savedDelay) >= initialDelay.getTime()
    }
}