# Currencies
[![Download APK](https://img.shields.io/badge/download-apk-green.svg)](https://github.com/svanegas/revolut_currencies/blob/develop/extras/Currencies.apk?raw=true)

<img src="/extras/images/logo.png" width="200">

> A simple Android application to know the exchange for a selected set of currencies

---

## Table of contents

  * [Functionality](#functionality)
  * [How is it built](#how-is-it-built)
  * [How it looks like](#how-it-looks-like)
  * [Dependencies](#dependencies)
  * [Setup](#setup)
  * [Unit tests](#unit-tests)
  * [Known issues](#known-issues)
  * [What's next?](#whats-next)
  * [Credits](#credits)

## Functionality

The application allows users to check the exchange for a selected set of currencies, where the user is be able to
choose an additional currency from the list to be converted.

For this version of the app, the number of currencies to be displayed in the list are limited to 6.
Meaning that if user tries to add an additional currency the app will automatically delete the last
currency in the list.

Every time the user taps on a currency value, it would go to the top, becoming the **base** currency,
all the other values are calculated based on the first one.

The list contains live information that is automatically refreshed.

The user is able to delete a currency (except the base) by swiping to the left on the corresponding row.

## How is it built

_Currencies_ is built using the **Android SDK** version 28, with backwards
compatibility down to devices running _Android Lollipop (26)_. **MVVM Architecture** was used in the project. [Google Design Guidelines] were also
implemented in order to create a comfortable UX and UI.

## How it looks like
| Animation |
|:---:|
| ![Animation](extras/images/animation.gif) |

## Dependencies

Currencies app is powered by great tech. The following dependencies were used to build the project.

- [Android X]
- [Dagger 2]
- [RxJava 2]
- [Retrofit]
- [Glide]
- [Firebase]
- [Crashlytics]
- [Timber]

## Setup

The project is expected to be built and run using [Android Studio] 3.5+, make sure you have
installed a proper version.

Open Android Studio and select the **Open an existing Android Studio project** option, then select
the project root and wait for the project to sync.

There are two flavors and two build types, making a total of 4 build variants.

- `devDebug`
- `devRelease`
- `productionDebug`
- `productionRelease`

In order to build `release` builds you would need to generate a key store to sign the app.
Once you have your key store save it as `currencies.jks` under the `extras/keystore` folder, and also,
modify the file `currencies.properties` to save your store settings. By default, it will look like this, but
it won't work if you would like to compile a `release` variant of the app:

```
keystore.key.password=123example
keystore.store.password=456example
keystore.key.alias=example1
```

## Unit tests

This project has a set of unit tests for the main view model, i.e. the `CurrenciesViewModel`,
those tests can be run using gradlew as follows:

```
./gradlew test[flavor][build type]
```

for instance
```
./gradlew testProductionDebug
```

or don't specify any flavor or build type and it would do it for each variant.

## Known issues

There are some issues that were identified during the development process but they haven't been solved yet:

- The currency inputs are limited to 20 characters, which will truncate a conversion that would take more
- When there's several currencies, selecting one from the bottom will create a loop of focus/unfocus, caused by the scrolling,
this issue is still not solved completely, but the workaround so far is to limit the number of shown currencies to 6, in that
way the chances to get that unexpected behavior are less. This issue should be worked on.

## What's next?

The project would need a nice touch to be better, including:

- Continuous Integration / Continuous Deployment
- Analytics
- Search feature in the currencies list
- ...

## Credits

Lottie animations used in the project:
- https://lottiefiles.com/719-loading
- https://lottiefiles.com/1272-disconnected
- https://lottiefiles.com/2609-face-animation

> Santiago Vanegas

[Android X]:https://developer.android.com/jetpack/androidx
[Dagger 2]:https://google.github.io/dagger/
[RxJava 2]:https://github.com/ReactiveX/RxJava
[Retrofit]:http://square.github.io/retrofit/
[Glide]:https://github.com/bumptech/glide
[Google Design Guidelines]:https://design.google.com/
[Android Studio]:https://developer.android.com/studio/index.html
[Firebase]:https://firebase.google.com
[Crashlytics]:https://firebase.google.com/products/crashlytics
[Timber]:https://github.com/JakeWharton/timber
