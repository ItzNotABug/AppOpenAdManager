@file:Suppress("unused")

package com.lazygeniouz.aoa.extensions

import android.app.Application
import android.util.Log
import com.lazygeniouz.aoa.AppOpenAdManager
import com.lazygeniouz.aoa.configs.Configs

internal const val TAG = "AppOpenAdManager"

internal fun logDebug(message: Any) = Log.d(TAG, message.toString())

internal fun logError(message: Any) = Log.e(TAG, message.toString())

/**
 * Extension function to use [AppOpenAdManager.get]
 * directly in a class that extends the [Application] class
 *
 * @param configs Configurations for the AppOpenAd
 * @see Configs for more info.
 *
 * @return [AppOpenAdManager]
 */
fun Application.getAppOpenAdManager(configs: Configs = Configs.DEFAULT): AppOpenAdManager {
    return AppOpenAdManager.get(this, configs)
}

/**
 * Handy extension variable with default [Configs].
 *
 * To customize the [Configs], use [Application.getAppOpenAdManager] extension function.
 *
 * @see [Configs]
 * @return [AppOpenAdManager]
 */
val Application.appOpenAdManager
    get() = AppOpenAdManager.get(this, Configs.DEFAULT)