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

package org.slf4j;

import android.support.annotation.NonNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static me.oriley.bunyan.Bunyan.getLoggerName;

/*
 * Shim class to hook into Lombok's @Slf4j annotation and provide a BunyanLogger
 */
@SuppressWarnings("unused")
public final class LoggerFactory {

    @NonNull
    private static final ConcurrentMap<String, Logger> mLoggerMap = new ConcurrentHashMap<>();

    @NonNull
    public static Logger getLogger(@NonNull Class<?> clazz) {
        String loggerName = getLoggerName(clazz);
        Logger logger = mLoggerMap.get(loggerName);
        if (logger == null) {
            Logger newLogger = new Logger(loggerName);
            Logger oldLogger = mLoggerMap.putIfAbsent(loggerName, newLogger);
            logger = oldLogger == null ? newLogger : oldLogger;
        }
        return logger;
    }
}