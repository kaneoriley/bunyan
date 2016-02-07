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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

final class LogCatAppender extends AppenderBase<ILoggingEvent> {

    @Nullable
    private PatternLayoutEncoder mTagEncoder;

    @Nullable
    private PatternLayoutEncoder mMessageEncoder;

    @Override
    public void start() {
        if (mTagEncoder == null) {
            mTagEncoder = new PatternLayoutEncoder();
            mTagEncoder.setPattern("%logger[%method]%nopex");
            mTagEncoder.setContext(context);
            mTagEncoder.start();
        }
        if (mMessageEncoder == null) {
            mMessageEncoder = new PatternLayoutEncoder();
            mMessageEncoder.setPattern("%msg%n%ex");
            mMessageEncoder.setContext(context);
            mMessageEncoder.start();
        }
        super.start();
    }

    @Override
    protected void append(@NonNull ILoggingEvent event) {
        if (mTagEncoder != null && mMessageEncoder != null) {
            String tag = mTagEncoder.getLayout().doLayout(event);
            String msg = mMessageEncoder.getLayout().doLayout(event);
            logMsg(tag, msg, event.getLevel());
        }
    }

    private static void logMsg(@NonNull String tag, @NonNull String msg, @NonNull Level level) {
        switch (level.toInt()) {
            case Level.ALL_INT:
            case Level.TRACE_INT:
                Log.v(tag, msg);
                break;
            case Level.INFO_INT:
                Log.i(tag, msg);
                break;
            case Level.DEBUG_INT:
            default:
                Log.d(tag, msg);
                break;
            case Level.WARN_INT:
                Log.w(tag, msg);
                break;
            case Level.ERROR_INT:
                Log.e(tag, msg);
                break;
        }
    }
}