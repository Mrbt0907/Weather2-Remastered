package net.CoroUtil.forge;

import net.CoroUtil.config.ConfigCoroUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CULog {

    private static final Logger LOGGER = LogManager.getLogger(CoroUtil.modID);

    
    public static void log(String string) {
        if (ConfigCoroUtil.useLoggingLog) {
            LOGGER.info(string);
        }
    }

    
    public static void err(String string) {
        if (ConfigCoroUtil.useLoggingError) {
            LOGGER.error(string);
        }
    }

    
    public static void dbg(String string) {
        if (ConfigCoroUtil.useLoggingDebug) {
            LOGGER.info(string);
        }
    }

}