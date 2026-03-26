package me.eduard.musicplayer.Components;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.Library.Cache.Window.WindowRegistry;
import me.eduard.musicplayer.Library.CustomComponents.WindowTitleBar;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.GlobalAppStyle;
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
                    )
                    .bindAllStagesCloseKeyCombination()
                    .addExitListenerWithEscape()
                    .requireModality()
                    .finishBuilding();

            boolean isInWindowRegistry = WindowRegistry.isInRegistry("CREDITS");

            Stage stage = WindowRegistry.isInRegistry("CREDITS") ?
                    WindowRegistry.getStage("CREDITS") : WindowRegistry.getAndRegister("CREDITS", fxmlStageBuilder.getStage());

            if(!isInWindowRegistry){
                WindowTitleBar windowTitleBar = new WindowTitleBar(stage).useDefaultPresets()
                        .setTitleString("Credentials")
                        .setTitleLabelStyling("-fx-text-fill: #000000")
                        .setOnClose(() -> MainApp.closeStage(stage, Player.ANIMATIONS))
                        .setButtonsArrangement(WindowTitleBar.ButtonsArrangement.CLOSE)
                        .setSupportStyling(MainApp.childWindowTitleBarTheme);
                windowTitleBar.linkToStage();
            }

            Credentials credentials = fxmlStageBuilder.getFxmlLoader().getController();
            credentials.ffmpegSource.setOnAction(event -> Utilities.openBrowserURL("https://www.ffmpeg.org/download.html"));
            credentials.yt_dlpSource.setOnAction(event -> Utilities.openBrowserURL("https://github.com/yt-dlp/yt-dlp"));
            credentials.devGithub.setOnAction(event -> Utilities.openBrowserURL("https://github.com/Eduard172"));
            GlobalAppStyle.applyToHyperLinks(credentials.ffmpegSource, credentials.yt_dlpSource, credentials.devGithub);
            MainApp.openStage(stage, Player.ANIMATIONS, true);
        }catch (IllegalStateException e){
            e.printStackTrace(System.err);
        }
    }
}
