package com.lazygeniouz.aoa.extensions

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.lazygeniouz.aoa.AppOpenAdManager
import com.lazygeniouz.aoa.configs.Configs

internal const val TAG = "AppOpenAdManager"

internal fun logDebug(message: Any) = Log.d(TAG, message.toString())

internal fun logError(message: Any) = Log.e(TAG, message.toString())

@Suppress("deprecation")
internal fun Context.isClientOnline(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork
        if (network != null) {
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            return (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true)
                    || (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                    || (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true))
        }
        return false
    } else {
        val netInfo = connectivityManager.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }
}

/**
 * Extension function to use [AppOpenAdManager.get]
 * directly in a class that extends the [Application] class
 *
 * @param configs Configurations for the AppOpenAd
 * @see Configs for more info.
 */
fun Application.getAppOpenAdManager(configs: Configs = Configs.DEFAULT): AppOpenAdManager {
    return AppOpenAdManager.get(this, configs)
}