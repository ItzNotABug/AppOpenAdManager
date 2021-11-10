package com.lazygeniouz.appopenads;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;
import com.lazygeniouz.aoa.AppOpenAdManager;

/**
 * Sample App's Main Application
 * Java Version, not registered in the manifest.
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public class AppJv extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        MobileAds.initialize(this);
        AppOpenAdManager manager = AppOpenAdManager.get(this);
        manager.loadAppOpenAd();
    }
}
