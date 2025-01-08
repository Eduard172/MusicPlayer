package me.eduard.musicplayer.Components.Notifications;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.Library.Animations.Animations;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;
import me.eduard.musicplayer.Utils.Utilities;

public class SlidingNotification{
    @FXML public AnchorPane corePane;
    @FXML public Label headerLabel, messageLabel;
    @FXML public Line separatorLine;
    @FXML public ImageView closeImage;
    @FXML public Button closeButton, wholeButton;
    public SlidingNotification setHeader(String string){
        this.headerLabel.setText(string);
        return this;
    }
    public SlidingNotification setMessage(String string){
        this.messageLabel.setText(string);
        return this;
    }
    public SlidingNotification setLineSeparatorColor(Color color){
        this.separatorLine.setFill(color);
        return this;
    }
    public SlidingNotification setCloseImage(String path){
        Utilities.setImageView(this.closeImage, path);
        return this;
    }
    public void launchNotification(Stage stage, boolean animate, double duration){
        Animations.animateNotification(stage, animate, duration);
    }
    public static void launch(String header, String message, boolean animate, double duration){
        try {
            FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("Notifications/SlidingNotification")
                    .withStageBuilder(
                            StageBuilder.newBuilder()
                                    .icon("icons/icon.png")
                                    .title("New Notification")
                                    .alwaysOnTop(true)
                                    .resizable(false)
                                    .removeUpperBar()
                                    .styleSheet("Notifications/AppUnfocused")
                    ).finishBuilding();
            Stage stage = fxmlStageBuilder.getStage();

            MainApp.openStage(stage, false, false);
            SlidingNotification slidingNotification = fxmlStageBuilder.getFxmlLoader().getController();
            slidingNotification.wholeButton.setVisible(true);
            slidingNotification.wholeButton.setOnAction(event -> {
                MainApp.stage1.setIconified(false);
                MainApp.closeStage(stage, false);
            });
            slidingNotification.closeButton.setOnAction(event -> MainApp.closeStage(stage, false));
            slidingNotification
                    .setHeader(header)
                    .setMessage(message)
                    .setCloseImage("TopBar/XButton.png")
                    .setLineSeparatorColor(Color.AQUA)
                    .launchNotification(stage, animate, duration);
        }catch (IllegalStateException exception){
            ErrorHandler.launchWindow(exception);
        }
    }
}
