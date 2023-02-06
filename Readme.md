# AppOpenAdManager

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/af51d9b73c4544cca0be5e0af1b2669c)](https://app.codacy.com/gh/ItzNotABug/AppOpenAdManager?utm_source=github.com&utm_medium=referral&utm_content=ItzNotABug/AppOpenAdManager&utm_campaign=Badge_Grade)

`AppOpenAdManager` is just a simple wrapper to handle the new `AppOpenAd` Format by **Google AdMob**.\
If you look at the [tutorial](https://developers.google.com/admob/android/app-open-ads), you'll see the detailed guide to create a `Helper Class` to manage `AppOpenAd`.

## Dependency
`val latest_version`: **2.6.2**

**Gradle:**
```groovy
implementation 'com.lazygeniouz:aoa_manager:$latest_version'
```

## Usage
```kotlin
val configs = Configs(InitialDelay.NONE, adUnitId, adRequest, showInActivity)
val adManager = AppOpenAdManager.get(application, configs)
adManager.setAppOpenAdListener(appOpenAdlistener)
adManager.setOnPaidEventListener(paidEventListener)
adManager.showAdWithDelay(false)
adManager.setImmersiveMode(true)
adManager.loadAppOpenAd()
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

     * `showOnColdStart: Boolean`\
        (Optional) Show Ad as soon as it load on the first Cold start if true,
        this will ignore the Activities passed in the [showInActivities]

     * `showOnCondition: (() -> Boolean)`\
        (Optional) Show the AppOpenAd only when a specific condition is met.
        AppOpenAd will be shown only when this block returns `true`.

     * `showInActivities: Class<out Activity>`\
        (Optional) If you want to show the Ad only in specific Activities (e.g: SplashActivity).\
        See: [this issue](https://github.com/ItzNotABug/AppOpenAdManager/issues/5) & the Example App for more info.

Other available methods:
* `getAppOpenAd: AppOpenAd?`: Returns the `AppOpenAd` instance, can be null.

* `clearAdInstance(): Unit`: Sets the `AppOpenAd` instance to `null` if it is not.

* `isAdAvailable(): Boolean`: Returns `true` if a valid `AppOpenAd` is available

* `setImmersiveMode(Boolean): Unit`: Sets whether to show the `AdOpenAd` in immersive mode.

* `showAdWithDelay(Boolean): Unit`: Delays showing the `AdOpenAd` by 1 second if true.

* `showAdWithDelay(Long): Unit`: Delays showing the `AdOpenAd` by the provided time in milliseconds.

* `getAdListener(): AppOpenAdListener?`: Returns the currently set Ad Listener, can be null.

* `setAppOpenAdListener(listener: AppOpenAdListener)`:\
    There are several callbacks with respect to the AppOpenAd.
    * `onAdLoaded()` = Invoked when the Ad is loaded successfully.
    * `onAdFailedToLoad(LoadAdError)` = Invoked when the Ad failed to load with supplied LoadAdError.
    * `onAdWillShow()` = Invoked before the Ad is shown, a default delay of 1000ms or value provided.
    * `onAdShown()` = Invoked when the Ad is shown
    * `onAdDismissed()` = Invoked after the Ad is dismissed from the screen
    * `onAdShowFailed(AdError)` = Invoked when there was an error showing the Ad with supplied AdError

* `setOnPaidEventListener(listener: OnPaidEventListener)`:
    * `onPaidEvent(AdValue)` = Called when an ad is estimated to have earned money.
      * `AdValue` = Contains information about the monetary value earned from an ad.

* `getPaidEventListener(): OnPaidEventListener?`: Returns the currently set Ad's PaidEventListener, can be null..