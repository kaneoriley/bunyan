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
import me.oriley.bunyan.Bunyan.Level;

@SuppressWarnings("unused")
public class BunyanCoreLogger {

    @NonNull
    private final String mName;


    public BunyanCoreLogger(@NonNull String name) {
        mName = name;
    }


    // region TRACE

    public void trace(String msg) {
        log(Level.TRACE, msg, (Throwable) null);
    }

    public void trace(String format, Object... argArray) {
        log(Level.TRACE, format, argArray);
    }

    public void trace(String msg, Throwable t) {
        log(Level.TRACE, msg, t);
    }

    // endregion TRACE

    // region DEBUG

    public void debug(String msg) {
        log(Level.DEBUG, msg, (Throwable) null);
    }

    public void debug(String format, Object... argArray) {
        log(Level.DEBUG, format, argArray);
    }

    public void debug(String msg, Throwable t) {
        log(Level.DEBUG, msg, t);
    }

    // endregion DEBUG

    // region INFO

    public void info(String msg) {
        log(Level.INFO, msg, (Throwable) null);
    }

    public void info(String format, Object... argArray) {
        log(Level.INFO, format, argArray);
    }

    public void info(String msg, Throwable t) {
        log(Level.INFO, msg, t);
    }

    // endregion INFO

    // region WARN

    public void warn(String msg) {
        log(Level.WARN, msg, (Throwable) null);
    }

    public void warn(String format, Object... argArray) {
        log(Level.WARN, format, argArray);
    }

    public void warn(String msg, Throwable t) {
        log(Level.WARN, msg, t);
    }

    // endregion WARN

    // region ERROR

    public void error(String msg) {
        log(Level.ERROR, msg, (Throwable) null);
    }

    public void error(String format, Object... argArray) {
        log(Level.ERROR, format, argArray);
    }

    public void error(String msg, Throwable t) {
        log(Level.ERROR, msg, t);
    }

    // endregion ERROR

    private void log(@NonNull Level level, @Nullable String format, @Nullable Object... argArray) {
        if (Bunyan.isLoggable(level)) {
            FormattingPair ft = MessageFormatter.formatArray(format, argArray);
            Bunyan.logEvent(level, mName, ft.getMessage(), ft.getThrowable());
        }
    }

    private void log(@NonNull Level level, @Nullable String message, @Nullable Throwable throwable) {
        if (Bunyan.isLoggable(level)) {
            Bunyan.logEvent(level, mName, message, throwable);
        }
    }
}