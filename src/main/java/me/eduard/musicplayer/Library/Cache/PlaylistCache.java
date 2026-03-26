package me.eduard.musicplayer.Library.Cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class PlaylistCache {

    private static final Map<String, List<String>> playlistCache = new HashMap<>();
    public static boolean isInRegistry(String playlist){
        return playlistCache.containsKey(playlist);
    }
    public static void register(String playlist, List<String> list){
        playlistCache.put(playlist, list);
    }
    public static void clear(){
        playlistCache.clear();
    }
    public static void remove(String playlist){
        playlistCache.remove(playlist);
    }
    public static Map<String, List<String>> getRegistry(){
        return Map.copyOf(playlistCache);
    }
    public static List<String> getPlaylist(String playlist){
        return playlistCache.get(playlist);
    }
    public static List<String> getAndRegister(String playlist, List<String> list){
        if(!isInRegistry(playlist))
            register(playlist, list);
        return playlistCache.get(playlist);
    }

}
