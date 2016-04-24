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

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import dalvik.system.BaseDexClassLoader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class Bunyan {

    private static final String TAG = Bunyan.class.getSimpleName();

    private static final String BUNYAN_CLASS = getEnclosingClassName(Bunyan.class.getName());
    private static final String BUNYAN_CONFIG = "assets/bunyan.xml";

    private static final String XML_ATTR_CLASS = "class";
    private static final String XML_ATTR_LEVEL = "level";
    private static final String XML_ATTR_TAGSTYLE = "tagstyle";

    private static final String XML_GLOBAL = "global";
    private static final String XML_LOGGER = "logger";

    // Constants for finding zip file name via reflection. Facebook makes this relatively safe, google it ;)
    private static final String ORIGINAL_PATH = "originalPath";
    private static final String DEX_ELEMENTS = "dexElements";
    private static final String PATH_LIST = "pathList";
    private static final String DEX_PATH_LIST_CLASS_NAME = "dalvik.system.DexPathList";
    private static final String DEX_PATH_LIST_ELEMENT_CLASS_NAME = "dalvik.system.DexPathList$Element";
    private static final String ZIP = "zip";

    @NonNull
    private static final List<BunyanLogger> sLoggers = new ArrayList<>();

    @NonNull
    private static final Map<String, Level> sClassThresholds = new HashMap<>();

    @NonNull
    private static Level sGlobalThreshold = Level.INFO;

    @NonNull
    private static TagStyle sTagStyle = TagStyle.SHORT;

    public enum Level {
        ERROR(Log.ERROR), WARN(Log.WARN), INFO(Log.INFO), DEBUG(Log.DEBUG), TRACE(Log.VERBOSE);

        public final int priority;

        Level(int priority) {
            this.priority = priority;
        }

        @Nullable
        static Level parse(@Nullable String levelName) {
            for (Level level : values()) {
                if (level.name().equals(levelName)) {
                    return level;
                }
            }

            // Couldn't find
            return null;
        }
    }

    public enum TagStyle {
        RESTRICTED, SHORT, LONG, FULL;

        @Nullable
        static TagStyle parse(@Nullable String styleName) {
            for (TagStyle style : values()) {
                if (style.name().equals(styleName)) {
                    return style;
                }
            }

            // Couldn't find
            return null;
        }
    }

    static {
        try {
            parseConfig();
        } catch (Throwable t) {
            Log.e(TAG, "Error reading XML config, using defaults", t);
        }
    }

    private static void parseConfig() throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();

        InputStream inputStream = getZipInputStream();
        if (inputStream == null) {
            // Not ideal, see http://blog.danlew.net/2013/08/20/joda_time_s_memory_issue_in_android/
            inputStream = Bunyan.class.getClassLoader().getResourceAsStream(BUNYAN_CONFIG);
            Log.w(TAG, "Using getResourceAsStream: " + inputStream);
        } else {
            Log.d(TAG, "Using zipInputStream: " + inputStream);
        }

        xpp.setInput(new InputStreamReader(inputStream));
        int eventType = xpp.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String name = xpp.getName();
                if (XML_LOGGER.equals(name)) {
                    String levelName = xpp.getAttributeValue(null, XML_ATTR_LEVEL);
                    Level level = Level.parse(levelName);
                    if (level == null) {
                        Log.e(TAG, "Invalid level specified: " + levelName);
                        continue;
                    }

                    String className = xpp.getAttributeValue(null, XML_ATTR_CLASS);
                    if (TextUtils.isEmpty(className)) {
                        Log.e(TAG, "Invalid class specified: " + className);
                        continue;
                    }

                    sClassThresholds.put(className, level);
                } else if (XML_GLOBAL.equals(name)) {
                    String levelName = xpp.getAttributeValue(null, XML_ATTR_LEVEL);
                    Level level = Level.parse(levelName);
                    if (level == null) {
                        Log.e(TAG, "Invalid level specified: " + levelName);
                        continue;
                    }

                    String styleName = xpp.getAttributeValue(null, XML_ATTR_TAGSTYLE);
                    TagStyle style = TagStyle.parse(styleName);
                    if (style == null) {
                        Log.e(TAG, "Invalid style specified: " + styleName);
                        continue;
                    }

                    sGlobalThreshold = level;
                    sTagStyle = style;
                }
            }

            eventType = xpp.next();
        }

        closeQuietly(inputStream);
    }

    /*
     * Try to find zip path so we don't need to rely on using ClassLoader.getResourceAsStream()
     *
     * Saves memory by not exposing us to the leak described here: http://blog.danlew.net/2013/08/20/joda_time_s_memory_issue_in_android/
     *
     * Reflection is always ugly but I haven't seen this one fail, and the fields we are accessing are luckily
     * the same as what Facebook reflect on, which in the past has proven enough of a reason for the Android engineers
     * not to change things too much (they once reverted a field name because Facebook were reflecting on it and they
     * didn't want it to crash lol).
     *
     */
    @Nullable
    private static InputStream getZipInputStream() {
        ClassLoader cl = Bunyan.class.getClassLoader();
        ZipFile zipFile = null;

        try {
            if (!(cl instanceof BaseDexClassLoader)) {
                Log.w(TAG, "ClassLoader was invalid class: " + cl);
                return null;
            }

            BaseDexClassLoader dx = (BaseDexClassLoader) cl;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                String originalPath = getFieldValue(BaseDexClassLoader.class, dx, ORIGINAL_PATH);
                if (originalPath != null) {
                    zipFile = new ZipFile(originalPath);
                } else {
                    Log.w(TAG, "Unable to retrieve originalPath");
                }
            } else {
                Class dexPathListClass = getClassForName(DEX_PATH_LIST_CLASS_NAME);
                if (dexPathListClass == null) {
                    Log.w(TAG, "Unable to retrieve DexPathList class");
                    return null;
                }

                Class elementClass = getClassForName(DEX_PATH_LIST_ELEMENT_CLASS_NAME);
                if (elementClass == null) {
                    Log.w(TAG, "Unable to retrieve DexPathList$Element class");
                    return null;
                }

                Object dexPathList = getFieldValue(BaseDexClassLoader.class, dx, PATH_LIST);
                Object[] elementArray = null;
                if (dexPathList != null) {
                    elementArray = getFieldValue(dexPathListClass, dexPathList, DEX_ELEMENTS);
                }

                if (elementArray == null || elementArray.length <= 0) {
                    Log.w(TAG, "Element array is invalid: " + Arrays.toString(elementArray));
                    return null;
                }

                for (Object object : elementArray) {
                    File file = getFieldValue(elementClass, object, ZIP);
                    if (file != null) {
                        zipFile = new ZipFile(file);
                        break;
                    }
                }
            }

            if (zipFile != null) {
                ZipEntry entry = zipFile.getEntry(BUNYAN_CONFIG);
                return zipFile.getInputStream(entry);
            } else {
                Log.e(TAG, "Unable to find ZipFile");
                return null;
            }
        } catch (Throwable t) {
            Log.e(TAG, "Error retrieving input stream zip file", t);
            closeQuietly(zipFile);
            return null;
        }
    }

    @NonNull
    static Level getThreshold(@NonNull String className) {
        if (sClassThresholds.containsKey(className)) {
            return sClassThresholds.get(className);
        } else {
            return sGlobalThreshold;
        }
    }

    @NonNull
    static TagStyle getTagStyle() {
        return sTagStyle;
    }

    public static void addLogger(@NonNull BunyanLogger logger) {
        sLoggers.add(logger);
    }

    public static void addLoggers(@NonNull BunyanLogger... loggers) {
        Collections.addAll(sLoggers, loggers);
    }

    static void logEvent(@NonNull Level level,
                         @NonNull String name,
                         @Nullable String message,
                         @Nullable Throwable t) {
        if (message == null) {
            // Not logging a null message. That would be stupid.
            return;
        }

        String methodName = "";

        if (sTagStyle == TagStyle.FULL) {
            boolean foundLocalClass = false;

            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            for (StackTraceElement element : trace) {
                boolean isLocal = BUNYAN_CLASS.equals(getEnclosingClassName(element.getClassName()));
                if (isLocal) {
                    foundLocalClass = true;
                } else if (foundLocalClass) {
                    methodName = "[" + element.getMethodName() + "]";
                    break;
                }
            }
        }

        for (BunyanLogger logger : sLoggers) {
            logger.logEvent(level, name + methodName, message, t);
        }
    }

    @NonNull
    private static String getEnclosingClassName(@NonNull String className) {
        return className.substring(0, className.lastIndexOf('.'));
    }

    @NonNull
    static String getLoggerName(@NonNull String className) {
        switch (sTagStyle) {
            case RESTRICTED:
            case SHORT:
            default:
                return className.substring(className.lastIndexOf('.') + 1).trim();
            case LONG:
            case FULL:
                return className;
        }
    }

    // region Utils

    @Nullable
    private static Class getClassForName(@NonNull String className) {
        try {
            return Class.forName(className);
        } catch (Throwable t) {
            Log.e(TAG, "Class " + className + " not found");
            return null;
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static <T> T getFieldValue(@NonNull Class<?> clazz,
                                       @Nullable Object obj,
                                       @NonNull String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(obj);
        } catch (Throwable t) {
            Log.e(TAG, "error retrieving field " + fieldName + " from class " + clazz, t);
            return null;
        }
    }

    private static void closeQuietly(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // Ignored
            }
        }
    }

    // endregion Utils
}
