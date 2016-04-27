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

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.util.Log;
import com.crashlytics.android.Crashlytics;

import me.oriley.bunyan.Bunyan.Level;
import me.oriley.bunyan.BunyanAppender;

@SuppressWarnings({"WeakerAccess", "unused"})
public class BunyanCrashlyticsAppender implements BunyanAppender {

    @Override
    public final void logEvent(@Level int level, @NonNull String tag, @NonNull String message, @Nullable Throwable t) {
        Crashlytics crashlytics = Crashlytics.getInstance();
        if (crashlytics == null || crashlytics.core == null) {
            // Not initialised yet, don't attempt
            return;
        }

        logEventInternal(level, tag, message, t);
    }

    @CallSuper
    protected void logEventInternal(@Level int level, @NonNull String tag, @NonNull String message, @Nullable Throwable t) {
        Crashlytics.log(formatLogMessage(level, tag, message));
    }

    @NonNull
    private static String formatLogMessage(@Level int level, String tag, String msg) {
        String priorityString;
        switch (level) {
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