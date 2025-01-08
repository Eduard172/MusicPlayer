package me.eduard.musicplayer.Utils;

import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.MainApp;

import java.io.*;

public class Settings {

    public static String[] DEFAULT_SETTINGS = {
        "auto-start: false",
        "app-volume: 100",
        "selected-playlist: [None]",
        "manage-instant-remove: false",
        "Sound-Type: Adjusted",
        "Media-End-Behaviour: Auto-Play",
        "animations: true",
        "sliding-notifications: true",
        "verbose-status: false",
        "fullscreen: false",
        "hide-player: false",
        "hide-title: false"
    };

    private final String path;

    public Settings(String path){
        this.path = MainApp.MAIN_APP_PATH.concat("/").concat(path);
    }

    public static Settings of(String path){
        return new Settings(path);
    }

    public String getSettingValue(String setting, boolean getLabel){
        File file = new File(this.path);
        if(!file.exists()){
            ErrorHandler.launchWindow(new NullPointerException("Settings file was not found in the working directory. Retrying to create it..."));
            setupSettingsFile(true, DEFAULT_SETTINGS);
        }
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))){
            String[] settings = bufferedReader.lines().toArray(String[]::new);
            for(String strings : settings){
                if(strings.contains(setting)){
                    return getValue(strings, getLabel);
                }
            }
        }catch (IOException exception){
            ErrorHandler.launchWindow(exception);
        }
        return "";
    }
    public void saveSetting(String setting, Object value){
        File file = new File(this.path);
        if(!file.exists()){
            return;
        }
        if(isValidSetting(setting)){
            String[] settings = getSettings();
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))){
                for(int i = 0; i < settings.length; i++){
                    if(settings[i].contains(setting)){
                        settings[i] = getValue(settings[i], true).concat(": ").concat(value.toString());
                    }
                }
                String updatedSettings = Utilities.stringFromArray(settings, "", "\n");
                bufferedWriter.write(updatedSettings);
            }catch (IOException exception){
                ErrorHandler.launchWindow(exception);
            }
        }
    }
    public String[] getSettings(){
        File file = new File(this.path);
        if(!file.exists())
            return new String[0];
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))){
            return bufferedReader.lines().toArray(String[]::new);
        }catch (IOException exception){
            return new String[0];
        }
    }
    public boolean isValidSetting(String setting){
        String[] settings = getSettings();
        for(String strings : settings){
            if(strings.contains(setting)){
                return true;
            }
        }
        return false;
    }
    public boolean isSettingsFileExists(){
        return new File(this.path).exists();
    }
    private String getValue(String string, boolean getLabel){
        return (getLabel) ? string.substring(0, string.lastIndexOf(":")).trim() :
                string.substring(string.lastIndexOf(":")+1).trim();
    }
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void setupSettingsFile(boolean resetValues, String... values){
        File settingsFile = new File(this.path);
        if(settingsFile.exists() && resetValues){
            settingsFile.delete();
        }
        if(!settingsFile.exists()){
            try {
                settingsFile.createNewFile();
                StringBuilder builder = new StringBuilder();
                for(int i = 0; i < values.length; i++){
                    if(i == values.length - 1){
                        builder.append(values[i]);
                    }else{
                        builder.append(values[i]).append("\n");
                    }
                }
                String finalSettings = builder.toString();
                try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(settingsFile))){
                    bufferedWriter.write(finalSettings);
                }catch (IOException exception){
                    ErrorHandler.launchWindow(exception);
                }
            }catch (IOException exception){
                ErrorHandler.launchWindow(exception);
            }
        }
    }
}
