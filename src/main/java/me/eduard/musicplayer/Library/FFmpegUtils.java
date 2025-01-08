package me.eduard.musicplayer.Library;

import javafx.application.Platform;
import javafx.scene.control.Label;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.FilesUtils;
import me.eduard.musicplayer.Utils.Logging.LoggerFormatter;
import me.eduard.musicplayer.Utils.Logging.LoggerHandler;

import java.io.*;
import java.util.logging.Logger;

@SuppressWarnings({"SpellCheckingInspection"})
public class FFmpegUtils {

    private static final Logger LOGGER = Logger.getLogger("FFmpeg-Utilities");

    static {
        LoggerHandler handler = new LoggerHandler();
        handler.setFormatter(new LoggerFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
    }

    public static void downloadZipFileIfNecessary(){
        downloadZipFileIfNecessary(null);
    }

    public static void downloadZipFileIfNecessary(Label label){
        File ffmpegFile = new File(MainApp.APP_EXTERNAL_HELPERS+"\\ffmpeg.exe");
        if(ffmpegFile.exists())
            return;
        String URL = "https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl.zip";
        String resultPath = MainApp.APP_EXTERNAL_HELPERS+"\\ffmpeg.zip";
        LOGGER.info("Downloading FFmpeg Zip from Github...");
        FilesUtils.downloadFromInternet(resultPath, URL, "[ffmpeg] Downloaded [p]% out of 100%", label);
        LOGGER.info("Unzipping the executable then removing the Zip file...");
        Platform.runLater(() -> label.setText(label.getText()+"\nExtracting ffmpeg.exe and removing the zip file..."));
        FilesUtils.unzipFile(MainApp.APP_EXTERNAL_HELPERS+"\\ffmpeg.zip", true, "ffmpeg.exe");
    }

}
