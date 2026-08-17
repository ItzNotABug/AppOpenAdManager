package com.lazygeniouz.aoa.configs

import android.app.Activity
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.lazygeniouz.aoa.AppOpenAdManager
import com.lazygeniouz.aoa.idelay.InitialDelay

/**
 * Bundle class to pass required data to [AppOpenAdManager]
 *
 * @param adUnitId Pass your created AdUnitId
 * @param initialDelay for setting up an Initial Delay
 * @param adRequest Pass a customized AdRequest to override [adUnitId]
 * @param showOnColdStart One-time predicate evaluated when the first preload completes. Return true
 * only while a loading screen is visible.
 * @param showOnCondition Show AppOpenAd only when a specific condition is met
 * @param showInActivities Show [AppOpenAd] only when the visible Activity is in this list
 *
 */
data class Configs @JvmOverloads constructor(
    val initialDelay: InitialDelay = InitialDelay(),
    val adUnitId: String = AppOpenAdManager.TEST_AD_UNIT_ID,
    val adRequest: AdRequest? = null,
    val showOnColdStart: (() -> Boolean)? = null,
    val showOnCondition: (() -> Boolean)? = null,
    val showInActivities: ArrayList<Class<out Activity>>? = null,
) {
    companion object {

        /**
         * Should **only be used for testing** as the
         * default parameter values are not meant for Production use.
         */
        @JvmField
        val DEFAULT = Configs()
    }
}