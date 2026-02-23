package com.lazygeniouz.appopenads.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle

/**
 * Demonstrating the usage of [com.lazygeniouz.aoa.configs.Configs.showInActivities]
 *
 * We use this class to show the Ad only when the App is loaded from a hard cold start.
 *
 * The current AppOpenAd has the architecture to show Ad every-time
 * the [android.app.Application] starts, & sometimes, that is not what we want.
 *
 * So, the [com.lazygeniouz.aoa.configs.Configs.showInActivities] will only show the AppOpenAd
 * when the specified activity shows up in the stack.
 *
 * [SplashActivity] to show a Android Icon for App's cold start,
 *
 * Has a **noHistory** tag in Manifest for this Activity.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}