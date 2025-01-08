package me.eduard.musicplayer.Components.Notifications;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.eduard.musicplayer.Components.Player.PlayerSettings;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.Library.Animations.Animations;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;
import me.eduard.musicplayer.Utils.Utilities;

@SuppressWarnings("all")
public class NextSong {

    @FXML private AnchorPane corePane;
    @FXML private Label musicName, upNext;
    @FXML public Button closeButton, wholeButton;
    @FXML private ImageView closeImage;
    public void setMusicName(String name){
        this.musicName.setText(name);
    }
    public void setCloseImage(String closeImage){
        Image image = new Image(Utilities.getResourceAsStream(closeImage));
        this.closeImage.setImage(image);
    }
    public void setUpNext(String name){
        this.upNext.setText(name);
    }
    public void positionToBottomRight(Stage stage, boolean animate, double duration){
        Animations.animateNotification(stage, animate, duration);
    }
    public static void launch(String musicName, String upNext){
        try {
            FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("Notifications/NextSong")
                    .withStageBuilder(StageBuilder.newBuilder()
                            .removeUpperBar()
                            .styleSheet("Notifications/AppUnfocused")
                            .alwaysOnTop(true)
                            .title("A new song is being played")
                    ).finishBuilding();
            Stage stage = fxmlStageBuilder.getStage();
            MainApp.openStage(stage, false, false);
            NextSong appUnfocused = fxmlStageBuilder.getFxmlLoader().getController();
            appUnfocused.wholeButton.setVisible(true);
            appUnfocused.setCloseImage("icons/TopBar/XButton.png");
            appUnfocused.setMusicName(musicName);
            appUnfocused.setUpNext(upNext);
            appUnfocused.positionToBottomRight(stage, PlayerSettings.ANIMATIONS, MainApp.DEFAULT_SN_DURATION);
            appUnfocused.wholeButton.setOnAction(event -> {
                MainApp.stage1.setIconified(false);
                Utilities.sleep(Duration.millis(50), 1, run -> MainApp.closeStage(stage, true), null);
            });
            appUnfocused.closeButton.setOnAction(event -> MainApp.closeStage(stage, false));
        }catch (IllegalStateException exception){
            ErrorHandler.launchWindow(exception);
        }
    }
}
