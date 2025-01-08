package me.eduard.musicplayer.Components.Player;

import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.util.Duration;
import me.eduard.musicplayer.AppState;
import me.eduard.musicplayer.Components.*;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.Library.Animations.Animations;
import me.eduard.musicplayer.Library.BasicKeyValuePair;
import me.eduard.musicplayer.Library.Cache.Caches;
import me.eduard.musicplayer.Library.Cache.Player.MediaCache;
import me.eduard.musicplayer.Library.Cache.PlaylistCache;
import me.eduard.musicplayer.Library.Cache.SongRegistry;
import me.eduard.musicplayer.Library.FullScreenMode;
import me.eduard.musicplayer.Library.LambdaObject;
import me.eduard.musicplayer.Library.Uninstaller;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.*;
import me.eduard.musicplayer.Utils.Logging.LoggerFormatter;
import me.eduard.musicplayer.Utils.Logging.LoggerHandler;
import me.eduard.musicplayer.Utils.PlaylistRelated.Playlists;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class Player extends PlayerVariables implements Initializable {

    private static final Logger LOGGER = Logger.getLogger("Player");
    public static volatile boolean STOPPED = false;

    static {
        LoggerHandler consoleHandler = new LoggerHandler();
        consoleHandler.setFormatter(new LoggerFormatter());
        LOGGER.addHandler(consoleHandler);
        LOGGER.setUseParentHandlers(false);
    }

    private final Settings settings = Settings.of("settings.yml");

    private final LambdaObject<Integer> allowedFailedAttempts = LambdaObject.of(5);
    private final LambdaObject<Integer> failedAttempts = LambdaObject.of(0);
    public String titleLabelString = "";

    private static String updatedPath;
    private static boolean isInitialized;
    //Key - Allow for first time click on listview without need of 2nd click. Value: previous onRefresh behaviour.
    private static final BasicKeyValuePair<Boolean, Boolean> preventSongRestart = BasicKeyValuePair.of(false, false);
    private static Task<Void> playerTask = null, audioTask = null;
    public final List<ExecutorService> THREADS = new ArrayList<>();
    private MouseEvent event;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        instance = this;
        this.loadSettings();
        this.initializeScreenBoundsVariables();
        this.initializeApplicationComponents();
        this.initializeOptionsMenuButtons();
        this.initializeComponentsTooltips();
        this.initializeAdditionalEventListeners();
        Platform.runLater(() -> {
            this.initializeListView(SELECTED_PLAYLIST);
            LOGGER.info("Automatic playlist content refresh happens at every 5 seconds.");
            this.autoRefresh();
        });
    }
    private void initializeAdditionalEventListeners() {
        this.searchSong.addEventFilter(KeyEvent.KEY_PRESSED, subEvent -> {
            if(STOPPED)
                return;
            MainApp main = MainApp.instance;
            main.setApplicationState(AppState.SEARCHING_FOR_MEDIA);
            if (subEvent.getCode() == KeyCode.ENTER) {
                Utilities.sleep(Duration.millis(45), 1, run -> {
                    if(!this.searchSong.getText().isEmpty() && !this.listView.getItems().isEmpty()){
                        this.playMedia(
                                SongRegistry.getSong(this.listView.getItems().get(0)),
                                0,
                                Playlists.PATH +"/"+SELECTED_PLAYLIST);
                    }
                    this.searchSong.setText("");
                    this.updateListViewBasedOnSongSearchText("");
                    main.setApplicationState(AppState.NORMAL);
                }, null);
                this.dummyLabel.requestFocus();
            }else if(subEvent.getCode() == KeyCode.ESCAPE) {
                this.searchSong.setText("");
                this.updateListViewBasedOnSongSearchText("");
                this.dummyLabel.requestFocus();
                main.setApplicationState(AppState.NORMAL);
            }else{
                Utilities.sleep(Duration.millis(45), 1, run ->
                    this.updateListViewBasedOnSongSearchText(this.searchSong.getText()), null);
            }
        });
        LOGGER.info("Search function has been loaded.");
    }

    private void initializeScreenBoundsVariables() {
        MainApp.VISUAL_SCREEN_WIDTH = Screen.getPrimary().getVisualBounds().getWidth();
        MainApp.VISUAL_SCREEN_HEIGHT = Screen.getPrimary().getVisualBounds().getHeight();
        MainApp.SCREEN_WIDTH = Screen.getPrimary().getBounds().getWidth();
        MainApp.SCREEN_HEIGHT = Screen.getPrimary().getBounds().getHeight();
        LOGGER.info("Screen resolution is " + (int) MainApp.SCREEN_WIDTH + "x" + (int) MainApp.SCREEN_HEIGHT +
                " (Visual: " + (int) MainApp.VISUAL_SCREEN_WIDTH + "x" + (int) MainApp.VISUAL_SCREEN_HEIGHT + ")");
    }

    private void autoRefresh(){
        if(STOPPED){
            return;
        }
        if(MainApp.instance.getApplicationState() == AppState.NORMAL){
            this.onRefreshAction();
        }
        Utilities.sleep(Duration.millis(5000), 1, run -> this.autoRefresh(), null);
    }

    private void initializeApplicationComponents() {
        LOGGER.info("Setting up each component...");
        this.setupComponentsImageViews();
        this.choosePlaylist.setOnAction(this::onPlaylistSelectAction);
        this.refreshButton.setOnAction(event -> this.onRefreshAction());
        this.closeAllActiveWindows.setOnAction(this::onCloseAllStagesAction);
        this.titleLabel.setOnMousePressed(this::onTitleBarPress);
        this.titleLabel.setOnMouseDragged(this::onTitleBarDrag);
        this.titleLabel.setOnMouseReleased(this::onTitleBarMouseRelease);
        this.titleLabel.setOnMouseClicked(this::onTitleBarMouseClick);

        this.timeSlider.setValue(0);
        this.timeSlider.setOnMousePressed(this::sliderPressed);
        this.timeSlider.setOnMouseDragged(this::sliderPressed);

        this.playButton.setOnAction(this::playButtonAction);
        this.titleBar.setOnMousePressed(this::onTitleBarPress);
        this.titleBar.setOnMouseDragged(this::onTitleBarDrag);
        this.titleBar.setOnMouseReleased(this::onTitleBarMouseRelease);
        this.titleBar.setOnMouseClicked(this::onTitleBarMouseClick);

        this.volumeSlider.setOnMouseDragged(this::volumeSliderDragged);
        this.volumeSlider.setOnMousePressed(this::volumeSliderPressed);
        this.noPlaylistsLink.setOnAction(this::onNoPlaylistLinkClick);
        this.nextSong.setOnAction(this::onNextSongPress);
        this.previousSong.setOnAction(this::onPreviousSongPress);
        this.CloseButton.setOnAction(this::onCloseButtonAction);
        this.MinimizeButton.setOnAction(this::onMinimizeButtonAction);
        this.fullscreenButton.setOnAction(this::onFullScreenButtonAction);
        this.optionsButton.setOnAction(event -> OptionsMenu.launch());
        this.fullscreenButton.setCursor(Cursor.HAND);
        this.CloseButton.setCursor(Cursor.HAND);
        this.MinimizeButton.setCursor(Cursor.HAND);
        this.hidePlayerLabel.setText("""
                Video was hidden due to user settings.
                
                To enable it back:
                - Open settings through the Options menu or
                  click on this label.
                - Uncheck the "Hide the Player Video" option.
                - Click "Save".
                """);
        this.hidePlayerLabel.setOnMouseMoved(event -> this.hidePlayerLabel.setStyle("-fx-text-fill: gray;"));
        this.hidePlayerLabel.setOnMouseExited(event -> this.hidePlayerLabel.setStyle(null));
        this.hidePlayerLabel.setOnMouseClicked(event -> SettingsPage.launchWindow(false));
        this.hidePlayerLabel.setCursor(Cursor.HAND);
        this.clearCaches.setOnAction(this::onClearCacheClick);

        mediaView.setOnMouseMoved(event -> Animations.animatePlayerHoverImageOnEnter(super.playerHoverImage));
        this.playerHoverImage.setOnMouseExited(event -> Animations.undoAnimatePlayerHoverImageOnEnter(super.playerHoverImage));
        this.playerHoverImage.setOnMouseClicked(event -> {
            if(KeyCombinationUtils.isKey(KeyCode.SHIFT, 0) && !PlayerFullScreen.isFullScreen){
                PlayerFullScreen.makeFullScreen(this);
            }else if(KeyCombinationUtils.isKey(KeyCode.SHIFT, 0) && PlayerFullScreen.isFullScreen){
                PlayerFullScreen.makeNormal(this);
            }else{
                this.playButton.fire();
            }
        });
        this.unfocusedScreen.setOnMouseMoved(event -> Animations.undoAnimateOverAllScreen(this, false));

        Utilities.sleep(Duration.millis(100), 1, run -> {
            this.hidePlayerLabel.setLayoutX((this.listView.getLayoutX() / 2) - (this.hidePlayerLabel.getWidth() / 2));
            //The height between "Close all Windows" button and the player "timeBorder"
            double heightDistance = this.timerBorder.getLayoutY() +
                    (this.closeAllActiveWindows.getLayoutY() + this.closeAllActiveWindows.getHeight());
            this.hidePlayerLabel.setLayoutY((heightDistance / 2) - (this.hidePlayerLabel.getHeight() / 2));
        }, null);

    }
    public void onSongNameClick(MouseEvent event){
        if(KeyCombinationUtils.isKey(KeyCode.SHIFT, 0)){
            String[] parts = this.songName1.getText().split(" ");
            String result = Utilities.stringFromArray(parts, "", "+").replace("|", "%7C");
            Utilities.openBrowserURL("https://youtube.com/results?search_query="+result);
            return;
        }
        Utilities.copyToClipboard(this.songName1.getText());
        this.setStatusLabel("This song's name has been copied to your clipboard.", OutputUtilities.Level.SUCCESS);
    }

    private void setupComponentsImageViews(){
        Utilities.setImageView(super.CloseImage, "TopBar/XButton2.png");
        Utilities.setImageView(super.MinimizeImage, "TopBar/MinimizeIcon.png");
        Utilities.setImageView(super.fullscreenImage, "TopBar/go_fullscreen.png");
        Utilities.setImageView2(this.previousSong2, "Player/previous-song.png");
        Utilities.setImageView2(this.playButton2, "Player/pause-player.png");
        Utilities.setImageView2(this.nextSong2, "Player/next-song.png");

        this.previousSong.setOnMousePressed(event -> Utilities.setImageView2(this.previousSong2, "Player/previous-song-hover.png"));
        this.previousSong.setOnMouseReleased(event -> Utilities.setImageView2(this.previousSong2, "Player/previous-song.png"));
        this.playButton.setOnMousePressed(event -> Utilities.setImageView2(
                this.playButton2, (super.isRunning) ? "Player/pause-player-hover.png" : "Player/resume-player-hover.png"
        ));
        this.playButton.setOnMouseReleased(event -> Utilities.setImageView2(
                this.playButton2, (super.isRunning) ? "Player/pause-player.png" : "Player/resume-player.png"
        ));
        this.nextSong.setOnMousePressed(event -> Utilities.setImageView2(this.nextSong2, "Player/next-song-hover.png"));
        this.nextSong.setOnMouseReleased(event -> Utilities.setImageView2(this.nextSong2, "Player/next-song.png"));
        Utilities.centralizeImageViewOnButton(this.playButton2, this.playButton);
        Utilities.centralizeImageViewOnButton(this.nextSong2, this.nextSong);
        Utilities.centralizeImageViewOnButton(this.previousSong2, this.previousSong);

        this.unfocusedScreen.setVisible(false);
        this.unfocusedScreen.setWidth(MainApp.WIDTH);
        this.unfocusedScreen.setHeight(MainApp.HEIGHT - this.titleBar.getHeight());
        this.unfocusedScreen.setLayoutX(0);

        this.playerHoverImage.setLayoutX(this.mediaView.getLayoutX());
        this.playerHoverImage.setLayoutY(this.mediaView.getLayoutY());
        this.playerHoverImage.setWidth(this.mediaView.getBoundsInLocal().getWidth());
        this.playerHoverImage.setHeight(this.mediaView.getBoundsInLocal().getHeight());
        this.playerHoverImage.setVisible(false);
    }

    public void initializeComponentsTooltips() {
        int generalMillisToShow = 200;

        NodeUtils.setTooltip(this.CloseButton, "Stops the player and closes the application and all it's components.", generalMillisToShow);
        NodeUtils.setTooltip(this.MinimizeButton, "Minimizes this window.", generalMillisToShow);
        NodeUtils.setTooltip(this.fullscreenButton, "Makes this window fullscreen or reduces it to it's normal size.", generalMillisToShow);
        NodeUtils.setTooltip(this.refreshButton, "Refreshes the current playlist song list.", generalMillisToShow);
        NodeUtils.setTooltip(this.nextSong, "Plays the next available song in the playlist.", generalMillisToShow);
        NodeUtils.setTooltip(this.previousSong, "Plays the previous available song in the playlist.", generalMillisToShow);
        NodeUtils.setTooltip(this.playButton, "Plays or Pauses the player.", generalMillisToShow);
        NodeUtils.setTooltip(this.choosePlaylist, "Shows a list of all playlists that you can select and play.", generalMillisToShow);
        NodeUtils.setTooltip(this.closeAllActiveWindows, "Closes all opened windows, except the main window.", generalMillisToShow);
        NodeUtils.setTooltip(this.clearCaches, "Clears all saved songs and windows from the memory.", generalMillisToShow);
        NodeUtils.setTooltip(this.timeSlider, "Shows the current time.", 1000);
        NodeUtils.setTooltip(this.volumeSlider, "Shows the volume percentage", generalMillisToShow);
        NodeUtils.setTooltip(this.searchSong, "Search songs from this playlist by their keywords.", generalMillisToShow);
        this.timeSlider.setOnMouseMoved(event -> {
            if(videoPlayer == null || videoPlayer.getStatus() == MediaPlayer.Status.DISPOSED || STOPPED)
                return;
            Tooltip tooltip = this.timeSlider.getTooltip();
            tooltip.setX(event.getScreenX());
            tooltip.setShowDelay(MainApp.STAGES.isEmpty() ? Duration.millis(0) : Duration.millis(1000));
            tooltip.setFont(Font.font(FullScreenMode.isFullScreen ? 20 : 15));
            if (videoPlayer == null) {
                tooltip.setText("Player is not currently active.");
                return;
            }
            int totalDuration = (int) videoPlayer.getTotalDuration().toSeconds();
            TimeConverter.LevelType levelType;
            if (totalDuration > 3600)
                levelType = TimeConverter.LevelType.HOURS;
            else
                levelType = TimeConverter.LevelType.MINUTES;
            double percentage = Utilities.getPercentage(event.getX(), timeSlider.getWidth());
            String durationString = TimeConverter.toString(
                    (int) Utilities.getPortionOfTotal(percentage, totalDuration),
                    levelType,
                    ":"
            );
            durationString = ((int) percentage == 0 ? "beginning (CTRL + Left Arrow)" : percentage >= 99 ? "end" : durationString);
            tooltip.setText("Jump to "+durationString);
        });

    }

    /**
     * This method is temporarily and planned to be removed.
     *
     * @param event On click event.
     */
    public void onClearCacheClick(ActionEvent event) {
        if(STOPPED)
            return;
        String additionalMessage = "Total cached items: " + Caches.getTotalItemsStored() +
                ". (" + Caches.getStoredSongs() + " song(s), " + Caches.getStoredWindows() + " window(s), " + Caches.getStoredMedias() + " media instance(s))";
        ScreenLauncher.launchPlannedToRemoveWarning(additionalMessage, event1 -> {
            Caches.clearCaches();
            NodeUtils.disableNodeActivity(this.clearCaches);
            Animations.undoAnimateObjectAppear(this.clearCaches, 20, true);
            this.setStatusLabel("All caches have been cleaned.", OutputUtilities.Level.SUCCESS);
            Utilities.sleep(Duration.seconds(15), 1, run -> {
                NodeUtils.setNodeActiveMethod(this.clearCaches, this::onClearCacheClick);
                Animations.animateObjectAppear(this.clearCaches, 20);
            }, null);
        });
    }

    public void onDevLabelVision(MouseEvent event) {
        super.devLabel.setText("X: " + (int) event.getX() + ", Y: " + (int) event.getY());
    }

    public void onFullScreenButtonAction(ActionEvent event) {
        FullScreenMode.enableOrDisableFullScreen();
    }

    public void onRefreshAction() {
        if(STOPPED)
            return;
        preventSongRestart.value(true);
        String selected = this.listView.getSelectionModel().getSelectedItem();
        updatedPath = MainApp.MAIN_APP_PATH + "/" + SELECTED_PLAYLIST;
        Playlists.updatePlaylistContents(SELECTED_PLAYLIST, this.listView);
        if(this.listView.getItems().contains(selected))
            this.listView.getSelectionModel().select(selected);
        preventSongRestart.value(false);
    }

    public void onTitleBarPress(MouseEvent event) {
        if(STOPPED)
            return;
        super.titleBarXOffset = event.getSceneX();
        super.titleBarYOffset = event.getSceneY();
    }

    public void onTitleBarDrag(MouseEvent event) {
        if(STOPPED || isMaximized)
            return;
        MainApp main = MainApp.instance;
        double screenX = event.getScreenX();
        double screenY = event.getScreenY();
        double resultY = screenY - super.titleBarYOffset;
        if(resultY < 0 && Math.abs(resultY) < this.titleBar.getHeight() / 2 && !FullScreenMode.isFullScreen){
            isMaximized = true;
            FullScreenMode.setFullScreen(true);
            return;
        }
        if(FullScreenMode.isFullScreen){
            FullScreenMode.setFullScreen(false);
            this.titleBarXOffset = 0;
            ignoreXYOffsets = true;
        }
        MainApp.stage1.setX(ignoreXYOffsets ? screenX - MainApp.stage1.getWidth() / 2 : screenX - super.titleBarXOffset);
        MainApp.stage1.setY(screenY - super.titleBarYOffset);
    }

    public void onTitleBarMouseClick(MouseEvent event){
        if(event.getClickCount() == 2){
            FullScreenMode.setFullScreen(!FullScreenMode.isFullScreen);
        }
    }

    public void onTitleBarMouseRelease (MouseEvent event){
        isMaximized = false;
        ignoreXYOffsets = false;
    }

    public void onCloseButtonAction(ActionEvent event) {
        STOPPED = true;
        MainApp.quitApp(false, MainApp.instance.getApplicationState());
    }

    public void onMinimizeButtonAction(ActionEvent event) {
        if(STOPPED)
            return;
        boolean is = MainApp.stage1.isIconified();
        MainApp.stage1.setIconified(!is);
    }

    private static Timeline preparingTime = null;

    public void playMedia(File selectedDirectory, int index, String directory) {
        if(STOPPED)
            return;
        Utilities.stopPlayer(this);
        NodeUtils.setNodeViewType(NodeUtils.ViewType.NORMAL, this.mediaView);
        LambdaObject<Integer> finalIndex = LambdaObject.of(index);
        if (selectedDirectory == null) {
            File file = SongRegistry.getAndRegister(listView.getItems().get(0), directory + "/" + listView.getItems().get(0));
            playMedia(file, 0, directory);
            return;
        }
        String selectedDirAbsPath = selectedDirectory.getAbsolutePath();
        String videoURL = Paths.get(selectedDirAbsPath, "Video.mp4").toUri().toString();
        String audioURL = Paths.get(selectedDirAbsPath, "Audio."+Playlists.audioExt).toUri().toString();
        MediaCache.register(selectedDirAbsPath, videoURL, audioURL);

        boolean enable_full_sound = SOUND_TYPE.equals(SoundType.FULL.get());
        if (index >= listView.getItems().size()) {
            finalIndex.set(0);
            File file = SongRegistry.getAndRegister(
                    listView.getItems().get(finalIndex.get()),
                    directory + "/" + listView.getItems().get(finalIndex.get()));
            String songName = Utilities.correctSongName(file, true);
            setSongName(songName, ANIMATIONS);
            playMedia(file, finalIndex.get(), directory);
            return;
        }

        try {
            Animations.checkAndStopAnimations(preparingTime);
            preparingTime = Utilities.sleep(Duration.millis(100), 1, null, end -> {
                hidePlayerLabel.setVisible(videoPlayer != null && HIDE_PLAYER);
                timeSlider.setValue(0.0);
                progressBar.setProgress(0.0);
                currentlyPlayingSong = Utilities.correctSongName(selectedDirectory, true);
                isRunning = true;
                playButton.setText("");
                setSongName(currentlyPlayingSong, ANIMATIONS);
                if (ANIMATIONS) {
                    startProgressBarAnimation(progressBar);
                }
                noPlaylistsLink.setVisible(false);
                noPlaylistsLabel.setVisible(false);
                if(playerTask != null && playerTask.isRunning()){
                    playerTask.cancel();
                }
                playerTask = new Task<>() {
                    @Override
                    protected Void call() {
                        playVideo(selectedDirectory, finalIndex, directory, false);
                        Utilities.sleep(Duration.millis(40), 1, run -> {
                            Utilities.centralizeMediaPlayer(Player.this);
                            Utilities.setImageView2(playButton2, "Player/pause-player.png");
                        }, null);
                        return null;
                    }
                };
                audioTask = new Task<>() {
                    @Override
                    protected Void call() {
                        playAudio(selectedDirectory, enable_full_sound, finalIndex, directory, false);
                        videoPlayer.seek(audioMedia.getDuration());
                        return null;
                    }
                };
                Platform.runLater(() -> {
                    ExecutorService audioThread, videoThread;
                    if(THREADS.isEmpty()){
                        audioThread = Executors.newFixedThreadPool(3);
                        videoThread = Executors.newFixedThreadPool(3);
                        THREADS.add(audioThread);
                        THREADS.add(videoThread);
                        LOGGER.info("New audio and video threads were created.");
                    }else{
                        audioThread = THREADS.get(0);
                        videoThread = THREADS.get(1);
                    }
                    audioThread.submit(audioTask);
                    videoThread.submit(playerTask);
                });
            });
        } catch (MediaException exception) {
            ErrorHandler.launchWindow(exception);
        }
    }
    @SuppressWarnings("SpellCheckingInspection")
    private void playVideo(File selectedDirectory, LambdaObject<Integer> finalIndex, String directory, boolean crashed){
        String selectedDirAbsPath = selectedDirectory.getAbsolutePath();
        videoMedia = MediaCache.getVideoMedia(selectedDirAbsPath);
        //Caching-ul pe MediaPlayer nu este posibil deoarece la fiecare select / media end isi ia dispose si automat devine null.
        videoPlayer = new MediaPlayer(videoMedia);
        videoPlayer.setMute(true);
        mediaView.setMediaPlayer(videoPlayer);
        mediaView.setOpacity((HIDE_PLAYER) ? 0.0 : 1.0);
        videoPlayer.play();
        videoPlayer.setOnError(() -> {
            LOGGER.warning("Video has crashed. Retrying initialization...");
            this.playVideo(selectedDirectory, finalIndex, directory, true);
        });
        if(crashed)
            this.videoPlayer.seek(this.audioPlayer.getCurrentTime());
        videoPlayer.setOnEndOfMedia(PlayerUtils.getOnEndOfMediaPlayer(finalIndex, directory));
        Utilities.centralizeMediaPlayer(this);
    }
    private void playAudio(File selectedDirectory, boolean fullSound, LambdaObject<Integer> finalIndex, String directory, boolean crashed){
        if(failedAttempts.get() >= allowedFailedAttempts.get()){
            failedAttempts.set(0);
            THREADS.forEach(ExecutorService::shutdown);
            THREADS.clear();
            setStatusLabel("Audio has crashed too many times. Reinitialization aborted. You can still try again later.", OutputUtilities.Level.INCOMPLETE);
            LOGGER.severe("Audio has crashed too many times. Reinitialization is aborted to prevent CPU overload.");
            return;
        }
        if(!Equalizers.equalizers.isSettingsFileExists()){
            Equalizers.equalizers.setupSettingsFile(true, Equalizers.SETTING_VALUES);
        }
        String selectedDirAbsPath = selectedDirectory.getAbsolutePath();
        String audioURI = Paths.get(selectedDirAbsPath, "Audio."+Playlists.audioExt).toUri().toString();
        this.audioMedia = MediaCache.getAudioMedia(selectedDirAbsPath);
        audioPlayer = new MediaPlayer(audioMedia);
        audioPlayer.getAudioEqualizer().setEnabled(true);
        audioPlayer.getAudioEqualizer().getBands().clear();
        audioPlayer.getAudioEqualizer().getBands().addAll(Equalizers.getEqualizers());
        audioPlayer.setVolume((fullSound) ? (volumeSlider.getValue() / 100) : (volumeSlider.getValue() / 100) / soundAdjust);
        audioPlayer.setOnEndOfMedia(PlayerUtils.getOnEndOfMediaPlayer(finalIndex, directory));
        audioPlayer.setOnError(() -> {
            failedAttempts.set(failedAttempts.get() + 1);
            LOGGER.warning("Audio has crashed. Retrying initialization... (Attempt "+failedAttempts.get()+" out of "+allowedFailedAttempts.get()+")");
            this.playAudio(selectedDirectory, fullSound, finalIndex, directory, true);
        });
        audioPlayer.play();
        if(crashed)
            this.audioPlayer.seek(this.videoPlayer.getCurrentTime());
        audioPlayer.setOnEndOfMedia(PlayerUtils.getOnEndOfMediaPlayer(finalIndex, directory));
        Utilities.sleep(Duration.millis(100), 1, run -> videoPlayer.seek(audioPlayer.getCurrentTime()), null);
        Utilities.sleep(Duration.millis(500), 1, run2 ->
                startTimerListener((int) audioMedia.getDuration().toSeconds()), null
        );
    }

    /**
     * This method is responsible to loading the saved settings when the application is starting up.
     * The settings are stored in the main working directory inside the "settings.yml" file.
     */
    public void loadSettings() {
        if(STOPPED)
            return;
        LOGGER.info("Loading application settings...");
        initializeApplicationSettings();

        boolean auto_play = MEDIA_END_BEHAVIOUR.equals(Media_End_Behaviour_Type.AUTO_PLAY.get());
        boolean infinite_play = MEDIA_END_BEHAVIOUR.equals(Media_End_Behaviour_Type.INFINITE_PLAY.get());
        boolean auto_start = AUTO_START;
        boolean enable_full_volume = SOUND_TYPE.equals(SoundType.FULL.get());
        int currentVolume = (int) VOLUME;
        boolean animations = ANIMATIONS;
        boolean hidePlayer = HIDE_PLAYER;

        this.volumeSlider.setValue((float) currentVolume);
        this.volumeLabel.setText(currentVolume + "%");
//        this.playButton.setText((this.isRunning) ? "PAUSE" : "PLAY");
        this.volumeLabel.setText((currentVolume == 100) ? "Max Volume" : (currentVolume == 0) ? "Sound Muted" : currentVolume + "%");
        this.memoryCleanup.setVisible(VERBOSE_STATUS);
        this.mediaView.setOpacity((hidePlayer) ? 0.0 : 1.0);

        if (START_IN_FULLSCREEN) {
            Utilities.sleep(Duration.millis(700), 1, run -> FullScreenMode.setFullScreen(true), null);
        }
    }

    /**
     * This method initializes the "Options..." items functionality.
     */
    private void initializeOptionsMenuButtons() {
        if(STOPPED)
            return;
        LOGGER.info("Loading the Options dropdown menu...");
        this.quitAppItem.setOnAction(event -> {
            Utilities.stopPlayer(this);
            DataStructures.cleanupTimelines(DataStructures.TEXT_ANIMATIONS, true);
            MainApp.quitApp(false, MainApp.instance.getApplicationState());
        });
        this.openDirectory.setOnAction(event -> Utilities.openApplicationDirectory());
        this.uninstall.setOnAction(event -> Uninstaller.launchWindow());
        this.createPlayList.setOnAction(event -> LinkDownloader.launchWindow(false));
        this.managePlaylists.setOnAction(event -> ManagePlaylists.launchWindow());
        this.keybinds.setOnAction(event -> Keybinds.launchWindow(true));
        this.settingsPage.setOnAction(event -> SettingsPage.launchWindow(false));
        this.manageBackups.setOnAction(event -> BackupWindow.launchWindow());
    }

    public void sendNewSettingsPing() {
        if(STOPPED)
            return;
        this.mediaView.setVisible(true);
        if (!ANIMATIONS) {
            MainApp.STAGES.forEach(stage -> stage.setOpacity(1.0));
            isProgressBarAnimationOn = false;
            DataStructures.cleanupTimelines(DataStructures.TEXT_ANIMATIONS, true);
            if (this.animationPending != null && this.animationPending.getStatus() == Animation.Status.RUNNING) {
                this.animationPending.stop();
            }
            Animations.undoAnimateOverAllScreen(this, true);
            this.mediaView.setOpacity((HIDE_PLAYER) ? 0.0 : 1.0);
        } else {
            MainApp.STAGES.forEach(stage -> stage.setOpacity(0.9));
            this.startProgressBarAnimation(this.progressBar);
            if(HIDE_PLAYER)
                Animations.undoAnimateObjectAppear(this.mediaView, 20, false);
            else
                Animations.animateObjectAppear(this.mediaView, 20);
        }
        if (videoPlayer != null) {
            if (SOUND_TYPE.equals(SoundType.FULL.get())) {
                audioPlayer.setVolume(this.volumeSlider.getValue() / 100);
            } else {
                audioPlayer.setVolume((this.volumeSlider.getValue() / 100) / super.soundAdjust);
            }
        }
        this.songName1.resetPosition();
        this.memoryCleanup.setVisible(VERBOSE_STATUS);
        this.hidePlayerLabel.setVisible(videoPlayer != null && HIDE_PLAYER);
        this.titleLabel.setText(MainApp.title +(HIDE_TITLE ? "" : " -- Playing " + this.currentlyPlayingSong+" from "+SELECTED_PLAYLIST));
        this.songName1.setText(HIDE_TITLE ? MEDIA_TITLE_LABEL_HIDDEN : currentlyPlayingSong);
    }

    private static final List<ListCell<String>> LIST_CELLS = new ArrayList<>();

    /**
     * This method initializes the list view object that has all the song names which can be played when being selected.
     *
     * @param selectedPlaylist Reference playlist
     */
    @SuppressWarnings("All")
    public void initializeListView(String selectedPlaylist) {
        if(STOPPED)
            return;
        LOGGER.info("Loading the songs ListView...");
        Utilities.stopPlayer(this);
        String playlistsPath = Playlists.PATH;
        File mainDirectory = new File(playlistsPath);
        if (!mainDirectory.exists()) {
            System.exit(0);
        }
        this.choosePlaylist.setText("Switch playlist [ ALT + C ]");
        String songsPath = playlistsPath + "/" + selectedPlaylist;
        File currentPlaylist = new File(songsPath);
        if (currentPlaylist == null) {
            LOGGER.warning("The playlist folder '" + selectedPlaylist + "' does not exist. Aborted.");
            return;
        }
        File[] currentPlaylistSongs = Playlists.getPlaylist(selectedPlaylist);
        if (currentPlaylistSongs == null || currentPlaylistSongs.length == 0) {
            for (int i = 0; i < Playlists.getPlaylists().size(); i++) {
                selectedPlaylist = Playlists.getPlaylists().get(i);
                SELECTED_PLAYLIST = selectedPlaylist;
                songsPath = playlistsPath + "/" + selectedPlaylist;
                File playlistDirectory = new File(songsPath);
                if (!playlistDirectory.exists() || !Playlists.hasSongs(selectedPlaylist)) {
                    continue;
                }
                settings.saveSetting("selected-playlist", selectedPlaylist);
                currentPlaylistSongs = playlistDirectory.listFiles();
                this.choosePlaylist.setText("Choose a playlist [ ALT + C ]");
                LOGGER.warning("Auto-Changing to fallback playlist '" + selectedPlaylist + "'... '" + selectedPlaylist +
                        "' was modified, moved out of the Playlists directory or deleted...");
                this.currentPlaylistLabel.setText("Playlist: " + selectedPlaylist);
                break;
            }
        } else {
            this.currentPlaylistLabel.setText("Playlist: " + selectedPlaylist);
        }
        if (currentPlaylistSongs == null || Playlists.getPlaylistCount() == 0) {
            settings.saveSetting("selected-playlist", "[None]");
            LOGGER.warning("No playlists available to choose from...");
            this.searchSong.setVisible(false);
            this.noPlaylistsLabel.setVisible(true);
            this.noPlaylistsLink.setVisible(true);
        }else{
            this.searchSong.setVisible(true);
            super.noPlaylistsLink.setVisible(false);
            this.noPlaylistsLabel.setVisible(false);
            this.listView.getItems().clear();
            List<String> elements = Arrays.stream(currentPlaylistSongs).filter(
                    file -> {
                        if(file.isDirectory()){
                            File[] files = file.listFiles();
                            String firstExt = Playlists.audioExt, secondExt = Playlists.videoExt; //1 - Audio, 2 - Video
                            return files.length == 2 &&
                                    (files[0].getName().equals("Video."+firstExt) && files[1].getName().equals("Audio."+secondExt)) ||
                                    (files[0].getName().equals("Audio."+firstExt) && files[1].getName().equals("Video."+secondExt));
                        }
                        return false;
                    }
            ).map(file -> Utilities.correctSongName(file, false)).toList();
            this.listView.getItems().addAll(elements);
            PlaylistCache.register(selectedPlaylist, new ArrayList<>(this.listView.getItems()));
            if (AUTO_START) {
                if(this.listView.getItems().isEmpty()){
                    LOGGER.warning("Playlist exists but there are no songs to start playing automatically...");
                    return;
                }
                String firstTitleName = this.listView.getItems().get(0);
                File firstTitle = SongRegistry.getAndRegister(firstTitleName, songsPath + "/" + firstTitleName);
                this.currentlyPlayingSong = Utilities.correctSongName(firstTitle, true);
                this.setSongName(this.currentlyPlayingSong, ANIMATIONS);
                this.playMedia(
                        this.getReformattedFile(firstTitle),
                        0,
                        songsPath
                );
                this.preventSongRestart.value(false);
                this.listView.getSelectionModel().select(0);
            }
            if (isInitialized) {
                return;
            }
            listView.setCellFactory(listview -> new ListCell<>(){
                @Override
                protected void updateItem(String string, boolean empty) {
                    super.updateItem(string, empty);
                    if(string == null || empty){
                        this.setText(null);
                    }else{
                        this.setText(string);
                        this.setOnMouseEntered(event -> {
                            if(this.getIndex() >= this.getListView().getItems().size()){
                                NodeUtils.setStyle(this, "-fx-background-color: transparent;");
                                return;
                            }
                            NodeUtils.setStyle(this, (this.isSelected()) ?
                                    "-fx-background-color: #5c5c5c;" :
                                    "-fx-background-color: gray; -fx-text-fill: #00f7ff;");
                        });
                        this.setOnMouseExited(event -> NodeUtils.setStyle(this,
                                (this.isSelected() ? "-fx-background-color: #5c5c5c;" : "-fx-background-color: #151515;")
                                        +" -fx-text-fill: lime;"));
                    }
                }
                @Override
                public void updateSelected(boolean selected) {
                    super.updateSelected(selected);
                    if(selected){
                        for(ListCell<String> listCell : LIST_CELLS){
                            NodeUtils.setStyle(listCell, "-fx-background-color: transparent;");
                        }
                        LIST_CELLS.add(this);
                        NodeUtils.setStyle(this, "-fx-background-color: #5c5c5c;");
                    }
                }
            });
            this.listView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue == null) {
                    return;
                }
                if(preventSongRestart.getValue() && preventSongRestart.getKey()){
                    preventSongRestart.value(false);
                    return;
                }
                if(!preventSongRestart.getKey())
                    preventSongRestart.key(true);
                this.durationLabel.setText("Preparing...");
                this.timeSlider.setValue(0.0);
                progressBar.setProgress(0.0);
                updatedPath = playlistsPath + "/" + SELECTED_PLAYLIST;
                File selectedSong = SongRegistry.getAndRegister(newValue, updatedPath.concat("/").concat(newValue));
                if (!selectedSong.exists()) {
                    ProblemFixer.launchWindow(
                            "The Player could not render this song because its path or name were modified or does not correspond.",
                            "Clear the application caches to allow them to refresh.",
                            event -> {
                                Caches.clearCaches();
                                Playlists.updatePlaylistContents(SELECTED_PLAYLIST, this.listView);
                            }
                    );
                    return;
                }
                this.setSongName(Utilities.correctSongName(selectedSong, true), ANIMATIONS);
                this.playMedia(
                        selectedSong,
                        this.listView.getSelectionModel().getSelectedIndex(),
                        updatedPath
                );
                List<String> cachedListView = PlaylistCache.getPlaylist(SELECTED_PLAYLIST);
                if(cachedListView != null && this.listView.getItems().size() != cachedListView.size()){
                    Utilities.sleep(Duration.millis(45), 1, run -> {
                        this.searchSong.setText("");
                        this.updateListViewBasedOnSongSearchText("");
                    }, null);
                }
            });
            isInitialized = true;
            LOGGER.info("Successfully initialized the ListView listener.");
        }
    }

    /**
     * Starts the time stamp bar animation when a song is selected.
     *
     * @param progressBar Time stamp bar to be animated.
     */
    public void startProgressBarAnimation(ProgressBar progressBar) {
        if(isProgressBarAnimationOn)
            return;
        isProgressBarAnimationOn = true;
        LambdaObject<Double> value = LambdaObject.of(this.progressBar.getOpacity());
        MainApp.executorService.submit(() -> {
            if(STOPPED)
                return;
            if (this.progressBarAnimation != null && this.progressBarAnimation.getStatus() == Animation.Status.RUNNING) {
                this.progressBarAnimation.stop();
            }
            LambdaObject<Boolean> increment = LambdaObject.of(false);
            this.progressBarAnimation = Utilities.sleep(Duration.millis(125), -1, run -> {
                if(STOPPED)
                    return;
                if (!ANIMATIONS) {
                    this.progressBarAnimation.stop();
                    this.progressBar.setOpacity(1.0);
                    return;
                }
                if (value.get() <= 0.6) {
                    increment.set(true);
                } else if (value.get() >= 1.0) {
                    increment.set(false);
                }
                value.set(increment.get() ? value.get() + 0.025f : value.get() - 0.025f);
                progressBar.setOpacity(value.get());
                if (increment.get()) value.set(value.get() + 0.025f);
                else value.set(value.get() - 0.025f);
            }, null);
        });
    }

    /**
     * Sets the song name label to be shown when a song is selected, and possible animate it if set so.
     *
     * @param songName The song name.
     */
    public void setSongName(String songName) {
        if(STOPPED)
            return;
        if (!isInitialized) {
            this.songName1.setText("Please, select a song to play");
            this.songName1.setOnMouseEntered(event -> this.songName1.setLabelStyling("-fx-text-fill: gray", "-fx-cursor: hand"));
            this.songName1.setOnMouseExited(event -> this.songName1.setLabelStyling(""));
            Utilities.sleep(Duration.seconds(1), 1, run -> this.setSongName(songName, ANIMATIONS), null);
        } else {
            this.songName1.setText(PlayerSettings.HIDE_TITLE ? MEDIA_TITLE_LABEL_HIDDEN : songName);
            this.songName1.setOnMouseEntered(event -> this.songName1.setLabelStyling("-fx-text-fill: gray", "-fx-cursor: hand"));
            this.songName1.setOnMouseExited(event -> this.songName1.setLabelStyling(""));
            this.songName1.setOnMouseClicked(this::onSongNameClick);
            boolean lengthFit = songName.length() <= 50;
            this.songName1.setToolTip(new Tooltip("Copy "+(lengthFit ? "'"+songName+"'" : "this song's name")+" in your clipboard."+
                    "\nClick while holding 'SHIFT' key to search on youtube."));
            this.songName1.getToolTip().setShowDelay(Duration.millis(350));
        }
    }

    /**
     * This method calls {@link Player#setSongName}, but adds a boolean value that specified whether to automatic
     * start the song title animation or not.
     *
     * @param songName        The song name.
     * @param allowAnimations True if you want to auto start the animation, False otherwise.
     */
    public void setSongName(String songName, boolean allowAnimations) {
        if(STOPPED)
            return;
        this.setSongName(songName == null ? "No song was chosen." : songName);
        if (isInitialized)
            this.titleLabel.setText(MainApp.title +(HIDE_TITLE ? "" : " -- Playing " + this.currentlyPlayingSong+" from "+SELECTED_PLAYLIST));
        if (allowAnimations) {
            Utilities.sleep(Duration.millis(this.waitUntilSongNameChange), 1, run -> DataStructures.cleanupTimelines(DataStructures.TEXT_ANIMATIONS, true), null);
        } else {
            Utilities.sleep(Duration.millis(this.waitUntilSongNameChange), 1, run -> {
                DataStructures.cleanupTimelines(DataStructures.TEXT_ANIMATIONS, true);
                this.songName1.resetPosition();
            }, null);
        }
    }

    /**
     * This method is replacing the songs from the previous playlist to the ones from the new selected playlist.
     *
     * @param newSongs The new songs that the new playlist contains.
     */
    public void replaceSongs(List<String> newSongs) {
        if(STOPPED)
            return;
        this.listView.getItems().clear();
        this.listView.getItems().setAll(newSongs);
        this.listView.getSelectionModel().select(0);
    }

    /**
     * This method is responsible in returning the original song file by its corrected name or
     * another referencing string that could be used in order to find the proper file.
     *
     * @param file The file that was not found because of the corrected name.
     * @return Original file found by the corrected name.
     */
    public File getReformattedFile(File file) {
        String[] pathParts = file.getPath().split(Pattern.quote(File.separator));
        String mainPath = "";
        for (int i = 0; i < pathParts.length - 1; i++) {
            mainPath = mainPath.concat(pathParts[i]).concat("/");
        }
        if (file.exists())
            return file;
        String currentName = Utilities.correctSongName(file, false);
        File directory = new File(mainPath);
        File[] files = directory.listFiles();
        assert files != null;
        for (File eachFile : files) {
            if (eachFile.getName().contains(currentName)) {
                return eachFile;
            }
        }
        return file;
    }

    /**
     * This method is responsible with the current time stamp of the song played and its interactions with the time stamp bar.
     * If there is no song played, it will stay as following "00:00/00:00".
     *
     * @param mediaDuration The media required to calculate the total time and its current time stamp.
     */
    public void startTimerListener(final int mediaDuration) {
        boolean moreThanAHour = mediaDuration > 3600;
        this.timeSlider.setValue(0.0);
        this.progressBar.setProgress(0.0);
        TimeConverter timeConverter = new TimeConverter();
        LambdaObject<Integer> updatedDuration = LambdaObject.of(mediaDuration);
        audioPlayer.currentTimeProperty().addListener((obs, oldValue, newValue) -> {
            if(STOPPED)
                return;
            timeConverter.setTimer((int) newValue.toSeconds());
            timeSlider.setValue(Utilities.getPercentage(newValue.toSeconds(), mediaDuration));
            progressBar.setProgress(Utilities.getProgressBarPercentage(newValue.toSeconds(), mediaDuration));
            if (!moreThanAHour) {
                durationLabel.setText(
                        timeConverter.toString(TimeConverter.LevelType.MINUTES) + " | " +
                                TimeConverter.toString(
                                        ANIMATIONS ? updatedDuration.get() - (int) newValue.toSeconds() : updatedDuration.get(),
                                        TimeConverter.LevelType.MINUTES, ":")
                );
            } else {
                durationLabel.setText(
                        timeConverter.toString(TimeConverter.LevelType.HOURS) + " | " +
                                TimeConverter.toString(
                                        ANIMATIONS ? updatedDuration.get() - (int) newValue.toSeconds() : updatedDuration.get(),
                                        TimeConverter.LevelType.HOURS, ":")
                );
            }
        });
    }

    /**
     * This method is called when the "Choose a playlist" button is clicked.
     *
     * @param event The parameter that it's required to process the button click.
     */
    @SuppressWarnings("unused")
    public void onPlaylistSelectAction(ActionEvent event) {
        PlaylistChooser.launchWindow(false);
    }

    /**
     * This method is responsible to handle the Next song button.
     * It will try to move from the current song to the next one. If the current song is the last song, it will move to the top song of the list.
     */
    public void onNextSongPress(ActionEvent event) {
        if (this.listView.getItems().isEmpty()) {
            return;
        }
        int currentSongSelected = this.listView.getSelectionModel().getSelectedIndex();
        if (currentSongSelected >= this.listView.getItems().size() - 1) {
            this.listView.getSelectionModel().select(0);
            return;
        }
        this.listView.getSelectionModel().select(currentSongSelected + 1);
    }

    /**
     * This method is responsible to handle the Previous song button.
     * It will try to move from the current song to the previous one. If the current song is the top song, it will move to the bottom song of the list.
     */
    public void onPreviousSongPress(ActionEvent event) {
        if (this.listView.getItems().isEmpty()) {
            return;
        }
        int currentSelected = this.listView.getSelectionModel().getSelectedIndex();
        if (currentSelected <= 0) {
            this.listView.getSelectionModel().select(this.listView.getItems().size() - 1);
            return;
        }
        this.listView.getSelectionModel().select(currentSelected - 1);
    }


    /**
     * This method closes all windows assigned to this application
     *
     * @param event The parameter that it's required to process the button click.
     */
    public void onCloseAllStagesAction(ActionEvent event) {
        MainApp.closeAllStages();
    }

    /**
     * This method is called when the time stamp bar is being pressed.
     *
     * @param event The parameter that it's required to process the slider click.
     */
    public void sliderPressed(MouseEvent event) {
        if (videoPlayer == null) {
            return;
        }
        double percentage = Utilities.getPercentage(event.getX(), timeSlider.getWidth());
        if(audioPlayer != null)
            audioPlayer.seek(Duration.millis(Utilities.getPortionOfTotal(percentage, audioMedia.getDuration().toMillis())));
        if(videoPlayer != null)
            videoPlayer.seek(Duration.millis(Utilities.getPortionOfTotal(percentage, videoMedia.getDuration().toMillis())));
        this.progressBar.setProgress(Utilities.getProgressBarPercentage(event.getX(), timeSlider.getWidth()));
    }

    /**
     * This method is called when the volume bar is dragged.
     *
     * @param event The parameter that it's required to process the bar click.
     */
    public void volumeSliderDragged(MouseEvent event) {
        this.updateVolumeSlider();
    }

    public void updateVolumeSlider() {
        if(STOPPED)
            return;
        settings.saveSetting("app-volume", (int) this.volumeSlider.getValue());
        double newValue = SOUND_TYPE.equals(SoundType.FULL.get()) ?
                (this.volumeSlider.getValue() / 100) : ((this.volumeSlider.getValue() / 100) / super.soundAdjust);
        if ((int) this.volumeSlider.getValue() == 100)
            this.volumeLabel.setText("Max Volume");
        else if ((int) this.volumeSlider.getValue() == 0)
            this.volumeLabel.setText("Sound Muted");
        else
            this.volumeLabel.setText((int) this.volumeSlider.getValue() + "%");
        if (videoPlayer == null) {
            return;
        }
        boolean b = (SOUND_TYPE.equals(SoundType.ADJUSTED.get()) && (int) volumeSlider.getValue() == 1);
        audioPlayer.setVolume(b ? 0.012 : newValue);
    }

    /**
     * This method is called when the volume bar is pressed.
     *
     * @param event The parameter that it's required to process the bar click.
     */
    public void volumeSliderPressed(MouseEvent event) {
        this.volumeSliderDragged(event);
    }

    /**
     * Processes the "PLAY" button click.
     * If a song is currently running, it will change its text to "PAUSE".
     * If any error occures, then its text will adjust acordingly, together with the song title
     *
     * @param event The event required to process this action.
     */
    @SuppressWarnings("all")
    public void playButtonAction(ActionEvent event) {
        if(STOPPED)
            return;
        if (videoPlayer == null) {
            this.setSongName("Please, select a song to play.");
            Utilities.setImageView2(this.playButton2, null);
            this.playButton.setText("Unable to process the media.");
            return;
        }
//        this.playButton.setText((!this.isRunning) ? "PAUSE" : "PLAY");
        if (this.isRunning) {
            this.titleLabelString = MainApp.title + " -- Paused";
            this.titleLabel.setText(PlayerFullScreen.isFullScreen ? "" : this.titleLabelString);
            Utilities.setImageView2(this.playButton2, "Player/resume-player.png");
            audioPlayer.pause();
            audioPlayer.pause();
            videoPlayer.pause();
            audioPlayer.seek(Duration.millis(audioPlayer.getCurrentTime().toMillis() - 0.01));
            this.isRunning = false;
            this.setStatusLabel("Player has been paused.", OutputUtilities.Level.SUCCESS);
            return;
        }
        this.titleLabelString = MainApp.title +(HIDE_TITLE ? "" : " -- Playing " + this.currentlyPlayingSong+" from "+SELECTED_PLAYLIST);
        this.titleLabel.setText(PlayerFullScreen.isFullScreen ? "" : this.titleLabelString);
        audioPlayer.play();
        videoPlayer.seek(audioPlayer.getCurrentTime());
        videoPlayer.play();
        Utilities.setImageView2(this.playButton2, "Player/pause-player.png");
        this.setStatusLabel("Player has been resumed.", OutputUtilities.Level.SUCCESS);
        this.isRunning = true;
    }

    /**
     * This will open up the "Create playlist" window, if the user has no active playlists created.
     *
     * @param event The event necessary to process the hyperlink click.
     */
    public void onNoPlaylistLinkClick(ActionEvent event) {
        LinkDownloader.launchWindow(false);
    }

    Timeline evaporateStatusLabel;

    /**
     * This method sets the status label under the "Play" button of the main stage.
     * After a short while, it will disappear in a fading effect
     *
     * @param string The string to be set in the label
     * @param level  The level of the label.
     */
    public void setStatusLabel(String string, OutputUtilities.Level level) {
        if(STOPPED)
            return;
        this.statusLabel.setOpacity(1.0);
        if (this.evaporateStatusLabel != null && this.evaporateStatusLabel.getStatus() == Animation.Status.RUNNING)
            this.evaporateStatusLabel.stop();
        new OutputUtilities().setOutputLabel(this.statusLabel, string, level);
        this.evaporateStatusLabel = Utilities.sleep(
                Duration.millis(2500), 1,
                run -> Utilities.sleep(
                        Duration.millis(20), (int) (1.0 / 0.025),
                        run2 -> {
                            if (this.statusLabel.getOpacity() <= 0.0)
                                return;
                            this.statusLabel.setOpacity(this.statusLabel.getOpacity() - 0.025);
                        }, null
                ), null
        );
    }

    public void updateListViewBasedOnSongSearchText(String text) {
        if(STOPPED)
            return;
        List<String> cachedListView = PlaylistCache.getPlaylist(SELECTED_PLAYLIST);
        this.listView.getItems().clear();
        if (text == null || text.isEmpty()) {
            this.listView.getItems().addAll(cachedListView);
            return;
        }
        List<String> possibleSongs = cachedListView.stream()
                .filter(string -> string.toLowerCase().contains(text.toLowerCase())).toList();
        this.listView.getItems().addAll(possibleSongs);
    }
}