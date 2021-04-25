package com.lazygeniouz.aoa.configs

import android.app.Activity
import androidx.annotation.NonNull
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.appopen.AppOpenAd
import com.lazygeniouz.aoa.AppOpenAdManager
import com.lazygeniouz.aoa.idelay.InitialDelay

/**
 * Bundle class to pass required data to [com.lazygeniouz.aoa.AppOpenAdManager]
 *
 * @param adUnitId Pass your created AdUnitId
 * @param initialDelay for Initial Delay
 * @param adRequest Pass a customised AdRequest if you have any.
 * @param showInActivities Show [AppOpenAd] only when the visible Activity is in this list
 * @param orientation Ad's Orientation, Can be PORTRAIT or LANDSCAPE (Default is Portrait)
 *
 */
data class Configs @JvmOverloads constructor(
    @NonNull val initialDelay: InitialDelay,
    val adUnitId: String = AppOpenAdManager.TEST_AD_UNIT_ID,
    val adRequest: AdRequest = AdRequest.Builder().build(),
    val showInActivities: ArrayList<Class<out Activity>>? = null,
    val orientation: Int = AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
)