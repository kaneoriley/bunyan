package org.slf4j.impl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.util.Log;
import com.crashlytics.android.Crashlytics;

import org.slf4j.impl.Bunyan.Level;

@SuppressWarnings({"WeakerAccess", "unused"})
public class BunyanCrashlyticsLogger implements BunyanLogger {


    public BunyanCrashlyticsLogger(@NonNull Class<? extends Crashlytics> crashlyticsClass) {
        // We don't need the class reference, but it prevents addition when Crashlytics is not included in the app.
    }

    @Override
    public void logEvent(@NonNull Level level, @NonNull String tag, @NonNull String message, @Nullable Throwable t) {
        Crashlytics crashlytics = Crashlytics.getInstance();
        if (crashlytics == null || crashlytics.core == null) {
            // Not initialised yet, don't attempt
            return;
        }

        Crashlytics.log(formatLogMessage(level.priority, tag, message));
        if (t != null && Bunyan.isLogExceptionsToCrashlytics()) {
            Crashlytics.logException(t);
        }
    }

    @NonNull
    private static String formatLogMessage(int priority, String tag, String msg) {
        String priorityString;
        switch (priority) {
            case Log.VERBOSE:
                priorityString = "V";
                break;
            case Log.DEBUG:
                priorityString = "D";
                break;
            case Log.INFO:
                priorityString = "I";
                break;
            case Log.WARN:
                priorityString = "W";
                break;
            case Log.ERROR:
                priorityString = "E";
                break;
            case Log.ASSERT:
                priorityString = "A";
                break;
            default:
                priorityString = "?";
        }
        return priorityString + "/" + tag + " " + msg;
    }
}