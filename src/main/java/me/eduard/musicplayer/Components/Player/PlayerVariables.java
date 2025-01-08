package me.eduard.musicplayer.Components.Player;

import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Rectangle;
import me.eduard.musicplayer.Library.CustomComponents.BetterLabel;

public class PlayerVariables extends PlayerSettings{
    public double titleBarXOffset, titleBarYOffset;
    public static Player instance;
    public final int waitUntilSongNameChange = 50;
    public final float soundAdjust = (float) 100 / 60;
    public boolean isRunning = false;
    public static boolean isProgressBarAnimationOn = false;
    public static boolean isMaximized = false;
    public static boolean ignoreXYOffsets = false;

    @FXML
    public MenuItem quitAppItem, createPlayList, openDirectory, managePlaylists, uninstall, keybinds, settingsPage, manageBackups;
    @FXML public MediaView mediaView;
    public MediaPlayer videoPlayer;
    public MediaPlayer audioPlayer;
    public Media videoMedia, audioMedia;
    @FXML
    public Button playButton, reset, choosePlaylist, refreshButton, closeAllActiveWindows, nextSong, previousSong, CloseButton, MinimizeButton,
                  fullscreenButton, optionsButton, loadSong;
    @FXML
    public ListView<String> listView;
    @FXML
    public Label durationLabel, volumeLabel, noPlaylistsLabel, statusLabel, titleLabel, devLabel, PlayerMainLabel, devLabel2,
                 memoryCleanup, dummyLabel, hidePlayerLabel;
    @FXML
    public Slider volumeSlider, timeSlider;
    @FXML
    public AnchorPane corePane;
    @FXML
    public ProgressBar progressBar;
    @FXML
    public Rectangle timerBorder, titleBar, unfocusedScreen, playerHoverImage, fullScreenBackground;
    @FXML
    public Hyperlink noPlaylistsLink;
    @FXML
    public Hyperlink clearCaches;
    @FXML
    public ImageView CloseImage, MinimizeImage, fullscreenImage, previousSong2, playButton2, nextSong2;
    @FXML public TextField searchSong;
    public Timeline progressBarAnimation, animationPending;
    public String currentlyPlayingSong;
    public BetterLabel currentPlaylistLabel = BetterLabel.of().setText("Playlist: [None]");
    public BetterLabel songName1 = BetterLabel.of().setText("Eduard is cool.");
    public final String MEDIA_TITLE_LABEL_HIDDEN = "[Media title was hidden due to user settings]";
}
