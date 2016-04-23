[![Release](https://jitpack.io/v/com.github.oriley-me/bunyan.svg)](https://jitpack.io/#com.github.oriley-me/bunyan) [![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0) [![Build Status](https://travis-ci.org/oriley-me/bunyan.svg?branch=master)](https://travis-ci.org/oriley-me/bunyan) [![Dependency Status](https://www.versioneye.com/user/projects/56b6a5840a0ff50035ba881d/badge.svg?style=flat)](https://www.versioneye.com/user/projects/56b6a5840a0ff50035ba881d)
<a href="http://www.methodscount.com/?lib=me.oriley%3Abunyan%3A0.1.1"><img src="https://img.shields.io/badge/bunyan_runtime-methods: 77 | deps: 20 | size: 16 KB-f44336.svg"></img></a>

# Bunyan
![Logo](artwork/icon.png)

Logger factory with simple Logcat and Crashlytics loggers built in.

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

Usage is the same as SLF4J based logging. The included `org.slf4j.LoggerFactory` class takes care of creating logger
instances for your classes. It is a replacement for the original SLF4J factory so that anyone using [Project Lombok](https://projectlombok.org/)
can still use the `@Slf4j` annotation to automatically create a logger for your class (it injects the call to
`LoggerFactory.getLogger(MyClass.class)` for you).

If you don't use Project Lombok, you will need to create a field in each class where you wish to use Bunyan, like so:

```java
public class MyClass {

    // Not required if using Lomboks @Slf4j annotation
    private static final Logger log = LoggerFactory.getLogger(MyClass.class);

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
}
```

If you like the efficiency and style of SLF4J, this library will give you that but with a much lower method count. Of
course not all features of SLF4J are implemented in order to keeps things simple and keep the size down, so if you find
it too limiting or something is missing, I would encourage you to look at any of the other available libraries which
include a full distribution.

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
