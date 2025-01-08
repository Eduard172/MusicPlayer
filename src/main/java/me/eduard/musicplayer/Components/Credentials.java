package me.eduard.musicplayer.Components;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.Library.CustomComponents.WindowTitleBar;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.NodeUtils;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;
import me.eduard.musicplayer.Utils.Utilities;

public class Credentials {
    @FXML public AnchorPane corePane;
    @FXML public Hyperlink yt_dlpSource, ffmpegSource, devGithub;
    public static void launch(){
        try {
            FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("Credentials")
                    .withStageBuilder(
                            StageBuilder.newBuilder()
                                    .title("Credits")
                                    .resizable(false)
                                    .icon("icons/icon.png")
                                    .removeUpperBar()
                                    .styleSheet("ApplicationWindow")
                    ).bindAllStagesCloseKeyCombination().addExitListenerWithEscape().finishBuilding();
            Stage stage = fxmlStageBuilder.getStage();
            Credentials credentials = fxmlStageBuilder.getFxmlLoader().getController();
            credentials.ffmpegSource.setOnAction(event -> Utilities.openBrowserURL("https://www.ffmpeg.org/download.html"));
            credentials.yt_dlpSource.setOnAction(event -> Utilities.openBrowserURL("https://github.com/yt-dlp/yt-dlp"));
            credentials.devGithub.setOnAction(event -> Utilities.openBrowserURL("https://github.com/Eduard172"));
            WindowTitleBar windowTitleBar = new WindowTitleBar(stage).useDefaultPresets();
            windowTitleBar.setTitleString("Credentials");
            windowTitleBar.linkToStage();
            windowTitleBar.applyAfterLinkPresets();
            NodeUtils.addBasicHyperLinkMouseEvents(credentials.ffmpegSource, credentials.yt_dlpSource);
            MainApp.openStage(stage, Player.ANIMATIONS, true);
        }catch (IllegalStateException e){
            e.printStackTrace(System.err);
        }
    }
}
