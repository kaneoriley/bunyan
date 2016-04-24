package org.slf4j;

import android.support.annotation.NonNull;
import me.oriley.bunyan.BunyanCoreLogger;

/*
 * Shim class to hook into Lombok's @Slf4j annotation and provide a BunyanLogger
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class Logger extends BunyanCoreLogger {


    Logger(@NonNull Class c) {
        super(c);
    }


}
