/*
 * Copyright (C) 2016 Kane O'Riley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package me.oriley.bunyan;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

public final class Bunyan {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Log.ASSERT, Log.ERROR, Log.WARN, Log.INFO, Log.DEBUG, Log.VERBOSE})
    public @interface Level {
    }

    private static final String TAG = Bunyan.class.getSimpleName();

    @NonNull
    private static final List<BunyanAppender> sAppenders = new ArrayList<>();

    @NonNull
    @Level
    private static final Map<String, Integer> sLoggerThresholds = new HashMap<>();

    @NonNull
    private static final Map<String, String> sAppenderTagPatterns;

    @Level
    private static int sGlobalThreshold = Log.INFO;

    private static final BunyanTagEncoder sTagEncoder = new BunyanTagEncoder();

    static {
        // Load config from plugin generated shim.
        sGlobalThreshold = parseLevel(BunyanConfig.getGlobalLevel());
        String globalTagPattern = BunyanConfig.getGlobalTagPattern();
        if (TextUtils.isEmpty(globalTagPattern)) {
            globalTagPattern = "%S";
        }

        for (Map.Entry<String, String> entry : BunyanConfig.getLoggerThresholdMap().entrySet()) {
            sLoggerThresholds.put(entry.getKey(), parseLevel(entry.getValue()));
        }

        sAppenderTagPatterns = BunyanConfig.getAppenderTagPatternMap();
        for (Class c : BunyanConfig.getAppenderList()) {
            try {
                BunyanAppender bunyanAppender = (BunyanAppender) c.newInstance();
                sAppenders.add(bunyanAppender);

                String name = c.getName();
                if (sAppenderTagPatterns.containsKey(name)) {
                    bunyanAppender.setTagPattern(sAppenderTagPatterns.get(name));
                } else {
                    bunyanAppender.setTagPattern(globalTagPattern);
                }
            } catch (Throwable t) {
                Log.e(TAG, "Error creating appender: " + c, t);
            }
        }
    }

    @Level
    private static int parseLevel(@Nullable String level) {
        if ("TRACE".equals(level)) {
            return Log.VERBOSE;
        } else if ("DEBUG".equals(level)) {
            return Log.DEBUG;
        } else if ("INFO".equals(level)) {
            return Log.INFO;
        } else if ("WARN".equals(level)) {
            return Log.WARN;
        } else if ("ERROR".equals(level)) {
            return Log.ERROR;
        } else if ("ASSERT".equals(level)) {
            return Log.ASSERT;
        } else {
            Log.e(TAG, "Invalid level " + level + ", using default (INFO)");
            return Log.INFO;
        }
    }

    @Level
    static int getThreshold(@NonNull String loggerName) {
        if (sLoggerThresholds.containsKey(loggerName)) {
            //noinspection ResourceType
            return sLoggerThresholds.get(loggerName);
        } else {
            return sGlobalThreshold;
        }
    }

    public static void addAppender(@NonNull BunyanAppender appender) {
        sAppenders.add(appender);
    }

    public static void addAppenders(@NonNull BunyanAppender... appenders) {
        Collections.addAll(sAppenders, appenders);
    }

    static void logEvent(@Level int level,
                         @NonNull String loggerName,
                         @NonNull Class loggerClass,
                         @Nullable String message,
                         @Nullable Throwable t) {
        if (message == null) {
            // Not logging a null message. That would be stupid.
            return;
        }

        for (BunyanAppender appender : sAppenders) {
            String tag = sTagEncoder.encodeTag(appender.getTagPattern(), level, loggerName, loggerClass);
            appender.logEvent(level, tag, message, t);
        }
    }
}
