# AppOpenAdManager

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/af51d9b73c4544cca0be5e0af1b2669c)](https://app.codacy.com/gh/ItzNotABug/AppOpenAdManager?utm_source=github.com&utm_medium=referral&utm_content=ItzNotABug/AppOpenAdManager&utm_campaign=Badge_Grade)

`AppOpenAdManager` is just a simple wrapper to handle the new `AppOpenAd` Format by **Google AdMob**.\
If you look at the [tutorial](https://developers.google.com/admob/android/app-open-ads), you'll see the detailed guide to create a `Helper Class` to manage `AppOpenAd`.

A simplistic **Plug&Play** wrapper should have been included in the core Ads SDK itself but it wasn't, so I ended up creating the wrapper!\
All that boilerplate, now wrapped to a Single line of Code for the developer.

## Dependency
`val latest_version`: **2.2-alpha**\
**Note: `AppOpenAdManager` is now available on MavenCentral**

**Gradle:**
```groovy
implementation 'com.lazygeniouz:aoa_manager:$latest_version'`
```

**Maven:**
```maven
<dependency>
  <groupId>com.lazygeniouz</groupId>
  <artifactId>aoa_manager</artifactId>
  <version>$latest_version</version>
  <type>aar</type>
</dependency>
```

## Usage
**Kotlin:**
```kotlin
val adManager = AppOpenManager.get(this, Configs(InitialDelay.NONE, adUnitId, adRequest, showInActivity, orientation))
adManager.loadAppOpenAd(listener)
```

**Java:**
```java
AppOpenManager adManager = AppOpenManager.get(App.this, new Configs(InitialDelay.NONE, adUnitId, adRequest, showInActivity, orientation));
adManager.loadAppOpenAd(listener)
```

The arguments for the static method `get` are:
*   `@NonNull application: Application`\
     Your class extending `android.app.Application`

*   `@NonNull configs: Configs` (Optional)\
     You can pass a `Configs` object which is a `data` class to pass relevant options.\
     Relevant options are:
     * `@NonNull initialDelay: InitialDelay`\
        You can specify an **Initial Delay** to load & display the Ad for **the first time**.\
        If you do not need a delay, simple pass `InitialDelay.NONE`\
        But it is a good practise to allow the user to first explore the App &\
        therefore 1 Day should be fine which is also the Default if you pass `InitialDelay()`

     * `adUnitId: String`\
        Your `adUnitId`\
        (Optional for Testing) Default is a Test AdUnitId

     * `adRequest: AdRequest`\
        (Optional) If you have a customised AdRequest

     * `showAdOnFirstColdStart: Boolean`\
        (Optional) Show Ad as soon as it load on the first Cold start if true,
        this will ignore the Activities passed in the [showInActivities]

     * `showOnCondition: (() -> Boolean)`\
        (Optional) Show the AppOpenAd only when a specific condition is met.
        AppOpenAd will be shown only when this block returns `true`.

     * `showInActivities: Class<out Activity>`\
        (Optional) If you want to show the Ad only in specific Activities (e.g: SplashActivity).\
        See: [this issue](https://github.com/ItzNotABug/AppOpenAdManager/issues/5) & the Example App for more info.

     * `orientation: Int`\
        (Optional) Default is `AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT`\
        Available variables are:\
        `AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT`\
        `AppOpenAd.APP_OPEN_AD_ORIENTATION_LANDSCAPE`

The `loadAppOpenAd` has an optional `listener: AppOpenAdListener` parameter:\
There are several callbacks with respect to the AppOpenAd.
* `onAdWillShow()` = Invoked before the Ad is shown, a delay of 1000ms.
* `onAdShown()` = Invoked when the Ad is shown
* `onAdDismissed()` = Invoked after the Ad is dismissed from the screen
* `onAdShowFailed(AdError)` = Invoked when there was an error showing the Ad with supplied AdError

Other available methods:
* `getAppOpenAd: AppOpenAd?`: Returns the `AppOpenAd` instance, can be null.
* `isAdAvailable(): Boolean`: Returns `true` if a valid `AppOpenAd` is available
* `getAdListener(): AppOpenAdListener?`: Returns the currently set Ad Listener, can be null.
