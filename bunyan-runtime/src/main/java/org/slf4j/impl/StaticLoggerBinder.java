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
import org.slf4j.spi.LoggerFactoryBinder;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class StaticLoggerBinder implements LoggerFactoryBinder {

    private static final String FACTORY_CLASS = BunyanLoggerFactory.class.getName();
    private static final String CRASHLYTICS = "com.crashlytics.android.Crashlytics";

    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    @NonNull
    private final ILoggerFactory mFactory;


    private StaticLoggerBinder() {
        mFactory = new BunyanLoggerFactory();
    }

    @NonNull
    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    @NonNull
    @Override
    public ILoggerFactory getLoggerFactory() {
        return mFactory;
    }

    @NonNull
    @Override
    public String getLoggerFactoryClassStr() {
        return FACTORY_CLASS;
    }
}
