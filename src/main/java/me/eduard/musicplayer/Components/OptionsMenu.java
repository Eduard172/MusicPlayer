package me.eduard.musicplayer.Components;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import me.eduard.musicplayer.Components.Player.Equalizers;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.Library.CustomComponents.BetterLabel;
import me.eduard.musicplayer.Library.CustomComponents.WindowTitleBar;
import me.eduard.musicplayer.Library.Uninstaller;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;
import me.eduard.musicplayer.Utils.Utilities;

public class OptionsMenu {
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
                    ).addExitListenerWithEscape().bindAllStagesCloseKeyCombination().finishBuilding();
            Stage stage = fxmlStageBuilder.getStage();
            WindowTitleBar windowTitleBar = new WindowTitleBar(stage)
                    .setButtonsWidth(35)
                    .setSupportHeight(30)
                    .setButtonsViewType(WindowTitleBar.ButtonsViewType.IMAGED)
                    .setTitleString("Manage settings, backups, create playlists or manage existing ones or uninstall")
                    .setCloseImage("icons/TopBar/XButton2.png")
                    .setMinimizeImage("icons/TopBar/MinimizeIcon.png");
            windowTitleBar.linkToStage();
            windowTitleBar.setSupportStyling("-fx-fill: #808080").setGeneralButtonsStyling("-fx-background-color: transparent");
            windowTitleBar.removeNode(windowTitleBar.getMaximizeButton());
            windowTitleBar.moveButtonNextTo(windowTitleBar.getMinimizeButton(), windowTitleBar.getCloseButton(), false);
            windowTitleBar.autoAdjustImageViews(true, true);

            BetterLabel settings = BetterLabel.of(stage).useDefaultPresets();
            settings.setText("Settings... [ ALT + S ]")
                    .setLayout_X(305)
                    .setLayout_Y(175)
                    .setWidth(270)
                    .setFont(15)
                    .setOnMouseEntered(event -> settings.setLabelStyling("-fx-text-fill: gray", "-fx-cursor: hand"))
                    .setOnMouseExited(event -> settings.setLabelStyling(""))
                    .setOnMouseClicked(event -> {
                        SettingsPage.launchWindow(false);
                        fxmlStageBuilder.close();
                    });
            settings.linkToStage();

            BetterLabel createPlaylist = BetterLabel.of(stage).useDefaultPresets();
            createPlaylist.setText("Create Playlist [ ALT + N ]")
                    .setLayout_X(305)
                    .setLayout_Y(200)
                    .setWidth(270)
                    .setFont(15)
                    .setOnMouseEntered(event -> createPlaylist.setLabelStyling("-fx-text-fill: gray", "-fx-cursor: hand"))
                    .setOnMouseExited(event -> createPlaylist.setLabelStyling(""))
                    .setOnMouseClicked(event -> {
                        LinkDownloader.launchWindow(false);
                        fxmlStageBuilder.close();
                    });
            createPlaylist.linkToStage();

            BetterLabel managePlaylists = BetterLabel.of(stage).useDefaultPresets();
            managePlaylists
                    .setText("Manage Playlists [ ALT + M ]")
                    .setLayout_X(305)
                    .setLayout_Y(225)
                    .setWidth(270)
                    .setFont(15)
                    .setOnMouseEntered(event -> managePlaylists.setLabelStyling("-fx-text-fill: gray", "-fx-cursor: hand"))
                    .setOnMouseExited(event -> managePlaylists.setLabelStyling(""))
                    .setOnMouseClicked(event -> {
                        ManagePlaylists.launchWindow();
                        fxmlStageBuilder.close();
                    });
            managePlaylists.linkToStage();

            BetterLabel equalizers = BetterLabel.of(stage).useDefaultPresets();
            equalizers
                    .setText("Audio Equalizer...")
                    .setLayout_X(305)
                    .setLayout_Y(250)
                    .setWidth(270)
                    .setFont(15)
                    .setOnMouseEntered(event -> equalizers.setLabelStyling("-fx-text-fill: gray", "-fx-cursor: hand"))
                    .setOnMouseExited(event -> equalizers.setLabelStyling(""))
                    .setOnMouseClicked(event -> {
                        Equalizers.launchWindow();
                        fxmlStageBuilder.close();
                    });
            equalizers.linkToStage();

            BetterLabel manageBackups = BetterLabel.of(stage).useDefaultPresets();
            manageBackups
                    .setText("Manage Backups [ ALT + B ]")
                    .setLayout_X(305)
                    .setLayout_Y(275)
                    .setWidth(270)
                    .setFont(15)
                    .setOnMouseEntered(event -> manageBackups.setLabelStyling("-fx-text-fill: gray", "-fx-cursor: hand"))
                    .setOnMouseExited(event -> manageBackups.setLabelStyling(""))
                    .setOnMouseClicked(event -> {
                        BackupWindow.launchWindow();
                        fxmlStageBuilder.close();
                    });
            manageBackups.linkToStage();

            BetterLabel openAppDirectory = BetterLabel.of(stage).useDefaultPresets();
            openAppDirectory.setText("Open Application Main Directory [ ALT + E ]")
                    .setLayout_X(305)
                    .setLayout_Y(300)
                    .setWidth(270)
                    .setFont(15)
                    .setOnMouseEntered(event -> openAppDirectory.setLabelStyling("-fx-text-fill: gray", "-fx-cursor: hand"))
                    .setOnMouseExited(event -> openAppDirectory.setLabelStyling(""))
                    .setOnMouseClicked(event -> {
                        Utilities.openApplicationDirectory();
                        fxmlStageBuilder.close();
                    });
            openAppDirectory.linkToStage();

            BetterLabel keyBinds = BetterLabel.of(stage).useDefaultPresets();
            keyBinds.setText("KeyBinds [ ALT + K ]")
                    .setLayout_X(305)
                    .setLayout_Y(325)
                    .setWidth(270)
                    .setFont(15)
                    .setOnMouseEntered(event -> keyBinds.setLabelStyling("-fx-text-fill: gray", "-fx-cursor: hand"))
                    .setOnMouseExited(event -> keyBinds.setLabelStyling(""))
                    .setOnMouseClicked(event -> {
                        Keybinds.launchWindow(true);
                        fxmlStageBuilder.close();
                    });
            keyBinds.linkToStage();

            BetterLabel credentials = BetterLabel.of(stage).useDefaultPresets();
            credentials.setText("Credentials...")
                    .setLayout_X(305)
                    .setLayout_Y(350)
                    .setWidth(270)
                    .setFont(15)
                    .setOnMouseEntered(event -> credentials.setLabelStyling("-fx-text-fill: gray", "-fx-cursor: hand"))
                    .setOnMouseExited(event -> credentials.setLabelStyling(""))
                    .setOnMouseClicked(event -> {
                        Credentials.launch();
                        fxmlStageBuilder.close();
                    });
            credentials.linkToStage();

            BetterLabel uninstall = BetterLabel.of(stage).useDefaultPresets();
            uninstall.setText("Uninstall...")
                    .setLayout_X(305)
                    .setLayout_Y(375)
                    .setWidth(270)
                    .setFont(15)
                    .setOnMouseEntered(event -> uninstall.setLabelStyling("-fx-text-fill: gray", "-fx-cursor: hand"))
                    .setOnMouseExited(event -> uninstall.setLabelStyling(""))
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
