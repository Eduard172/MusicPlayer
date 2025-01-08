package me.eduard.musicplayer.Utils.Logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LoggerHandler extends ConsoleHandler {
    @Override
    public void publish(LogRecord record){
        if(record.getLevel() != Level.SEVERE && record.getLevel() != Level.WARNING){
            System.out.print(super.getFormatter().format(record));
        }else{
            System.err.print(super.getFormatter().format(record));
        }
    }
}
