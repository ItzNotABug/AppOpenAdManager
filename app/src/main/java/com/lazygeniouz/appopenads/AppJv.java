package com.lazygeniouz.appopenads;

import com.google.android.gms.ads.MobileAds;
import com.lazygeniouz.aoa.AppOpenManager;
import com.lazygeniouz.aoa.idelay.InitialDelay;

/**
 * Sample App's Main Application
 * Java Version, not registered in the manifest.
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public class AppJv extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        MobileAds.initialize(this);
        new AppOpenManager(AppJv.this, InitialDelay.NONE);
    }
}
