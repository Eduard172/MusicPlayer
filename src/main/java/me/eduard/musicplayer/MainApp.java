package me.eduard.musicplayer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.eduard.musicplayer.Components.*;
import me.eduard.musicplayer.Components.Notifications.SlidingNotification;
import me.eduard.musicplayer.Components.Player.PlayerFullScreen;
import me.eduard.musicplayer.Library.*;
import me.eduard.musicplayer.Library.Animations.Animations;
import me.eduard.musicplayer.Library.Animations.LabelAnimations;
import me.eduard.musicplayer.Library.Cache.Caches;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.Library.CustomComponents.BetterLabel;
import me.eduard.musicplayer.Utils.*;
import me.eduard.musicplayer.Utils.FXMLUtils;
import me.eduard.musicplayer.Utils.Logging.LoggerFormatter;
import me.eduard.musicplayer.Utils.Logging.LoggerHandler;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@SuppressWarnings({"unused", "ConstantValue"})
public class MainApp extends Application{

    private static final Logger LOGGER = Logger.getLogger("Main");

    static{
        LoggerHandler consoleHandler = new LoggerHandler();
        consoleHandler.setFormatter(new LoggerFormatter());
        LOGGER.addHandler(consoleHandler);
        LOGGER.setUseParentHandlers(false);
    }

    private final Settings settings = Settings.of("settings.yml");

    public static final ExecutorService executorService = Executors.newCachedThreadPool();

    private boolean firstTimeStart = false;

    private static volatile boolean firstGCCall = true;

    public static double VISUAL_SCREEN_WIDTH;
    public static double VISUAL_SCREEN_HEIGHT;
    public static double SCREEN_WIDTH;
    public static double SCREEN_HEIGHT;

    public static final double WIDTH = 1242;
    public static final double HEIGHT = 758;

    private volatile AppState applicationState = AppState.NORMAL;

    public void setApplicationState(AppState state){
        this.applicationState = state;
    }

    public AppState getApplicationState(){
        return this.applicationState;
    }

    /**
     * This variable will determinate the default living duration for the sliding notification.
     * <p>
     *     The default duration is 5.0 seconds.
     * </p>
     */
    public static final double DEFAULT_SN_DURATION = 5.0d;

    public static boolean isDevBuild = false;
    public static final double VERSION = 1.182;
    public static final String title = "Media Player, v"+VERSION+" "+ (isDevBuild ? "(Unstable/ Development Build)" : "");

    //START

    @Override
    public void start(Stage stage) throws Exception{
        LOGGER.info("Application developed by Edward76.");
        instance = this;
        this.setupComponents();
    }

    //END

    public static MainApp instance;
    public static final String USER = System.getProperty("user.name");
    public static final String MAIN_APP_PATH = "C:\\Users\\"+USER+"\\Desktop\\MusicPlayer";
    public static final String APP_EXTERNAL_HELPERS = MAIN_APP_PATH+"\\Helpers";
    public static final String DESKTOP_PATH = "C:\\Users\\"+USER+"\\Desktop";
    public static final List<Stage> STAGES = new ArrayList<>();
    public static Stage stage1;
    public static Scene scene;
    public static Pane parent;

    public static void main(String[] args){
        launch(args);
    }


    public static void openStage(Stage stage, boolean animatedLaunch){
        openStage(stage, animatedLaunch, false);
    }
    public static void openStage(Stage stage, boolean animatedLaunch, boolean addToStorage){
        if(STAGES.contains(stage)){
            return;
        }
        if(addToStorage)
            STAGES.add(stage);
        updateClosedWindows(STAGES.size());
        stage.show();
        if(STAGES.size() == 1){
            Animations.allowedToUndoAnimation = false;
        }
        if(animatedLaunch)
            Animations.animateStageLaunch(stage);
    }
    public static boolean isAnyStageShowing(){
        for(Stage stage : STAGES){
            if(!stage.isIconified()){
                return true;
            }
        }
        return false;
    }
    public static void closeStage(Stage stage, boolean animatedClose){
        STAGES.remove(stage);
        updateClosedWindows(STAGES.size());
        KeyCombinationUtils.clear();
        if(animatedClose)
            Animations.animateStageClose(stage);
        else stage.close();
    }
    public static void closeAllStages(){
        if(STAGES.isEmpty()){
            return;
        }
        int currentSize = STAGES.size();
        for(Stage stage : STAGES){
            Animations.animateStageClose(stage);
            updateClosedWindows(currentSize);
            currentSize--;
        }
        STAGES.clear();
        updateClosedWindows(currentSize);
    }

