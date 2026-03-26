package me.eduard.musicplayer.Library.Cache.Player;

import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import me.eduard.musicplayer.Library.SimplePair;

import java.awt.desktop.SystemEventListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MediaCache {

    private static final Map<String, SimplePair<Media, Media>> MEDIA_INSTANCES = new ConcurrentHashMap<>();

    public static void register(String dirAbsPath, String videoURL, String audioURL){
        try {
            if(isInCache(dirAbsPath) || dirAbsPath == null){
                return;
            }
            MEDIA_INSTANCES.put(dirAbsPath, SimplePair.of(new Media(videoURL), new Media(audioURL)));
        }catch (MediaException exception){
            exception.printStackTrace(System.err);
            System.out.println("If the song directory contains only Audio file, it means it's Audio-Only mode. If so, you can ignore above warning");
        }
    }

    public static boolean isInCache(String dirAbsPath){
        return MEDIA_INSTANCES.containsKey(dirAbsPath);
    }
    public static void remove(String dirAbsPath){
        MEDIA_INSTANCES.remove(dirAbsPath);
    }
    public static void clear(){
        MEDIA_INSTANCES.clear();
    }
    public static Media getVideoMedia(String dirAbsPath){
        return MEDIA_INSTANCES.get(dirAbsPath).getKey();
    }
    public static Media getAudioMedia(String dirAbsPath){
        return MEDIA_INSTANCES.get(dirAbsPath).getValue();
    }

    public static SimplePair<Media, Media> getAndRegister(String dirAbsPath, String videoURL, String audioURL){
        if(!isInCache(dirAbsPath)){
            register(dirAbsPath, videoURL, audioURL);
        }
        return SimplePair.of(getVideoMedia(dirAbsPath), getAudioMedia(dirAbsPath));
    }
    public static Map<String, SimplePair<Media, Media>> getRegistry(){
        return Map.copyOf(MEDIA_INSTANCES);
    }
}
