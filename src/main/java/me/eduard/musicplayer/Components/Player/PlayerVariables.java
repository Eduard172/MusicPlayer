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

    public static Player instance;
    public final int waitUntilSongNameChange = 50;
    public final float soundAdjust = (float) 100 / 75;
    public boolean isRunning = false;
    public static boolean isProgressBarAnimationOn = false;

    public static PlayerFullScreen fullScreenMode;

    @FXML public MenuItem quitAppItem;
    @FXML public MenuItem createPlayList;
    @FXML public MenuItem openDirectory;
    @FXML public MenuItem managePlaylists;
    @FXML public MenuItem uninstall;
    @FXML public MenuItem keybinds;
    @FXML public MenuItem settingsPage;
    @FXML public MenuItem manageBackups;
    @FXML public MediaView mediaView;

    public MediaPlayer videoPlayer;
    public MediaPlayer audioPlayer;

    public Media videoMedia;
    public Media audioMedia;

    @FXML public Button playButton;
    @FXML public Button reset;
    @FXML public Button choosePlaylist;
    @FXML public Button refreshButton;
    @FXML public Button closeAllActiveWindows;
    @FXML public Button nextSong;
    @FXML public Button previousSong;
    @FXML public Button optionsButton;
    @FXML public Button loadSong;

    @FXML public ListView<String> listView;
    @FXML public Label durationLabel;
    @FXML public Label volumeLabel;
    @FXML public Label noPlaylistsLabel;
    @FXML public Label statusLabel;
    @FXML public Label devLabel;
    @FXML public Label PlayerMainLabel;
    @FXML public Label devLabel2;
    @FXML public Label memoryCleanup;
    @FXML public Label dummyLabel;
    @FXML public Label avSyncIndicator;

    @FXML public Slider volumeSlider, timeSlider;

    @FXML public AnchorPane corePane;

    @FXML public ProgressBar progressBar;

    @FXML public Rectangle timerBorder;
    @FXML public Rectangle unfocusedScreen;
    @FXML public Rectangle fullScreenBackground;

    @FXML public Hyperlink noPlaylistsLink;

    @FXML public ImageView previousSong2;
    @FXML public ImageView playButton2;
    @FXML public ImageView nextSong2;
    @FXML public ImageView thumbnail;

    @FXML public TextField searchSong;

    public Timeline progressBarAnimation;
    public Timeline animationPending;

    public String currentlyPlayingSong = "";
    public String currentlyPlayingSongUnmodified = "";
    public final String MEDIA_TITLE_LABEL_HIDDEN = "[Media title was hidden due to user settings]";

    public BetterLabel currentPlaylistLabel = BetterLabel.of().setText("Playlist: [None]");
    public BetterLabel songName1 = BetterLabel.of().setText("Eduard is cool.");

}
