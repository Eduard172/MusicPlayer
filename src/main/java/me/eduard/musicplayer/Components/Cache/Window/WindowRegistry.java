package me.eduard.musicplayer.Components.Cache.Window;

import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class WindowRegistry {
    private static final Map<String, Stage> WINDOW_REGISTRY = new HashMap<>();

    public static void register(final String identifier, Stage stage){
        WINDOW_REGISTRY.put(identifier, stage);
    }
    public static void remove(final String identifier){
        WINDOW_REGISTRY.remove(identifier);
    }
    public static void clear(){
        WINDOW_REGISTRY.clear();
    }
    public static boolean isInRegistry(String identifier){
        return WINDOW_REGISTRY.containsKey(identifier);
    }
    public static Stage getStage(String identifier){
        return WINDOW_REGISTRY.get(identifier);
    }
    public static Stage getAndRegister(String identifier, Stage stage){
        if(!WINDOW_REGISTRY.containsKey(identifier))
            register(identifier, stage);
        return WINDOW_REGISTRY.get(identifier);
    }

}
