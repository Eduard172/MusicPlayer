package me.eduard.musicplayer;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import me.eduard.musicplayer.Components.Credentials;
import me.eduard.musicplayer.Components.MessageBox_Ok;
import me.eduard.musicplayer.Components.VersionManagement.VersionHandler;
import me.eduard.musicplayer.Library.BackupManager;
import me.eduard.musicplayer.Library.CustomComponents.WindowTitleBar;
import me.eduard.musicplayer.Library.DirectorySelector;
import me.eduard.musicplayer.Library.FFmpegUtils;
import me.eduard.musicplayer.Utils.*;
import me.eduard.musicplayer.Utils.Logging.LoggerFormatter;
import me.eduard.musicplayer.Utils.Logging.LoggerHandler;
import me.eduard.musicplayer.Utils.PlaylistRelated.Playlists;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;

import java.io.File;
import java.util.logging.Logger;

public class Consent {

    private static final Logger LOGGER = Logger.getLogger("User-Consent");

    static {
        LoggerHandler consoleHandler = new LoggerHandler();
        consoleHandler.setFormatter(new LoggerFormatter());
        LOGGER.addHandler(consoleHandler);
        LOGGER.setUseParentHandlers(false);
    }

    private final Settings settings = Settings.of("settings.yml");

    @SuppressWarnings("all")
    public void launchConsentScreen(File mainDirectory){
        try {
            final VersionHandler versionHandler = new VersionHandler();
            FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("MessageBox_Ok")
                    .withStageBuilder(
                            StageBuilder.newBuilder()
                                    .title("A quick note before everything continues...")
                                    .resizable(false)
                                    .icon("icons/icon.png")
                                    .removeUpperBar()
                                    .styleSheet("ApplicationWindow")
                    ).finishBuilding();
            Stage stage = fxmlStageBuilder.getStage();
            WindowTitleBar windowTitleBar = new WindowTitleBar(stage).useDefaultPresets()
                    .setTitleString("A quick note before everything continues...")
                    .setButtonsArrangement(WindowTitleBar.ButtonsArrangement.CLOSE)
                    .setOnClose(() -> {
                        LOGGER.warning("Setup closed by the user.");
                        MainApp.instance.quitApp(false, MainApp.instance.getApplicationState());
                    })
                    .setSupportStyling("-fx-fill: transparent");
            windowTitleBar.linkToStage();
            MessageBox_Ok messageBoxOk = fxmlStageBuilder.getFxmlLoader().getController();
            messageBoxOk.okButton.setText("Install");
            messageBoxOk.cancelButton.setText("Quit");
            //Credentials button
            Button button = new Button("Credentials");
            button.setPrefWidth(messageBoxOk.cancelButton.getPrefWidth() + 10);
            button.setPrefHeight(messageBoxOk.cancelButton.getPrefHeight());
            button.setOnAction(event -> Credentials.launch());
            button.setLayoutX(10);
            button.setLayoutY(messageBoxOk.cancelButton.getLayoutY());
            Button restoreBackup = new Button("Restore Backup");
            restoreBackup.setPrefWidth(button.getPrefWidth() + 15);
            restoreBackup.setPrefHeight(button.getPrefHeight());
            restoreBackup.setOnAction(event -> {
                DirectorySelector selector = new DirectorySelector()
                        .setInitialDirectory(new File(MainApp.DESKTOP_PATH))
                        .setTitle("Restore an existing backup without re-installing the application.");
                selector.launch();
                File result = selector.getResult();
                if(result == null || !BackupManager.isActiveBackupDirectory(result))
                    return;
                messageBoxOk.setMessage("Please wait...")
                            .setHeader("Restoring existing backup");
                windowTitleBar.setTitleString("Waiting for backup to restore...");
                Utilities.runAsynchronously(() -> {
                    BackupManager.restoreBackup(selector.getResult());
                    Platform.runLater(() -> {
                        MainApp.instance.startApp();
                        fxmlStageBuilder.close();
                    });
                });

            });
            restoreBackup.setLayoutX(button.getLayoutX() + button.getPrefWidth() + 5);
            restoreBackup.setLayoutY(messageBoxOk.cancelButton.getLayoutY());
            fxmlStageBuilder.getRoot().getChildren().addAll(button, restoreBackup);

            GlobalAppStyle.applyToButtons(restoreBackup, button, messageBoxOk.cancelButton, messageBoxOk.okButton);

            LOGGER.info("Waiting for user approval before starting installation process...");
            //End of credentials button
            messageBoxOk
                    .setHeader("A quick note to consider...")
                    .setMessage("""
                            This application will run on your PC.
                            
                            What does this mean?
                            - Every setting, playlist, song linked to that specific playlist will be saved on your PC.
                            - All files will be stored in a main folder called 'MusicPlayer' on your Desktop.
                            - This app will consume some of your storage, so if your storage is low, please avoid installing it.
                            - Around 2.5 GB should be fine if you plan to use a richer setup.
                            
                            This application uses third party softwares to download / encode the songs you desire. \
                            More information about them can be found by clicking the "Credentials" button.
                            
                            Important note to consider: Downloading & Encoding process may trigger an Anti-Virus warning. \
                            Please, try to disable it or add this app to exclusion list if possible, since it will interfere with the \
                            application's functionality. 
                            
                            To continue and create the necessarry files, please, click on "Install" button below, \
                            otherwise, click on "Quit" button.
                            """);
            messageBoxOk.message.setFont(Font.font(14));
            messageBoxOk.cancelButton.setOnAction(event -> {
                LOGGER.warning("Setup closed by the user.");
                fxmlStageBuilder.close();
            });
            messageBoxOk.okButton.setOnAction(event -> {
                windowTitleBar.setTitleString("Preparing application files then starting up...");
                messageBoxOk.setHeader("Thank you!")
                            .setMessage("""
                                    Thank you for giving this App a chance!
                                    Necessarry files will be created and the main window will open as soon as \
                                    everything finishes.
                                    This shouldn't take long to finish up.
                                    """);
                messageBoxOk.message.setFont(Font.font(16));
                NodeUtils.setNodesVisibility(false, messageBoxOk.okButton, messageBoxOk.cancelButton, button, restoreBackup);
                Utilities.runAsynchronously(() -> {
                    Platform.runLater(() -> windowTitleBar.setButtonsArrangement(WindowTitleBar.ButtonsArrangement.NONE));
                    LOGGER.info("Creating 'Helpers' directory...");
                    mainDirectory.mkdir();
                    File helpersDir = new File(MainApp.APP_EXTERNAL_HELPERS);
                    if(!helpersDir.exists()){
                        helpersDir.mkdir();
                    }
                    LOGGER.info("Downloading downloader file...");
                    String downloaderURL = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe";
                    Platform.runLater(() ->
                            messageBoxOk.message.setText(messageBoxOk.message.getText()+"\nDownloading 'downloader.exe'... (1/2)")
                    );
                    FilesUtils.downloadFromInternet(MainApp.APP_EXTERNAL_HELPERS+"\\downloader.exe",
                            downloaderURL,
                            "[downloader.exe] Downloaded [p]% out of 100%", messageBoxOk.message);
                    Playlists.createPlaylistsDirectory();
                    Platform.runLater(() ->
                            messageBoxOk.message.setText(messageBoxOk.message.getText()+"\nDownloading ffmpeg archive... (2/2)")
                    );
                    FFmpegUtils.downloadZipFileIfNecessary(messageBoxOk.message);
                    Platform.runLater(() ->
                            messageBoxOk.message.setText(messageBoxOk.message.getText()+"\nStarting up...")
                    );
                    settings.setupSettingsFile(true, Settings.DEFAULT_SETTINGS);
                    System.out.printf("Preparing for first start up...");
                    Platform.runLater(() -> {
                        versionHandler.writeVersion();
                        MainApp.instance.startApp();
                        fxmlStageBuilder.close();
                    });
                });
            });
            MainApp.openStage(stage, true, false);
        }catch (IllegalStateException e){
            e.printStackTrace(System.err);
        }
    }
}
