package me.eduard.musicplayer.Library;

import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.Components.Player.PlayerFullScreen;
import me.eduard.musicplayer.Components.ScreenLauncher;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.Logging.LoggerFormatter;
import me.eduard.musicplayer.Utils.Logging.LoggerHandler;
import me.eduard.musicplayer.Utils.Utilities;

import java.util.logging.Logger;

@SuppressWarnings("unused")
public class FullScreenMode {

    private static final Logger LOGGER = Logger.getLogger("FullScreen-Mode");

    static {
        LoggerHandler handler = new LoggerHandler();
        handler.setFormatter(new LoggerFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
    }

    private static final BasicKeyValuePair<Double, Double> lastCoordsBeforeFullScreen = new BasicKeyValuePair<>(0.0d, 0.0d);

    public static boolean isFullScreen = false;
    private static boolean warningShowed =  false;

     private static void launchWarning(){
         if(!warningShowed){
             ScreenLauncher.launchWarningMessageBox(
                     "This feature is not finished.",
                     "This feature might not work as expected.",
                     """
                             The 'FullScreen' mode is still under development and the player's controls' position may NOT be accurately \
                             calculated, resulting in some of them being out of the screen's bounds or overlaid over other essential \
                             components that you couldn't access with this mode enabled.
 
 
                             If the components do not come back to their original location when turning off this feature, \
                             then an application restart might be required.""",
                     null
             );
             warningShowed = true;
         }
     }

     public static void setFullScreen(boolean fullScreen, boolean afterPlayerFS) {
         if(afterPlayerFS)
             disableFullScreen();
         setFullScreen(fullScreen);
     }
    public static void setFullScreen(boolean fullScreen){
        if (fullScreen)
            enableFullScreen();
//            launchWarning();
        else
            disableFullScreen();
    }

    public static void enableOrDisableFullScreen(){
//        launchWarning();
        if(isFullScreen){
            disableFullScreen();
        }
        else{
            enableFullScreen();
        }
    }

    private static void enableFullScreen(){
        isFullScreen = true;
        Player player = Player.instance;
        if(MainApp.stage1.getX() != lastCoordsBeforeFullScreen.getKey() || MainApp.stage1.getY() != lastCoordsBeforeFullScreen.getValue()){
            lastCoordsBeforeFullScreen
                    .key(MainApp.stage1.getX())
                    .value(MainApp.stage1.getY());
            LOGGER.info(
                    "Saved 'X' and 'Y' before fullscreen as '"+lastCoordsBeforeFullScreen.getKey()+"' and '"+lastCoordsBeforeFullScreen.getValue()+"'."
            );
        }
        Utilities.setImageView(player.fullscreenImage, "TopBar/exit_fullscreen.png");
        player.setSongName(player.currentlyPlayingSong, Player.ANIMATIONS);
        BasicKeyValuePair<Double, Double> screenBinds = new BasicKeyValuePair<>(MainApp.VISUAL_SCREEN_WIDTH, MainApp.VISUAL_SCREEN_HEIGHT);
        MainApp.stage1.setX(0);
        MainApp.stage1.setY(0);
        MainApp.stage1.setWidth(screenBinds.getKey());
        MainApp.stage1.setHeight(screenBinds.getValue());
        adjustTopBar(player, screenBinds);
        adjustPlaylistSection(player, screenBinds);
        adjustVerboseLabels(player, screenBinds);
        adjustPlayerComponents(player, screenBinds, isFullScreen);
    }
    private static void disableFullScreen(){
        isFullScreen = false;
        Player player = Player.instance;
        if(PlayerFullScreen.isFullScreen){
            PlayerFullScreen.makeNormal(player);
        }
        Utilities.setImageView(player.fullscreenImage,"TopBar/go_fullscreen.png");
        player.setSongName(player.currentlyPlayingSong, Player.ANIMATIONS);
        BasicKeyValuePair<Double, Double> defaultBindings = new BasicKeyValuePair<>(MainApp.WIDTH, MainApp.HEIGHT);
        MainApp.stage1.setWidth(defaultBindings.getKey());
        MainApp.stage1.setHeight(defaultBindings.getValue());
        MainApp.stage1.setX(lastCoordsBeforeFullScreen.getKey());
        MainApp.stage1.setY(lastCoordsBeforeFullScreen.getValue());
        adjustTopBar(player, defaultBindings);
        adjustPlaylistSection(player, defaultBindings);
        adjustVerboseLabels(player, defaultBindings);
        adjustPlayerComponents(player, defaultBindings, isFullScreen);
    }
    private static void adjustPlaylistSection(Player player, BasicKeyValuePair<Double, Double> screen){
        player.listView.setLayoutX(screen.getKey() - player.listView.getWidth() - 13);
        player.listView.setPrefHeight(screen.getValue() - (126 + 23));
        player.currentPlaylistLabel.setLayout_X(screen.getKey() - ( player.currentPlaylistLabel.getWidth() + 17));
        player.choosePlaylist.setLayoutX(screen.getKey() - (player.choosePlaylist.getWidth() + 13));
        player.refreshButton.setLayoutX(screen.getKey() - (player.refreshButton.getWidth() + 280));
    }

    private static void adjustTopBar(Player player, BasicKeyValuePair<Double, Double> screen){
        player.titleBar.setWidth(screen.getKey());
        player.titleLabel.setPrefWidth(screen.getKey() - (player.CloseButton.getWidth() * 3));
        player.CloseButton.setLayoutX(screen.getKey()- player.CloseButton.getWidth());
        player.CloseImage.setLayoutX(screen.getKey() - player.CloseImage.getFitWidth() - 4);
        player.fullscreenButton.setLayoutX(screen.getKey() - (player.fullscreenButton.getWidth() * 2));
        player.fullscreenImage.setLayoutX(
                screen.getKey() - (player.fullscreenButton.getWidth() * 2) + (player.CloseButton.getWidth() - player.fullscreenImage.getFitWidth()) + 5
        );
        player.MinimizeButton.setLayoutX(screen.getKey() - (player.MinimizeButton.getWidth() * 3));
        player.MinimizeImage.setLayoutX(
                screen.getKey() - (player.MinimizeButton.getWidth() * 3) + (player.MinimizeButton.getWidth() - player.MinimizeImage.getFitWidth()) + 10
        );
    }

    private static void adjustVerboseLabels(Player player, BasicKeyValuePair<Double, Double> screen){
        player.memoryCleanup.setLayoutX(screen.getKey() - (player.memoryCleanup.getWidth() + 13));
        player.memoryCleanup.setLayoutY(screen.getValue() - player.memoryCleanup.getPrefHeight() - 5);
    }

    private static void adjustPlayerComponents(Player player, BasicKeyValuePair<Double, Double> screen, boolean isFullScreen){
        player.timeSlider.setPrefWidth(screen.getKey() - 370 - 7);
        player.timeSlider.setLayoutY(screen.getValue() - player.timeSlider.getPrefHeight() - 195);

        player.progressBar.setPrefWidth(screen.getKey() - 371 - 17);
        player.progressBar.setLayoutY(screen.getValue() - player.progressBar.getPrefHeight() - 193);

        player.timerBorder.setWidth(screen.getKey() - 370 - 18);
        player.timerBorder.setLayoutY(screen.getValue() - player.timerBorder.getHeight() - 193);

        player.clearCaches.setLayoutY(screen.getValue() - player.clearCaches.getHeight() - 10);

        player.statusLabel.setLayoutX(0);
        player.statusLabel.setPrefWidth(player.listView.getLayoutX());
        player.statusLabel.setLayoutY(screen.getValue() - player.statusLabel.getHeight() - 17);

        //This is where the 'isFullScreen' boolean is used.
        double statusLabelWidth = player.statusLabel.getPrefWidth();
        player.previousSong.setLayoutY(screen.getValue() - player.previousSong.getPrefHeight() - 53);
        player.nextSong.setLayoutY(screen.getValue() - player.nextSong.getPrefHeight() - 53);
        player.playButton.setLayoutY(screen.getValue() - player.playButton.getPrefHeight() - 53);
        player.songName1.setLayout_Y(screen.getValue() - player.songName1.getHeight() - 145);
        player.songName1.setWidth(player.listView.getLayoutX() - 206 - player.songName1.getLayoutX());
        player.songName1.resetPosition();
        player.durationLabel.setLayoutY(screen.getValue() - player.durationLabel.getPrefHeight() - 101);
        if(isFullScreen){
            player.playButton.setPrefWidth(player.playButton.getPrefWidth() * 1.6);
            player.playButton.setLayoutX(statusLabelWidth / 2 - (player.playButton.getPrefWidth() / 2));
            player.durationLabel.setLayoutX(statusLabelWidth / 2 - (player.durationLabel.getPrefWidth() / 2));
            player.previousSong.setPrefWidth(player.previousSong.getPrefWidth() * 1.6);
            player.previousSong.setLayoutX(player.playButton.getLayoutX() - player.previousSong.getPrefWidth() - 20);
            player.nextSong.setPrefWidth(player.nextSong.getPrefWidth() * 1.6);
            player.nextSong.setLayoutX(player.playButton.getLayoutX() + player.playButton.getPrefWidth() + 20);
            player.closeAllActiveWindows.setPrefWidth(player.closeAllActiveWindows.getPrefWidth() * 1.6);
        }else{
            player.previousSong.setPrefWidth(104);
            player.previousSong.setLayoutX(219);
            player.playButton.setPrefWidth(216);
            player.playButton.setLayoutX(332);
            player.nextSong.setPrefWidth(104);
            player.nextSong.setLayoutX(554);
            player.closeAllActiveWindows.setPrefWidth(161);
            player.durationLabel.setLayoutX(statusLabelWidth / 2 - (player.durationLabel.getPrefWidth() / 2));
        }
        player.closeAllActiveWindows.setLayoutX(screen.getKey() - player.closeAllActiveWindows.getPrefWidth() - 406);
        player.volumeSlider.setPrefWidth(player.closeAllActiveWindows.getLayoutX() - 323 - player.volumeLabel.getPrefWidth() - 8);
        player.volumeLabel.setLayoutX(player.volumeSlider.getLayoutX() + player.volumeSlider.getPrefWidth() + 4);

        player.mediaView.setFitWidth(screen.getKey() - 367 - 15);
        player.mediaView.setFitHeight(screen.getValue() - 218 - 80);
        player.playerHoverImage.setLayoutY(player.mediaView.getLayoutY());
        player.playerHoverImage.setWidth(screen.getKey() - 367 - 15);
        player.playerHoverImage.setHeight(screen.getValue() - 218 - 80);
        player.searchSong.setLayoutX(screen.getKey() - player.searchSong.getWidth() - 13);
        player.unfocusedScreen.setLayoutY(player.titleBar.getHeight());
        player.unfocusedScreen.setWidth(screen.getKey());
        player.unfocusedScreen.setHeight(screen.getKey() - player.titleBar.getHeight());

        //Optional components (+15px for mediaView / playerHoverImage)
        player.devLabel.setLayoutY(screen.getValue() - player.devLabel.getHeight() - 107);
        player.noPlaylistsLink.setLayoutX((player.mediaView.getFitWidth() / 2) - (player.noPlaylistsLink.getWidth() / 2) + 15);
        player.noPlaylistsLabel.setLayoutX((player.mediaView.getFitWidth() / 2) - (player.noPlaylistsLabel.getWidth() / 2) + 15);
        player.hidePlayerLabel.setLayoutX((player.listView.getLayoutX() / 2) - (player.hidePlayerLabel.getWidth() / 2));
        //The height between "Close all Windows" button and the player "timeBorder"
        double heightDistance = player.timerBorder.getLayoutY() +
                (player.closeAllActiveWindows.getLayoutY() + player.closeAllActiveWindows.getHeight());
        player.hidePlayerLabel.setLayoutY((heightDistance / 2) - (player.hidePlayerLabel.getHeight() / 2));
        Utilities.centralizeMediaPlayer(player);
        Utilities.centralizeImageViewOnButton(player.previousSong2, player.previousSong);
        Utilities.centralizeImageViewOnButton(player.playButton2, player.playButton);
        Utilities.centralizeImageViewOnButton(player.nextSong2, player.nextSong);
    }
}