package me.eduard.musicplayer.Library;

import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.FilesUtils;
import me.eduard.musicplayer.Utils.Logging.LoggerFormatter;
import me.eduard.musicplayer.Utils.Logging.LoggerHandler;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

@SuppressWarnings("all")
public class BackupManager {

    private static final Logger LOGGER = Logger.getLogger("Backup Manager");

    static{
        LoggerHandler consoleHandler = new LoggerHandler();
        consoleHandler.setFormatter(new LoggerFormatter());
        LOGGER.addHandler(consoleHandler);
        LOGGER.setUseParentHandlers(false);
    }

    public static void createBackup(File destination, boolean force){
        if(!destination.isDirectory()){
            throw new IllegalArgumentException("Cannot create backup on other file formats than directories.");
        }
        LOGGER.info("Creating Application backup...");
        String newFilePath = destination.getAbsolutePath()+"/MusicPlayer -- Backup";
        File newDirectory = new File(newFilePath);
        File backupVersion = new File(newFilePath+"/BackupVersion.yml");
        if(backupExists(destination.getAbsolutePath()) && !force){
            return;
        }
        if(newDirectory.exists()){
            File[] oldBackupFiles = FilesUtils.getEveryFile(destination.getAbsolutePath()+"\\MusicPlayer -- Backup");
            for(int i = oldBackupFiles.length - 1; i >= 0; i--){
                LOGGER.info("Deleting old backup file '"+oldBackupFiles[i].getAbsolutePath()+"'...");
                oldBackupFiles[i].delete();
            }
            newDirectory.delete();
            LOGGER.info("Creating application backup...");
        }
        newDirectory.mkdir();
        File[] allFiles = FilesUtils.getEveryMainDirectoryFile();
        LambdaObject<String> currentAbsolutePath = LambdaObject.of(null);

        for(File file : allFiles){
            currentAbsolutePath.set(file.getAbsolutePath());
            String newPath = newFilePath+"/"+removeEssentialPathPart(currentAbsolutePath.get(), MainApp.MAIN_APP_PATH+"\\");
            LOGGER.info("Copying '"+currentAbsolutePath.get()+"' to '"+newPath+"'...");
            FilesUtils.copyFile(currentAbsolutePath.get(), newPath);
        }

        LOGGER.info("Writing backup version...");
        if(!backupVersion.exists()){
            try {
                backupVersion.createNewFile();
            }catch (IOException exception){
                exception.printStackTrace(System.err);
            }
        }
        FilesUtils.writeToFile(backupVersion, false,
                "#Do not attempt to modify the version value! Doing this will result in a fail when trying to restore this backup.",
                "Version: "+MainApp.VERSION);
        LOGGER.info("Application backup has been saved!");
    }
    public static void restoreBackup(File backupDirectory){
        LOGGER.info("Preparing to restore previous backup...");
        if(!backupDirectory.exists())
            return;
        String path = backupDirectory.getAbsolutePath();
        LOGGER.info("Removing Backup Version file...");
        File backupVersion = new File(path+"/BackupVersion.yml");
        if(backupVersion.exists())
            backupVersion.delete();
        File[] allFiles = FilesUtils.getEveryFile(path);
        File mainDir = new File(MainApp.MAIN_APP_PATH);
        LOGGER.info("Removing current files to replace with ones from backup...");
        Uninstaller.uninstall();
        if(!mainDir.exists()){
            mainDir.mkdir();
        }
        LOGGER.info("Main directory was created! Starting file copy process...");
        LambdaObject<String> currentAbsolutePath = LambdaObject.of(null);
        for(File file : allFiles){
            currentAbsolutePath.set(file.getAbsolutePath());
            String newPath = MainApp.MAIN_APP_PATH+"/"+removeEssentialPathPart(currentAbsolutePath.get(), path+"\\");
            LOGGER.info("Restoring '"+newPath+"'...");
            FilesUtils.copyFile(currentAbsolutePath.get(), newPath);
        }
        LOGGER.info("Cleaning up...");
        for(int i = allFiles.length - 1; i >= 0; i--){
            allFiles[i].delete();
        }
        backupDirectory.delete();
        LOGGER.info("Backup has been successfully restored!");
        LOGGER.info("Starting the player based on selected playlist...");
        Player.initializeApplicationSettings();
        Player.instance.loadSettings();
        MainApp.closeAllStages();
        Player.instance.initializeListView(Player.SELECTED_PLAYLIST);
    }
    public static boolean backupExists(String backupDirectory){
        File backupDir = new File(backupDirectory+"\\MusicPlayer -- Backup");
        File backupVer = new File(backupDirectory+"\\MusicPlayer -- Backup\\BackupVersion.yml");
        return backupDir.exists() && backupDir.isDirectory() && backupVer.exists();
    }
    public static boolean isVersionOkay(File backupDirectory){
        if(backupDirectory == null || !backupDirectory.exists())
            return false;
        String path = backupDirectory.getAbsolutePath();
        String[] fileContents = FilesUtils.getFileContents(path+"/BackupVersion.yml");
        if(fileContents == null)
            return false;
        String exactLine = "";
        for(String strings : fileContents){
            if(strings.contains("Version"))
                exactLine = strings;
        }
        double version = Double.parseDouble(exactLine.replace("Version:", "").trim());
        return version == MainApp.VERSION;
    }
    public static boolean isActiveBackupDirectory(File file){
        if(file == null)
            return false;
        return file.getName().equals("MusicPlayer -- Backup") && file.isDirectory();
    }
    private static String removeEssentialPathPart(String path, String partToRemove){
        return path.replace(partToRemove, "");
    }

}
