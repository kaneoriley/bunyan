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

A short summary is that Bunyan is about 20% faster and has 4% of the method count when compared to Logback.

## Configuration

Bunyan requires you to add a `bunyan.xml` to your root `assets` folder, to read configuration details. If this file is
not present, there are default values which will be used.

A basic configuration file that will perform standard logging is as follows:

```xml
<bunyan>
    <!-- global threshold and tag style -->
    <global level="INFO" tagstyle="SHORT"/> <!-- These are the default values -->

    <!-- Simple logcat appender, explained below -->
    <appender class="me.oriley.bunyan.BunyanLogcatAppender"/>
</bunyan>
```

There are 6 acceptable values for logging threshold `level`:

 * ASSERT:  Threshold for `android.util.Log.ASSERT` / `log.wtf()`
 * ERROR:  Threshold for `android.util.Log.ERROR` / `log.error()`
 * WARN:   Threshold for `android.util.Log.WARN` / `log.warn()`
 * INFO:   Threshold for `android.util.Log.INFO` / `log.info()`
 * DEBUG:  Threshold for `android.util.Log.DEBUG` / `log.debug()`
 * TRACE:  Threshold for `android.util.Log.VERBOSE` / `log.trace()`

Any logs with a lower priority than the value specified will not be passed along to any of the appenders.

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

The gradle plugin `bunyan-plugin` (more below) will parse this configuration and generate a class file at compile time,
so that at runtime the static initialisation takes < 0ms and uses no `InputStream` or `getResourceAsStream` methods that
other libraries use and can introduce lag in application startup, as well as bloating memory consumption.

You can override individual configuration options in a `bunyan-overrides.xml` file placed in your root `assets` folder.
This can be used to change specific values for certain build types or flavors, without needing to copy and paste the
entire base configuration everywhere.

Included are three sample appenders, that you will need to add to your configuration xml if you would like to have them
used. The same method applies to any custom appenders you create, provided they have a zero argument constructor.

#### BunyanLogcatAppender

```xml
<bunyan>
    ...
    <appender class="me.oriley.bunyan.BunyanLogcatAppender"/>
</bunyan>
```

Takes no arguments and appends all logs to Logcat. This should be used unless you have implemented your own appender
to handle this.

#### BunyanCrashlyticsAppender and BunyanCrashlyticsExceptionAppender

Note: Requires an extra dependency (listed in the dependency section below)

There are two different Crashlytics appenders. Only include one of them, depending on your desired usage, otherwise
all logs will be sent to the Crashlytics SDK twice.

```xml
<bunyan>
    ...
    <appender class="me.oriley.bunyan.crashlytics.BunyanCrashlyticsAppender"/>
</bunyan>
```

Will forward all logs on to the Crashlytics SDK, which will use them when sending in crash reports.

```xml
<bunyan>
    ...
    <appender class="me.oriley.bunyan.crashlytics.BunyanCrashlyticsExceptionAppender"/>
</bunyan>
```

The same as `BunyanCrashlyticsAppender`, except all logged exceptions will also be reported to Crashlytics as
Non-Fatals. Use this if you like to keep track of exceptions you are logging.

Remember, no appenders are automatically installed, so by default no logging will be done if you don't add any to your
configuration.

## Custom Appenders

You also have the option of adding your own custom appenders for capturing logs to send to your analytics platform of
choice. Simply implement the `BunyanAppender` interface and handle log events as required. You will also need to add
an entry for the appender to your configuration xml.

```xml
<bunyan>
    ...
    <!-- Where MyAnalyticsAppender is your custom appender with a zero argument constructor -->
    <appender class="com.my.app.MyAnalyticsAppender"/>
</bunyan>
```

If you need to perform initialisation on your appender, you can add it manually from your code. I would suggest doing
it inside your `Application`s `attachBaseContext` method, so that it can be capturing logs as soon as possible.

```java
protected void attachBaseContext(Context base) {
    ...
    // Where MyComplicatedAppender is your custom appender
    Bunyan.addAppender(new MyComplicatedAppender(this, "Needs", "Arguments")); 
}
```

There is also an `addAppenders` method that takes a varargs array, so you can add multiple at the same time:

```java
protected void attachBaseContext(Context base) {
    ...
    Bunyan.addAppenders(new Appender1(context), new Appender2(context));
}
```

## Usage

You will need to create a field in each class where you wish to use Bunyan, like so:

```java
public class MyClass {

    private static final BunyanCoreLogger log = new BunyanCoreLogger(MyClass.class);

}
```

Once that is done, you can use the logging interface as per usual. A few examples:

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

If you use [Project Lombok](https://projectlombok.org/), there are four extra modules that can allow you to hook into
the automatic `log` field generation that is provided. You should only include one, depending on which annotation you
wish to use to create your log fields. Because Lombok will look for specific classes that are available in the relevant
logging framework, make sure to choose one that does not exist in your application, to avoid namespace clashes


    |---------------------| ------------------------------------------------------------------|
    | Lombok Annotation   | Required Dependency                                               |
    |---------------------| ------------------------------------------------------------------|
    | @Log4j              | compile 'com.github.oriley-me.bunyan:bunyan-lombok-log4j:0.4.0'   |
    | @Log4j2             | compile 'com.github.oriley-me.bunyan:bunyan-lombok-log4j2:0.4.0'  |
    | @Slf4j              | compile 'com.github.oriley-me.bunyan:bunyan-lombok-slf4j:0.4.0'   |
    | @XSlf4j             | compile 'com.github.oriley-me.bunyan:bunyan-lombok-xslf4j:0.4.0'  |
    |---------------------| ------------------------------------------------------------------|

After including the correct dependency, you can annotate a class to have the the field generated as per usual:

```java
@Slf4j // If you included the -slf4j dependency
public class MyClass {
    // log field is automatically generated by the Lombok plugin
}
```

## Gradle Dependency

 * Add JitPack.io repo and `bunyan-plugin` dependency to your buildscript:

```gradle
buildscript {
    repositories {
        maven { url "https://jitpack.io" }
    }

    dependencies {
        classpath 'com.github.oriley-me.bunyan:bunyan-plugin:0.4.1'
    }
}
```

 * Add JitPack.io to your app projects repositories list:

```gradle
repositories {
    maven { url "https://jitpack.io" }
}
```

 * Apply the plugin to your application or library project, and add the module runtime dependency:

```gradle
apply plugin: 'com.android.application' || apply plugin: 'com.android.library'
apply plugin: 'me.oriley.bunyan-plugin'

...

dependencies {
    // Required
    compile 'com.github.oriley-me.bunyan:bunyan-core:0.4.1'

    // Only necessary if you plan on using a BunyanCrashlyticsAppender/BunyanCrashlyticsExceptionAppender
    compile 'com.github.oriley-me.bunyan:bunyan-crashlytics:0.4.1'

    // Make sure include any Lombok helper module you require here
}
```

If you would like to check out the latest development version, please substitute all versions for `develop-SNAPSHOT`.
Keep in mind that it is very likely things could break or be unfinished, so stick the official releases if you want
things to be more predictable.