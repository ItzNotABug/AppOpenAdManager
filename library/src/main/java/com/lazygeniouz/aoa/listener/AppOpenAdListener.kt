package com.lazygeniouz.aoa.listener

import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

/**
 * Interface to listen to the AppOpenAd's events
 */
abstract class AppOpenAdListener {

    /**
     * Callback fired whenever a preloaded Ad is ready to be shown on the next app resume.
     */
    open fun onAdLoaded() {}

    /**
     * An AppOpenAd preload attempt failed. The SDK retries automatically.
     *
     * @param loadAdError Contains info. as to what went wrong with loading Ad
     * @see LoadAdError
     */
    open fun onAdFailedToLoad(loadAdError: LoadAdError) {}

    /**
     * Fired immediately before the SDK is asked to show the Ad, after any configured delay.
     */
    open fun onAdWillShow() {}

    /**
     * Fired when the AppOpenAd is shown
     */
    open fun onAdShown() {}

    /**
     * Fired when the AppOpenAd is dismissed
     */
    open fun onAdDismissed() {}

    /**
     * Fired when there was a problem showing AppOpenAd
     * @param error [FullScreenContentError] passed from the AppOpenAd callback.
     */
    open fun onAdShowFailed(error: FullScreenContentError) {}
}