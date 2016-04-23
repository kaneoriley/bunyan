[![Release](https://jitpack.io/v/com.github.oriley-me/bunyan.svg)](https://jitpack.io/#com.github.oriley-me/bunyan) [![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0) [![Build Status](https://travis-ci.org/oriley-me/bunyan.svg?branch=master)](https://travis-ci.org/oriley-me/bunyan) [![Dependency Status](https://www.versioneye.com/user/projects/56b6a5840a0ff50035ba881d/badge.svg?style=flat)](https://www.versioneye.com/user/projects/56b6a5840a0ff50035ba881d)
<a href="http://www.methodscount.com/?lib=me.oriley.bunyan%3Abunyan-core%3A0.2.0"><img src="https://img.shields.io/badge/bunyan_core-methods: 133 | deps: 20 | size: 13 KB-f44336.svg"></img></a> <a href="http://www.methodscount.com/?lib=me.oriley.bunyan%3Abunyan-lombok%3A0.2.0"><img src="https://img.shields.io/badge/bunyan_lombok-methods: 11 | deps: 153 | size: 3 KB-f44336.svg"></img></a>

# Bunyan
![Logo](artwork/icon.png)

Logger factory with SLF4J message formatting, and support for custom logger extensions.

## Initialisation

Bunyan is designed to provide an SLF4J style logging API without required massive dependencies like most other libraries.
It also includes an easy to use interface for adding custom loggers, to hook into whatever analytics package you use.
Included are two sample loggers, that you will need to add to `Bunyan` manually in a static block inside your
`Application` class like so:

```java
static {
    // For outputting to Logcat
    Bunyan.addLogger(new BunyanLogcatLogger());

    // For Crashlytics logging (if you have the Crashlytics library included in your application)
    Bunyan.addLogger(new BunyanCrashlyticsLogger(Crashlytics.class));

    // The addLogger method accepts a varargs array of loggers, so you can include multiple in the same call:
    Bunyan.addLogger(new BunyanLogcatLogger(), new BunyanCrashlyticsLogger(Crashlytics.class));
}
```

No loggers are automatically installed, so by default no logging will be done if you don't add any.

Configuration options must also be setup inside the same static block:

```java
static {

    ...

    // Set the threshold logging level (more info below). Here is my suggested configuration (defaults to INFO).
    Bunyan.setThreshold(BuildConfig.DEBUG ? Level.DEBUG : Level.INFO);

    // Set the desired style of the log tags (more info below). Here is a suggested configuration (defaults to SHORT).
    Bunyan.setTagStyle(BuildConfig.DEBUG ? TagStyle.FULL : TagStyle.SHORT);

    // Set whether to log any exceptions passed to log.error() to Crashlytics as Non-Fatals. Defaults to false.
    // Has no effect if your application does not include Crashlytics.
    Bunyan.setLogExceptionsToCrashlytics(true);
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

 Note that FULL tags require traversing the stack trace to find method names, which could have an adverse impact on
 performance. This is why I would suggest only using FULL on debug builds, or passing in the method name as part of
 the message instead if necessary.

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

# Usage

There are two methods for using Bunyan, depending on whether you use [Project Lombok](https://projectlombok.org/) and like
to take advantage of the `@Slf4j` annotation. This requires an extra dependency (listed below) in order to create a shim
so that the code Lombok generates will be able to successfully retrieve a `BunyanCoreLogger` instance.

If you don't use Project Lombok, you will need to create a field in each class where you wish to use Bunyan, like so:

```java
public class MyClass {

    private static final BunyanCoreLogger log = BunyanLoggerFactory.getLogger(MyClass.class);

}
```

If you do use Lombok, it will take care of the logger creation for you by adding a simple annotation:

```java
@Slf4j
public class MyClass {
    // log field is automatically generated by the Lombok plugin
}
```

Some examples of using the SLF4J logging interface:

```java
public void myMethod(int value) {
    try {
        log.trace("method called, time: {}", System.currentTimeMillis());
        log.debug("current value: {}, new value: {}", mValue, value);
        log.info("slf4j logging is much nicer than android.util.Log: {}, how much?: {}", true, Integer.MAX_VALUE);
        log.warn("wtf, this shouldn't be happening");
    } catch (Exception e) {
        log.error("error running myMethod", e);
    }
}
```

Check out [this explanation](http://www.slf4j.org/faq.html#logging_performance) of why the SLF4J logging format (which
I have reproduced in Bunyan) is much more efficient than standard logging, both in terms of code maintenance and runtime
performance.

# Gradle Dependency

 * Add JitPack.io to your repositories list in the root projects build.gradle:

```gradle
repositories {
    maven { url "https://jitpack.io" }
}
```

 * Add the required dependencies:

```gradle
dependencies {
    // Required
    compile 'me.oriley.bunyan:bunyan-core:0.2.0'

    // Only necessary to take advantage of Lombok's @Slf4j annotations
    compile 'me.oriley.bunyan:bunyan-lombok:0.2.0'
}
```

#### Note: Clash with org.slf4j:slf4j-api

Due to Lombok requiring the factory to be a `org.slf4j.LoggerFactory` class, you cannot include `bunyan-lombok` in a
project which already includes the standard SLF4J dependency. But if that were already a dependency, you must already
have a logging interface included in your project, reducing the usefulness of Bunyan ;-).
