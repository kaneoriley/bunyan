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

package me.oriley.bunyan;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import me.oriley.bunyan.Bunyan.Level;

@SuppressWarnings("WeakerAccess")
public final class BunyanTagEncoder {

    private static final String BUNYAN_CORE_LOGGER = BunyanCoreLogger.class.getName();

    private static final String NAME_SHORT = "%n";
    private static final String NAME_LONG = "%N";
    private static final String METHOD = "%m";
    private static final String THREAD_IF_NOT_MAIN = "%t";
    private static final String THREAD_ALWAYS = "%T";
    private static final String LEVEL = "%l";

    @NonNull
    public String encodeTag(@NonNull String pattern,
                            @Level int level,
                            @NonNull String loggerName,
                            @Nullable Class loggerClass) {
        StringBuilder tagBuilder = new StringBuilder(pattern);

        int index = tagBuilder.indexOf(NAME_SHORT);
        if (index >= 0) {
            tagBuilder.replace(index, index + NAME_SHORT.length(), getTagName(loggerName, loggerClass, true));
        }

        index = tagBuilder.indexOf(NAME_LONG);
        if (index >= 0) {
            tagBuilder.replace(index, index + NAME_LONG.length(), getTagName(loggerName, loggerClass, false));
        }

        index = tagBuilder.indexOf(METHOD);
        if (index >= 0) {
            String methodName = getMethodName();
            tagBuilder.replace(index, index + METHOD.length(), methodName != null ? methodName : "");
        }

        index = tagBuilder.indexOf(THREAD_ALWAYS);
        if (index >= 0) {
            tagBuilder.replace(index, index + THREAD_ALWAYS.length(), getThreadName());
        }

        index = tagBuilder.indexOf(THREAD_IF_NOT_MAIN);
        if (index >= 0) {
            String threadName = Looper.myLooper() != Looper.getMainLooper() ? getThreadName() : "";
            tagBuilder.replace(index, index + THREAD_IF_NOT_MAIN.length(), threadName);
        }

        index = tagBuilder.indexOf(LEVEL);
        if (index >= 0) {
            tagBuilder.replace(index, index + LEVEL.length(), getLevelString(level));
        }

        return tagBuilder.toString();
    }

    @Nullable
    protected final String getMethodName() {
        boolean foundLocalClass = false;
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (int i = 2; i < elements.length; i++) {
            StackTraceElement element = elements[i];
            if (BUNYAN_CORE_LOGGER.equals(element.getClassName())) {
                foundLocalClass = true;
            } else if (foundLocalClass) {
                return element.getMethodName();
            }
        }

        // Couldn't find
        return null;
    }

    @NonNull
    protected final String getThreadName() {
        return Thread.currentThread().getName();
    }

    @NonNull
    protected final String getLevelString(@Level int level) {
        switch (level) {
            case Log.VERBOSE:
                return "V";
            case Log.DEBUG:
                return "D";
            case Log.INFO:
                return "I";
            case Log.WARN:
                return "W";
            case Log.ERROR:
                return "E";
            case Log.ASSERT:
                return "A";
            default:
                return "?";
        }
    }

    @NonNull
    protected String getTagName(@NonNull String loggerName, @Nullable Class c, boolean shortName) {
        if (c == null) {
            return loggerName;
        } else {
            String className = c.getName();
            return shortName ? className.substring(className.lastIndexOf('.') + 1).trim() : className;
        }
    }
}