package com.lazygeniouz.aoa.listener

import com.google.android.gms.ads.AdError

/**
 * Interface to listen to the AppOpenAd's visibility events
 */
interface AppOpenAdListener {

    /**
     * Fired when the AppOpenAd is shown
     */
    fun onAdShown()

    /**
     * Fired when the AppOpenAd is dismissed
     */
    fun onAdDismissed()

    /**
     * Fired when there was a problem showing AppOpenAd
     * @param error [AdError] that is passed on from the AppOpenAd's callback stating the reason
     */
    fun onAdShowFailed(error: AdError?)
}