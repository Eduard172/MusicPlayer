package me.eduard.musicplayer.Library.Cache;

import me.eduard.musicplayer.Components.Player.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SongRegistry {
    private static final Map<String, File> REGISTRY = new HashMap<>();
    public static boolean isInRegistry(String song){
        return REGISTRY.containsKey(song);
    }
    public static void register(String song, File file){
        REGISTRY.put(song, file);
    }
    public static void clear(){
        REGISTRY.clear();
    }
    public static void remove(String song){
        REGISTRY.remove(song);
    }
    public static Map<String, File> getRegistry(){
        return Map.copyOf(REGISTRY);
    }
    public static File getSong(String song){
        return REGISTRY.get(song);
    }
    public static File getAndRegister(String song, String path){
        if(!isInRegistry(song))
            register(song, Player.instance.getReformattedFile(new File(path)));
        return REGISTRY.get(song);
    }
}
