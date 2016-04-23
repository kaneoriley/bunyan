[![Release](https://jitpack.io/v/com.github.oriley-me/bunyan.svg)](https://jitpack.io/#com.github.oriley-me/bunyan) [![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0) [![Build Status](https://travis-ci.org/oriley-me/bunyan.svg?branch=master)](https://travis-ci.org/oriley-me/bunyan) [![Dependency Status](https://www.versioneye.com/user/projects/56b6a5840a0ff50035ba881d/badge.svg?style=flat)](https://www.versioneye.com/user/projects/56b6a5840a0ff50035ba881d)

# Bunyan
![Logo](artwork/icon.png)

SLF4J logger factory with simple Logcat and Crashlytics loggers built in.

## Usage

All you need to do is add the dependency and Bunyan will automatically handle SLF4J logging. Included are two sample
loggers, that you will need to add manually in a static block inside your `Application` class like so:

```java
static {
    // For output to Logcat
    Bunyan.addLogger(new BunyanLogcatLogger());

    // For Crashlytics logging
    Bunyan.addLogger(new BunyanCrashlyticsLogger(Crashlytics.class));

    // The addLogger method also accepts a varargs array of loggers, so you can include both like this:
    Bunyan.addLogger(new BunyanLogcatLogger(), new BunyanCrashlyticsLogger(Crashlytics.class));
}
```

Configuration options must be setup inside the same static block:

```java
static {

    ...

    // Set the threshold logging level (more info below). Here is my suggested configuration (defaults to INFO).
    Bunyan.setThreshold(BuildConfig.DEBUG ? Level.DEBUG : Level.INFO);

    // Set the desired style of the log tags (more info below). Here is a suggested configuration (defaults to SHORT).
    Bunyan.setTagStyle(BuildConfig.DEBUG ? TagStyle.FULL : TagStyle.SHORT);

    // Set whether to log any exceptions passed to SLF4J to Crashlytics as Non-Fatals. Defaults to false.
    // Has no effect if your application does not include Crashlytics.
    Bunyan.setLogExceptionToCrashlytics(true);
}
```

There are 5 values for logging threshold `Level`:

 * ERROR:  Threshold for `android.util.Log.ERROR` / `log.error()`
 * WARN:   Threshold for `android.util.Log.WARN` / `log.warn()`
 * INFO:   Threshold for `android.util.Log.INFO` / `log.info()`
 * DEBUG:  Threshold for `android.util.Log.DEBUG` / `log.debug()`
 * TRACE:  Threshold for `android.util.Log.VERBOSE` / `log.trace()`

Any logs with a lower priority than the value passed to `setThreshold()` will not be passed along to any appenders.

There are 4 values for `TagStyle`:

 * RESTRICTED:  Restrict to 23 characters (suggested maximum for `Log`)
 * SHORT:       The enclosing class simple name, i.e. `MyActivity`
 * LONG:        The enclosing class name including package, i.e. `com.myapp.MyActivity`
 * FULL:        Same as LONG but with calling method name appended, i.e. `com.myapp.MyActivity[onResume]`

# Custom Loggers

You also have the option of adding your own custom loggers for capturing logs to send to your analytics platform of
choice. Simply implement the `BunyanLogger` interface and handle log events as required. You will also need to add
the logger to Bunyan inside the static initialisation block like so:

```java
static {

    // Other Bunyan configuration
    ...

    Bunyan.addLogger(new MyAnalyticsLogger()); // Where MyAnalyticsLogger is your custom class
}
```

# Gradle Dependency

 * Add JitPack.io to your repositories list in the root projects build.gradle:

```gradle
repositories {
    maven { url "https://jitpack.io" }
}
```

 * Add the required dependency:

```gradle
dependencies {
    compile 'me.oriley:bunyan:0.1.1'
}
```
