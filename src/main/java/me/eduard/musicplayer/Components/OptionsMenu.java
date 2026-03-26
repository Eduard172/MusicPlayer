package me.eduard.musicplayer.Components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import me.eduard.musicplayer.Components.ManagePlaylist.AddSongs;
import me.eduard.musicplayer.Components.ManagePlaylist.MoveSongs;
import me.eduard.musicplayer.Components.ManagePlaylist.PlaylistInfo;
import me.eduard.musicplayer.Components.ManagePlaylist.RemoveSongs;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.Library.BackupManager;
import me.eduard.musicplayer.Library.CustomComponents.BetterLabel;
import me.eduard.musicplayer.Library.CustomComponents.WindowTitleBar;
import me.eduard.musicplayer.Library.DirectorySelector;
import me.eduard.musicplayer.Library.Uninstaller;
import me.eduard.musicplayer.Library.WrappedValue;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.OutputUtilities;
import me.eduard.musicplayer.Utils.PlaylistRelated.Playlists;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;
import me.eduard.musicplayer.Utils.Utilities;

import java.io.File;

public class OptionsMenu {

    private static final String onMouseEnterTextFill = "-fx-text-fill: #00fff7";

    @FXML private Label quickPlaylist;
    @FXML private Label otherPlaylistOptions;
    @FXML private Label backup;
    @FXML private Label otherOptions;
    @FXML private Label uninstallation;
    @FXML private Label statusLabel;

