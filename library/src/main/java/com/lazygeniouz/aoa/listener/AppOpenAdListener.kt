package com.lazygeniouz.aoa.listener

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError

/**
 * Interface to listen to the AppOpenAd's events
 */
abstract class AppOpenAdListener {

    /**
     * Callback fired when the Ad is loaded & ready to be shown on the next app resume.
     */
    open fun onAdLoaded() {}

    /**
     * AppOpenAd failed to load, see **loadAdError** for more info.
     *
     * @param loadAdError Contains info. as to what went wrong with loading Ad
     * @see LoadAdError
     */
    open fun onAdFailedToLoad(loadAdError: LoadAdError) {}

    /**
     * Fired before the Ad is shown
     *
     * The Ad will be shown after a delay of a 750ms post this callback
     * so that an action can be performed if needed before showing the Ad
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
     * @param error [AdError] that is passed on from the AppOpenAd's callback stating the reason
     */
    open fun onAdShowFailed(error: AdError?) {}
}