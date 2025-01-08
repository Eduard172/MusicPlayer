package me.eduard.musicplayer.Components.Player;

import javafx.util.Duration;
import me.eduard.musicplayer.Library.BasicKeyValuePair;
import me.eduard.musicplayer.Library.FullScreenMode;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.Utilities;

public class PlayerFullScreen {

    public static boolean isFullScreen = false;
    private static boolean isInitialized = false;


    private static void initializePlayerFSBackground(Player player){
        isInitialized = true;
        player.fullScreenBackground.setLayoutX(0);
        player.fullScreenBackground.setLayoutY(player.titleBar.getHeight());
        player.fullScreenBackground.setWidth(MainApp.VISUAL_SCREEN_WIDTH);
        player.fullScreenBackground.setHeight(MainApp.VISUAL_SCREEN_HEIGHT);
    }

    public static void makeFullScreen(Player player){
        if(!isInitialized){
            initializePlayerFSBackground(player);
        }
        player.titleLabelString = player.titleLabel.getText();
        isFullScreen = true;
        FullScreenMode.setFullScreen(true);
        player.mediaView.setLayoutX(0);
        player.mediaView.setLayoutY(0);
        player.mediaView.setViewOrder(0);
        player.mediaView.setFitWidth(MainApp.VISUAL_SCREEN_WIDTH);
        player.mediaView.setFitHeight(MainApp.VISUAL_SCREEN_HEIGHT);
        player.mediaView.setLayoutX(15); //Default value in Scene Builder.
        player.mediaView.setLayoutY(80); //Default value in Scene Builder.
        BasicKeyValuePair<Double, Double> layoutX = BasicKeyValuePair.of(0.0, MainApp.VISUAL_SCREEN_WIDTH);
        BasicKeyValuePair<Double, Double> layoutY = BasicKeyValuePair.of(player.titleBar.getHeight(), MainApp.VISUAL_SCREEN_HEIGHT);
        Utilities.sleep(Duration.millis(50), 1, run -> {
            player.titleLabel.setText("");
            player.fullScreenBackground.setVisible(true);
            double videoWidth = player.mediaView.getBoundsInLocal().getWidth();
            double videoHeight = player.mediaView.getBoundsInLocal().getHeight();
            double resultPosition = (layoutX.getKey() + layoutX.getValue()) / 2 - (videoWidth / 2);
            double resultYPos = (layoutY.getKey() + layoutY.getValue()) / 2 - (videoHeight / 2);
            player.mediaView.setLayoutX(resultPosition);
            player.mediaView.setLayoutY(resultYPos);
            player.playerHoverImage.setWidth(videoWidth);
            player.playerHoverImage.setHeight(videoHeight);
            player.playerHoverImage.setLayoutX(player.mediaView.getLayoutX());
            player.playerHoverImage.setLayoutY(player.mediaView.getLayoutY());
        }, null);
    }

    public static void makeNormal(Player player){
        isFullScreen = false;
        player.titleLabel.setText(player.titleLabelString);
        player.fullScreenBackground.setVisible(false);
        player.mediaView.setPreserveRatio(true);
        player.mediaView.setViewOrder(0);
        Utilities.sleep(Duration.millis(100), 1, run -> FullScreenMode.setFullScreen(FullScreenMode.isFullScreen, true), null);
    }

}
