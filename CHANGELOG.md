Change Log
==========

## Version 0.4.1 WIP

 *  API Change: Split Lombok helper module into four separate dependencies to cater for cases where there may
    be a namespace clash (there's now `-log4j`, `-log4j2`, `-slf4j` and `-xslf4j` modules for `@Log4j`, `@Log4j2`, `@Slf4J` and 
    `@XSlf4j` respectively)
 *  API Change: Now requires adding `bunyan-plugin` to the classpath, and applying the plugin to the application
    or library module. The plugin generates a configuration class at compile time, removing all need for reflection,
    input streams, and excess memory usage. Initialisation time is now < 0ms with no overhead, you're welcome :)
  * API Change: Rename `BunyanLogger` to `BunyanAppender` and related fields similarly
  * API Change: Appenders can now be specified in the configuration xml, provided they have a zero argument constructor.
    Appenders which require more than that should be added in the `attachBaseContext` method of your application.
  * Feature: Add support for `bunyan-overrides.xml` to specify build/flavor specific tweaks to the configuration
  * Feature: Support for constructing a logger with a hardcoded name (to complete Lombok compatbility)
  * Bugfix: Fix ASSERT level threshold parsing

## Version 0.4.0

_2016-04-25_

 *  Breaking API Change: `Bunyan.Level` is now an `IntDef` rather than an `enum`. This means any custom loggers will
    need to be updated for the new parameter type. Also, as the raw values are in reverse order now due to using the
    `android.util.Log` constants, any `level < threshold` or `level > threshold` parameters will need to switch
    comparators.
 *  Fix log output when a single array is passed as an argument (previously, and in other libraries, it would only log
    the first entry due to the varargs method).
 *  Simplify message formatter and reduce method count.
 *  Add support for Log.ASSERT/log.wtf level

## Version 0.3.1

_2016-04-25_

 *  Avoid using getResourceAsStream if possible (see http://blog.danlew.net/2013/08/20/joda_time_s_memory_issue_in_android/)
    (Incidentally, the library I made this to replace was using it so this is another thing that's been improved on)

## Version 0.3.0

_2016-04-24_

 *  Move Crashlytics logger to seperate module
 *  Simplify initialisation
 *  Allow specifying class level logging threshold overrides
 *  Move configuration to `assets/bunyan.xml`
 *  Fixes for initialisation order

## Version 0.2.0

_2016-04-23_

 *  Initial release.
