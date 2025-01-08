package me.eduard.musicplayer.Library.Cache;

import me.eduard.musicplayer.Library.Cache.Player.MediaCache;
import me.eduard.musicplayer.Library.Cache.Window.WindowRegistry;
import me.eduard.musicplayer.Utils.Logging.LoggerFormatter;
import me.eduard.musicplayer.Utils.Logging.LoggerHandler;

import java.util.logging.Logger;

public class Caches {

    private static final Logger LOGGER = Logger.getLogger("Caches");

    static{
        LoggerHandler handler = new LoggerHandler();
        handler.setFormatter(new LoggerFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
    }

    public static void clearCaches(){
        int
            windowCacheSize = WindowRegistry.getRegistry().size(),
            songCacheSize   = SongRegistry.getRegistry().size(),
            mediaCacheSize  = MediaCache.getRegistry().size();
        if(windowCacheSize == 0 && songCacheSize == 0){
            LOGGER.info("Nothing was stored in the cache memory.\n");
            return;
        }
        WindowRegistry.clear();
        LOGGER.info("Removed "+windowCacheSize+" item(s) from application windows cache.");
        SongRegistry.clear();
        LOGGER.info("Removed "+songCacheSize+" item(s) from songs cache.");
        MediaCache.clear();
        LOGGER.info("Removed "+mediaCacheSize+" item(s) from media cache.");
        LOGGER.info("Caches were cleaned.\n");
    }
    public static int getStoredSongs(){
        return SongRegistry.getRegistry().size();
    }
    public static int getStoredWindows() {
        return WindowRegistry.getRegistry().size();
    }
    public static int getStoredMedias(){
        return MediaCache.getRegistry().size();
    }
    public static int getTotalItemsStored(){
        return getStoredSongs() + getStoredWindows() + getStoredMedias();
    }
}
