package me.eduard.musicplayer.Utils.Logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LoggerFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
        return "["+record.getLoggerName()+"] "+record.getLevel().getName()+": "+record.getMessage()+"\n";
    }
}
