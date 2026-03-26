package me.eduard.musicplayer.Components.Player;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.eduard.musicplayer.Library.SimplePair;
import me.eduard.musicplayer.Library.WrappedValue;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.*;
import me.eduard.musicplayer.Utils.Logging.LoggerUtils;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;

import java.util.logging.Logger;

@SuppressWarnings("CodeBlock2Expr")
public class PlayerFullScreen {

    public final SimplePair<Double, Double> mediaViewBounds = SimplePair.of(0d, 0d);
    public final SimplePair<Double, Double> thumbnailBounds = SimplePair.of(0d, 0d);

    private final Label mediaTitle = new Label();
    private final Label timer = new Label();
    private final Label volumeLabel = new Label("Volume");
    private final Label volumePercentage = new Label("X%");
    private final Rectangle bottomSide = new Rectangle();
    private final Rectangle topSide = new Rectangle();
    private final Slider timeSlider = new Slider();
    private final Slider volumeSlider = new Slider();
    private final Button quitFullScreen = new Button("Quit FullScreen");
    private final Button nextSong = new Button();
    private final Button previousSong = new Button();
    private final Button pauseOrPlay = new Button();
    private final ImageView previousSongImage = new ImageView();
    private final ImageView pauseOrPlayImage = new ImageView();
    private final ImageView nextSongImage = new ImageView();

    private static final Logger LOGGER = LoggerUtils.createOrGet("PlayerFullScreen");

    private ChangeListener<? super Number> onMediaTitleWidthChange = null;
    private ChangeListener<? super Number> onTimerWidthChange = null;
    private ChangeListener<? super Number> onMediaFitHeightChange = null;
    private ChangeListener<? super Number> onMediaFitWidthChange = null;
    private ChangeListener<? super Number> onNextSongWidthChange = null;
    private ChangeListener<? super Number> onPreviousSongWidthChange = null;
    private ChangeListener<? super Number> onPauseOrPlaySongWidthChange = null;
    private ChangeListener<? super Number> onSliderValueMax = null;

    private final SimplePair<EventType<KeyEvent>, EventHandler<KeyEvent>> sceneKeyPress = SimplePair.of(null, null);
    private final SimplePair<EventType<KeyEvent>, EventHandler<KeyEvent>> sceneKeyRelease = SimplePair.of(null, null);
    private final SimplePair<EventType<MouseEvent>, EventHandler<MouseEvent>> globalSceneMouseMove = SimplePair.of(null, null);

    private final WrappedValue<Boolean> isRunning = WrappedValue.of(true);

    private EventHandler<ActionEvent> onQuitFullScreen = null;
    private EventHandler<ActionEvent> onPauseOrPlay = null;
    private EventHandler<MouseEvent> onTimeSliderPress = null;
    private EventHandler<MouseEvent> onPreviousSongMouseEnter = null;
    private EventHandler<MouseEvent> onPauseOrPlaySongMouseEnter = null;
    private EventHandler<MouseEvent> onNextSongMouseEnter = null;
    private EventHandler<MouseEvent> onPreviousSongMouseExit = null;
    private EventHandler<MouseEvent> onPauseOrPlaySongMouseExit = null;
    private EventHandler<MouseEvent> onNextSongMouseExit = null;
    private EventHandler<MouseEvent> onVolumeSliderMouseDrag = null;

    private final Duration DURATION_TO_WAIT = Duration.millis(1500);
    private final Duration FADE_TRANS_DURATION = Duration.millis(150);

    private final FadeTransition mediaTitleTrans = new FadeTransition();
    private final FadeTransition timerTrans = new FadeTransition();
    private final FadeTransition bottomSideTrans = new FadeTransition();
    private final FadeTransition topSideTrans = new FadeTransition();
    private final FadeTransition timerSliderTrans = new FadeTransition();
    private final FadeTransition quitFullScreenTrans = new FadeTransition();
    private final FadeTransition nextSongTrans = new FadeTransition();
    private final FadeTransition previousSongTrans = new FadeTransition();
    private final FadeTransition pauseOrPlayTrans = new FadeTransition();
    private final FadeTransition nextSongImageTrans = new FadeTransition();
    private final FadeTransition previousSongImageTrans = new FadeTransition();
    private final FadeTransition pauseOrPlayImageTrans = new FadeTransition();
    private final FadeTransition volumeLabelTrans = new FadeTransition();
    private final FadeTransition volumeSliderTrans = new FadeTransition();
    private final FadeTransition volumePercentageTrans = new FadeTransition();

    private final PauseTransition pauseTransition = new PauseTransition(this.DURATION_TO_WAIT);

