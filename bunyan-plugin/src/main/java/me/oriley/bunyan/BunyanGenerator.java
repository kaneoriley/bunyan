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
    private static final String XML_ATTR_TAGSTYLE = "tagstyle";

    private static final String XML_GLOBAL = "global";
    private static final String XML_APPENDER = "appender";
    private static final String XML_LOGGER = "logger";

    private static final String METHOD_APPENDER_LIST = "getAppenderList";
    private static final String METHOD_CLASS_THRESHOLD_MAP = "getClassThresholdMap";
    private static final String METHOD_GLOBAL_LEVEL = "getGlobalLevel";
    private static final String METHOD_TAG_STYLE = "getTagStyle";

    private static final String VAR_APPENDER_LIST = "appenderList";
    private static final String VAR_CLASS_THRESHOLD_MAP = "classThresholdMap";

    private static final Logger log = LoggerFactory.getLogger(BunyanGenerator.class.getSimpleName());

    @NonNull
    private final Map<String, String> mClassThresholds = new HashMap<>();

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
    private String mTagStyle = "SHORT";


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
        builder.addMethod(createStringMethod(METHOD_TAG_STYLE, mTagStyle));
        builder.addMethod(createAppenderListMethod(mAppenders));
        builder.addMethod(createClassThresholdMapMethod(mClassThresholds));

        JavaFile.Builder javaBuilder = JavaFile.builder(PACKAGE_NAME, builder.build())
                .indent("    ");

        for (String comment : getComments()) {
            javaBuilder.addFileComment(comment + "\n");
        }

        return javaBuilder.build();
    }

    private void parseFile(@NonNull String fileName) throws XmlPullParserException, IOException {
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
                        continue;
                    }
                    mClassThresholds.put(className, levelName);
                } else if (XML_APPENDER.equals(name)) {
                    String className = xpp.getAttributeValue(null, XML_ATTR_CLASS);
                    if (isEmpty(className)) {
                        log("Invalid appender specified: " + className);
                        continue;
                    }
                    mAppenders.add(className);
                } else if (XML_GLOBAL.equals(name)) {
                    String globalLevel = xpp.getAttributeValue(null, XML_ATTR_LEVEL);
                    if (!isEmpty(globalLevel)) {
                        mGlobalLevel = globalLevel;
                    }

                    String tagStyle = xpp.getAttributeValue(null, XML_ATTR_TAGSTYLE);
                    if (!isEmpty(tagStyle)) {
                        mTagStyle = tagStyle;
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

        builder.addStatement("$T $L = new $T()", typeName, VAR_APPENDER_LIST, typeName);

        for (String className : classes) {
            builder.addStatement("$L.add($L.class)", VAR_APPENDER_LIST, className);
        }
        builder.addStatement("return $L", VAR_APPENDER_LIST);

        return builder.build();
    }

    @NonNull
    private MethodSpec createClassThresholdMapMethod(@NonNull Map<String, String> classes) {
        TypeName stringType = TypeVariableName.get(String.class);
        TypeName typeName = ParameterizedTypeName.get(ClassName.get(HashMap.class), stringType, stringType);

        MethodSpec.Builder builder = MethodSpec.methodBuilder(METHOD_CLASS_THRESHOLD_MAP)
                .addModifiers(STATIC)
                .returns(typeName);

        builder.addStatement("$T $L = new $T()", typeName, VAR_CLASS_THRESHOLD_MAP, typeName);

        for (Map.Entry<String, String> entry : classes.entrySet()) {
            builder.addStatement("$L.put($S, $S)", VAR_CLASS_THRESHOLD_MAP, entry.getKey(), entry.getValue());
        }
        builder.addStatement("return $L", VAR_CLASS_THRESHOLD_MAP);

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