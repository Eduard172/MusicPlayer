package me.eduard.musicplayer.Components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.*;
import me.eduard.musicplayer.Utils.PlaylistRelated.Playlists;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;

/**
 * Every single Window inside this class has no "Belong-Class", which means they are considered some 'utils' windows.
 */
@SuppressWarnings({"unused"})
public class ScreenLauncher {

    private static final Settings settings = Settings.of("settings.yml");

    @SuppressWarnings("all")
    public static void launchDeletePlaylistScreen(String selectedPlaylist){
        try {
            FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("MessageBox_Ok")
                    .withStageBuilder(StageBuilder.newBuilder()
                            .styleSheet("MessageBox_Ok")
                            .title("Playlist Manager - Playlist Delete")
                            .removeUpperBar()
                            .icon("icons/icon.png")
                            .resizable(false)
                    ).bindAllStagesCloseKeyCombination().addExitListenerWithEscape().finishBuilding();
            Stage stage = fxmlStageBuilder.getStage();

            stage.setOnCloseRequest(event -> MainApp.closeStage(stage, true));
            MainApp.openStage(stage, Player.ANIMATIONS, true);
            MessageBox_Ok messageBoxOk = fxmlStageBuilder.getFxmlLoader().getController();
            messageBoxOk.setStage(stage).title("Confirmation await.").useDefaultFunctionalityPresets();

            messageBoxOk
                    .setHeader("Are you sure you want to do this?")
                    .setMessage("""
                            You are about to permanently delete this playlist and all it's linked songs.
                            
                            This action cannot be undone.
                            """);
            messageBoxOk.cancelButton.setOnAction(event -> MainApp.closeStage(stage, true));
            messageBoxOk.okButton.setOnAction(event -> {
                if(settings.getSettingValue("selected-playlist", false).equals(selectedPlaylist)){
                    Utilities.stopPlayer(Player.instance);
                    Utilities.sleep(Duration.millis(300), 1, run -> {
                        settings.saveSetting("selected-playlist", Playlists.getPlaylists().get(0));
                        Player.instance.initializeListView(settings.getSettingValue("selected-playlist", false));
                    }, null);
                }
                Playlists.deletePlaylist(selectedPlaylist);
                MainApp.closeStage(stage, true);
            });

        }catch (IllegalStateException exception){
            ErrorHandler.launchWindow(exception);
        }
    }

    public static void launchPlaylistCleanupScreen(String selectedPlaylist){
        launchWarningMessageBox(
                "Cleaning up '"+selectedPlaylist+"'",
                "You are about to cleanup this playlist.",
                """
                        This action will permanently remove all files that do not consist of a workable \
                        media for this App inside this playlist. If these files are important to you, \
                        please take a backup or move them to another location.
                        
                        This is recommended if this playlist ('%playlist%') does not behave as expected because \
                        of some App errors such as leftovers from audio encoding failures or playlist not \
                        rendering as it should.
                        
                        To proceed to this action, click on the 'OK' button below.
                        """.replace("%playlist%", selectedPlaylist),
                event -> {
                    Player player = Player.instance;
                    player.setStatusLabel("Cleaning up '"+selectedPlaylist+"'...", OutputUtilities.Level.SUCCESS);
                    FilesUtils.removeNonPlaylistFiles(selectedPlaylist);
                }
        );
    }


    public static void launchPlannedToRemoveWarning(String additionalMessage, EventHandler<ActionEvent> whenClickedOK){
        try {
            FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("MessageBox_Ok")
                            .withStageBuilder(StageBuilder.newBuilder()
                                    .icon("icons/icon.png")
                                    .styleSheet("MessageBox_Ok")
                                    .removeUpperBar()
                                    .title("Feature planned to be removed.")
                                    .resizable(false)
                            ).bindAllStagesCloseKeyCombination().addExitListenerWithEscape().finishBuilding();
            Stage stage = fxmlStageBuilder.getStage();

            MainApp.openStage(stage, true, true);
            MessageBox_Ok messageBoxOk = fxmlStageBuilder.getFxmlLoader().getController();

            messageBoxOk.setStage(stage).title("This feature is planned to be removed").useDefaultFunctionalityPresets();

            String plannedToRemoveLabel = """
                            
                            This feature has been marked as deprecated as it won't get any updates or changes.
                            It will still work in this version of the application, \
                            but the probability that it will be removed in future updates is high.
                            
                            If you still want to access this feature, click the 'OK' button, otherwise click on the \
                            'Cancel' button, or press 'ESC' on keyboard.""";
            messageBoxOk
                    .setHeader("This feature is planned to be removed in the future.")
                    .setMessage((additionalMessage == null || additionalMessage.isEmpty()) ? plannedToRemoveLabel : plannedToRemoveLabel+"\n\n"+additionalMessage);
            EventHandler<ActionEvent> handleEvents = event -> {
                MainApp.closeStage(stage, true);
                if(whenClickedOK != null){
                    whenClickedOK.handle(event);
                }
            };
            messageBoxOk.okButton.setOnAction(handleEvents);
            messageBoxOk.cancelButton.setOnAction(event -> fxmlStageBuilder.close());
        }catch (IllegalStateException exception){
            ErrorHandler.launchWindow(exception);
        }
    }
    @SuppressWarnings("unused")
    public static void launchWarningMessageBox(String title, String header, String message, EventHandler<ActionEvent> onOKPress){
        try {
            FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("MessageBox_Ok")
                            .withStageBuilder(StageBuilder.newBuilder()
                                    .resizable(false)
                                    .title(title)
                                    .removeUpperBar()
                                    .icon("icons/icon.png")
                                    .styleSheet("MessageBox_Ok")
                            ).bindAllStagesCloseKeyCombination().addExitListenerWithEscape().finishBuilding();
            Stage stage = fxmlStageBuilder.getStage();

            MainApp.openStage(stage, true, true);

            MessageBox_Ok messageBoxOk = fxmlStageBuilder.getFxmlLoader().getController();
            messageBoxOk.setStage(stage).title(title).useDefaultFunctionalityPresets();

            messageBoxOk.setHeader(header).setMessage(message);

            messageBoxOk.cancelButton.setVisible(false);

            EventHandler<ActionEvent> handler = event -> {
                MainApp.closeStage(stage, Player.ANIMATIONS);
                if(onOKPress != null)
                    onOKPress.handle(event);
            };

            messageBoxOk.okButton.setOnAction(handler);

        }catch (IllegalStateException exception){
            ErrorHandler.launchWindow(exception);
        }
    }

    public static void launchTestingWindow(){
        FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("TestingWindow")
                .withStageBuilder(
                        StageBuilder.newBuilder()
                                .title("Manage backups")
                                .resizable(true)
                                .icon("icons/icon.png")
                ).finishBuilding();

        Stage stage = fxmlStageBuilder.getStage();
        TestingWindow testingWindow = fxmlStageBuilder.getFxmlLoader().getController();
        testingWindow.corePane.setOnMouseMoved(event -> testingWindow.devLabel.setText("X: "+event.getX()+", Y: "+event.getY()));

        MainApp.openStage(stage, true, true);
        stage.setOpacity(1);
    }
}