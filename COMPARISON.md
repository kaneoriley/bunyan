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
    for (int i = 0; i < 10000; i++) {
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

    |         | Logback 1.1.1-5               | Bunyan 0.4.1                 |
    |---------| ------------------------------|------------------------------|
    | 4.2.2   | 4219, 4052, 3590, 4229, 2844  | 1533, 1756, 2033, 1354, 1337 |
    | 4.2.2   | 4424, 4313, 3872, 2947, 3695  | 1482, 1475, 2148, 1836, 1823 |
    | 5.1.0   | 1103, 1027, 1042, 994, 984    | 469, 370, 368, 383, 299      |
    | 5.1.0   | 1026, 1132, 957, 1086, 948    | 453, 414, 439, 377, 374      |
    | 6.0.0   | 1053, 995, 748, 815, 978      | 392, 464, 441, 374, 348      |
    | 6.0.0   | 1092, 917, 883, 992, 883      | 413, 516, 397, 452, 398      |

And heres a quick comparison of the method counts and dex sizes:

    |                | Logback 1.1.1-5   | Bunyan 0.4.1   |
    |----------------| ------------------|----------------|
    | Core           | 2716 (287KB)      | 116 (17KB)     |
    | Dependencies   | 582 (48KB)        | 20 (7KB)       |
    |----------------| ------------------|----------------|
    | Total          | 3298 (335KB)      | 136 (24KB)     |

Make of this what you will, but I wanted to demonstrate that if fast, simple logging is what you're after, maybe give
Bunyan a go before moving on to one of the fully fledged SLF4J loggers.
