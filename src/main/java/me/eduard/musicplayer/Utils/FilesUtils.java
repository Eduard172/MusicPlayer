package me.eduard.musicplayer.Utils;

import javafx.application.Platform;
import javafx.scene.control.Label;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.Logging.LoggerFormatter;
import me.eduard.musicplayer.Utils.Logging.LoggerHandler;
import me.eduard.musicplayer.Utils.PlaylistRelated.Playlists;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SuppressWarnings("all")
public final class FilesUtils {

    private static final Logger LOGGER = Logger.getLogger("MP-File-Manager");

    static {
        LoggerHandler handler = new LoggerHandler();
        handler.setFormatter(new LoggerFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
    }


    @SuppressWarnings("all")
    public static File[] getEveryMainDirectoryFile(){
        List<File> allFiles = new ArrayList<>();
        File mainDir = new File(MainApp.MAIN_APP_PATH);
        if(mainDir == null || !mainDir.exists())
            return new File[]{};
        for(File file : mainDir.listFiles()){
            updateFileList(allFiles, file);
        }
        return allFiles.toArray(File[]::new);
    }
    public static File[] getEveryFile(String path){
        List<File> allFiles = new ArrayList<>();
        File dir = new File(path);
        if(!dir.exists())
            return new File[0];
        for(File file : dir.listFiles()){
            updateFileList(allFiles, file);
        }
        return allFiles.toArray(File[]::new);
    }
    public static String[] getDirectoryFiles(String path){
        File file = new File(path);
        if(!file.isDirectory())
            return null;
        File[] files = file.listFiles();
        assert files != null;
        String[] arr = new String[files.length];
        for(int i = 0; i < files.length; i++){
            arr[i] = files[i].getAbsolutePath();
        }
        return arr;
    }
    public static boolean removeDirectoryFiles(String dir, boolean removeDirectory){
        if(FilesUtils.isFileCurrentlyOpened(new File(dir))){
            return false;
        }
        List<String> files = Utilities.reverseList(Arrays.stream(getEveryFile(dir)).map(File::getAbsolutePath).toList());

        for(String s : files){
            File f = new File(s);
            if(isFileCurrentlyOpened(f))
                return false;
            f.delete();
        }
        if(removeDirectory){
            new File(dir).delete();
        }
        return true;
    }
    private static void updateFileList(List<File> currentList, File currentFile){
        if(!currentFile.isDirectory()){
            currentList.add(currentFile);
            return;
        }
        goOneStepFurther(currentList, currentFile);
    }
    private static void goOneStepFurther(List<File> currentList, File currentFile){
        File[] files = currentFile.listFiles();
        assert files != null;
        currentList.add(currentFile);
        for(File eachFile : files){
            updateFileList(currentList, eachFile);
        }
    }

    @SuppressWarnings("all")
    public static int getTotalFilesCount(){
        return getEveryMainDirectoryFile().length;
    }
    public static boolean isFileCurrentlyOpened(File file){
        return !file.renameTo(file);
    }
    public static String getFileExtension(File file){
        String fileName = file.getName();
        return fileName.substring(fileName.lastIndexOf("."));
    }
    public static synchronized double copyFile(String source, String destination){
        File src = new File(source);
        File dest = new File(destination);
        if(src.isDirectory()){
            dest.mkdir();
            return 0;
        }
        int totalLength = 0;
        try (FileInputStream fin = new FileInputStream(src); FileOutputStream fos = new FileOutputStream(dest)){
            int length;
            byte[] bytes = new byte[4096];
            while ((length = fin.read(bytes)) > 0){
                fos.write(bytes, 0, length);
                totalLength += length;
            }
        }catch (IOException exception){
            ErrorHandler.launchWindow(exception);
        }
        return MainApp.calculateFileSize(dest, true);
    }
    public static void downloadFromInternet(String destination, String link, String downloadMessage, Label label){
        File dest = new File(destination);
        try (BufferedInputStream bis = new BufferedInputStream(new URL(link).openStream()); FileOutputStream fos = new FileOutputStream(dest)){
            int len;
            long totalBytes = new URL(link).openConnection().getContentLength();
            long downloaded = 0;
            byte[] bytes = new byte[8120];
            long previousProcent = 0;
            String labelText = (label != null) ? label.getText() : "";
            while ((len = bis.read(bytes)) > 0){
                fos.write(bytes, 0, len);
                downloaded += len;
                long procent = (downloaded * 100) / totalBytes;
                if(procent != previousProcent && downloadMessage != null){
                    String message = downloadMessage.replace("[p]", String.valueOf(procent));
                    previousProcent = procent;
                    if(label != null)
                        Platform.runLater(() -> label.setText(labelText+"\n"+message));
                }
            }
        }catch (Exception exception){
            ErrorHandler.launchWindow(exception);
        }
    }
    public static void writeToFile(String filePath, boolean keepExisting, String... strings){
        writeToFile(new File(filePath), keepExisting, strings);
    }
    public static void writeToFile(File file, boolean keepExisting, String... strings){
        if(file.isDirectory())
            return;
        StringBuilder builder = new StringBuilder();
        if(keepExisting){
           String[] fileContents = getFileContents(file);
           if(fileContents == null)
               return;
           try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))){
               for(String string : fileContents)
                   builder.append(string).append("\n");
               for(String string : strings)
                   builder.append(string).append("\n");
               bufferedWriter.write(builder.toString().trim());
               bufferedWriter.flush();
           }catch (IOException exception){
               exception.printStackTrace(System.err);
           }
        }else{
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))){
                for(String string : strings)
                    builder.append(string).append("\n");
                bufferedWriter.write(builder.toString().trim());
                bufferedWriter.flush();
            }catch (IOException exception){
                exception.printStackTrace(System.err);
            }
        }
    }
    public static String[] getFileContents(File file){
        if(file == null || !file.exists()){
            return null;
        }
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))){
            return bufferedReader.lines().toArray(String[]::new);
        }catch (IOException exception){
            exception.printStackTrace(System.err);
        }
        return new String[0];
    }
    public static String getFileNameUpdated(String absolutePath, boolean getOnlyName, String... toEliminate){
        String[] pathParts = absolutePath.split("[/\\\\]");
        String lastPortion = pathParts[pathParts.length - 1];
        String updatedPortion = lastPortion;
        for(String string : toEliminate){
            updatedPortion = updatedPortion.replace(string, "");
        }
        if(getOnlyName)
            return updatedPortion;
        else
            return absolutePath.replace(lastPortion, updatedPortion);
    }
    public static String getBelongingDirectory(String absolutePath){
        String[] parts = absolutePath.split("[/\\\\]");
        String lastPortion = parts[parts.length - 1];
        return absolutePath
                .replace("\\"+lastPortion, "")
                .replace("/"+lastPortion, "");
    }
    public static boolean fileExists(String path){
        return new File(path).exists();
    }
    public static String[] getFileContents(String filePath){
        return getFileContents(new File(filePath));
    }
    public static void moveDirectory(String dir, String newPath){
        File file = new File(dir);
        if(!file.isDirectory())
            return;
        String name = getFileNameUpdated(dir, true);
        String newFilePath = newPath+"\\"+name;
        File newDir = new File(newFilePath);
        if(newDir.exists())
            return;
        newDir.mkdir();
        for(File f : file.listFiles()){
            String path = newFilePath+"\\"+getFileNameUpdated(f.getAbsolutePath(), true);
            if(f.renameTo(new File(path))){
                LOGGER.info("[Success] Moved "+f.getAbsolutePath()+" to "+path);
            }else{
                LOGGER.warning("Something went wrong while trying to move "+f.getAbsolutePath());
            }
        }
        removeDirectoryFiles(dir, true);
    }
    public static void unzipFile(String zipPath, boolean delete, String... specificFilesName){
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipPath))){
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = MainApp.APP_EXTERNAL_HELPERS + "\\" + entry.getName();
                for(String s : specificFilesName){
                    if (!entry.isDirectory() && (filePath.endsWith(s))){
                        String[] parts = filePath.split("/");
                        LOGGER.info("Found: "+parts[parts.length - 1]);
                        extract(zipIn, MainApp.APP_EXTERNAL_HELPERS+"\\"+parts[parts.length - 1]);
                    }
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
            zipIn.closeEntry();
            zipIn.close();
            if(delete){
                String finalPath = MainApp.APP_EXTERNAL_HELPERS+"\\ffmpeg.zip";
                File file = new File(finalPath);
                if(file.exists())
                    file.delete();
            }
            LOGGER.info("Unzip successful for "+zipPath+".");
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
    public static void removeNonPlaylistFiles(String playlist){
        final String videoName = "Video."+Playlists.videoExt;
        final String audioName = "Audio."+Playlists.audioExt;
        List<File> dirFiles = Arrays
                .stream(Objects.requireNonNull(getDirectoryFiles(Playlists.getPlaylistPathByName(playlist))))
                .map(File::new)
                .toList();
        for(File f : dirFiles){
            if(!f.isDirectory() && f.delete()) LOGGER.info("Removed non-playlist file: "+f.getAbsolutePath());
            if(f.isDirectory()){
                File[] files = f.listFiles();
                assert files != null;
                if(files.length != 2){
                    removeDirectoryFiles(f.getAbsolutePath(), true);
                    LOGGER.info("Removed non-playlist directory: "+f.getAbsolutePath());
                }else{
                    boolean deleted = false;
                    for(File ff : files){
                        if(ff.getName().equals(videoName) || ff.getName().equals(audioName))
                            continue;
                        deleted = true;
                        removeDirectoryFiles(f.getAbsolutePath(), true);
                    }
                    if(deleted) LOGGER.info("Removed non-playlist directory: "+f.getAbsolutePath());
                }
            }
        }
        LOGGER.info("Playlist cleanup for '"+playlist+"' has finished.");
        Player.instance.setStatusLabel("Cleanup finished.", OutputUtilities.Level.SUCCESS);
    }
}
