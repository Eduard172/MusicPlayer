package me.eduard.musicplayer.Library;

import javafx.application.Platform;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.Library.Animations.Animations;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.Logging.LoggerFormatter;
import me.eduard.musicplayer.Utils.Logging.LoggerHandler;
import me.eduard.musicplayer.Utils.Utilities;

import java.util.logging.Logger;

@SuppressWarnings("unused")
public class FullScreenMode {

    public static boolean isFullScreen = false;

    private static final Logger LOGGER = Logger.getLogger("FullScreen-Mode");

    static {
        LoggerHandler handler = new LoggerHandler();
        handler.setFormatter(new LoggerFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
    }

    public static void enableFullScreenIf(boolean b){
        if(b) enableFullScreen().run();
        else disableFullScreen().run();
    }

    public static Runnable enableFullScreen(){
        return () -> {
            isFullScreen = true;
            Player player = Player.instance;
            Animations.playerFullScreenAnimate.clear();
            player.setSongName(player.currentlyPlayingSong, Player.ANIMATIONS, (player.videoPlayer != null && player.audioPlayer != null));
            SimplePair<Double, Double> screenBinds = new SimplePair<>(MainApp.VISUAL_SCREEN_WIDTH, MainApp.VISUAL_SCREEN_HEIGHT);
            adjustPlaylistSection(player, screenBinds);
            adjustVerboseLabels(player, screenBinds);
            adjustPlayerComponents(player, screenBinds);
        };
    }
    public static Runnable disableFullScreen(){
        return () -> {
            isFullScreen = false;
            Player player = Player.instance;
            player.setSongName(player.currentlyPlayingSong, Player.ANIMATIONS, (player.videoPlayer != null && player.audioPlayer != null));
            SimplePair<Double, Double> defaultBindings = new SimplePair<>(MainApp.WIDTH, MainApp.HEIGHT);
            adjustPlaylistSection(player, defaultBindings);
            adjustVerboseLabels(player, defaultBindings);
            adjustPlayerComponents(player, defaultBindings);
        };
    }
    private static void adjustPlaylistSection(Player player, SimplePair<Double, Double> screen){
        Platform.runLater(() -> {
            player.listView.setLayoutX(screen.getKey() - player.listView.getWidth() - 13);
            player.listView.setPrefHeight(screen.getValue() - (126 + 23));
            player.currentPlaylistLabel.setLayout_X(screen.getKey() - ( player.currentPlaylistLabel.getWidth() + 17));
            player.choosePlaylist.setLayoutX(screen.getKey() - (player.choosePlaylist.getWidth() + 13));
            player.refreshButton.setLayoutX(screen.getKey() - (player.refreshButton.getWidth() + 280));
        });
    }

    private static void adjustVerboseLabels(Player player, SimplePair<Double, Double> screen){
        Platform.runLater(() -> {
            player.memoryCleanup.setLayoutX(screen.getKey() - (player.memoryCleanup.getWidth() + 13));
            player.memoryCleanup.setLayoutY(screen.getValue() - player.memoryCleanup.getPrefHeight() - 5);
        });
    }

    private static void adjustPlayerComponents(Player player, SimplePair<Double, Double> screen){
        Platform.runLater(() -> {
            player.timeSlider.setPrefWidth(screen.getKey() - 370 - 7);
            player.timeSlider.setLayoutY(screen.getValue() - player.timeSlider.getPrefHeight() - 195);

            player.progressBar.setPrefWidth(screen.getKey() - 371 - 17);
            player.progressBar.setLayoutY(screen.getValue() - player.progressBar.getPrefHeight() - 193);

            player.timerBorder.setWidth(screen.getKey() - 370 - 18);
            player.timerBorder.setLayoutY(screen.getValue() - player.timerBorder.getHeight() - 193);

            player.statusLabel.setLayoutX(0);
            player.statusLabel.setPrefWidth(player.listView.getLayoutX());
            player.statusLabel.setLayoutY(screen.getValue() - player.statusLabel.getHeight() - 17);

            player.avSyncIndicator.setLayoutX(4);
            player.avSyncIndicator.setLayoutY(screen.getValue() - player.avSyncIndicator.getHeight() - 5);

            //This is where the 'isFullScreen' boolean is used.
            double statusLabelWidth = player.statusLabel.getPrefWidth();
            boolean isFullScreen = FullScreenMode.isFullScreen;
            player.previousSong.setLayoutY(screen.getValue() - player.previousSong.getPrefHeight() - 53);
            player.nextSong.setLayoutY(screen.getValue() - player.nextSong.getPrefHeight() - 53);
            player.playButton.setLayoutY(screen.getValue() - player.playButton.getPrefHeight() - 53);
            player.songName1.setLayout_Y(screen.getValue() - player.songName1.getHeight() - 145);
            player.songName1.setWidth(player.listView.getLayoutX() - 206 - player.songName1.getLayoutX());
            player.closeAllActiveWindows.setPrefWidth(isFullScreen ? player.closeAllActiveWindows.getPrefWidth() * 1.6 : 161);
            player.durationLabel.setLayoutY(screen.getValue() - player.durationLabel.getPrefHeight() - 101);
            if(MainApp.instance.playerTitleBar.isFullScreen()){
                player.playButton.setPrefWidth(player.playButton.getPrefWidth() * 1.6);
                player.playButton.setLayoutX(statusLabelWidth / 2 - (player.playButton.getPrefWidth() / 2));
                player.durationLabel.setLayoutX(statusLabelWidth / 2 - (player.durationLabel.getPrefWidth() / 2));
                player.previousSong.setPrefWidth(player.previousSong.getPrefWidth() * 1.6);
                player.previousSong.setLayoutX(player.playButton.getLayoutX() - player.previousSong.getPrefWidth() - 20);
                player.nextSong.setPrefWidth(player.nextSong.getPrefWidth() * 1.6);
                player.nextSong.setLayoutX(player.playButton.getLayoutX() + player.playButton.getPrefWidth() + 20);
            }else{
                player.previousSong.setPrefWidth(104);
                player.previousSong.setLayoutX(219);
                player.playButton.setPrefWidth(216);
                player.playButton.setLayoutX(332);
                player.nextSong.setPrefWidth(104);
                player.nextSong.setLayoutX(554);
                player.durationLabel.setLayoutX(statusLabelWidth / 2 - (player.durationLabel.getPrefWidth() / 2));
            }

            player.closeAllActiveWindows.setLayoutX(screen.getKey() - player.closeAllActiveWindows.getPrefWidth() - 406);
            player.volumeSlider.setPrefWidth(player.closeAllActiveWindows.getLayoutX() - 323 - player.volumeLabel.getPrefWidth() - 8);
            player.volumeLabel.setLayoutX(player.volumeSlider.getLayoutX() + player.volumeSlider.getPrefWidth() + 4);

            player.mediaView.setFitWidth(screen.getKey() - 367);
            player.mediaView.setFitHeight(screen.getValue() - 218 - 80);
            player.thumbnail.setFitWidth(screen.getKey() - 367);
            player.thumbnail.setFitHeight(screen.getValue() - 218 - 80);
//            player.playerHoverImage.setLayoutY(player.mediaView.getLayoutY());
            player.searchSong.setLayoutX(screen.getKey() - player.searchSong.getWidth() - 13);
            player.unfocusedScreen.setLayoutY(0);
            player.unfocusedScreen.setWidth(screen.getKey());
            player.unfocusedScreen.setHeight(screen.getKey() - MainApp.instance.playerTitleBar.getSupportHeight());

            //Optional components (+15px for mediaView / playerHoverImage)
            player.devLabel.setLayoutY(screen.getValue() - player.devLabel.getHeight() - 107);
            player.noPlaylistsLink.setLayoutX((player.mediaView.getFitWidth() / 2) - (player.noPlaylistsLink.getWidth() / 2) + 15);
            player.noPlaylistsLabel.setLayoutX((player.mediaView.getFitWidth() / 2) - (player.noPlaylistsLabel.getWidth() / 2) + 15);
            //The height between "Close all Windows" button and the player "timeBorder"
            double heightDistance = player.timerBorder.getLayoutY() +
                    (player.closeAllActiveWindows.getLayoutY() + player.closeAllActiveWindows.getHeight());
            Utilities.centralizeMediaPlayer(player);
            Utilities.centralizeThumbnail(player);
            Utilities.centralizeImageViewOnButton(player.previousSong2, player.previousSong);
            Utilities.centralizeImageViewOnButton(player.playButton2, player.playButton);
            Utilities.centralizeImageViewOnButton(player.nextSong2, player.nextSong);
            Player.fullScreenMode.mediaViewBounds.setBoth(player.mediaView.getFitWidth(), player.mediaView.getFitHeight());
            Player.fullScreenMode.thumbnailBounds.setBoth(player.thumbnail.getFitWidth(), player.thumbnail.getFitHeight());
            player.songName1.resetPosition();
        });


    }
}