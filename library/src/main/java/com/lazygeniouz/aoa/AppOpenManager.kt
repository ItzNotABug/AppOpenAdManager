package com.lazygeniouz.aoa

import android.app.Application
import android.util.Log
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

/**
 * @AppOpenManager = A class that handles all of the App Open Ad operations.
 *
 * Constructor arguments:
 * @param application = Required to keep a track of App's state.
 * @param adUnitId = Pass your created AdUnitId
 *
 * @param orientation = Ad's Orientation, Can be PORTRAIT or LANDSCAPE (Default is Portrait)
 * @see AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
 * @see AppOpenAd.APP_OPEN_AD_ORIENTATION_LANDSCAPE
 *
 * @param adRequest = Pass a customised AdRequest if you have any.
 * @see AdRequest
 */
class AppOpenManager(
    private val application: Application,
    override var adUnitId: String = TEST_AD_UNIT_ID,
    override var orientation: Int = AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
    override var adRequest: AdRequest = AdRequest.Builder().build(),
) : BaseManager(application),
    LifecycleObserver {

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        Log.d(TAG, "onStart()")
        showAdIfAvailable()
    }

    /**
     * Let's fetch the Ad
     */
    private fun fetchAd() {
        if (isAdAvailable()) return
        loadAd()
    }

    // Show the Ad if the conditions are met.
    private fun showAdIfAvailable() {
        if (!isShowingAd && isAdAvailable()) appOpenAd?.show(
            currentActivity,
            getFullScreenContentCallback()
        )
        else {
            Log.d(TAG, "Can not show the Ad.")
            fetchAd()
        }
    }

    // Requires no explanation
    private fun loadAd() {
        // this is a good check.
        if (adUnitId == TEST_AD_UNIT_ID) Log.d(
            TAG,
            "Current adUnitId is a Test Ad Unit Id, make sure to replace with yours in Production "
        )

        loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAppOpenAdLoaded(loadedAd: AppOpenAd) {
                this@AppOpenManager.appOpenAd = loadedAd
                loadTime = getCurrentTime()
                Log.d(TAG, "onAppOpenAdLoaded()")
            }

            override fun onAppOpenAdFailedToLoad(loadError: LoadAdError) {
                Log.e(
                    TAG,
                    "App Open Ad Failed To Load, Reason: ${loadError.responseInfo}, " +
                            "\nDo not manually call fetch again, the Ads SDK handles that for you."
                )
            }
        }
        AppOpenAd.load(
            application,
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
                Log.e(TAG, "onAdFailedToShowFullScreenContent: ${adError?.message}")
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
            }
        }
    }

    // Returns `true` if available, `false` otherwise.
    private fun isAdAvailable(): Boolean = (appOpenAd != null) &&
            fourHoursAgo()
}