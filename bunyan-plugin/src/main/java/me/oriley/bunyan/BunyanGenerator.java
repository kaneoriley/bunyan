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
import com.squareup.javapoet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static javax.lang.model.element.Modifier.*;

public final class BunyanGenerator {

    // Deprecated options
    private static final String PACKAGE_NAME = BunyanGenerator.class.getPackage().getName();
    private static final String CLASS_NAME = "BunyanConfig";
    private static final String BUNYAN_HASH = BunyanHasher.getActualHash();

    private static final String BUNYAN_XML = "/bunyan.xml";
    private static final String BUNYAN_OVERRIDES_XML = "/bunyan-overrides.xml";

    private static final String XML_ATTR_CLASS = "class";
    private static final String XML_ATTR_LEVEL = "level";
    private static final String XML_ATTR_TAGPATTERN = "tagPattern";

    private static final String XML_GLOBAL = "global";
    private static final String XML_APPENDER = "appender";
    private static final String XML_LOGGER = "logger";

    private static final String METHOD_APPENDER_LIST = "getAppenderList";
    private static final String METHOD_APPENDER_TAGPATTERN_MAP = "getAppenderTagPatternMap";
    private static final String METHOD_LOGGER_THRESHOLD_MAP = "getLoggerThresholdMap";
    private static final String METHOD_GLOBAL_LEVEL = "getGlobalLevel";
    private static final String METHOD_GLOBAL_TAG_PATTERN = "getGlobalTagPattern";

    private static final String VAR_LOCAL_LIST = "list";
    private static final String VAR_LOCAL_MAP = "map";

    private static final String TAG_NAME_SHORT = "%n";
    private static final String TAG_NAME_LONG = "%N";
    private static final String TAG_METHOD = "%m";
    private static final String TAG_THREAD_IF_NOT_MAIN = "%t";
    private static final String TAG_THREAD_ALWAYS = "%T";
    private static final String TAG_LEVEL = "%l";

    private static final Logger log = LoggerFactory.getLogger(BunyanGenerator.class.getSimpleName());

    @NonNull
    private final Map<String, String> mAppenderTagPatterns = new HashMap<>();

    @NonNull
    private final Map<String, String> mLoggerThresholds = new HashMap<>();

    @NonNull
    private final List<String> mAppenders = new ArrayList<>();

    @NonNull
    private final String mBaseOutputDir;

    @NonNull
    private final String mVariantAssetDir;

    @NonNull
    private final String mTaskName;

    private final boolean mDebugLogging;

    @NonNull
    private String mGlobalLevel = "INFO";

    @NonNull
    private String mGlobalTagPattern = "%S";


    public BunyanGenerator(@NonNull String baseOutputDir,
                           @NonNull String taskName,
                           @NonNull String variantAssetDir,
                           boolean debugLogging) {
        mBaseOutputDir = baseOutputDir;
        mTaskName = taskName;
        mVariantAssetDir = variantAssetDir;
        mDebugLogging = debugLogging;

        log("BunyanGenerator constructed\n" +
                "    Output: " + mBaseOutputDir + "\n" +
                "    Asset: " + mVariantAssetDir + "\n" +
                "    Package: " + PACKAGE_NAME + "\n" +
                "    Class: " + CLASS_NAME + "\n" +
                "    Logging: " + mDebugLogging);
    }


    public void buildBunyan() {
        long startNanos = System.nanoTime();
        File variantDir = new File(mVariantAssetDir);
        if (!variantDir.exists() || !variantDir.isDirectory()) {
            log("Asset directory does not exist, aborting");
            return;
        }

        try {
            brewJava().writeTo(new File(mBaseOutputDir));
        } catch (IOException e) {
            logError("Failed to generate java", e, true);
        }

        long lengthMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        log("Time to build was " + lengthMillis + "ms");
    }

