package com.lazygeniouz.aoa

import android.app.Activity
import androidx.annotation.NonNull
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.appopen.AppOpenAd
import com.lazygeniouz.aoa.idelay.InitialDelay

/**
 * Bundle class to pass required data to [com.lazygeniouz.aoa.AppOpenManager]
 *
 * @param adUnitId Pass your created AdUnitId
 * @param initialDelay for Initial Delay
 * @param showInActivity Show [AppOpenAd] only when the Class matches the Current visible Activity
 * @param adRequest Pass a customised AdRequest if you have any.
 * @param orientation Ad's Orientation, Can be PORTRAIT or LANDSCAPE (Default is Portrait)
 *
 */
data class Configs @JvmOverloads constructor(
    @NonNull val initialDelay: InitialDelay,
    val adUnitId: String = AppOpenManager.TEST_AD_UNIT_ID,
    val adRequest: AdRequest = AdRequest.Builder().build(),
    val showInActivity: Class<out Activity>? = null,
    val orientation: Int = AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
)