package com.lazygeniouz.aoa.configs

import android.app.Activity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.appopen.AppOpenAd
import com.lazygeniouz.aoa.AppOpenAdManager
import com.lazygeniouz.aoa.idelay.InitialDelay

/**
 * Bundle class to pass required data to [AppOpenAdManager]
 *
 * @param adUnitId Pass your created AdUnitId
 * @param initialDelay for setting up an Initial Delay
 * @param adRequest Pass a customised AdRequest if you have any
 * @param showOnColdStart Show Ad as soon as it load on the first Cold start if true,
 * this will ignore the Initial Activity irrespective of what is passed in the [showInActivities]
 * @param showOnCondition Show AppOpenAd only when a specific condition is met
 * @param showInActivities Show [AppOpenAd] only when the visible Activity is in this list
 *
 */
data class Configs @JvmOverloads constructor(
    val initialDelay: InitialDelay = InitialDelay(),
    val adUnitId: String = AppOpenAdManager.TEST_AD_UNIT_ID,
    val adRequest: AdRequest = AdRequest.Builder().build(),
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