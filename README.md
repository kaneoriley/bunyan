[![Release](https://jitpack.io/v/com.github.oriley-me/bunyan.svg)](https://jitpack.io/#com.github.oriley-me/bunyan)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://travis-ci.org/oriley-me/bunyan.svg?branch=master)](https://travis-ci.org/oriley-me/bunyan)
[![Dependency Status](https://www.versioneye.com/user/projects/571b91d2fcd19a00415b267b/badge.svg?style=flat)](https://www.versioneye.com/user/projects/571b91d2fcd19a00415b267b)

<a href="http://www.methodscount.com/?lib=com.github.oriley-me.bunyan%3Abunyan-core%3A0.4.0"><img src="https://img.shields.io/badge/bunyan_core-methods: 123 | deps: 20 | size: 17 KB-f44336.svg"></img></a>  
<a href="http://www.methodscount.com/?lib=com.github.oriley-me.bunyan%3Abunyan-crashlytics%3A0.4.0"><img src="https://img.shields.io/badge/bunyan_crashlytics-methods: 11 | additional deps: 0 | size: 3 KB-ff9800.svg"></img></a>  
<a href="http://www.methodscount.com/?lib=com.github.oriley-me.bunyan%3Abunyan-lombok%3A0.4.0"><img src="https://img.shields.io/badge/bunyan_lombok-methods: 6 | additional deps: 0 | size: 2 KB-ff9800.svg"></img></a>  

# Bunyan
![Logo](artwork/icon.png)

Logger factory with SLF4J message formatting, and support for custom logger extensions, without using any external
dependencies.

## Why?

I wanted to have the convenience and performance benefits of the SLF4J logging interface, without the overhead of
including the SLF4J dependency (500+ methods), and a third party wrapper (1000+). So I replicated the basic
functionality here, in an extremely small dependency (look at the top of the page for sizes/method counts).

Consider the following `Log` statement:

```java
Log.d("MyTag", "Value: " + value + ", Old Value: " + oldValue + ", Object: " + object.toString());
```

Using SLF4J/Bunyan, this could be rewritten as:
```java
log.debug("Value: {}, Old Value: {}, Object: {}", value, oldValue, object);
```

This results in no String concatenation being necessary until after we have checked whether the log is within the set
threshold level, resulting in less Object allocation, faster execution, and lower memory consumption.

Check out [this SLF4j documentation](http://www.slf4j.org/faq.html#logging_performance) for a more thorough explanation.

## Benchmark Comparison

Have a look at [COMPARISON.md](COMPARISON.md) for a quick benchmark comparison demonstrating the speed of Bunyan

A short summary is that Bunyan is faster than the library I tested against.

## Configuration

Bunyan requires you to add a `bunyan.xml` to your root `assets` folder, to read configuration details. If this file is
not present, there are default values which will be used.

A basic configuration file is as follows:

```xml
<bunyan>
    <!-- global threshold and tag style -->
    <global level="INFO" tagstyle="SHORT"/> <!-- These are the default values -->
</bunyan>
```

There are 5 acceptable values for logging threshold `level`:

 * ASSERT:  Threshold for `android.util.Log.ASSERT` / `log.wtf()`
 * ERROR:  Threshold for `android.util.Log.ERROR` / `log.error()`
 * WARN:   Threshold for `android.util.Log.WARN` / `log.warn()`
 * INFO:   Threshold for `android.util.Log.INFO` / `log.info()`
 * DEBUG:  Threshold for `android.util.Log.DEBUG` / `log.debug()`
 * TRACE:  Threshold for `android.util.Log.VERBOSE` / `log.trace()`

Any logs with a lower priority than the value specified will not be passed along to any of the loggers.

There are 4 acceptable values for `tagstyle`:

 * RESTRICTED:  Restrict to 23 characters (suggested maximum for `Log`)
 * SHORT:       The enclosing class simple name, i.e. `MyActivity`
 * LONG:        The enclosing class name including package, i.e. `com.myapp.MyActivity`
 * FULL:        Same as LONG but with calling method name appended, i.e. `com.myapp.MyActivity[onResume]`

 Note that FULL tags require traversing the stack trace to find method names, which could have an adverse impact on
 performance. This is why I would suggest only using FULL on debug builds, or passing in the method name as part of
 the message instead if necessary.

You can also set class specific thresholds to override the global level if you need to:

```xml
<bunyan>
    <!-- global default values and configuration -->
    <global level="INFO" tagstyle="SHORT"/>

    <!-- Class specific thresholds -->
    <logger class="my.app.SpamController" level="ERROR" /> <!-- To quieten a particularly noisy class temporarily -->
    <logger class="my.app.CurrentActivity" level="TRACE" /> <!-- For additional logging whilst developing -->
</bunyan>
```


Included are two sample loggers, that you will need to add to Bunyan manually inside a static block inside your
`Application` class, to ensure they are initialised early and can capture all logging in your application.

#### BunyanLogcatLogger

Takes no arguments and appends all logs to Logcat. This should be used unless you have implemented your own logger
to handle this.

```java
static {
    Bunyan.addLogger(new BunyanLogcatLogger());
}
```

#### BunyanCrashlyticsLogger

Note: Requires an extra dependency (listed in the dependency section below)

Takes 2 arguments. The first is `Crashlytics.class`, to prevent instantiation in projects where Crashlytics is not
available as it is not included as a compile dependency in Bunyan. The second parameter tells the logger whether any
logged exceptions should be sent to Crashlytics as a Non-Fatal (defaults to false).

```java
static {
    ...
    Bunyan.addLogger(new BunyanCrashlyticsLogger(Crashlytics.class, true));
}
```

No loggers are automatically installed, so by default no logging will be done if you don't add any. There is also an
`addLoggers` method that takes a varargs array, so you could simplify the above to:

```java
static {
    Bunyan.addLoggers(new BunyanLogcatLogger(), new BunyanCrashlyticsLogger(Crashlytics.class, true));
}
```

## Custom Loggers

You also have the option of adding your own custom loggers for capturing logs to send to your analytics platform of
choice. Simply implement the `BunyanLogger` interface and handle log events as required. You will also need to add
the logger to Bunyan inside the static initialisation block like so:

```java
static {
    ...
    Bunyan.addLogger(new MyAnalyticsLogger()); // Where MyAnalyticsLogger is your custom class
}
```

## Usage

There are two methods for using Bunyan, depending on whether you use [Project Lombok](https://projectlombok.org/) and like
to take advantage of the `@Slf4j` annotation. This requires an extra dependency (listed below) in order to create a shim
so that the code Lombok generates will be able to successfully retrieve a `BunyanCoreLogger` instance.

If you don't use Project Lombok, you will need to create a field in each class where you wish to use Bunyan, like so:

```java
public class MyClass {

    private static final BunyanCoreLogger log = new BunyanCoreLogger(MyClass.class);

}
```

If you do use Lombok and include the extra dependency, it will take care of the logger creation for you
by adding a simple annotation:

```java
@Slf4j
public class MyClass {
    // log field is automatically generated by the Lombok plugin
}
```

Some examples of using the SLF4J logging interface:

```java
public void myMethod(int value) {
    log.trace("method called, time: {}", System.currentTimeMillis());
    log.debug("current value: {}, new value: {}", mValue, value);
    log.info("slf4j logging is much nicer than android.util.Log: {}, how much?: {}", true, Integer.MAX_VALUE);
    log.warn("wtf, much logs");

    try {
        doSomethingThatMightCauseAnException();
    } catch (Exception e) {
        log.error("error running myMethod with parameter: {}", value, e);
    }
}
```

## Gradle Dependency

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
    compile 'com.github.oriley-me.bunyan:bunyan-core:0.4.0'

    // Only necessary if you plan on using a BunyanCrashlyticsLogger
    compile 'com.github.oriley-me.bunyan:bunyan-crashlytics:0.4.0'

    // Only necessary to take advantage of Lombok's @Slf4j annotations
    compile 'com.github.oriley-me.bunyan:bunyan-lombok:0.4.0'
}
```

#### Note: Clash with org.slf4j:slf4j-api

Due to Lombok requiring the factory to be a `org.slf4j.LoggerFactory` class, you cannot include `bunyan-lombok` in a
project which already includes the standard SLF4J dependency. But if that were already a dependency, you must already
have a logging interface included in your project, reducing the usefulness of Bunyan ;-).
