package me.eduard.musicplayer.Library;

import javafx.stage.Stage;
import me.eduard.musicplayer.Components.MessageBox_Ok;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.Library.Cache.Window.WindowRegistry;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.Logging.LoggerFormatter;
import me.eduard.musicplayer.Utils.Logging.LoggerHandler;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;
import me.eduard.musicplayer.Utils.Utilities;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Uninstaller {

    private static final Logger LOGGER = Logger.getLogger("Uninstaller");

    static{
        LoggerHandler consoleHandler = new LoggerHandler();
        consoleHandler.setFormatter(new LoggerFormatter());
        LOGGER.addHandler(consoleHandler);
        LOGGER.setUseParentHandlers(false);
    }

    private static int totalLength = 0;
    private static double totalSize = 0.0;
    private static int totalDirectories = 0;

    public static void uninstall(){
        Utilities.stopPlayer(Player.instance);
        LOGGER.info("Attempting to stop all active player threads...");
        Player.instance.THREADS.forEach(ExecutorService::shutdownNow);
        resetValues();
        File mainDir = new File(MainApp.MAIN_APP_PATH);
        File[] files = mainDir.listFiles();
        assert files != null;
        for(File file : files)
            removeFile(file);
        LOGGER.info("Removing main directory '"+mainDir.getAbsolutePath()+"'...");
        mainDir.delete();
    }

    private static void removeFile(File file){
        if(file == null)
            return;
        File[] files = file.listFiles();
        assert files != null;
        if(file.isDirectory() && files.length > 0){
            LOGGER.info("\nRemoving items from directory '"+file.getAbsolutePath()+"'...");
            goOneStepFurther(file);
        }else{
            boolean isEmptyFolder = file.isDirectory() && files.length == 0;
            totalLength++;
            if(!isEmptyFolder){
                totalSize += MainApp.calculateFileSize(file, true);
                LOGGER.info("Removing '"+file.getAbsolutePath()+"'...");
            }else{
                LOGGER.info("Removing '"+file.getAbsolutePath()+"'... (Empty Directory)");
            }
            file.delete();
        }
    }
    private static void goOneStepFurther(File file){
        totalDirectories++;
        File[] fileList = file.listFiles();
        assert fileList != null;
        for(File eachFile : fileList){
            removeFile(eachFile);
        }
        LOGGER.info("Removing '"+file.getAbsolutePath()+"'...");
        file.delete();
    }

    public static void showFinalSummary(){
        String summary = """
                Uninstallation Complete. Summary:
                
                Overall files removed: [total] ([dirs] directories / [files] non-directories)
                Space saved: [size] MB
                
                We're sorry that you had to take this decision... Hope you enjoyed using this app!
                """
                .replace("[total]", String.valueOf(totalLength))
                .replace("[dirs]", String.valueOf(totalDirectories))
                .replace("[files]", String.valueOf((totalLength - totalDirectories)))
                .replace("[size]", Utilities.betterDouble(totalSize, 2));
        LOGGER.info(summary);
    }
    private static void resetValues(){
        totalLength = 0;
        totalDirectories = 0;
        totalSize = 0.0;
    }
    public static void launchWindow(){
        try {
            Player player = Player.instance;
            FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("MessageBox_Ok")
                    .withStageBuilder(StageBuilder.newBuilder()
                            .title("You are about to uninstall this app.")
                            .icon("icons/icon.png")
                            .removeUpperBar()
                            .styleSheet("MessageBox_Ok")
                            .resizable(false)
                    ).bindAllStagesCloseKeyCombination().addExitListenerWithEscape().finishBuilding();
            Stage stage = (WindowRegistry.isInRegistry("UNINSTALL")) ?
                    WindowRegistry.getStage("UNINSTALL") : WindowRegistry.getAndRegister("UNINSTALL", fxmlStageBuilder.getStage());

            MainApp.openStage(stage, true, true);
            MessageBox_Ok messageBoxOk = fxmlStageBuilder.getFxmlLoader().getController();

            messageBoxOk.setStage(stage).title("You are about to uninstall this app.").useDefaultFunctionalityPresets();

            messageBoxOk
                    .setHeader("Are you sure you want to do this?")
                    .setMessage("""
                            You are about to delete every file that this application is using.
                            This includes:
                            
                            - All playlists.
                            - Saved settings.
                            - Other files created by this app.
                            
                            This action is permanent and cannot be undone!
                            """);
            messageBoxOk.cancelButton.setOnAction(event1 -> MainApp.closeStage(stage, true));
            messageBoxOk.okButton.setOnAction(event1 -> {
                LOGGER.info("\nStarting uninstallation process...");
                messageBoxOk.okButton.setVisible(false);
                messageBoxOk.cancelButton.setVisible(false);
                Player.instance.hidePlayerLabel.setVisible(false);
                if(Player.instance.videoPlayer != null){
                    LOGGER.warning("Disabling the Player to avoid any 'in use' media file...");
                    Utilities.stopPlayer(player);
                    shutdownAllThreads();
                    LOGGER.info("Player has been disabled.");
                }
                Player.instance.listView.getItems().clear();
                messageBoxOk
                        .setHeader("Uninstalling application...")
                        .setMessage("""
                                   Please, be patient while the app removes it's data from your PC...
                                   The application will automatically exit once there are no files left to remove.
                                   
                                   We are sorry that you had to take this decision...
                                   """);
                MainApp.uninstallApp(1);
            });
        } catch (IllegalStateException exception) {
            ErrorHandler.launchWindow(exception);
        }
    }

    public static void shutdownAllThreads(){
        LOGGER.info("Trying to shutdown all active app threads...");
        while (!MainApp.executorService.isShutdown()){
            LOGGER.info("Insisting to shutdown MainApp executor service...");
            MainApp.executorService.shutdownNow();
        }
        Player player = Player.instance;
        player.THREADS.forEach(ex -> {
            while (!ex.isShutdown()){
                LOGGER.info("Insisting to shutdown player thread "+ex+"...");
                ex.shutdownNow();
            }
        });
        LOGGER.info("Threads were successfully stopped.");
    }

}
