package me.eduard.musicplayer.Utils.Logging;

import java.util.logging.Logger;

public class LoggerUtils {

    public static Logger createOrGet(String loggerName) {
        Logger logger = Logger.getLogger(loggerName);
        logger.addHandler(new LoggerHandler(new LoggerFormatter()));
        logger.setUseParentHandlers(false);
        return logger;
    }

}