    public boolean isBunyanHashValid() {
        String bunyanOutputFile = mBaseOutputDir + '/' + PACKAGE_NAME.replace('.', '/') + "/" + CLASS_NAME + ".java";
        long startNanos = System.nanoTime();
        File file = new File(bunyanOutputFile);

        boolean returnValue = false;
        if (!file.exists()) {
            log("File " + bunyanOutputFile + " doesn't exist, hash invalid");
        } else if (!file.isFile()) {
            log("File " + bunyanOutputFile + " is not a file (?), hash invalid");
        } else {
            returnValue = isFileValid(file, getComments());
        }

        long lengthMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        log("Hash check took " + lengthMillis + "ms, was valid: " + returnValue);
        return returnValue;
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

    private boolean isFileValid(@NonNull File bunyanOutputFile, @NonNull String[] comments) {
        if (comments.length <= 0) {
            return false;
        }

        boolean isValid = true;
        try {
            FileReader reader = new FileReader(bunyanOutputFile);
            BufferedReader input = new BufferedReader(reader);

            for (String comment : comments) {
                String fileLine = input.readLine();
                if (fileLine == null || comment == null || !contains(fileLine, comment)) {
                    log("Aborting, comment: " + comment + ", fileLine: " + fileLine);
                    isValid = false;
                    break;
                } else {
                    log("Line valid, comment: " + comment + ", fileLine: " + fileLine);
                }
            }

            input.close();
            reader.close();
        } catch (IOException e) {
            logError("Error parsing file", e, false);
            isValid = false;
        }

        log("File check result -- isValid ? " + isValid);
        return isValid;
    }

    private void logError(@NonNull String message, @NonNull Throwable error, boolean throwError) {
        log.error("Bunyan: " + message, error);
        if (throwError) {
            throw new IllegalStateException("Bunyan: Fatal Exception");
        }
    }

    private void log(@NonNull String message) {
        if (mDebugLogging) {
            log.warn(mTaskName + ": " + message);
        }
    }

    @NonNull
    private JavaFile brewJava() {

        TypeSpec.Builder builder = TypeSpec.classBuilder(CLASS_NAME)
                .addModifiers(PUBLIC, FINAL);

        try {
            parseFile(BUNYAN_XML);
        } catch (XmlPullParserException | IOException e) {
            logError("Failure parsing " + BUNYAN_XML, e, false);
        }

        try {
            parseFile(BUNYAN_OVERRIDES_XML);
        } catch (XmlPullParserException | IOException e) {
            logError("Failure parsing " + BUNYAN_OVERRIDES_XML, e, false);
        }

        builder.addMethod(createStringMethod(METHOD_GLOBAL_LEVEL, mGlobalLevel));
        builder.addMethod(createStringMethod(METHOD_GLOBAL_TAG_PATTERN, mGlobalTagPattern));
        builder.addMethod(createAppenderListMethod(mAppenders));
        builder.addMethod(createMapMethod(mLoggerThresholds, METHOD_LOGGER_THRESHOLD_MAP));
        builder.addMethod(createMapMethod(mAppenderTagPatterns, METHOD_APPENDER_TAGPATTERN_MAP));

        JavaFile.Builder javaBuilder = JavaFile.builder(PACKAGE_NAME, builder.build())
                .indent("    ");

        for (String comment : getComments()) {
            javaBuilder.addFileComment(comment + "\n");
        }

        return javaBuilder.build();
    }

    private void parseFile(@NonNull String fileName) throws XmlPullParserException, IOException {
        File file = new File(mVariantAssetDir + fileName);
        if (!file.exists()) {
            log(fileName + " does not exist, skipping");
            return;
        }

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();

        InputStream inputStream = new FileInputStream(new File(mVariantAssetDir + fileName));
        xpp.setInput(new InputStreamReader(inputStream));
        int eventType = xpp.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String name = xpp.getName();
                if (XML_LOGGER.equals(name)) {
                    String className = xpp.getAttributeValue(null, XML_ATTR_CLASS);
                    String levelName = xpp.getAttributeValue(null, XML_ATTR_LEVEL);
                    if (isEmpty(className) || isEmpty(levelName)) {
                        log("Invalid logger specified: " + className + " -- " + levelName);
                        eventType = xpp.next();
                        continue;
                    }
                    mLoggerThresholds.put(className, levelName);
                } else if (XML_APPENDER.equals(name)) {
                    String className = xpp.getAttributeValue(null, XML_ATTR_CLASS);
                    if (isEmpty(className)) {
                        log("Invalid appender specified: " + className);
                        eventType = xpp.next();
                        continue;
                    }

                    if (!mAppenders.contains(className)) {
                        mAppenders.add(className);
                    }

                    String tagPattern = xpp.getAttributeValue(null, XML_ATTR_TAGPATTERN);
                    if (!isEmpty(tagPattern)) {
                        int nameCount = getMatchCount(tagPattern, TAG_NAME_LONG);
                        nameCount += getMatchCount(tagPattern, TAG_NAME_SHORT);
                        if (nameCount != 1) {
                            throw new IllegalArgumentException(fileName + " must contain exactly one of " + TAG_NAME_LONG + " or " + TAG_NAME_SHORT + "\n" +
                                "Found " + nameCount + " in " + tagPattern);
                        }

                        int threadCount = getMatchCount(tagPattern, TAG_THREAD_IF_NOT_MAIN);
                        threadCount += getMatchCount(tagPattern, TAG_THREAD_ALWAYS);
                        if (threadCount > 1) {
                            throw new IllegalArgumentException(fileName + " cannot have more than one of " + TAG_THREAD_IF_NOT_MAIN + " or " + TAG_THREAD_ALWAYS+ "\n" +
                                    "Found " + threadCount + " in " + tagPattern);
                        }

                        int methodCount = getMatchCount(tagPattern, TAG_METHOD);
                        if (methodCount > 1) {
                            throw new IllegalArgumentException(fileName + " cannot have more than one of " + TAG_METHOD + "\n" +
                                    "Found " + methodCount + " in " + tagPattern);
                        }

                        int levelCount = getMatchCount(tagPattern, TAG_LEVEL);
                        if (levelCount > 1) {
                            throw new IllegalArgumentException(fileName + " cannot have more than one of " + TAG_LEVEL + "\n" +
                                    "Found " + levelCount + " in " + tagPattern);
                        }
                        mAppenderTagPatterns.put(className, tagPattern);
                    }
                } else if (XML_GLOBAL.equals(name)) {
                    String globalLevel = xpp.getAttributeValue(null, XML_ATTR_LEVEL);
                    if (!isEmpty(globalLevel)) {
                        mGlobalLevel = globalLevel;
                    }

                    String globalTagPattern = xpp.getAttributeValue(null, XML_ATTR_TAGPATTERN);
                    if (!isEmpty(globalTagPattern)) {
                        mGlobalTagPattern = globalTagPattern;
                    }
                }
            }

            eventType = xpp.next();
        }

