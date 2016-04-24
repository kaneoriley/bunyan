/*
 * Copyright (C) 2016 Kane O'Riley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.oriley.bunyan.crashlytics;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.util.Log;
import com.crashlytics.android.Crashlytics;

import me.oriley.bunyan.Bunyan.Level;
import me.oriley.bunyan.BunyanLogger;

@SuppressWarnings({"WeakerAccess", "unused"})
public class BunyanCrashlyticsLogger implements BunyanLogger {

    private final boolean mLogExceptions;


    public BunyanCrashlyticsLogger(@NonNull Class<? extends Crashlytics> c, boolean logExceptions) {
        // Class is not needed but ensures user has Crashlytics imported in their application
        mLogExceptions = logExceptions;
    }


    @Override
    public void logEvent(@NonNull Level level, @NonNull String tag, @NonNull String message, @Nullable Throwable t) {
        Crashlytics crashlytics = Crashlytics.getInstance();
        if (crashlytics == null || crashlytics.core == null) {
            // Not initialised yet, don't attempt
            return;
        }

        Crashlytics.log(formatLogMessage(level.priority, tag, message));
        if (t != null && mLogExceptions) {
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