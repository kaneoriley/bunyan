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

import ch.qos.logback.classic.Level;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

@SuppressWarnings("unused")
public final class Logging {

    @SuppressWarnings("FieldCanBeLocal")
    @NonNull
    private final LoggerContext mContext;

    @SuppressWarnings("FieldCanBeLocal")
    @NonNull
    private final Logger mRootLogger;

    public Logging(@NonNull Level level) {
        mContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        mRootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        mRootLogger.setLevel(level);

        // Logcat logging always configured immediately.
        LogCatAppender appender = new LogCatAppender();
        appender.setContext(mContext);
        appender.start();
        mRootLogger.addAppender(appender);
    }
}
