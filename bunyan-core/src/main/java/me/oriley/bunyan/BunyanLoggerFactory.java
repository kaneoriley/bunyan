package me.oriley.bunyan;

import android.support.annotation.NonNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static me.oriley.bunyan.Bunyan.getLoggerName;

@SuppressWarnings("unused")
public final class BunyanLoggerFactory {

    @NonNull
    private static final ConcurrentMap<Class, BunyanCoreLogger> mLoggerMap = new ConcurrentHashMap<>();

    @NonNull
    public static BunyanCoreLogger getLogger(@NonNull Class<?> c) {
        BunyanCoreLogger logger = mLoggerMap.get(c);
        if (logger == null) {
            BunyanCoreLogger newLogger = new BunyanCoreLogger(c);
            BunyanCoreLogger oldLogger = mLoggerMap.putIfAbsent(c, newLogger);
            logger = oldLogger == null ? newLogger : oldLogger;
        }
        return logger;
    }
}
