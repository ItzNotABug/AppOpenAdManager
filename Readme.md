# AppOpenAdManager

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/af51d9b73c4544cca0be5e0af1b2669c)](https://app.codacy.com/gh/ItzNotABug/AppOpenAdManager?utm_source=github.com&utm_medium=referral&utm_content=ItzNotABug/AppOpenAdManager&utm_campaign=Badge_Grade)

`AppOpenAdManager` is just a simple wrapper to handle the new `AppOpenAd` Format by **Google AdMob**.\
If you look at the [tutorial](https://developers.google.com/admob/android/app-open-ads), you'll see the detailed guide to create a `Helper Class` to manage `AppOpenAd`.

A simplistic **Plug&Play** wrapper should have been included in the core Ads SDK itself but it wasn't, so I ended up creating the wrapper!\
All that boilerplate, now wrapped to a Single line of Code for the developer.

## Dependency
`val latest_version`:**1.8.2**\
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
AppOpenManager.loadAppOpenAds(this, Configs(InitialDelay.NONE, adUnitId, adRequest, showInActivity, orientation))
```

**Java:**
```java
AppOpenManager.loadAppOpenAds(App.this, new Configs(InitialDelay.NONE, adUnitId, adRequest, showInActivity, orientation));
```

The static method `loadAppOpenAds`' arguments are:
*   `@NonNull application: Application`\
     Your class extending `android.app.Application`

*   `@NonNull configs: Configs`\
     Now you can pass a `Configs` object which is a `data` class to pass relevant options.\
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

     * `showInActivity: Class<out Activity>`\
        (Optional) If you want to show the Ad only in a specific `Activity` (e.g: SplashActivity).\
        See: [this issue](https://github.com/ItzNotABug/AppOpenAdManager/issues/5) & the Example App for more info.

     * `orientation: Int`\
        (Optional) Default is `AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT`\
        Available variables are:\
        `AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT`\
        `AppOpenAd.APP_OPEN_AD_ORIENTATION_LANDSCAPE`