    private FadeTransition[] fadeTransitions = null;


    private enum StageType {
        THIS, MAIN
    }

    private StageType stageType = StageType.MAIN;
    public boolean isFullScreen = false;
    private Stage stage;
    private final Player player;

    public PlayerFullScreen() {
        this.player = Player.instance;
        this.init();
    }

    private void init() {
        this.stage = StageBuilder.newBuilder()
                .title("FullScreen Player")
                .resizable(false)
                .icon("icons/icon.png")
                .color("black")
                .fullScreenHint("")
                .removeUpperBar()
                .buildAndGet();
        assert this.stage.getScene() != null;

        LinearGradient bottomSidePaint = new LinearGradient(
                0, 1,
                0, 0,
                true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.BLACK), new Stop(1, Color.TRANSPARENT)
        );

        LinearGradient topSidePaint = new LinearGradient(
                0, 1,
                0, 0,
                true, CycleMethod.NO_CYCLE,
                new Stop(1, Color.BLACK), new Stop(0, Color.TRANSPARENT)
        );

        this.topSide.setFill(topSidePaint);
        this.bottomSide.setFill(bottomSidePaint);

        this.mediaTitle.setFont(Font.font(25));
        this.mediaTitle.setStyle("-fx-text-fill: white;");

        this.timer.setFont(Font.font(20));
        this.timer.setStyle("-fx-text-fill: white;");

        this.volumeLabel.setFont(Font.font(16));
        this.volumeLabel.setStyle("-fx-text-fill: white;");

        this.volumePercentage.setFont(Font.font(16));
        this.volumePercentage.setStyle("-fx-text-fill: white;");

        GlobalAppStyle.applyToButtons(this.quitFullScreen);
        this.applyCustomButtonsStyle(
                this.previousSong,
                this.pauseOrPlay,
                this.nextSong
        );

        Utilities.setImageViewWithFullPath(this.nextSongImage, "images/Player/next-song.png");
        Utilities.setImageViewWithFullPath(this.previousSongImage, "images/Player/previous-song.png");

        this.mediaTitleTrans.setNode(this.mediaTitle);
        this.timerTrans.setNode(this.timer);
        this.topSideTrans.setNode(this.topSide);
        this.bottomSideTrans.setNode(this.bottomSide);
        this.timerSliderTrans.setNode(this.timeSlider);
        this.quitFullScreenTrans.setNode(this.quitFullScreen);
        this.nextSongTrans.setNode(this.nextSong);
        this.previousSongTrans.setNode(this.previousSong);
        this.pauseOrPlayTrans.setNode(this.pauseOrPlay);
        this.nextSongImageTrans.setNode(this.nextSongImage);
        this.previousSongImageTrans.setNode(this.previousSongImage);
        this.pauseOrPlayImageTrans.setNode(this.pauseOrPlayImage);
        this.volumeLabelTrans.setNode(this.volumeLabel);
        this.volumeSliderTrans.setNode(this.volumeSlider);
        this.volumePercentageTrans.setNode(this.volumePercentage);

        this.updateTransitionArray();

        this.pauseTransition.setOnFinished(e -> {
            this.isRunning.set(false);
            this.runTransitionsNormally();
        });

        ((Pane) this.stage.getScene().getRoot()).getChildren().addAll(
                this.bottomSide,
                this.topSide,
                this.mediaTitle,
                this.timer,
                this.timeSlider,
                this.quitFullScreen,
                this.previousSongImage,
                this.pauseOrPlayImage,
                this.nextSongImage,
                this.nextSong,
                this.pauseOrPlay,
                this.previousSong,
                this.volumeLabel,
                this.volumePercentage,
                this.volumeSlider
        );
        this.volumeSlider.getStylesheets().add(Utilities.getResource("StyleSheets/PlayerFullScreen/style.css").toExternalForm());
        this.timeSlider.getStylesheets().add(Utilities.getResource("StyleSheets/PlayerFullScreen/style.css").toExternalForm());
        stage.setOnCloseRequest(event -> {
            this.disableFullScreen();
        });

