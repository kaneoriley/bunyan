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

package org.slf4j.impl;

import android.support.annotation.NonNull;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.impl.Bunyan.Level;

@SuppressWarnings({ "WeakerAccess", "unused" })
final class BunyanCoreLogger extends MarkerIgnoringBase {


    BunyanCoreLogger(String tag) {
        this.name = tag;
    }


    // region TRACE

    @Override
    public boolean isTraceEnabled() {
        return Bunyan.isLoggable(Level.TRACE);
    }

    @Override
    public void trace(String msg) {
        log(Level.TRACE, msg, (Throwable) null);
    }

    @Override
    public void trace(String format, Object arg) {
        log(Level.TRACE, format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        log(Level.TRACE, format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... argArray) {
        log(Level.TRACE, format, argArray);
    }

    @Override
    public void trace(String msg, Throwable t) {
        log(Level.TRACE, msg, t);
    }

    // endregion TRACE

    // region DEBUG

    @Override
    public boolean isDebugEnabled() {
        return Bunyan.isLoggable(Level.DEBUG);
    }

    @Override
    public void debug(String msg) {
        log(Level.DEBUG, msg, (Throwable) null);
    }

    @Override
    public void debug(String format, Object arg) {
        log(Level.DEBUG, format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        log(Level.DEBUG, format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... argArray) {
        log(Level.DEBUG, format, argArray);
    }

    @Override
    public void debug(String msg, Throwable t) {
        log(Level.DEBUG, msg, t);
    }

    // endregion DEBUG

    // region INFO

    @Override
    public boolean isInfoEnabled() {
        return Bunyan.isLoggable(Level.INFO);
    }

    @Override
    public void info(String msg) {
        log(Level.INFO, msg, (Throwable) null);
    }

    @Override
    public void info(String format, Object arg) {
        log(Level.INFO, format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        log(Level.INFO, format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... argArray) {
        log(Level.INFO, format, argArray);
    }

    @Override
    public void info(String msg, Throwable t) {
        log(Level.INFO, msg, t);
    }

    // endregion INFO

    // region WARN

    @Override
    public boolean isWarnEnabled() {
        return Bunyan.isLoggable(Level.WARN);
    }

    @Override
    public void warn(String msg) {
        log(Level.WARN, msg, (Throwable) null);
    }

    @Override
    public void warn(String format, Object arg) {
        log(Level.WARN, format, arg);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        log(Level.WARN, format, arg1, arg2);
    }

    @Override
    public void warn(String format, Object... argArray) {
        log(Level.WARN, format, argArray);
    }

    @Override
    public void warn(String msg, Throwable t) {
        log(Level.WARN, msg, t);
    }

    // endregion WARN

    // region ERROR

    @Override
    public boolean isErrorEnabled() {
        return Bunyan.isLoggable(Level.ERROR);
    }

    @Override
    public void error(String msg) {
        log(Level.ERROR, msg, (Throwable) null);
    }

    @Override
    public void error(String format, Object arg) {
        log(Level.ERROR, format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        log(Level.ERROR, format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... argArray) {
        log(Level.ERROR, format, argArray);
    }

    @Override
    public void error(String msg, Throwable t) {
        log(Level.ERROR, msg, t);
    }

    // endregion ERROR

    private void log(@NonNull Level level, String format, Object... argArray) {
        if (Bunyan.isLoggable(level)) {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
            Bunyan.logEvent(level, name, ft.getMessage(), ft.getThrowable());
        }
    }

    private void log(@NonNull Level level, String message, Throwable throwable) {
        if (Bunyan.isLoggable(level)) {
            Bunyan.logEvent(level, name, message, throwable);
        }
    }
}