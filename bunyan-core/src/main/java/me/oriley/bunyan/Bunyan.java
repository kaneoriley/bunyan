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
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class Bunyan {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Log.ASSERT, Log.ERROR, Log.WARN, Log.INFO, Log.DEBUG, Log.VERBOSE})
    public @interface Level {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TAG_STYLE_RESTRICTED, TAG_STYLE_SHORT, TAG_STYLE_LONG, TAG_STYLE_FULL})
    public @interface TagStyle {
    }

    public static final int TAG_STYLE_RESTRICTED = 0;
    public static final int TAG_STYLE_SHORT = 1;
    public static final int TAG_STYLE_LONG = 2;
    public static final int TAG_STYLE_FULL = 3;

    private static final String TAG = Bunyan.class.getSimpleName();

    private static final String BUNYAN_CLASS = getEnclosingClassName(Bunyan.class.getName());

    private static final int INVALID = -1;

    @NonNull
    private static final List<BunyanAppender> sAppenders = new ArrayList<>();

    @NonNull
    @Level
    private static final Map<String, Integer> sClassThresholds = new HashMap<>();

    @Level
    private static int sGlobalThreshold = Log.INFO;

    @TagStyle
    private static int sTagStyle = TAG_STYLE_SHORT;

    static {
        // Load config from plugin generated shim.
        sGlobalThreshold = parseLevel(BunyanConfig.getGlobalLevel());
        sTagStyle = parseTagStyle(BunyanConfig.getTagStyle());

        for (Map.Entry<String, String> entry : BunyanConfig.getClassThresholdMap().entrySet()) {
            sClassThresholds.put(entry.getKey(), parseLevel(entry.getValue()));
        }

        for (Class c : BunyanConfig.getAppenderList()) {
            try {
                BunyanAppender bunyanAppender = (BunyanAppender) c.newInstance();
                sAppenders.add(bunyanAppender);
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

    @TagStyle
    private static int parseTagStyle(@Nullable String style) {
        if ("RESTRICTED".equals(style)) {
            return TAG_STYLE_RESTRICTED;
        } else if ("SHORT".equals(style)) {
            return TAG_STYLE_SHORT;
        } else if ("LONG".equals(style)) {
            return TAG_STYLE_LONG;
        } else if ("FULL".equals(style)) {
            return TAG_STYLE_FULL;
        } else {
            Log.e(TAG, "Invalid tag style " + style + ", using default (SHORT)");
            return TAG_STYLE_SHORT;
        }
    }

    @Level
    static int getThreshold(@NonNull String className) {
        if (sClassThresholds.containsKey(className)) {
            //noinspection ResourceType
            return sClassThresholds.get(className);
        } else {
            return sGlobalThreshold;
        }
    }

    @TagStyle
    static int getTagStyle() {
        return sTagStyle;
    }

    public static void addAppender(@NonNull BunyanAppender appender) {
        sAppenders.add(appender);
    }

    public static void addAppenders(@NonNull BunyanAppender... appenders) {
        Collections.addAll(sAppenders, appenders);
    }

    static void logEvent(@Level int level,
                         @NonNull String name,
                         @Nullable String message,
                         @Nullable Throwable t) {
        if (message == null) {
            // Not logging a null message. That would be stupid.
            return;
        }

        String methodName = "";

        if (sTagStyle == TAG_STYLE_FULL) {
            boolean foundLocalClass = false;

            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            for (StackTraceElement element : trace) {
                boolean isLocal = BUNYAN_CLASS.equals(getEnclosingClassName(element.getClassName()));
                if (isLocal) {
                    foundLocalClass = true;
                } else if (foundLocalClass) {
                    methodName = "[" + element.getMethodName() + "]";
                    break;
                }
            }
        }

        for (BunyanAppender appender : sAppenders) {
            appender.logEvent(level, name + methodName, message, t);
        }
    }

    @NonNull
    private static String getEnclosingClassName(@NonNull String className) {
        return className.substring(0, className.lastIndexOf('.'));
    }

    @NonNull
    static String getLoggerName(@NonNull String className) {
        if (sTagStyle >= TAG_STYLE_LONG) {
            return className;
        } else {
            return className.substring(className.lastIndexOf('.') + 1).trim();
        }
    }
}
