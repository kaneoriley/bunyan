package me.oriley.bunyan;

import android.support.annotation.NonNull;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("unused")
public class BunyanLoggerFactory {

    @NonNull
    private static final ConcurrentMap<String, Logger> mLoggerMap = new ConcurrentHashMap<>();

    @NonNull
    public static Logger getLogger(@NonNull Class<?> clazz) {
        String loggerName = getLoggerName(clazz.getName());
        Logger logger = mLoggerMap.get(loggerName);
        if (logger == null) {
            Logger newLogger = new Logger(loggerName);
            Logger oldLogger = mLoggerMap.putIfAbsent(loggerName, newLogger);
            logger = oldLogger == null ? newLogger : oldLogger;
        }
        return logger;
    }

    @NonNull
    private static String getLoggerName(@NonNull String loggerName) {
        switch (Bunyan.getTagStyle()) {
            case RESTRICTED:
            case SHORT:
            default:
                return loggerName.substring(loggerName.lastIndexOf('.') + 1).trim();
            case LONG:
            case FULL:
                return loggerName;
        }
    }
}
