Change Log
==========

## Version 0.4.0 WIP

 *  Breaking API Change: `Bunyan.Level` is now an `IntDef` rather than an `enum`. This means any custom loggers will
    need to be updated for the new parameter type. Also, as the raw values are in reverse order now due to using the
    `android.util.Log` constants, any `level < threshold` or `level > threshold` parameters will need to switch
    comparators.

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
