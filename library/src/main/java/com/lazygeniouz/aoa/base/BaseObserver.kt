package com.lazygeniouz.aoa.base

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * Base Observer class to identify the Current visible Activity
 * @param application Required to register Activity Lifecycle Callbacks
 *
 * We only need onActivityStarted, onActivityResumed & onActivityDestroyed
 * to track the Current visible Activity & therefore it was meaningless
 * to add all those abstract methods in a single class.
 *
 * Extended ahead by:
 * @see BaseAdManager
 */
open class BaseObserver(private val application: Application) :
    Application.ActivityLifecycleCallbacks {

    protected var currentActivity: Activity? = null

    init {
        registerActivityLifecycleCallbacks()
    }

    private fun registerActivityLifecycleCallbacks() {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityDestroyed(activity: Activity) {
        // this is required for [Configs.showOnColdStart]
        if (currentActivity?.javaClass?.simpleName.equals(activity.javaClass.simpleName)) {
            currentActivity = null
        }
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
}