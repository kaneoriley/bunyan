package me.oriley.bunyan;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;

final class MessageFormatter {

    private static final String PLACEHOLDER = "{}";

    @NonNull
    static FormattingPair formatArray(@Nullable String messagePattern, @Nullable Object[] argArray) {
        Throwable throwable = null;
        Object[] args = argArray;

        if (argArray != null && argArray.length != 0) {
            Object lastEntry = argArray[argArray.length - 1];

            if (lastEntry instanceof Throwable) {
                throwable = (Throwable) lastEntry;

                // Trim the array
                int trimmedLength = argArray.length - 1;
                args = new Object[trimmedLength];
                System.arraycopy(argArray, 0, args, 0, trimmedLength);
            }
        }

        return formatArray(messagePattern, args, throwable);
    }

    @NonNull
    private static FormattingPair formatArray(@Nullable String messagePattern, @Nullable Object[] arguments, @Nullable Throwable t) {
        if (messagePattern == null) {
            return new FormattingPair(null, t);
        } else if (arguments == null) {
            return new FormattingPair(messagePattern);
        } else {
            int i = 0;
            StringBuilder sbuf = new StringBuilder(messagePattern.length() + 50);

            int placeholderCount = 0;
            while (i < messagePattern.length()) {
                int j = messagePattern.indexOf(PLACEHOLDER, i);
                if (j > 0) {
                    placeholderCount++;
                    i = j + 1;
                } else {
                    break;
                }
            }

            if (placeholderCount == 1 && arguments.length > 1) {
                // Passed an array as the only argument, so wrap it for correct expansion
                arguments = new Object[] { arguments };
            }

            i = 0;
            for (int L = 0; L < arguments.length; ++L) {
                int j = messagePattern.indexOf(PLACEHOLDER, i);
                if (j == -1) {
                    if (i == 0) {
                        return new FormattingPair(messagePattern, t);
                    }

                    sbuf.append(messagePattern, i, messagePattern.length());
                    return new FormattingPair(sbuf.toString(), t);
                }

                // Check whether character is escape delimiter
                if (j != 0 && messagePattern.charAt(j - 1) == 92) {
                    // Check whether it is double escaped
                    if (j >= 2 && messagePattern.charAt(j - 2) == 92) {
                        --L;
                        sbuf.append(messagePattern, i, j - 1);
                        sbuf.append('{');
                        i = j + 1;
                    } else {
                        sbuf.append(messagePattern, i, j - 1);
                        deeplyAppendParameter(sbuf, arguments[L]);
                        i = j + 2;
                    }
                } else {
                    sbuf.append(messagePattern, i, j);
                    deeplyAppendParameter(sbuf, arguments[L]);
                    i = j + 2;
                }
            }

            sbuf.append(messagePattern, i, messagePattern.length());
            return new FormattingPair(sbuf.toString(), t);
        }
    }

    private static void deeplyAppendParameter(@NonNull StringBuilder sbuf, @Nullable Object o) {
        if (o == null) {
            sbuf.append("null");
        } else {
            if (!o.getClass().isArray()) {
                safeObjectAppend(sbuf, o);
            } else {
                safeObjectAppend(sbuf, Arrays.deepToString((Object[]) o));
            }
        }
    }

    private static void safeObjectAppend(@NonNull StringBuilder sbuf, @NonNull Object o) {
        try {
            String t = o.toString();
            sbuf.append(t);
        } catch (Throwable t) {
            System.err.println("Bunyan: Failed toString() invocation on an object of type [" + o.getClass().getName() + "]");
            System.err.println("Reported exception:");
            t.printStackTrace();
            sbuf.append("[FAILED toString()]");
        }
    }
}
