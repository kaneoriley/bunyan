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
public final class BunyanLogcatLogger implements BunyanLogger {

    private static final int MAX_MSG_LENGTH = 4000;
    private static final int MAX_TAG_LENGTH = 23;

    @Override
    public void logEvent(@Level int level,
                         @NonNull String tag,
                         @NonNull String message,
                         @Nullable Throwable t) {
        message = sanitiseMessage(message).trim();
        tag = sanitiseTag(tag);

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

    @NonNull
    private static String sanitiseTag(@NonNull String name) {
        int length = name.length();
        if (length <= MAX_TAG_LENGTH || Bunyan.getTagStyle() != Bunyan.TAG_STYLE_RESTRICTED) {
            return name;
        }

        int tagLength = 0;
        int lastTokenIndex = 0;
        int lastPeriodIndex;
        StringBuilder tagName = new StringBuilder(MAX_TAG_LENGTH + 3);
        while ((lastPeriodIndex = name.indexOf('.', lastTokenIndex)) != -1) {
            tagName.append(name.charAt(lastTokenIndex));
            // token of one character appended as is otherwise truncate it to one character
            int tokenLength = lastPeriodIndex - lastTokenIndex;
            if (tokenLength > 1) {
                tagName.append('*');
            }
            tagName.append('.');
            lastTokenIndex = lastPeriodIndex + 1;

            // check if name is already too long
            tagLength = tagName.length();
            if (tagLength > MAX_TAG_LENGTH) {
                return getSimpleName(name);
            }
        }

        // Either we had no useful dot location at all
        // or last token would exceed TAG_MAX_LENGTH
        int tokenLength = length - lastTokenIndex;
        if (tagLength == 0 || (tagLength + tokenLength) > MAX_TAG_LENGTH) {
            return getSimpleName(name);
        }

        // last token (usually class name) appended as is
        tagName.append(name, lastTokenIndex, length);
        return tagName.toString();
    }

    @NonNull
    private static String getSimpleName(String loggerName) {
        // Take leading part and append '*' to indicate that it was truncated
        int length = loggerName.length();
        int lastPeriodIndex = loggerName.lastIndexOf('.');
        return lastPeriodIndex != -1 && length - (lastPeriodIndex + 1) <= MAX_TAG_LENGTH
                ? loggerName.substring(lastPeriodIndex + 1)
                : '*' + loggerName.substring(length - MAX_TAG_LENGTH + 1);
    }

    @NonNull
    private static String sanitiseMessage(@NonNull String message) {
        final StringBuilder sb = new StringBuilder(message.length());
        boolean lastCharWasNewLine = false;

        for (int i = 0; i < message.length(); i++) {
            final char c = message.charAt(i);
            if (c != '\n' || lastCharWasNewLine) {
                sb.append((c >= 32 || c == '\n') ? c : ' ');
            }
            lastCharWasNewLine = c == '\n';
        }
        return sb.toString();
    }
}
