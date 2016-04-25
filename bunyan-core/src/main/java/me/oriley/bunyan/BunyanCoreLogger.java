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
import me.oriley.bunyan.Bunyan.Level;

@SuppressWarnings("unused")
public class BunyanCoreLogger {

    @Level
    private final int mThreshold;

    @NonNull
    private final String mName;


    public BunyanCoreLogger(@NonNull Class c) {
        String className = c.getName();
        mThreshold = Bunyan.getThreshold(className);
        mName = Bunyan.getLoggerName(className);
    }


    // region TRACE

    public void trace(String msg) {
        log(Log.VERBOSE, msg, (Throwable) null);
    }

    public void trace(String format, Object... argArray) {
        log(Log.VERBOSE, format, argArray);
    }

    public void trace(String msg, Throwable t) {
        log(Log.VERBOSE, msg, t);
    }

    // endregion TRACE

    // region DEBUG

    public void debug(String msg) {
        log(Log.DEBUG, msg, (Throwable) null);
    }

    public void debug(String format, Object... argArray) {
        log(Log.DEBUG, format, argArray);
    }

    public void debug(String msg, Throwable t) {
        log(Log.DEBUG, msg, t);
    }

    // endregion DEBUG

    // region INFO

    public void info(String msg) {
        log(Log.INFO, msg, (Throwable) null);
    }

    public void info(String format, Object... argArray) {
        log(Log.INFO, format, argArray);
    }

    public void info(String msg, Throwable t) {
        log(Log.INFO, msg, t);
    }

    // endregion INFO

    // region WARN

    public void warn(String msg) {
        log(Log.WARN, msg, (Throwable) null);
    }

    public void warn(String format, Object... argArray) {
        log(Log.WARN, format, argArray);
    }

    public void warn(String msg, Throwable t) {
        log(Log.WARN, msg, t);
    }

    // endregion WARN

    // region ERROR

    public void error(String msg) {
        log(Log.ERROR, msg, (Throwable) null);
    }

    public void error(String format, Object... argArray) {
        log(Log.ERROR, format, argArray);
    }

    public void error(String msg, Throwable t) {
        log(Log.ERROR, msg, t);
    }

    // endregion ERROR

    // region ASSERT

    public void wtf(String msg) {
        log(Log.ASSERT, msg, (Throwable) null);
    }

    public void wtf(String format, Object... argArray) {
        log(Log.ASSERT, format, argArray);
    }

    public void wtf(String msg, Throwable t) {
        log(Log.ASSERT, msg, t);
    }

    // endregion ASSERT

    private void log(@Level int level, @Nullable String format, @Nullable Object... argArray) {
        if (isLoggable(level)) {
            FormattingPair ft = MessageFormatter.formatArray(format, argArray);
            Bunyan.logEvent(level, mName, ft.message, ft.throwable);
        }
    }

    private void log(@Level int level, @Nullable String message, @Nullable Throwable throwable) {
        if (isLoggable(level)) {
            Bunyan.logEvent(level, mName, message, throwable);
        }
    }

    private boolean isLoggable(@Level int level) {
        return level >= mThreshold;
    }
}