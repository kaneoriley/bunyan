Comparison
==========

Out of curiosity, I put together a quick (and by no means comprehensive) benchmark test between Bunyan and [Logback](http://tony19.github.io/logback-android/)

Let me first say that this is by no means bashing the authors of Logback. It was my go to logging framework until I
realised I didn't need the majority of what it provided, but if you do have extra requirements that Bunyan cannot
satisfy I would highly recommend it (hence why it's the best candidate for a comparison).

The following is the code which was executed to perform the benchmark:

```java
private void logTest() {
    Drawable drawable = ContextCompat.getDrawable(this, R.drawable.splash_logo);
    Object[] objArray = new Object[] { 7, "WORD", this, new String[] { "Some", "Words" }, drawable};

    long startMillis = System.currentTimeMillis();
    for (int i = 0; i < 1000; i++) {
        log.debug("Single object: {}", drawable);
        log.debug("Single array: {}", objArray);
        log.debug("Multiple objects: {}, {}, {}", this, drawable, 9);
    }

    long finishMillis = System.currentTimeMillis();
    log.debug("Duration: {}ms", finishMillis - startMillis);
}
```

In both cases I used the same logcat appender (with minor adjustments to hook into the SLF4J/Logback framework, but
nothing of consequence to runtime logging performance). Tests were performed using Genymotion emulators.

The results were as follows (all figures are the execution time in milliseconds):

    |         | Logback                  | Bunyan                  |
    |---------| -------------------------|-------------------------|
    | 4.2.2   | 994, 876, 884, 913, 874  | 723, 840, 769, 737, 791 |
    | 4.2.2   | 872, 910, 903, 900, 911  | 761, 812, 733, 742, 744 |
    | 5.1.0   | 463, 350, 323, 420, 452  | 263, 303, 286, 290, 230 |
    | 5.1.0   | 384, 410, 514, 375, 396  | 219, 243, 285, 235, 204 |
    | 6.0.0   | 356, 379, 410, 483, 420  | 256, 271, 276, 307, 266 |
    | 6.0.0   | 396, 387, 502, 493, 469  | 296, 274, 291, 295, 249 |

And heres a quick comparison of the method counts and dex sizes:

    |                | Logback 1.1.1-5   | Bunyan 0.4.0   |
    |----------------| ------------------|----------------|
    | Core           | 2716 (287KB)      | 123 (17KB)     |
    | Dependencies   | 582 (48KB)        | 20 (7KB)       |
    |----------------| ------------------|----------------|
    | Total          | 3298 (335KB)      | 143 (24KB)     |

Make of this what you will, but I wanted to demonstrate that if fast, simple logging is what you're after, maybe give
Bunyan a go before moving on to one of the fully fledged SLF4J loggers.