        this.mediaViewBounds.setBoth(player.mediaView.getFitWidth(), player.mediaView.getFitHeight());
        this.thumbnailBounds.setBoth(player.mediaView.getFitWidth(), player.mediaView.getFitHeight());
    }

    private void updateTransitionArray() {
        this.fadeTransitions = new FadeTransition[] {
                this.mediaTitleTrans,
                this.timerTrans,
                this.topSideTrans,
                this.bottomSideTrans,
                this.timerSliderTrans,
                this.quitFullScreenTrans,
                this.nextSongTrans,
                this.previousSongTrans,
                this.pauseOrPlayTrans,
                this.nextSongImageTrans,
                this.previousSongImageTrans,
                this.pauseOrPlayImageTrans,
                this.volumeSliderTrans,
                this.volumePercentageTrans,
                this.volumeLabelTrans
        };
    }

    private void configureDefaultsForTransitions(){
        for(FadeTransition f : this.fadeTransitions){
            f.setFromValue(1);
            f.setToValue(0);
            f.setDuration(this.FADE_TRANS_DURATION);
        }
    }

    private void resetNodesOpacity() {
        NodeUtils.setNodesOpacity(1.0, this.mediaTitle, this.timer, this.bottomSide, this.topSide);
    }

    private void runTransitions() {
        for(FadeTransition f : this.fadeTransitions){
            f.play();
        }
    }

    private void runTransitionsNormally() {
        this.configureDefaultsForTransitions();
        this.runTransitions();
        this.stage.getScene().setCursor(Cursor.NONE);
    }

    private void runTransitionBackwards() {
        if(this.isRunning.get()){
            return;
        }
        this.stopAllTransitions();
        this.isRunning.set(true);
        for(FadeTransition f : this.fadeTransitions){
            f.setFromValue(this.mediaTitle.getOpacity());
            f.setToValue(1);
        }
        this.runTransitions();
        this.stage.getScene().setCursor(Cursor.DEFAULT);
    }

    private void stopAllTransitions() {
        for(FadeTransition f : this.fadeTransitions){
            f.stop();
        }
    }

    private void arrangeElements() {
        this.previousSong.setPadding(Insets.EMPTY);
        this.pauseOrPlay.setPadding(Insets.EMPTY);
        this.nextSong.setPadding(Insets.EMPTY);
        this.quitFullScreen.setPadding(Insets.EMPTY);
        this.timer.setLayoutY(this.stage.getScene().getHeight() - 2 - this.timer.getHeight());
        this.timer.setLayoutX(this.stage.getScene().getWidth() - 5 - this.timer.getWidth());
        this.timeSlider.setLayoutX(2);
        this.timeSlider.setLayoutY(this.timer.getLayoutY() + (this.timer.getHeight() / 3));
        this.timeSlider.setPrefWidth(this.previousSong.getLayoutX() - 30);
        this.volumeLabel.setLayoutX(10);
        this.volumeLabel.setLayoutY(this.timeSlider.getLayoutY() - 40);
        this.volumeSlider.setLayoutX(this.volumeLabel.getLayoutX() + volumeLabel.getWidth() + 12);
        this.volumeSlider.setPrefWidth(500);
        this.volumePercentage.setLayoutY(this.volumeLabel.getLayoutY());
        this.volumePercentage.setLayoutX(this.volumeSlider.getLayoutX() + this.volumeSlider.getPrefWidth() + 12);
        this.volumeSlider.setLayoutY(this.volumeLabel.getLayoutY() + (this.volumeLabel.getHeight() / 3));
        this.mediaTitle.setLayoutY(2);
        this.mediaTitle.setLayoutX((this.stage.getScene().getWidth() / 2) - (this.mediaTitle.getWidth() / 2));
        this.quitFullScreen.setPrefWidth(150);
        this.quitFullScreen.setPrefHeight(25);
        this.quitFullScreen.setLayoutY(5);
        this.quitFullScreen.setLayoutX(this.stage.getScene().getWidth() - this.quitFullScreen.getPrefWidth() - 5);
        this.nextSong.setPrefWidth(100);
        this.nextSong.setPrefHeight(28);
        this.nextSong.setLayoutY(this.timer.getLayoutY());
        this.nextSong.setLayoutX(this.timer.getLayoutX() - 10 - this.nextSong.getPrefWidth());
        this.pauseOrPlay.setPrefWidth(100);
        this.pauseOrPlay.setPrefHeight(28);
        this.pauseOrPlay.setLayoutY(this.timer.getLayoutY());
        this.pauseOrPlay.setLayoutX(this.nextSong.getLayoutX() - 5 - this.pauseOrPlay.getPrefWidth());
        this.previousSong.setPrefWidth(100);
        this.previousSong.setPrefHeight(28);
        this.previousSong.setLayoutX(200);
        this.previousSong.setLayoutY(this.timer.getLayoutY());
        this.previousSong.setLayoutX(this.pauseOrPlay.getLayoutX() - 5 - this.previousSong.getPrefWidth());
        this.timeSlider.setPrefWidth(this.previousSong.getLayoutX() - 10);
        this.topSide.setLayoutX(0);
        this.topSide.setLayoutY(0);
        this.topSide.setHeight(100);
        this.bottomSide.setLayoutX(0);
        this.bottomSide.setHeight(200);
        this.bottomSide.setLayoutY(this.stage.getScene().getHeight() - this.bottomSide.getHeight());
    }

    private void applyCustomButtonsStyle(Button... buttons){
        for(Button b : buttons){
            b.setStyle("-fx-background-color: transparent;");
        }
    }

    public void enableFullScreen() {
        Platform.runLater(() -> {
            this.mediaViewBounds.setBoth(player.mediaView.getFitWidth(), player.mediaView.getFitHeight());
            this.thumbnailBounds.setBoth(player.mediaView.getFitWidth(), player.mediaView.getFitHeight());
            Utilities.setImageViewWithFullPath(
                    this.pauseOrPlayImage,
                    player.audioPlayer != null && player.audioPlayer.getStatus() == MediaPlayer.Status.PLAYING ?
                            "images/Player/pause-player.png" : "images/Player/resume-player.png"
            );
            this.volumeSlider.setValue(player.volumeSlider.getValue());
            this.volumePercentage.setText(player.volumeLabel.getText());
            KeyCombinationUtils.clear();
            this.setupListeners();
            this.bindAllListeners();
            this.switchMediaView(StageType.THIS);
            this.player.thumbnail.setViewOrder(1);
            this.resetNodesOpacity();
            this.stage.setFullScreen(true);
            Platform.runLater(this::arrangeElements);
            if(!this.player.mediaView.fitWidthProperty().isBound()){
                this.player.mediaView.fitWidthProperty().bind(stage.getScene().widthProperty());
            }
            if(!this.player.mediaView.fitHeightProperty().isBound()){
                this.player.mediaView.fitHeightProperty().bind(stage.getScene().heightProperty());
            }
            if(!this.player.thumbnail.fitWidthProperty().isBound()){
                this.player.thumbnail.fitWidthProperty().bind(stage.getScene().widthProperty());
            }
            if(!this.player.thumbnail.fitHeightProperty().isBound()){
                this.player.thumbnail.fitHeightProperty().bind(stage.getScene().heightProperty());
            }
            if(this.stage.getOwner() == null) {
                this.stage.initOwner(MainApp.stage1);
            }
            this.stage.show();
            this.isFullScreen = true;
        });
    }

    private void setupListeners() {
        this.onMediaTitleWidthChange = (obs, oldVal, newVal) -> {
            this.mediaTitle.setLayoutX((this.stage.getScene().getWidth() / 2) - (this.mediaTitle.getWidth() / 2));
        };
        this.onTimerWidthChange = (obs, oldVal, newVal) -> {
            this.timer.setLayoutX(this.stage.getScene().getWidth() - 10 - this.timer.getWidth());
            this.nextSong.setLayoutX(this.timer.getLayoutX() - 10 - this.nextSong.getPrefWidth());
            this.pauseOrPlay.setLayoutX(this.nextSong.getLayoutX() - 5 - this.pauseOrPlay.getPrefWidth());
            this.previousSong.setLayoutX(this.pauseOrPlay.getLayoutX() - 5 - this.previousSong.getPrefWidth());
        };
        this.onMediaFitWidthChange = (obs, oldVal, newVal) -> {
            this.player.mediaView.setLayoutX((this.stage.getScene().getWidth() / 2) - (this.player.mediaView.getFitWidth() / 2));
        };
        this.onMediaFitHeightChange = (obs, oldVal, newVal) -> {
            this.player.mediaView.setLayoutY((this.stage.getScene().getHeight() / 2) - (this.player.mediaView.getFitHeight() / 2));
        };
        this.onPreviousSongWidthChange = (obs, oldVal, newVal) -> {
            final double subtract = 4;
            Utilities.adjustImageViewSize(
                    this.previousSongImage,
                    this.previousSong.getWidth() - subtract,
                    this.previousSong.getHeight() - subtract,
                    true
            );
            Utilities.centralizeImageViewOnButton(this.previousSongImage, this.previousSong);
        };
        this.onVolumeSliderMouseDrag = event -> {
            player.settings.saveSetting("app-volume", (int) this.volumeSlider.getValue());
            double newValue = Player.SOUND_TYPE.equals(PlayerSettings.SoundType.FULL.get()) ?
                    (this.volumeSlider.getValue() / 100) : ((this.volumeSlider.getValue() / 100) / player.soundAdjust);
            if ((int) this.volumeSlider.getValue() == 100)
                this.volumePercentage.setText("Max (100%)");
            else if ((int) this.volumeSlider.getValue() == 0)
                this.volumePercentage.setText("Muted (0%)");
            else
                this.volumePercentage.setText((int) this.volumeSlider.getValue() + "%");
            if (player.audioPlayer == null) {
                return;
            }
            boolean b = (Player.SOUND_TYPE.equals(PlayerSettings.SoundType.ADJUSTED.get()) && (int) volumeSlider.getValue() == 1);
            player.volumeSlider.setValue(this.volumeSlider.getValue());
            player.volumeLabel.setText(this.volumePercentage.getText());
            player.audioPlayer.setVolume(b ? 0.012 : newValue);
        };
        this.onPauseOrPlaySongWidthChange = (obs, oldVal, newVal) -> {
            final double subtract = 4;
            Utilities.adjustImageViewSize(
                    this.pauseOrPlayImage,
                    this.pauseOrPlay.getWidth() - subtract,
                    this.pauseOrPlay.getHeight() - subtract,
                    true
            );
            Utilities.centralizeImageViewOnButton(this.pauseOrPlayImage, this.pauseOrPlay);
        };
        this.onNextSongWidthChange = (obs, oldVal, newVal) -> {
            final double subtract = 4;
            Utilities.adjustImageViewSize(
                    this.nextSongImage,
                    this.nextSong.getWidth() - subtract,
                    this.nextSong.getHeight() - subtract,
                    true
            );
            Utilities.centralizeImageViewOnButton(this.nextSongImage, this.nextSong);
        };
        this.onSliderValueMax = (obs, oldVal, newVal) -> {
            if(newVal.doubleValue() == 100.0) {
                this.runTransitionBackwards();
                this.pauseTransition.stop();
                this.pauseTransition.play();
            }
        };
        this.onPreviousSongMouseEnter = event -> {
            String css = """
                    -fx-padding: 0;
                    -fx-border-color: #919191;
                    -fx-background-color: transparent;
                    -fx-border-width: 2px;
                    -fx-border-radius: 5px;
                    -fx-background-radius: 5px;
                    """;
            this.previousSong.setStyle(css);
            Utilities.setImageViewWithFullPath(this.previousSongImage, "images/Player/previous-song-hover.png");
        };
        this.onPauseOrPlaySongMouseEnter = event -> {
            String css = """
                    -fx-padding: 0;
                    -fx-border-color: #919191;
                    -fx-background-color: transparent;
                    -fx-border-width: 2px;
                    -fx-border-radius: 5px;
                    -fx-background-radius: 5px;
                    """;
            this.pauseOrPlay.setStyle(css);
            Utilities.setImageViewWithFullPath(
                    this.pauseOrPlayImage,
                    player.audioPlayer != null && player.audioPlayer.getStatus() == MediaPlayer.Status.PLAYING ?
                            "images/Player/pause-player-hover.png" : "images/Player/resume-player-hover.png"
            );
        };
        this.onNextSongMouseEnter = event -> {
            String css = """
                    -fx-padding: 0;
                    -fx-border-color: #919191;
                    -fx-background-color: transparent;
                    -fx-border-width: 2px;
                    -fx-border-radius: 5px;
                    -fx-background-radius: 5px;
                    """;
            this.nextSong.setStyle(css);
            Utilities.setImageViewWithFullPath(this.nextSongImage, "images/Player/next-song-hover.png");
        };
        this.onPreviousSongMouseExit = event -> {
            this.previousSong.setStyle("-fx-background-color: transparent;");
            Utilities.setImageViewWithFullPath(this.previousSongImage, "images/Player/previous-song.png");
        };
        this.onPauseOrPlaySongMouseExit = event -> {
            this.pauseOrPlay.setStyle("-fx-background-color: transparent;");
            Utilities.setImageViewWithFullPath(
                    this.pauseOrPlayImage,
                    player.audioPlayer != null && player.audioPlayer.getStatus() == MediaPlayer.Status.PLAYING ?
                            "images/Player/pause-player.png" : "images/Player/resume-player.png"
            );
        };
        this.onNextSongMouseExit = event -> {
            this.nextSong.setStyle("-fx-background-color: transparent;");
            Utilities.setImageViewWithFullPath(this.nextSongImage, "images/Player/next-song.png");
        };

        this.onQuitFullScreen = event -> this.disableFullScreen();
        this.onPauseOrPlay = event -> {
            if(player.audioPlayer.getStatus() == MediaPlayer.Status.PAUSED){

                Utilities.setImageViewWithFullPath(this.pauseOrPlayImage, "images/Player/pause-player.png");
                if(player.videoPlayer != null){
                    player.audioPlayer.seek(Duration.millis(player.audioPlayer.getCurrentTime().toMillis() - 0.01));
                    player.audioPlayer.play();
                    player.videoPlayer.seek(player.audioPlayer.getCurrentTime());
                    player.videoPlayer.play();
                }
            }else{
                if(player.audioPlayer != null){
                    player.audioPlayer.pause();
                }
                if(player.videoPlayer != null){
                    assert player.audioPlayer != null;
                    player.videoPlayer.seek(player.audioPlayer.getCurrentTime());
                    player.videoPlayer.pause();
                }
                Utilities.setImageViewWithFullPath(this.pauseOrPlayImage, "images/Player/resume-player.png");
            }
        };
        this.onTimeSliderPress = event -> {
            if(player.audioPlayer == null) {
                return;
            }
            double percentage = Utilities.getPercentage(event.getX(), this.timeSlider.getWidth());
            if(player.videoPlayer != null){
                player.videoPlayer.seek(Duration.millis(Utilities.getPortionOfTotal(percentage, player.videoMedia.getDuration().toMillis())));
            }
            if(player.audioPlayer != null) {
                player.audioPlayer.seek(Duration.millis(Utilities.getPortionOfTotal(percentage, player.audioMedia.getDuration().toMillis())));
            }
        };
        EventHandler<MouseEvent> onMouseMove = event -> {
            this.runTransitionBackwards();
            this.pauseTransition.stop();
            this.pauseTransition.play();
        };
        this.globalSceneMouseMove.setBoth(MouseEvent.MOUSE_MOVED, onMouseMove);
        this.sceneKeyPress.setBoth(
                KeyEvent.KEY_PRESSED, event -> {
                    if(event.getCode() == KeyCode.ESCAPE){
                        this.disableFullScreen();
                    }else{
                        if(player.audioPlayer != null){
                            KeyCombinationUtils.registerKey(event.getCode());
                            //Key Combinations start
                            if(KeyCombinationUtils.isKey(KeyCode.SHIFT, 0) && KeyCombinationUtils.isKey(KeyCode.RIGHT, 1)){
                                player.nextSong.fire();
                            }else if(KeyCombinationUtils.isKey(KeyCode.SHIFT, 0) && KeyCombinationUtils.isKey(KeyCode.LEFT, 1)){
                                player.previousSong.fire();
                            }else if(KeyCombinationUtils.isKey(KeyCode.CONTROL, 0) && KeyCombinationUtils.isKey(KeyCode.LEFT, 1)){
                                player.audioPlayer.seek(Duration.seconds(0.0));
                                player.videoPlayer.seek(Duration.seconds(0.0));
                            }
                            //Key Combinations end
                            else if(event.getCode() == KeyCode.RIGHT){
                                player.audioPlayer.seek(Duration.seconds(player.audioPlayer.getCurrentTime().toSeconds() + 5.0));
                                player.videoPlayer.seek(Duration.seconds(player.audioPlayer.getCurrentTime().toSeconds() + 5.0));
                            }else if(event.getCode() == KeyCode.LEFT){
                                player.audioPlayer.seek(Duration.seconds(player.audioPlayer.getCurrentTime().toSeconds() - 5.0));
                                player.videoPlayer.seek(Duration.seconds(player.audioPlayer.getCurrentTime().toSeconds() - 5.0));
                            }else if(event.getCode() == KeyCode.SPACE){
                                player.playButton.fire();
                                Utilities.setImageViewWithFullPath(
                                        this.pauseOrPlayImage, player.audioPlayer.getStatus() == MediaPlayer.Status.PAUSED ?
                                                "images/Player/pause-player.png" : "images/Player/resume-player.png"
                                );
                            }else if(event.getCode() == KeyCode.DOWN){
                                player.volumeSlider.setValue(player.volumeSlider.getValue() - 5);
                                player.updateVolumeSlider();
                            }else if(event.getCode() == KeyCode.UP){
                                player.volumeSlider.setValue(player.volumeSlider.getValue() + 5);
                                player.updateVolumeSlider();
                            }
                            event.consume();
                        }
                    }
                }
        );
        this.sceneKeyRelease.setBoth(KeyEvent.KEY_RELEASED, event -> KeyCombinationUtils.removeKey(event.getCode()));
    }

    private void bindAllListeners() {
        this.mediaTitle.widthProperty().addListener(this.onMediaTitleWidthChange);
        this.timer.widthProperty().addListener(this.onTimerWidthChange);
        this.player.mediaView.fitWidthProperty().addListener(this.onMediaFitWidthChange);
        this.player.mediaView.fitHeightProperty().addListener(this.onMediaFitHeightChange);

        this.stage.getScene().addEventFilter(this.sceneKeyPress.getKey(), this.sceneKeyPress.getValue());
        this.stage.getScene().addEventFilter(this.sceneKeyRelease.getKey(), this.sceneKeyRelease.getValue());

        this.previousSong.widthProperty().addListener(this.onPreviousSongWidthChange);
        this.previousSong.heightProperty().addListener(this.onPreviousSongWidthChange);
        this.previousSong.setOnMouseMoved(this.onPreviousSongMouseEnter);
        this.previousSong.setOnMouseExited(this.onPreviousSongMouseExit);
        this.pauseOrPlay.widthProperty().addListener(this.onPauseOrPlaySongWidthChange);
        this.pauseOrPlay.heightProperty().addListener(this.onPauseOrPlaySongWidthChange);
        this.pauseOrPlay.setOnMouseMoved(this.onPauseOrPlaySongMouseEnter);
        this.pauseOrPlay.setOnMouseExited(this.onPauseOrPlaySongMouseExit);
        this.nextSong.widthProperty().addListener(this.onNextSongWidthChange);
        this.nextSong.heightProperty().addListener(this.onNextSongWidthChange);
        this.nextSong.setOnMouseMoved(this.onNextSongMouseEnter);
        this.nextSong.setOnMouseExited(this.onNextSongMouseExit);
        this.volumeSlider.setOnMouseDragged(this.onVolumeSliderMouseDrag);
        this.volumeSlider.setOnMousePressed(this.onVolumeSliderMouseDrag);

        this.stage.getScene().addEventFilter(this.globalSceneMouseMove.getKey(), this.globalSceneMouseMove.getValue());
        this.stage.getScene().addEventFilter(MouseEvent.MOUSE_DRAGGED, this.globalSceneMouseMove.getValue());
        this.stage.getScene().addEventFilter(MouseEvent.MOUSE_CLICKED, this.globalSceneMouseMove.getValue());

        this.timeSlider.setOnMouseDragged(this.onTimeSliderPress);
        this.timeSlider.setOnMousePressed(this.onTimeSliderPress);
        this.timeSlider.valueProperty().addListener(this.onSliderValueMax);
        this.quitFullScreen.setOnAction(this.onQuitFullScreen);
        this.nextSong.setOnAction(player.nextSong.getOnAction());
        this.pauseOrPlay.setOnAction(this.onPauseOrPlay);
        this.previousSong.setOnAction(player.previousSong.getOnAction());

        this.timeSlider.valueProperty().bind(player.timeSlider.valueProperty());
        this.timer.textProperty().bind(this.player.durationLabel.textProperty());
        this.mediaTitle.textProperty().bind(this.player.songName1.textProperty());
        this.bottomSide.widthProperty().bind(this.stage.getScene().widthProperty());
        this.topSide.widthProperty().bind(this.stage.getScene().widthProperty());
        LOGGER.info("Bound all listeners.");
    }

    private void unbindAllListeners() {
        this.mediaTitle.widthProperty().removeListener(this.onMediaTitleWidthChange);
        this.timer.widthProperty().removeListener(this.onTimerWidthChange);
        this.player.mediaView.fitWidthProperty().removeListener(this.onMediaFitWidthChange);
        this.player.mediaView.fitHeightProperty().removeListener(this.onMediaFitHeightChange);
        this.previousSong.widthProperty().removeListener(this.onPreviousSongWidthChange);
        this.previousSong.heightProperty().removeListener(this.onPreviousSongWidthChange);
        this.previousSong.setOnMouseMoved(null);
        this.previousSong.setOnMouseExited(null);
        this.pauseOrPlay.widthProperty().removeListener(this.onPauseOrPlaySongWidthChange);
        this.pauseOrPlay.heightProperty().removeListener(this.onPauseOrPlaySongWidthChange);
        this.pauseOrPlay.setOnMouseMoved(null);
        this.pauseOrPlay.setOnMouseExited(null);
        this.nextSong.widthProperty().removeListener(this.onNextSongWidthChange);
        this.nextSong.heightProperty().removeListener(this.onNextSongWidthChange);
        this.timeSlider.valueProperty().removeListener(this.onSliderValueMax);
        this.nextSong.setOnMouseMoved(null);
        this.nextSong.setOnMouseExited(null);
        this.timeSlider.setOnMouseMoved(null);
        this.volumeSlider.setOnMouseDragged(null);
        this.volumeSlider.setOnMousePressed(null);

        this.timeSlider.setOnMouseMoved(null);
        this.timeSlider.setOnMouseDragged(null);
        this.quitFullScreen.setOnAction(null);
        this.nextSong.setOnAction(null);
        this.pauseOrPlay.setOnAction(null);
        this.previousSong.setOnAction(null);

        this.stage.getScene().removeEventFilter(this.globalSceneMouseMove.getKey(), this.globalSceneMouseMove.getValue());
        this.stage.getScene().removeEventFilter(MouseEvent.MOUSE_DRAGGED, this.globalSceneMouseMove.getValue());
        this.stage.getScene().removeEventFilter(MouseEvent.MOUSE_CLICKED, this.globalSceneMouseMove.getValue());
        this.stage.getScene().removeEventFilter(this.sceneKeyPress.getKey(), this.sceneKeyPress.getValue());
        this.stage.getScene().removeEventFilter(this.sceneKeyRelease.getKey(), this.sceneKeyRelease.getValue());

        this.timeSlider.valueProperty().unbind();
        this.timer.textProperty().unbind();
        this.mediaTitle.textProperty().unbind();
        this.bottomSide.widthProperty().unbind();
        this.topSide.widthProperty().unbind();
        LOGGER.info("Unbound all listeners.");
    }

    public void disableFullScreen() {
        Platform.runLater(() -> {
            this.player.mediaView.setViewOrder(1);
            this.player.mediaView.fitWidthProperty().unbind();
            this.player.mediaView.fitHeightProperty().unbind();
            this.player.thumbnail.fitWidthProperty().unbind();
            this.player.thumbnail.fitHeightProperty().unbind();
            this.unbindAllListeners();
            this.switchMediaView(StageType.MAIN);
            this.isFullScreen = false;
            KeyCombinationUtils.clear();
            System.gc();
            this.stage.close();
        });
    }

    public void persistIfNeeded() {
        this.centralizeMediaView(this.stageType);
    }

    private void switchMediaView(StageType type){
        this.stageType = type;
        Platform.runLater(() -> {
            switch (type) {
                case MAIN -> {
                    ((Pane) MainApp.stage1.getScene().getRoot()).getChildren().addAll(this.player.mediaView, this.player.thumbnail);
                    this.centralizeMediaView(type);
                }
                case THIS -> {
                    ((Pane) this.stage.getScene().getRoot()).getChildren().addAll(this.player.mediaView, this.player.thumbnail);
                    this.centralizeMediaView(type);
                }
            }
        });
    }
    private void centralizeMediaView(StageType type){
        Platform.runLater(() -> {
            switch (type){
                case MAIN -> {
                    PlayerUtils.restoreMediaViewToOriginalSize();
                    Utilities.sleep(Duration.millis(40), 1, run -> {
                        Utilities.centralizeThumbnail(this.player);
                        Utilities.centralizeMediaPlayer(this.player);
                        this.player.mediaView.setCursor(Cursor.HAND);
                        this.player.thumbnail.setCursor(Cursor.HAND);
                    }, null);
                }
                case THIS -> {
                    if(player.videoPlayer.getStatus() != MediaPlayer.Status.DISPOSED && !Player.HIDE_PLAYER) {
                        final double videoWidth = this.player.mediaView.getBoundsInLocal().getWidth();
                        final double videoHeight = this.player.mediaView.getBoundsInLocal().getHeight();

                        final double resultX = (this.stage.getScene().getWidth() / 2) - (videoWidth / 2);
                        final double resultY = (this.stage.getScene().getHeight() / 2) - (videoHeight / 2);

                        this.player.mediaView.setLayoutX(resultX);
                        this.player.mediaView.setLayoutY(resultY);
                        LOGGER.info("Width = "+this.stage.getScene().getWidth()+", Height = "+this.stage.getScene().getHeight()+" (Video)");
                        this.player.mediaView.setCursor(null);
                    }else{
                        final double thumbnailWidth = this.player.thumbnail.getBoundsInLocal().getWidth();
                        final double thumbnailHeight = this.player.thumbnail.getBoundsInLocal().getHeight();

                        final double resultX = (this.stage.getScene().getWidth() / 2) - (thumbnailWidth / 2);
                        final double resultY = (this.stage.getScene().getHeight() / 2) - (thumbnailHeight / 2);
                        LOGGER.info("Width = "+this.stage.getScene().getWidth()+", Height = "+this.stage.getScene().getHeight()+" (Thumbnail)");

                        this.player.thumbnail.setLayoutX(resultX);
                        this.player.thumbnail.setLayoutY(resultY);
                        this.player.thumbnail.setCursor(null);
                    }
                }
            }
        });
    }

}
