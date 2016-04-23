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
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class BunyanLoggerFactory implements ILoggerFactory {

    @NonNull
    private final ConcurrentMap<String, Logger> mLoggerMap = new ConcurrentHashMap<>();

    @NonNull
    public Logger getLogger(@NonNull String name) {
        String loggerName = getLoggerName(name);
        Logger logger = mLoggerMap.get(loggerName);
        if (logger == null) {
            Logger newLogger = new BunyanCoreLogger(loggerName);
            Logger oldLogger = mLoggerMap.putIfAbsent(loggerName, newLogger);
            logger = oldLogger == null ? newLogger : oldLogger;
        }
        return logger;
    }

    @NonNull
    private String getLoggerName(@NonNull String loggerName) {
        switch (Bunyan.getTagStyle()) {
            case RESTRICTED:
            case SHORT:
            default:
                return loggerName.substring(loggerName.lastIndexOf('.') + 1).trim();
            case LONG:
            case FULL:
                return loggerName;
        }
    }
}