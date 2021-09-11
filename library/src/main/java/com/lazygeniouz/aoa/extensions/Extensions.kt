package com.lazygeniouz.aoa.extensions

import android.app.Application
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.lazygeniouz.aoa.AppOpenAdManager
import com.lazygeniouz.aoa.configs.Configs
import com.lazygeniouz.aoa.listener.AppOpenAdListener

internal const val TAG = "AppOpenAdManager"

internal fun logDebug(message: Any) = Log.d(TAG, message.toString())

internal fun logError(message: Any) = Log.e(TAG, message.toString())


/**
 * Extension function to use [AppOpenAdManager.loadAppOpenAds]
 * directly in a class that extends the [Application] class
 */
@JvmOverloads
@JvmSynthetic
fun Application.loadAppOpenAds(
    @NonNull configs: Configs,
    @Nullable listener: AppOpenAdListener? = null
) = AppOpenAdManager.loadAppOpenAds(this, configs, listener)