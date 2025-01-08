package me.eduard.musicplayer.Utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.MainApp;

import java.io.IOException;
import java.net.URL;

@SuppressWarnings("unused")
public class FXMLUtils {
    public static FXMLLoader getFXMLLoader(String fxmlPath){
        return new FXMLLoader(getFXMLResource(fxmlPath));
    }
    public static URL getFXMLResource(String string) {
        return MainApp.class.getResource(string.concat(".fxml"));
    }
    public static Parent parseParent(String fxmlLoader){
        try {
            return new FXMLLoader(getFXMLResource(fxmlLoader)).load();
        }catch (IOException exception){
            ErrorHandler.launchWindow(exception);
            return null;
        }
    }
}
