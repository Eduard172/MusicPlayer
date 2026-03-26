package me.eduard.musicplayer.Library;

import javafx.application.Platform;
import javafx.scene.control.Label;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.FilesUtils;
import me.eduard.musicplayer.Utils.Logging.LoggerFormatter;
import me.eduard.musicplayer.Utils.Logging.LoggerHandler;

import java.io.*;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SuppressWarnings({"SpellCheckingInspection", "ResultOfMethodCallIgnored"})
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
        unzipFile(MainApp.APP_EXTERNAL_HELPERS+"\\ffmpeg.zip", label);
    }
    private static void unzipFile(String path, Label label){
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(path))){
            if(label != null)
                Platform.runLater(() -> label.setText(label.getText()+"\nExtracting ffmpeg.exe and removing the zip file..."));
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = MainApp.APP_EXTERNAL_HELPERS + "\\" + entry.getName();
                if (!entry.isDirectory() && (filePath.endsWith("ffmpeg.exe"))){
                    String[] parts = filePath.split("/");
                    LOGGER.info("Found: "+parts[parts.length - 1]);
                    extract(zipIn, MainApp.APP_EXTERNAL_HELPERS+"\\"+parts[parts.length - 1]);
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
            zipIn.closeEntry();
            String finalPath = MainApp.APP_EXTERNAL_HELPERS+"\\ffmpeg.zip";
            File file = new File(finalPath);
            zipIn.close();
            if(file.exists())
                file.delete();
            LOGGER.info("FFmpeg copy successful.");
        }catch (IOException exception){
            exception.printStackTrace(System.err);
        }
    }
    private static void extract(ZipInputStream zipIn, String filePath) throws IOException{
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            byte[] bytes = new byte[8120];
            int len;
            while ((len = zipIn.read(bytes)) != -1){
                fos.write(bytes, 0, len);
            }
        }catch (IOException exception){
            exception.printStackTrace(System.err);
        }
    }



}
