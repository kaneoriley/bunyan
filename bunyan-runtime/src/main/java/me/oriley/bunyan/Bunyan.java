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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class Bunyan {

    public enum Level {
        ERROR(Log.ERROR), WARN(Log.WARN), INFO(Log.INFO), DEBUG(Log.DEBUG), TRACE(Log.VERBOSE);

        public final int priority;

        Level(int priority) {
            this.priority = priority;
        }
    }

    public enum TagStyle {
        RESTRICTED, SHORT, LONG, FULL
    }

    private static final String BUNYAN_CLASS = getEnclosingClassName(Bunyan.class.getName());

    @NonNull
    private static final List<BunyanLogger> sLoggers = new ArrayList<>();

    @NonNull
    private static Level sLoggingLevel = Level.INFO;

    @NonNull
    private static TagStyle sTagStyle = TagStyle.SHORT;

    private static boolean sLogExceptionsToCrashlytics;

    public static boolean isLoggable(@NonNull Level level) {
        return level.ordinal() <= sLoggingLevel.ordinal();
    }

    public static void setThreshold(@NonNull Level level) {
        sLoggingLevel = level;
    }

    @NonNull
    public static TagStyle getTagStyle() {
        return sTagStyle;
    }

    public static void setTagStyle(@NonNull TagStyle tagStyle) {
        sTagStyle = tagStyle;
    }

    public static boolean isLogExceptionsToCrashlytics() {
        return sLogExceptionsToCrashlytics;
    }

    public static void setLogExceptionsToCrashlytics(boolean logExceptionsToCrashlytics) {
        sLogExceptionsToCrashlytics = logExceptionsToCrashlytics;
    }

    public static void addLogger(@NonNull BunyanLogger logger) {
        sLoggers.add(logger);
    }

    public static void addLogger(@NonNull BunyanLogger... loggers) {
        Collections.addAll(sLoggers, loggers);
    }

    public static void logEvent(@NonNull Level level,
                                @NonNull String name,
                                @Nullable String message,
                                @Nullable Throwable t) {
        if (message == null) {
            // Not logging a null message. That would be stupid.
            return;
        }

        String methodName = "";

        if (sTagStyle == TagStyle.FULL) {
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

        for (BunyanLogger logger : sLoggers) {
            logger.logEvent(level, name + methodName, message, t);
        }
    }

    @NonNull
    private static String getEnclosingClassName(@NonNull String className) {
        return className.substring(0, className.lastIndexOf('.'));
    }
}
