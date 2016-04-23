package me.oriley.bunyan;

import android.support.annotation.Nullable;

final class FormattingPair {

    @Nullable
    private final String mMessage;

    @Nullable
    private final Throwable mThrowable;

    FormattingPair(String message) {
        this(message, null);
    }

    FormattingPair(@Nullable String message, @Nullable Throwable throwable) {
        mMessage = message;
        mThrowable = throwable;
    }

    @Nullable
    String getMessage() {
        return mMessage;
    }

    @Nullable
    Throwable getThrowable() {
        return mThrowable;
    }
}