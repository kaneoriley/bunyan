package org.slf4j;

import android.support.annotation.NonNull;
import me.oriley.bunyan.BunyanCoreLogger;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class Logger extends BunyanCoreLogger {


    Logger(@NonNull String name) {
        super(name);
    }


}
