# AppOpenAdManager

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/af51d9b73c4544cca0be5e0af1b2669c)](https://app.codacy.com/gh/ItzNotABug/AppOpenAdManager?utm_source=github.com&utm_medium=referral&utm_content=ItzNotABug/AppOpenAdManager&utm_campaign=Badge_Grade)

**New enhancements are added in this branch**

`AppOpenAdManager` is just a simple wrapper to handle the new `AppOpenAd` Format by AdMob (Google).\
If you look at the [tutorial](https://developers.google.com/admob/android/app-open-ads), you'll see the detailed guide to create a `Helper Class` to manage `AppOpenAd`.

The wrapper should have been included in the core Ads SDK itself but it wasn't, So I ended up creating the wrapper!\
All that boilerplate, now wrapped to a Single line of Code for the developer.

## Dependency
`val latest_version`: &nbsp; [![Download](https://api.bintray.com/packages/itznotabug/Maven/AppOpenAdManager/images/download.svg)](https://bintray.com/itznotabug/Maven/AppOpenAdManager/_latestVersion)

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
  <type>pom</type>
</dependency>
```

## Usage
**Kotlin:**
```kotlin
AppOpenManager(this, InitialDelay.NONE, adUnitId, adRequest, orientation)
```

**Java:**
```java
new AppOpenManager(App.this, InitialDelay.NONE, adUnitId, adRequest, orientation);
```

The constructor arguments are:
*   `@NonNull application: Application`\
     Your class extending `android.app.Application`

*   `@NonNull initialDelay: InitialDelay`\
     You can specify an **Initial Delay** to load & display the Ad for **the first time**.\
     If you do not need a delay, simple pass `InitialDelay.NONE`\
     But it is a good practise to allow the user to first explore the App &\
     therefore 1 Day should be fine which is also the Default if you pass `InitialDelay()`

*   `adUnitId: String`\
     Your `adUnitId`\
     (Optional for Testing) Default is a Test AdUnitId

*   `adRequest: AdRequest`\
     (Optional) If you have a customised AdRequest

*   `orientation: Int`\
     (Optional) Default is `AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT`\
     Available variables are:\
     `AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT`\
     `AppOpenAd.APP_OPEN_AD_ORIENTATION_LANDSCAPE`
