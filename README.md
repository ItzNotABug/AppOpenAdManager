# AppOpenAdManager

[![Maven Central](https://img.shields.io/maven-central/v/com.lazygeniouz/aoa_manager?color=blue)](https://central.sonatype.com/artifact/com.lazygeniouz/aoa_manager) [![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)

A lightweight Android lifecycle wrapper for App Open Ads from the
[Google Mobile Ads Next-Gen SDK](https://developers.google.com/admob/android/next-gen).

## Requirements

- minSdk 24+
- compileSdk 35+
- Kotlin 2.4+
- SDK initialization before starting preloading

The library targets Java 11 and does not require core-library desugaring. The sample uses API 37
for the latest Material 3 Expressive alpha.

## Installation

```gradle
dependencies {
    implementation 'com.lazygeniouz:aoa_manager:3.0.0'
}
```

## Usage

Initialize GMA Next-Gen on a background thread, then start preloading once:

```kotlin
import android.app.Application
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.lazygeniouz.aoa.AppOpenAdManager
import com.lazygeniouz.aoa.configs.Configs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApp : Application() {
    private lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate() {
        super.onCreate()

        appOpenAdManager = AppOpenAdManager.get(
            this,
            Configs(adUnitId = "ca-app-pub-xxxxx/xxxxx"),
        )

        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(
                this@MyApp,
                InitializationConfig
                    .Builder("ca-app-pub-xxxxx~xxxxx")
                    .build(),
            )
            appOpenAdManager.loadAppOpenAd()
        }
    }
}
```

`loadAppOpenAd()` uses Google's beta
[App Open Ad preloader](https://developers.google.com/admob/android/next-gen/app-open#start_ad_preloading),
which manages caching, retries, refills, and expiration. `clearAdInstance()` stops preloading and
destroys its cached ads; call `loadAppOpenAd()` to start again. Create and retain one manager per ad
configuration.

For AdMob Mediation, start preloading from the initialization completion callback. Apps using UMP
must also keep their AdMob app ID in the manifest.

## Configuration

```kotlin
val adManager = AppOpenAdManager.get(
    this,
    Configs(
        initialDelay = InitialDelay(1, DelayType.DAYS),
        adUnitId = "ca-app-pub-xxxxx/xxxxx",
        showOnColdStart = { isLoadingScreenVisible },
        showOnCondition = { !isPremiumUser },
        showInActivities = arrayListOf(MainActivity::class.java),
    ),
)
```

Use `InitialDelay.NONE` to disable the default one-day delay.
Cold-start display is attempted once; return `false` as soon as the loading screen finishes.

## Callbacks

```kotlin
adManager.setAppOpenAdListener(object : AppOpenAdListener() {
    override fun onAdLoaded() {}
    override fun onAdWillShow() {}
    override fun onAdShown() {}
    override fun onAdDismissed() {}
    override fun onAdFailedToLoad(error: LoadAdError) {}
    override fun onAdShowFailed(error: FullScreenContentError) {}
})

adManager.setOnPaidEventListener { adValue ->
    val revenue = adValue.valueMicros / 1_000_000.0
    analytics.logRevenue(revenue, adValue.currencyCode)
}
```

## Migrating from 2.x

Version 3 uses GMA Next-Gen and is intentionally a major release. Update these integration points:

- Raise minSdk to 24, compileSdk to at least 35, and Kotlin to 2.4 or newer.
- Initialize `MobileAds` with `InitializationConfig` on a background thread before preloading.
- Replace `com.google.android.gms.ads` types with `com.google.android.libraries.ads.mobile.sdk`
  types.
- Remove `play-services-ads` and `play-services-ads-lite`; exclude both from mediation adapters.
- Build custom requests with `AdRequest.Builder(adUnitId)`.
- Replace `getAppOpenAd()` checks with `isAdAvailable()`; the SDK now owns preloaded ads.
- Retain one manager instance; the `Application.appOpenAdManager` shortcut is deprecated.
- Treat `onAdWillShow()` as the final callback after any configured show delay.
- Handle `FullScreenContentError` in `onAdShowFailed()`.
- Replace `OnPaidEventListener` with the `(AdValue) -> Unit` paid callback.

During development, use `AppOpenAdManager.TEST_AD_UNIT_ID`; do not use a production ad unit for
test traffic.