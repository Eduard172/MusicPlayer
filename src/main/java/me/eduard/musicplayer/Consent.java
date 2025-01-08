package me.eduard.musicplayer;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import me.eduard.musicplayer.Components.Credentials;
import me.eduard.musicplayer.Components.MessageBox_Ok;
import me.eduard.musicplayer.Library.CustomComponents.WindowTitleBar;
import me.eduard.musicplayer.Library.FFmpegUtils;
import me.eduard.musicplayer.Utils.FilesUtils;
import me.eduard.musicplayer.Utils.Logging.LoggerFormatter;
import me.eduard.musicplayer.Utils.Logging.LoggerHandler;
import me.eduard.musicplayer.Utils.PlaylistRelated.Playlists;
import me.eduard.musicplayer.Utils.Settings;
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
            FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("MessageBox_Ok")
                    .withStageBuilder(
                            StageBuilder.newBuilder()
                                    .title("Something before everything continues...")
                                    .resizable(false)
                                    .icon("icons/icon.png")
                                    .removeUpperBar()
                                    .styleSheet("ApplicationWindow")
                    ).finishBuilding();
            Stage stage = fxmlStageBuilder.getStage();
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
            fxmlStageBuilder.getRoot().getChildren().add(button);
            LOGGER.info("Waiting for user approval before starting installation process...");
            //End of credentials button
            messageBoxOk
                    .setHeader("What you might need to know is that...")
                    .setMessage("""
                            Before creating it's files, the application will run on your PC.
                            
                            What does this mean?
                            - Every setting, playlist, song linked to that specific playlist will be saved on your PC.
                            - All files will be stored in a main folder called 'MusicPlayer' on your Desktop.
                            - This app will consume some of your storage, so if your storage is low, please avoid installing it.
                            - If you have enough storage, ~1 GB should be enough. If you plan to use richer setups, then ~2GB should be enough.
                            
                            This application uses third party softwares to download / re-encode the songs you desire. \
                            More information about them could be found by clicking the "Credentials" button.
                            
                            To continue and create the necessarry files, please, click on "Install" button below, \
                            otherwise, click on "Quit" button.
                            """);
            messageBoxOk.message.setFont(Font.font(14));
            WindowTitleBar windowTitleBar = new WindowTitleBar(stage).useDefaultPresets();
            windowTitleBar.setTitleString("Something before everything continues...");
            windowTitleBar.linkToStage();
            windowTitleBar.applyAfterLinkPresets();
            windowTitleBar.removeNode(windowTitleBar.getCloseButton());
            windowTitleBar.removeNode(windowTitleBar.getMinimizeButton());
            windowTitleBar.removeNode(windowTitleBar.getCloseImage());
            windowTitleBar.removeNode(windowTitleBar.getMinimizeImage());
            messageBoxOk.cancelButton.setOnAction(event -> {
                LOGGER.warning("Setup closed by the user.");
                fxmlStageBuilder.close();
            });
            messageBoxOk.okButton.setOnAction(event -> {
                windowTitleBar.setTitleString("Preparing application files then starting up...");
                messageBoxOk.setHeader("Thank you!")
                            .setMessage("""
                                    Thank you for giving this application a chance!
                                    Necessarry files will be creating and the main window will open as soon as \
                                    everything finishes. To watch the progress, open the application terminal, or just wait \
                                    until the main window opens.
                                    """);
                messageBoxOk.message.setFont(Font.font(16));
                messageBoxOk.okButton.setVisible(false);
                messageBoxOk.cancelButton.setVisible(false);
                button.setVisible(false);
                Service<Void> service = new Service<Void>() {
                    @Override
                    protected Task<Void> createTask() {
                        return new Task<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                LOGGER.info("Creating 'Helpers' directory...");
                                mainDirectory.mkdir();
                                File helpersDir = new File(MainApp.APP_EXTERNAL_HELPERS);
                                if(!helpersDir.exists()){
                                    helpersDir.mkdir();
                                }
                                LOGGER.info("Downloading downloader file...");
                                String downloaderURL = "https://github.com/yt-dlp/yt-dlp/releases/download/2024.12.23/yt-dlp.exe";
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
                                    MainApp.instance.startApp();
                                    fxmlStageBuilder.close();
                                });
                                return null;
                            }
                        };
                    }
                };
                service.start();
            });
            MainApp.openStage(stage, true, false);
        }catch (IllegalStateException e){
            e.printStackTrace(System.err);
        }
    }
}
