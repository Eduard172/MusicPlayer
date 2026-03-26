package me.eduard.musicplayer.Utils;

import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class KeyCombinationUtils {
    private static final List<String> KEYS_PRESSED = new ArrayList<>();
    public static void registerKey(KeyCode key){
        if(KEYS_PRESSED.contains(key.toString().toUpperCase())){
            return;
        }
        KEYS_PRESSED.add(key.toString().toUpperCase());
    }
    public static void removeKey(KeyCode key){
        KEYS_PRESSED.remove(key.toString().toUpperCase());
    }
    public static void clear(){
        KEYS_PRESSED.clear();
    }
    public static boolean isKey(KeyCode key, int index){
        try {
            return KEYS_PRESSED.get(index).equals(key.toString().toUpperCase());
        }catch (IndexOutOfBoundsException exception){
            return false;
        }
    }
    public static List<String> keys(){
        return KEYS_PRESSED;
    }
}
