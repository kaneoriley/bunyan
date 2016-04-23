package org.slf4j;

import android.support.annotation.NonNull;
import me.oriley.bunyan.BunyanCoreLogger;

public final class Logger extends BunyanCoreLogger {


    public Logger(@NonNull String name) {
        super(name);
    }


}
