package com.lazygeniouz.aoa.extensions

import android.app.Application
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.lazygeniouz.aoa.AppOpenAdManager
import com.lazygeniouz.aoa.configs.Configs
import com.lazygeniouz.aoa.listener.AppOpenAdListener

internal const val TAG = "AppOpenManager"

internal fun logDebug(message: String) = Log.d(TAG, message)

internal fun logError(message: String) = Log.e(TAG, message)


/**
 * ext. fun. to use [AppOpenAdManager.loadAppOpenAds]
 * directly in the [Application] class
 */
@JvmOverloads
@JvmSynthetic
fun Application.loadAppOpenAds(
    @NonNull configs: Configs,
    @Nullable listener: AppOpenAdListener? = null
) = AppOpenAdManager.loadAppOpenAds(this, configs, listener)