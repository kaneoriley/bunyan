package me.oriley.bunyan;

import android.support.annotation.Nullable;

@SuppressWarnings("WeakerAccess")
final class FormattingPair {

    @Nullable
    public final String message;

    @Nullable
    public final Throwable throwable;

    FormattingPair(String message) {
        this(message, null);
    }

    FormattingPair(@Nullable String message, @Nullable Throwable throwable) {
        this.message = message;
        this.throwable = throwable;
    }
}