package com.lazygeniouz.aoa.base

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.lazygeniouz.aoa.AppOpenAdManager
import com.lazygeniouz.aoa.idelay.InitialDelay
import com.lazygeniouz.aoa.listener.AppOpenAdListener

/**
 * A Base class that extends [BaseObserver] to handle Activity Lifecycle.
 *
 * This class is created to declare a few helper methods and variables,
 * so that our main usable class does not have a lot of methods and variables.
 * @see com.lazygeniouz.aoa.AppOpenAdManager
 */
abstract class BaseAdManager(
    application: Application,
) : BaseObserver(application),
    LifecycleEventObserver {

    /** Resume handling starts only after [AppOpenAdManager.loadAppOpenAd] is called. */
    @Volatile
    protected var isPreloadingStarted = false
    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("appOpenAdsManager", Context.MODE_PRIVATE)

    // Callbacks
    protected var listener: AppOpenAdListener? = null
    protected var adPaidEventListener: ((AdValue) -> Unit)? = null

    protected var isImmersive: Boolean = false
    protected var adShowDelayPeriod: Long = 1000

    @Volatile
    protected var isShowingAd = false
    protected var coldStartHandled = false

    protected var initialDelay: InitialDelay = InitialDelay()

    protected val mainHandler = Handler(Looper.getMainLooper())
    private fun isMainThread() = Looper.myLooper() == Looper.getMainLooper()

    protected fun registerProcessObserver() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    protected fun runOnMainThread(action: () -> Unit) {
        if (isMainThread()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }

    /**
     * Called when the process lifecycle reaches **ON_RESUME**.
     */
    protected abstract fun onAppResume()

    /**
     * State observer callback to handle relevant operations.
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_RESUME) onAppResume()
        if (event == Lifecycle.Event.ON_START && initialDelay != InitialDelay.NONE) saveInitialDelayTime()
    }

    private fun saveInitialDelayTime() {
        val initialDelayKey = "savedDelay"
        val savedDelay = sharedPreferences.getLong(initialDelayKey, 0L)
        if (savedDelay != 0L) return
        sharedPreferences.edit()
            .putLong(initialDelayKey, System.currentTimeMillis())
            .apply()
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
        val elapsed = System.currentTimeMillis() - savedDelay
        return if (initialDelay == InitialDelay.NONE) elapsed >= initialDelay.getTime()
        else savedDelay != 0L && elapsed >= initialDelay.getTime()
    }
}