    @FXML public AnchorPane corePane;
    public static void launch(){
        try {
            FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("OptionsMenu")
                    .withStageBuilder(
                            StageBuilder.newBuilder()
                                    .removeUpperBar()
                                    .styleSheet("ApplicationWindow")
                                    .resizable(false)
                                    .title("Options")
                                    .icon("icons/icon.png")
                    )
                    .addExitListenerWithEscape()
                    .bindAllStagesCloseKeyCombination()
                    .requireModality()
                    .finishBuilding();
            Stage stage = fxmlStageBuilder.getStage();

            WindowTitleBar windowTitleBar = new WindowTitleBar(stage).useDefaultPresets()
                    .setTitleString("Quick Options menu")
                    .setTitleLabelStyling("-fx-text-fill: #000000")
                    .setButtonsArrangement(WindowTitleBar.ButtonsArrangement.CLOSE)
                    .setOnClose(() -> MainApp.closeStage(stage, Player.ANIMATIONS))
                    .setSupportStyling(MainApp.childWindowTitleBarTheme);
            windowTitleBar.linkToStage();

            OptionsMenu menu = fxmlStageBuilder.getFxmlLoader().getController();
            //Font: 20
            //QuickPlaylistManagement: Add/Remove/Move Songs; Cleanup/Info/Delete Playlist
            //Other Playlist Tools: Manage/Create playlists
            //Backup: Create/ Restore Backup
            //Other Options: App Settings, Manage Playlists, Stop Player, Credits
            //Uninstallation: Uninstall

            BetterLabel addSongs = BetterLabel.of(stage).useDefaultPresets();
            double quickPlaylistWidth = menu.quickPlaylist.getPrefWidth();
            double quickPlaylistX = menu.quickPlaylist.getLayoutX();
            addSongs.setText("Add Songs")
                    .setWidth(quickPlaylistWidth)
                    .setFont(20)
                    .setLayout_X(quickPlaylistX)
                    .setLayout_Y(96)
                    .setOnMouseEntered(event -> addSongs.setLabelStyling(onMouseEnterTextFill, "-fx-cursor: hand"))
                    .setOnMouseExited(event -> addSongs.setLabelStyling(""))
                    .setOnMouseClicked(event -> {
                        if(Playlists.getPlaylistsCount() == 0) {
                            new OutputUtilities().setOutputLabel(menu.statusLabel, "There are no available playlists", OutputUtilities.Level.INCOMPLETE, Player.ANIMATIONS);
                            return;
                        }
                        AddSongs.launchWindow(Player.SELECTED_PLAYLIST);
                        fxmlStageBuilder.close();
                    });
            addSongs.linkToStage();

            BetterLabel removeSongs = BetterLabel.of(stage).useDefaultPresets();
            removeSongs.setText("Remove Songs")
                        .setWidth(quickPlaylistWidth)
                        .setFont(20)
                        .setLayout_X(quickPlaylistX)
                        .setLayout_Y(126)
                        .setOnMouseEntered(event -> removeSongs.setLabelStyling(onMouseEnterTextFill, "-fx-cursor: hand"))
                        .setOnMouseExited(event -> removeSongs.setLabelStyling(""))
                        .setOnMouseClicked(event -> {
                            if(Playlists.getPlaylistsCount() == 0) {
                                new OutputUtilities().setOutputLabel(menu.statusLabel, "There are no available playlists", OutputUtilities.Level.INCOMPLETE, Player.ANIMATIONS);
                                return;
                            }
                            RemoveSongs.launchWindow(Player.SELECTED_PLAYLIST);
                            fxmlStageBuilder.close();
                        });
            removeSongs.linkToStage();

            BetterLabel moveSongs = BetterLabel.of(stage).useDefaultPresets();
            moveSongs.setText("Move Songs")
                      .setWidth(quickPlaylistWidth)
                      .setFont(20)
                      .setLayout_X(quickPlaylistX)
                      .setLayout_Y(156)
                      .setOnMouseEntered(event -> moveSongs.setLabelStyling(onMouseEnterTextFill, "-fx-cursor: hand"))
                      .setOnMouseExited(event -> moveSongs.setLabelStyling(""))
                      .setOnMouseClicked(event -> {
                          if(Playlists.getPlaylistsCount() == 0) {
                              new OutputUtilities().setOutputLabel(menu.statusLabel, "There are no available playlists", OutputUtilities.Level.INCOMPLETE, Player.ANIMATIONS);
                              return;
                          }
                          if(Playlists.getPlaylistsCount() < 2){
                              new OutputUtilities().setOutputLabel(menu.statusLabel, "You must have at least 2 active playlists to access this feature.", OutputUtilities.Level.INCOMPLETE);
                              return;
                          }
                          MoveSongs.launchWindow(Player.SELECTED_PLAYLIST);
                          fxmlStageBuilder.close();
                      });
            moveSongs.linkToStage();

            BetterLabel cleanupPlaylist = BetterLabel.of(stage).useDefaultPresets();
            cleanupPlaylist.setText("Cleanup Playlist")
                    .setWidth(quickPlaylistWidth)
                    .setFont(20)
                    .setLayout_X(quickPlaylistX)
                    .setLayout_Y(186)
                    .setOnMouseEntered(event -> cleanupPlaylist.setLabelStyling(onMouseEnterTextFill, "-fx-cursor: hand"))
                    .setOnMouseExited(event ->  cleanupPlaylist.setLabelStyling(""))
                    .setOnMouseClicked(event -> {
                        if(Playlists.getPlaylistsCount() == 0) {
                            new OutputUtilities().setOutputLabel(menu.statusLabel, "There are no available playlists", OutputUtilities.Level.INCOMPLETE, Player.ANIMATIONS);
                            return;
                        }
                        ScreenLauncher.launchPlaylistCleanupScreen(Player.SELECTED_PLAYLIST);
                        fxmlStageBuilder.close();
                    });
            cleanupPlaylist.linkToStage();

            BetterLabel playlistInfo = BetterLabel.of(stage).useDefaultPresets();
            playlistInfo.setText("Playlist Information")
                    .setWidth(quickPlaylistWidth)
                    .setFont(20)
                    .setLayout_X(quickPlaylistX)
                    .setLayout_Y(216)
                    .setOnMouseEntered(event -> playlistInfo.setLabelStyling(onMouseEnterTextFill, "-fx-cursor: hand"))
                    .setOnMouseExited(event ->  playlistInfo.setLabelStyling(""))
                    .setOnMouseClicked(event -> {
                        if(Playlists.getPlaylistsCount() == 0) {
                            new OutputUtilities().setOutputLabel(menu.statusLabel, "There are no available playlists", OutputUtilities.Level.INCOMPLETE, Player.ANIMATIONS);
                            return;
                        }
                        PlaylistInfo.launchWindow(Player.SELECTED_PLAYLIST);
                        fxmlStageBuilder.close();
                    });
            playlistInfo.linkToStage();

            BetterLabel deletePlaylist = BetterLabel.of(stage).useDefaultPresets();
            deletePlaylist.setText("Delete Playlist")
                    .setWidth(quickPlaylistWidth)
                    .setFont(20)
                    .setLayout_X(quickPlaylistX)
                    .setLayout_Y(246)
                    .setOnMouseEntered(event -> deletePlaylist.setLabelStyling(onMouseEnterTextFill, "-fx-cursor: hand"))
                    .setOnMouseExited(event ->  deletePlaylist.setLabelStyling(""))
                    .setOnMouseClicked(event -> {
                        if(Playlists.getPlaylistsCount() == 0) {
                            new OutputUtilities().setOutputLabel(menu.statusLabel, "There are no available playlists", OutputUtilities.Level.INCOMPLETE, Player.ANIMATIONS);
                            return;
                        }
                        ScreenLauncher.launchDeletePlaylistScreen(Player.SELECTED_PLAYLIST);
                        fxmlStageBuilder.close();
                    });
            deletePlaylist.linkToStage();

            //Other Playlist Tools
            double otherPlaylistToolsWidth = menu.otherPlaylistOptions.getPrefWidth();
            double otherPlaylistToolsX = menu.otherPlaylistOptions.getLayoutX();

            BetterLabel createPlaylist = BetterLabel.of(stage).useDefaultPresets();
            createPlaylist.setText("Create Playlist [ALT + N]")
                    .setWidth(otherPlaylistToolsWidth)
                    .setFont(20)
                    .setLayout_X(otherPlaylistToolsX)
                    .setLayout_Y(96)
                    .setOnMouseEntered(event -> createPlaylist.setLabelStyling(onMouseEnterTextFill, "-fx-cursor: hand"))
                    .setOnMouseExited(event ->  createPlaylist.setLabelStyling(""))
                    .setOnMouseClicked(event -> {
                        LinkDownloader.launchWindow(false);
                        fxmlStageBuilder.close();
                    });
            createPlaylist.linkToStage();

            BetterLabel managePlaylists = BetterLabel.of(stage).useDefaultPresets();
            managePlaylists.setText("Manage Playlists [ALT + M]")
                    .setWidth(otherPlaylistToolsWidth)
                    .setFont(20)
                    .setLayout_X(otherPlaylistToolsX)
                    .setLayout_Y(126)
                    .setOnMouseEntered(event -> managePlaylists.setLabelStyling(onMouseEnterTextFill, "-fx-cursor: hand"))
                    .setOnMouseExited(event ->  managePlaylists.setLabelStyling(""))
                    .setOnMouseClicked(event -> {
                        ManagePlaylists.launchWindow();
                        fxmlStageBuilder.close();
                    });
            managePlaylists.linkToStage();

            //Backup
            double backupWidth = menu.backup.getPrefWidth();
            double backupX = menu.backup.getLayoutX();

            BetterLabel createBackup = BetterLabel.of(stage).useDefaultPresets();
            createBackup.setText("Create Backup")
                    .setWidth(backupWidth)
                    .setFont(20)
                    .setLayout_X(backupX)
                    .setLayout_Y(96)
                    .setOnMouseEntered(event -> createBackup.setLabelStyling(onMouseEnterTextFill, "-fx-cursor: hand"))
                    .setOnMouseExited(event -> createBackup.setLabelStyling(""))
                    .setOnMouseClicked(event -> {
                        OutputUtilities outputUtilities = new OutputUtilities();
                        WrappedValue<File> result = WrappedValue.of(null);
                        File desktopFile = new File(MainApp.DESKTOP_PATH);
                        DirectorySelector directorySelector = new DirectorySelector();
                        directorySelector
                                .setInitialDirectory(desktopFile)
                                .setTitle("Select the directory where you want to save the backup to.")
                                .launch();
                        result.set(directorySelector.getResult());
                        if(result.get() == null){
                            outputUtilities.setOutputLabel(menu.statusLabel, "Invalid backup path", OutputUtilities.Level.INCOMPLETE, Player.ANIMATIONS);
                            return;
                        }else{
                            if(BackupManager.backupExists(result.get().getAbsolutePath())){
                                ScreenLauncher.launchWarningMessageBox(
                                        "Backup already existing",
                                        "Uhm... There's already another live backup there.",
                                        """
                                                The directory where you want to store this backup has another one already.
                                                You can override it by pressing the 'OK' button.
                                                
                                                WARNING! Every file inside the old backup folder will be replaced with the ones from a fresh one.
                                                If you have important data there, please save it first.
                                                """, event2 -> {
                                            outputUtilities.setOutputLabel(menu.statusLabel, "Creating your backup...", OutputUtilities.Level.SUCCESS, Player.ANIMATIONS);
                                            BackupManager.createBackup(result.get(), true);
                                            outputUtilities.setOutputLabel(menu.statusLabel, "Backup created successfully", OutputUtilities.Level.SUCCESS, Player.ANIMATIONS);
                                        }
                                );
                            }else{
                                outputUtilities.setOutputLabel(menu.statusLabel, "Creating your backup...", OutputUtilities.Level.SUCCESS, Player.ANIMATIONS);
                                BackupManager.createBackup(result.get(), false);
                                outputUtilities.setOutputLabel(menu.statusLabel, "Backup created successfully", OutputUtilities.Level.SUCCESS, Player.ANIMATIONS);
                            }
                        }
                        fxmlStageBuilder.close();
                    });
            createBackup.linkToStage();

            BetterLabel restoreBackup = BetterLabel.of(stage).useDefaultPresets();
            restoreBackup.setText("Restore Backup")
                    .setWidth(backupWidth)
                    .setFont(20)
                    .setLayout_X(backupX)
                    .setLayout_Y(126)
                    .setOnMouseEntered(event -> restoreBackup.setLabelStyling(onMouseEnterTextFill, "-fx-cursor: hand"))
                    .setOnMouseExited(event ->  restoreBackup.setLabelStyling(""))
                    .setOnMouseClicked(event -> {
                        OutputUtilities outputUtilities = new OutputUtilities();
                        WrappedValue<File> result = WrappedValue.of(null);
                        File desktopFile = new File(MainApp.DESKTOP_PATH);
                        DirectorySelector directorySelector = new DirectorySelector();
                        directorySelector
                                .setInitialDirectory(desktopFile)
                                .setTitle("Select the directory where you want to save the backup to.")
                                .launch();
                        result.set(directorySelector.getResult());
                        if(result.get() == null) {
                            outputUtilities.setOutputLabel(menu.statusLabel, "Invalid backup path", OutputUtilities.Level.INCOMPLETE, Player.ANIMATIONS);
                        }else {
                            if (!BackupManager.isVersionOkay(result.get()) && BackupManager.isActiveBackupDirectory(result.get())) {
                                ScreenLauncher.launchWarningMessageBox(
                                        "Backup version not compatible!",
                                        "Backup version is not compatible with the application!",
                                        """
                                                Version incompatibility could lead to app not starting properly, errors or inconsistent user experience.
                                                
                                                Before proceeding, please keep in mind that before restoring, all the current files will be removed and \
                                                replaced with the ones from the backup. If after backup the application does not work properly, \
                                                there's nothing that could be done about it, and a clean app install could be required. Do it at your own risk.
                                                
                                                If you would still like to continue, press the 'OK' button below.
                                                """, event2 -> BackupManager.restoreBackup(result.get())
                                );
                            }else{
                                if(!BackupManager.isActiveBackupDirectory(result.get())){
                                    outputUtilities.setOutputLabel(menu.statusLabel, "This path doesn't contain a valid backup directory", OutputUtilities.Level.INCOMPLETE, Player.ANIMATIONS);
                                    return;
                                }
                                BackupManager.restoreBackup(result.get());
                                fxmlStageBuilder.close();
                            }
                        }
                    });
            restoreBackup.linkToStage();

            //Other Options
            double otherOptionsWidth = menu.otherOptions.getPrefWidth();
            double otherOptionsX = menu.otherOptions.getLayoutX();

            BetterLabel settings = BetterLabel.of(stage).useDefaultPresets();
            settings.setText("App Settings [ALT+S]")
                    .setWidth(otherOptionsWidth)
                    .setFont(20)
                    .setLayout_X(otherOptionsX)
                    .setLayout_Y(96)
                    .setOnMouseEntered(event -> settings.setLabelStyling(onMouseEnterTextFill, "-fx-cursor: hand"))
                    .setOnMouseExited(event ->  settings.setLabelStyling(""))
                    .setOnMouseClicked(event -> {
                        SettingsPage.launchWindow(false);
                        fxmlStageBuilder.close();
                    });
            settings.linkToStage();

            BetterLabel stopPlayer = BetterLabel.of(stage).useDefaultPresets();
            stopPlayer.setText("Stop the Player")
                    .setWidth(otherOptionsWidth)
                    .setFont(20)
                    .setLayout_X(otherOptionsX)
                    .setLayout_Y(126)
                    .setOnMouseEntered(event -> stopPlayer.setLabelStyling(onMouseEnterTextFill, "-fx-cursor: hand"))
                    .setOnMouseExited(event ->  stopPlayer.setLabelStyling(""))
                    .setOnMouseClicked(event -> {
                        Utilities.forceStopPlayer(Player.instance);
                        fxmlStageBuilder.close();
                    });
            stopPlayer.linkToStage();

            BetterLabel credits = BetterLabel.of(stage).useDefaultPresets();
            credits.setText("Credentials")
                    .setWidth(otherOptionsWidth)
                    .setFont(20)
                    .setLayout_X(otherOptionsX)
                    .setLayout_Y(156)
                    .setOnMouseEntered(event -> credits.setLabelStyling(onMouseEnterTextFill, "-fx-cursor: hand"))
                    .setOnMouseExited(event ->  credits.setLabelStyling(""))
                    .setOnMouseClicked(event -> {
                        Credentials.launch();
                        fxmlStageBuilder.close();
                    });
            credits.linkToStage();

            BetterLabel equalizer = BetterLabel.of(stage).useDefaultPresets();
            equalizer.setText("Audio Equalizer")
                    .setWidth(otherOptionsWidth)
                    .setFont(20)
                    .setLayout_X(otherOptionsX)
                    .setLayout_Y(186)
                    .setOnMouseEntered(event -> equalizer.setLabelStyling(onMouseEnterTextFill, "-fx-cursor: hand"))
                    .setOnMouseExited(event ->  equalizer.setLabelStyling(""))
                    .setOnMouseClicked(event -> {
                        Equalizers.launchWindow();
                        fxmlStageBuilder.close();
                    });
            equalizer.linkToStage();

            //Uninstallation
            BetterLabel uninstall = BetterLabel.of(stage).useDefaultPresets();
            uninstall.setText("Uninstall this App")
                    .setWidth(menu.uninstallation.getPrefWidth())
                    .setFont(20)
                    .setLayout_X(menu.uninstallation.getLayoutX())
                    .setLayout_Y(96)
                    .setOnMouseEntered(event -> uninstall.setLabelStyling("-fx-text-fill: #fc0303", "-fx-cursor: hand"))
                    .setOnMouseExited(event ->  uninstall.setLabelStyling(""))
                    .setOnMouseClicked(event -> {
                        Uninstaller.launchWindow();
                        fxmlStageBuilder.close();
                    });
            uninstall.linkToStage();

            MainApp.openStage(stage, Player.ANIMATIONS, true);
        }catch (IllegalStateException e){
            e.printStackTrace(System.err);
        }
    }
}
