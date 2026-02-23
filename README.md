# AppOpenAdManager

[![Maven Central](https://img.shields.io/maven-central/v/com.lazygeniouz/aoa_manager?color=blue)](https://central.sonatype.com/artifact/com.lazygeniouz/aoa_manager) [![API](https://img.shields.io/badge/API-23%2B-brightgreen.svg)](https://android-arsenal.com/api?level=23) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/af51d9b73c4544cca0be5e0af1b2669c)](https://app.codacy.com/gh/ItzNotABug/AppOpenAdManager) [![Kotlin](https://img.shields.io/badge/Kotlin-2.3-purple.svg)](https://kotlinlang.org)

Android Kotlin library
for [AdMob App Open Ads](https://developers.google.com/admob/android/app-open-ads).
Ship App Open Ads faster without maintaining lifecycle-heavy ad management code.

## Why AppOpenAdManager

Implementing App Open Ads in Android means handling lifecycle observers, activity state, ad expiry,
and display conditions. AppOpenAdManager wraps that into a focused API, so you keep control over
monetization logic and reduce integration overhead.

Use it when you need:

- Delayed first impression ads for a better new-user experience
- Activity-level ad targeting (show only on selected screens)
- Conditional display logic (for example, no ads for premium users)
- App Open Ad lifecycle callbacks and paid-event hooks in one place

## Installation

![Latest Version](https://img.shields.io/maven-central/v/com.lazygeniouz/aoa_manager?label=latest&color=blue)

```gradle
dependencies {
    implementation 'com.lazygeniouz:aoa_manager:$version'
}
```

## Quick Start (Android)

Minimal setup in your `Application` class:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        AppOpenAdManager.get(
            this,
            Configs(
                adUnitId = "ca-app-pub-xxxxx/xxxxx"
            )
        ).loadAppOpenAd()
    }
}
```

Advanced setup with common monetization controls:

```kotlin
val adManager = AppOpenAdManager.get(
    this,
    Configs(
        initialDelay = InitialDelay(1, DelayType.DAYS),
        adUnitId = "ca-app-pub-xxxxx/xxxxx",
        showOnColdStart = { true },
        showOnCondition = { !isPremiumUser },
        showInActivities = arrayListOf(MainActivity::class.java)
    )
)

adManager.apply {
    setAppOpenAdListener(adListener)
    setOnPaidEventListener(paidListener)
    loadAppOpenAd()
}
```

## Configuration

### `Configs`

| Parameter          | Type                          | Default | Description                   |
|--------------------|-------------------------------|---------|-------------------------------|
| `initialDelay`     | `InitialDelay`                | 1 day   | Delay before showing first ad |
| `adUnitId`         | `String`                      | Test ID | AdMob ad unit ID              |
| `adRequest`        | `AdRequest`                   | Default | Custom ad request             |
| `showOnColdStart`  | `(() -> Boolean)?`            | `null`  | Show on app cold start        |
| `showOnCondition`  | `(() -> Boolean)?`            | `null`  | Conditional display logic     |
| `showInActivities` | `ArrayList<Class<Activity>>?` | `null`  | Activity whitelist            |

### `InitialDelay`

Control when the first App Open Ad appears:

```kotlin
InitialDelay.NONE                    // No delay
InitialDelay()                       // 1 day (default)
InitialDelay(3, DelayType.DAYS)      // 3 days
InitialDelay(12, DelayType.HOURS)    // 12 hours
```

The delay is tracked per device and applied once.

## API

### Core Methods

| Method                      | Description           |
|-----------------------------|-----------------------|
| `loadAppOpenAd()`           | Load the ad           |
| `isAdAvailable(): Boolean`  | Check if ad is ready  |
| `clearAdInstance()`         | Remove ad instance    |
| `setImmersiveMode(Boolean)` | Toggle immersive mode |
| `showAdWithDelay(Long)`     | Delay ad display (ms) |

### Event Listeners

Ad lifecycle events:

```kotlin
setAppOpenAdListener(object : AppOpenAdListener() {
    override fun onAdLoaded() {}
    override fun onAdWillShow() {}
    override fun onAdShown() {}
    override fun onAdDismissed() {}
    override fun onAdFailedToLoad(loadAdError: LoadAdError) {}
    override fun onAdShowFailed(error: AdError?) {}
})
```

Revenue tracking:

```kotlin
setOnPaidEventListener { adValue ->
    val revenue = adValue.valueMicros / 1_000_000.0
    analytics.logRevenue(revenue, adValue.currencyCode)
}
```

## Testing

Use the official test ad unit during development:

```kotlin
adUnitId = AppOpenAdManager.TEST_AD_UNIT_ID
```

Do not use production ad units in debug builds. This can risk AdMob account suspension.

## Common App Open Ad Use Cases

Show ads only in specific activities:

```kotlin
showInActivities = arrayListOf(
    MainActivity::class.java,
    SplashActivity::class.java
)
```

Skip ads for premium users:

```kotlin
showOnCondition = {
    !userRepository.isPremium()
}
```

Add a brief delay before showing:

```kotlin
showAdWithDelay(2000) // 2 seconds
```