package me.oriley.bunyan;

import android.support.annotation.NonNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static me.oriley.bunyan.Bunyan.getLoggerName;

@SuppressWarnings("unused")
public final class BunyanLoggerFactory {

    @NonNull
    private static final ConcurrentMap<String, BunyanCoreLogger> mLoggerMap = new ConcurrentHashMap<>();

    @NonNull
    public static BunyanCoreLogger getLogger(@NonNull Class<?> clazz) {
        String loggerName = getLoggerName(clazz);
        BunyanCoreLogger logger = mLoggerMap.get(loggerName);
        if (logger == null) {
            BunyanCoreLogger newLogger = new BunyanCoreLogger(loggerName);
            BunyanCoreLogger oldLogger = mLoggerMap.putIfAbsent(loggerName, newLogger);
            logger = oldLogger == null ? newLogger : oldLogger;
        }
        return logger;
    }
}
