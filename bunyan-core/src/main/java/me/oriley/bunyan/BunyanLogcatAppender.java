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

/*
 * Simple logcat appender
 *
 * If TagStyle set to restricted, will limit length to 23 characters (based on https://github.com/mvysny/slf4j-handroid)
 *
 * Log messages are line wrapped every 4000 characters due to limit (based on https://github.com/JakeWharton/timber)
 */
@SuppressWarnings("unused")
public final class BunyanLogcatAppender extends BunyanAppender {

    private static final int MAX_MSG_LENGTH = 4000;
    private static final int MAX_TAG_LENGTH = 23;

    @Override
    public void logEvent(@Level int level,
                         @NonNull String tag,
                         @NonNull String message,
                         @Nullable Throwable t) {
        if (message.length() < MAX_MSG_LENGTH) {
            Log.println(level, tag, message);
        } else {
            for (int i = 0, length = message.length(); i < length; i++) {
                int newline = message.indexOf('\n', i);
                newline = newline != -1 ? newline : length;

                while (i < newline) {
                    int end = Math.min(newline, i + MAX_MSG_LENGTH);
                    String part = message.substring(i, end);
                    Log.println(level, tag, part);
                    i = end;
                }
            }
        }

        if (t != null) {
            t.printStackTrace();
        }
    }
}
