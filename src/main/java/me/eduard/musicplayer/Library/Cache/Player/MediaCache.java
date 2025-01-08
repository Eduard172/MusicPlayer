package me.eduard.musicplayer.Library.Cache.Player;

import javafx.scene.media.Media;
import me.eduard.musicplayer.Library.BasicKeyValuePair;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MediaCache {

    private static final Map<String, BasicKeyValuePair<Media, Media>> MEDIA_INSTANCES = new ConcurrentHashMap<>();

    public static void register(String dirAbsPath, String videoURL, String audioURL){
        if(isInCache(dirAbsPath) || dirAbsPath == null){
            return;
        }
        MEDIA_INSTANCES.put(dirAbsPath, BasicKeyValuePair.of(new Media(videoURL), new Media(audioURL)));
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

    public static BasicKeyValuePair<Media, Media> getAndRegister(String dirAbsPath, String videoURL, String audioURL){
        if(!isInCache(dirAbsPath)){
            register(dirAbsPath, videoURL, audioURL);
        }
        return BasicKeyValuePair.of(getVideoMedia(dirAbsPath), getAudioMedia(dirAbsPath));
    }
    public static Map<String, BasicKeyValuePair<Media, Media>> getRegistry(){
        return Map.copyOf(MEDIA_INSTANCES);
    }
}