    private static void updateClosedWindows(int size){
        Player player = Player.instance;
        if(player == null)
            return;
        player.closeAllActiveWindows.setText("Close all windows ("+size+")");
    }

    @SuppressWarnings("all")
    public void setupComponents(){
        try {
            String path = MainApp.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            LOGGER.info("The jar is running on '"+path+"'");
        }catch (Exception exception){
            LOGGER.warning("An error has occurred when trying to get the JAR location.");
        }
        LOGGER.info("Preparing application core-components...");
        File mainDirectory = new File(MAIN_APP_PATH);
        if(!mainDirectory.exists() && !mainDirectory.getPath().contains(".")){
            this.firstTimeStart = true;
            new Consent().launchConsentScreen(mainDirectory);
        }else{
            settings.setupSettingsFile(false, Settings.DEFAULT_SETTINGS);
            this.startApp();
        }
    }

    public static void quitApp(boolean force, AppState applicationState){
        if(force || applicationState == AppState.NORMAL){
            LOGGER.info("Closing application...\n");
            Utilities.stopPlayer(Player.instance);
            Caches.clearCaches();
            executorService.shutdown();
            Player.instance.THREADS.forEach(ExecutorService::shutdownNow);
            LOGGER.info("Background threads were stopped.");
            closeAllStages();
            LOGGER.info("All other application-related windows were closed.");
            Animations.animateStageClose(stage1);
            LOGGER.info("Application main window was closed.\n\nGood bye! Hope I see you back soon. :)");
        }else{
            LOGGER.info("Background tasks in progress. Quit Cancelled.");
            ScreenLauncher.launchWarningMessageBox(
                    "Downloads in progress",
                    "Wait a second.. There are downloads in progress.",
                    """
                            Application cannot close properly because there are downloads in background.
                            Forcing the application to quit while doing important background tasks could \
                            cause future errors/ file corruptions that will prevent the App from working properly.
                            
                            If you'd still like to force-quit the app, click the 'OK' button below.""",
                    event -> {
                        LOGGER.info("Forcing Application quit... Killing "+DataStructures.PROCESSES.size()+" process(es).");
                        DataStructures.PROCESSES.forEach(Process::destroyForcibly);
                        quitApp(true, MainApp.instance.getApplicationState());
                    }
            );
        }
    }
    public void startApp() {
        try {
            parent = FXMLUtils.getFXMLLoader("Player").load();
            Player player = Player.instance;
            scene = new Scene(player.corePane);
            stage1 = StageBuilder.newBuilder()
                    .styleSheet("Player")
                    .withScene(scene)
                    .removeUpperBar()
                    .icon("icons/icon.png")
                    .title("Media Player")
                    .resizable(false)
                    .buildAndGet();
            player.titleLabel.setText(title);
            player.PlayerMainLabel.setText(isDevBuild ? "Dev. Build" : "MP v"+VERSION); //Player
            player.devLabel.setVisible(isDevBuild);
            player.devLabel2.setVisible(isDevBuild);
            player.currentPlaylistLabel = BetterLabel.of(stage1)
                    .setText("Playlist: [Not found]")
                    .setLayout_X(879)
                    .setLayout_Y(37)
                    .setFont(20)
                    .setWidth(350)
                    .setBackgroundStyling("-fx-background-color: #151515")
                    .setMarginsStyling("-fx-fill: #151515", "-fx-effect: dropshadow(gaussian, #151515, 12, 0.6, 0, 0)")
                    .setSpeed(0.4)
                    .setViewMode(BetterLabel.ViewMode.BEHIND)
                    .setRelativeLabelDistance(50);
            player.songName1 = BetterLabel.of(stage1)
                    .setText("No song was chosen.")
                    .setLayout_X(210)
                    .setLayout_Y(583)
                    .setWidth(462)
                    .setHeight(30)
                    .setFont(18)
                    .setBackgroundStyling("-fx-background-color: #151515")
                    .setMarginsStyling("-fx-effect: dropshadow(gaussian, #151515, 12, 0.6, 0, 0)", "-fx-fill: #151515")
                    .setRelativeLabelDistance(80)
                    .setViewMode(BetterLabel.ViewMode.BEHIND);
            player.songName1.linkToStage();
            player.currentPlaylistLabel.linkToStage();
            this.initializeSceneKeyBinds(scene);
            if (this.firstTimeStart) {
                SlidingNotification.launch(
                        "Hello, "+USER+"!",
                        """
                                Welcome to the Music Player. Feel free to search for your favourite songs and \
                                create your very first playlist. I hope you'll enjoy using this application.
                                :)
                                
                                - Eduard (Application Developer)
                                """,
                        true, 5
                );
                LOGGER.info("Seems it's the first time this application starts. Welcome!");
            }
            stage1.show();
            stage1.setOnCloseRequest(event -> quitApp(false, MainApp.instance.getApplicationState()));
        } catch (IOException error) {
            ErrorHandler.launchWindow(error);
        }
        startAutomaticGarbageCollectorCall(5 * 60 + 1); // 5 minute si 1 secunda.
    }
    private void initializeSceneKeyBinds(Scene scene){
        Player player = Player.instance;
        if(player.devLabel.isVisible()){
            scene.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
                scene.setOnMouseMoved(player::onDevLabelVision);
                HoveringBooleans.isMouseOverMainStage = true;
                Animations.undoAnimateOverAllScreen(player, false);
            });
        }else{
            scene.addEventFilter(MouseEvent.MOUSE_MOVED, event -> HoveringBooleans.isMouseOverMainStage = true);
        }
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(player.searchSong.isFocused()){
                return;
            }
            player.durationLabel.requestFocus();
            KeyCombinationUtils.registerKey(event.getCode());
            if(event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN){
                event.consume();
            }
            if(KeyCombinationUtils.isKey(KeyCode.ALT, 0) && KeyCombinationUtils.isKey(KeyCode.S, 1)){
                SettingsPage.launchWindow(false);
                LOGGER.info("\"Settings Page\" was invoked.");
            }else if(KeyCombinationUtils.isKey(KeyCode.ALT, 0) && KeyCombinationUtils.isKey(KeyCode.N, 1)){
                LinkDownloader.launchWindow(false);
                LOGGER.info("\"Link Downloader\" was invoked.");
            }else if(KeyCombinationUtils.isKey(KeyCode.ALT, 0) && KeyCombinationUtils.isKey(KeyCode.M, 1)){
                ManagePlaylists.launchWindow();
                LOGGER.info("\"Playlist Manager\" was invoked.");
            }else if(KeyCombinationUtils.isKey(KeyCode.ALT, 0) && KeyCombinationUtils.isKey(KeyCode.E, 1)){
                KeyCombinationUtils.clear();
                Utilities.openApplicationDirectory();
                LOGGER.info("\"Application Directory\" was invoked.");
            }else if(KeyCombinationUtils.isKey(KeyCode.ALT, 0) && KeyCombinationUtils.isKey(KeyCode.K, 1)){
                Keybinds.launchWindow(true);
                LOGGER.info("\"KeyBinds Screen\" was invoked.");
            }else if(KeyCombinationUtils.isKey(KeyCode.ALT, 0) && KeyCombinationUtils.isKey(KeyCode.C, 1)) {
                PlaylistChooser.launchWindow(false);
                LOGGER.info("\"Playlist Selector\" was invoked.");
            }else if(KeyCombinationUtils.isKey(KeyCode.ALT, 0) && KeyCombinationUtils.isKey(KeyCode.B, 1)){
                BackupWindow.launchWindow();
                LOGGER.info("\"Backup Manager\" was invoked.");
            }else if(KeyCombinationUtils.isKey(KeyCode.SHIFT, 0) && KeyCombinationUtils.isKey(KeyCode.F4, 1)){
                closeAllStages();
                LOGGER.info("All related-stages were closed.");
            }else if(KeyCombinationUtils.isKey(KeyCode.SHIFT, 0) && KeyCombinationUtils.isKey(KeyCode.F11, 1)){
                if(PlayerFullScreen.isFullScreen){
                    PlayerFullScreen.makeNormal(player);
                    LOGGER.info("Media Player was returned to normal fullscreen state.");
                }else{
                    PlayerFullScreen.makeFullScreen(player);
                    LOGGER.info("Media Player was made fullscreen.");
                }
            }else if(KeyCombinationUtils.isKey(KeyCode.ALT, 0) && KeyCombinationUtils.isKey(KeyCode.F4, 1)){
                LOGGER.info("Application exit was manually invoked by key combination.");
                stage1.close();
                quitApp(false, MainApp.instance.getApplicationState());
            }else if(event.getCode() == KeyCode.F1){
                KeyCombinationUtils.clear();
                LabelAnimations.instance(player.statusLabel)
                        .getByIdentifier("KEY_COMBINATION_RESET")
                        .text("Key combination storage has been reset.").color(Color.LIME).waitingTime(1000).incrementedNumericalValue(true)
                        .startAnimation();
            }else if(event.getCode() == KeyCode.F11){
                player.fullscreenButton.fire();
                player.setStatusLabel("Fullscreen was toggled "+ (FullScreenMode.isFullScreen ? "on" : "off"), OutputUtilities.Level.SUCCESS);
            }else if(event.getCode() == KeyCode.ESCAPE){
                LabelAnimations.instance(player.statusLabel)
                        .text("You're about to minimize this window...")
                        .waitingTime(300)
                        .color(Color.LIME)
                        .startAnimation();
                Utilities.sleep(Duration.millis(100), 1, run -> stage1.setIconified(true), null);
            }
            //[ !! ] Player-Based KeyBinds
            if(player.videoPlayer != null){
                //Key Combinations start
                if(KeyCombinationUtils.isKey(KeyCode.CONTROL, 0) && KeyCombinationUtils.isKey(KeyCode.F, 1)){
                    player.searchSong.requestFocus();
                    LabelAnimations.instance(player.statusLabel)
                            .waitingTime(1000).text("Search function is now on").color(Color.LIME)
                            .startAnimation();
                }else if(KeyCombinationUtils.isKey(KeyCode.SHIFT, 0) && KeyCombinationUtils.isKey(KeyCode.RIGHT, 1)){
                    player.nextSong.fire();
                    LabelAnimations.instance(player.statusLabel)
                            .waitingTime(1000).text("Playing next song...").color(Color.LIME)
                            .startAnimation();
                }else if(KeyCombinationUtils.isKey(KeyCode.SHIFT, 0) && KeyCombinationUtils.isKey(KeyCode.LEFT, 1)){
                    player.previousSong.fire();
                    LabelAnimations.instance(player.statusLabel)
                            .waitingTime(1000).text("Playing previous song...").color(Color.LIME)
                            .startAnimation();
                }else if(KeyCombinationUtils.isKey(KeyCode.CONTROL, 0) && KeyCombinationUtils.isKey(KeyCode.LEFT, 1)){
                    player.audioPlayer.seek(Duration.seconds(0.0));
                    player.videoPlayer.seek(Duration.seconds(0.0));
                    LabelAnimations.instance(player.statusLabel)
                            .waitingTime(1000).text("Playing current song from beginning...").color(Color.LIME)
                            .startAnimation();
                }
                //Key Combinations end
                else if(event.getCode() == KeyCode.RIGHT){
                    player.audioPlayer.seek(
                            Duration.seconds(player.audioPlayer.getCurrentTime().toSeconds() + 5.0)
                    );
                    player.videoPlayer.seek(
                            Duration.seconds(player.audioPlayer.getCurrentTime().toSeconds() + 5.0)
                    );
                    LabelAnimations.instance(player.statusLabel)
                            .getByIdentifier("PLAYER_FORWARD")
                            .waitingTime(1000).incrementedNumericalValue(true).text("Forward 5 seconds").color(Color.LIME)
                            .startAnimation();
                }else if(event.getCode() == KeyCode.LEFT){
                    player.audioPlayer.seek(
                            Duration.seconds(player.audioPlayer.getCurrentTime().toSeconds() - 5.0)
                    );
                    player.videoPlayer.seek(
                            Duration.seconds(player.audioPlayer.getCurrentTime().toSeconds() - 5.0)
                    );
                    LabelAnimations.instance(player.statusLabel)
                            .getByIdentifier("PLAYER_BACKWARD")
                            .waitingTime(1000).incrementedNumericalValue(true).text("Back 5 seconds").color(Color.LIME)
                            .startAnimation();
                }else if(event.getCode() == KeyCode.SPACE){
                    player.playButton.fire();
                }else if(event.getCode() == KeyCode.DOWN){
                    player.volumeSlider.setValue(player.volumeSlider.getValue() - 5);
                    player.updateVolumeSlider();
                    LabelAnimations.instance(player.statusLabel)
                            .getByIdentifier("VOLUME_DOWN")
                            .text("Volume -5%").waitingTime(1000).color(Color.LIME).incrementedNumericalValue(true)
                            .startAnimation();
                }else if(event.getCode() == KeyCode.UP){
                    player.volumeSlider.setValue(player.volumeSlider.getValue() + 5);
                    player.updateVolumeSlider();
                    LabelAnimations.instance(player.statusLabel)
                            .getByIdentifier("VOLUME_UP")
                            .text("Volume +5%").waitingTime(1000).color(Color.LIME).incrementedNumericalValue(true)
                            .startAnimation();
                }
                event.consume();
            }
        });
        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> KeyCombinationUtils.removeKey(event.getCode()));
    }
    @SuppressWarnings("all") public static void startAutomaticGarbageCollectorCall(int duration){
        LOGGER.info("Automatic garbage collector timer is set to "
                +TimeConverter.toExplicitString((duration - 1), TimeConverter.LevelType.MINUTES, false));
        executorService.submit(() -> {
            AtomicInteger seconds = new AtomicInteger(0);
            Utilities.sleep(Duration.seconds(1), -1,
                    event -> {
                        int remaining = duration - seconds.incrementAndGet();
                        if(firstGCCall){
                            LOGGER.info("First Garbage collector call will happen in 25 seconds...");
                            seconds.set(duration - 25);
                            firstGCCall = false;
                        }
                        String timer = TimeConverter.toExplicitString(remaining, TimeConverter.LevelType.MINUTES, false);
                        if(Player.VERBOSE_STATUS){
                            Player.instance.memoryCleanup.setText((remaining > 0) ?
                                    "Automatic Garbage Collector call in "+timer+"..." : "Garbage collector has freed up as much as it could.");
                        }
                        if(remaining <= 0){
                            seconds.set(0);
                            Runtime.getRuntime().gc();
                        }
                    },
                    null);
        });
    }
    @SuppressWarnings("all")
    public static void uninstallApp(int attempt){
        Player player = Player.instance;
        int totalFilesRemoved = 0;
        LOGGER.info("\nUninstalling application... (Attempt no."+attempt+")");
        LOGGER.info("Removing associated files...");
        if(attempt > 1){
            if(player.videoPlayer != null){
                LOGGER.info("Disabling the Player and background threads because the previous attempt failed...");
                Uninstaller.shutdownAllThreads();
                Utilities.forceStopPlayer(player);
                LOGGER.info("The player has been disabled.");
            }
        }
        Uninstaller.uninstall();
        Utilities.sleep(Duration.seconds(2), 1, run -> {
            if(FilesUtils.getTotalFilesCount() > 0){
                LOGGER.warning("\nThere are still files remaining... Did the player close before the uninstall process? Retrying...");
                uninstallApp(attempt + 1);
                return;
            }
            quitApp(false, MainApp.instance.getApplicationState());
            Uninstaller.showFinalSummary();
        }, null);
    }
    public static double calculateFileSize(File file, boolean getInMegaBytes){
        double size = 0.0;
        if(file.isDirectory()){
            File[] files = file.listFiles();
            assert files != null;
            for(File file1 : files){
                if(FilesUtils.isFileCurrentlyOpened(file1)){
                    continue;
                }
                size += file1.length();
            }
            return getInMegaBytes ? (size / Math.pow(1024, 2)) : size;
        }else{
            if(FilesUtils.isFileCurrentlyOpened(file))
                return 0;
        }
        return getInMegaBytes ? (file.length() / Math.pow(1024, 2)) : file.length();
    }
}