        closeQuietly(inputStream);
    }

    @NonNull
    private MethodSpec createAppenderListMethod(@NonNull List<String> classes) {
        TypeName type = TypeVariableName.get(Class.class);
        TypeName typeName = ParameterizedTypeName.get(ClassName.get(ArrayList.class), type);

        MethodSpec.Builder builder = MethodSpec.methodBuilder(METHOD_APPENDER_LIST)
                .addModifiers(STATIC)
                .returns(typeName);

        builder.addStatement("$T $L = new $T()", typeName, VAR_LOCAL_LIST, typeName);

        for (String className : classes) {
            builder.addStatement("$L.add($L.class)", VAR_LOCAL_LIST, className);
        }
        builder.addStatement("return $L", VAR_LOCAL_LIST);

        return builder.build();
    }

    @NonNull
    private MethodSpec createMapMethod(@NonNull Map<String, String> entries, @NonNull String methodName) {
        TypeName stringType = TypeVariableName.get(String.class);
        TypeName typeName = ParameterizedTypeName.get(ClassName.get(HashMap.class), stringType, stringType);

        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(STATIC)
                .returns(typeName);

        builder.addStatement("$T $L = new $T()", typeName, VAR_LOCAL_MAP, typeName);

        for (Map.Entry<String, String> entry : entries.entrySet()) {
            builder.addStatement("$L.put($S, $S)", VAR_LOCAL_MAP, entry.getKey(), entry.getValue());
        }
        builder.addStatement("return $L", VAR_LOCAL_MAP);

        return builder.build();
    }

    @NonNull
    private MethodSpec createStringMethod(@NonNull String methodName, @NonNull String value) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(STATIC)
                .returns(String.class);
        builder.addStatement("return $S", value);
        return builder.build();
    }

    private static int getMatchCount(@NonNull String string, @NonNull String pattern) {
        int lastIndex = 0;
        int count = 0;

        while(lastIndex != -1){
            lastIndex = string.indexOf(pattern, lastIndex);
            if (lastIndex != -1) {
                count ++;
                lastIndex += pattern.length();
            }
        }
        return count;
    }

    private static boolean isEmpty(@Nullable String string) {
        return string == null || string.trim().length() <= 0;
    }

    private static boolean contains(@Nullable String str, @Nullable String searchStr) {
        return str != null && searchStr != null && str.contains(searchStr);
    }

    @NonNull
    private String[] getComments() {
        return new String[]{BUNYAN_HASH, "Package: " + PACKAGE_NAME, "Class: " + CLASS_NAME, "Debug: " + mDebugLogging};
    }
}