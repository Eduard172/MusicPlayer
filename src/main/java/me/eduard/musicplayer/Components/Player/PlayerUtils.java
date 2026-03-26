package me.eduard.musicplayer.Components.Player;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import me.eduard.musicplayer.Library.Animations.Animations;
import me.eduard.musicplayer.Library.Cache.Player.MediaCache;
import me.eduard.musicplayer.Library.Cache.PlaylistCache;
import me.eduard.musicplayer.Library.Cache.SongRegistry;
import me.eduard.musicplayer.Library.WindowsCommands;
import me.eduard.musicplayer.Library.WrappedValue;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.Logging.LoggerFormatter;
import me.eduard.musicplayer.Utils.Logging.LoggerHandler;
import me.eduard.musicplayer.Utils.PlaylistRelated.Playlists;
import me.eduard.musicplayer.Utils.Utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class PlayerUtils{

    private static final Logger LOGGER = Logger.getLogger("Player-Utilities");

    static {
        LoggerHandler loggerHandler = new LoggerHandler();
        loggerHandler.setFormatter(new LoggerFormatter());
        LOGGER.addHandler(loggerHandler);
        LOGGER.setUseParentHandlers(false);
    }

    public static void simulatePlayerFullScreen() {
        Player.fullScreenMode.enableFullScreen();
    }

    public static Runnable getOnEndOfMediaPlayer(WrappedValue<Integer> finalIndex, String directory){
        Player player = Player.instance;
        return () -> {
            List<String> cachedListView = PlaylistCache.getPlaylist(Player.SELECTED_PLAYLIST);
            if(player.listView.getItems().size() != cachedListView.size()){
                player.updateListViewBasedOnSongSearchText("");
            }
            finalIndex.set(finalIndex.get() + 1);
            if (Player.MEDIA_END_BEHAVIOUR.equals(PlayerSettings.Media_End_Behaviour_Type.INFINITE_PLAY.get())) {
                player.videoPlayer.seek(Duration.seconds(0.0));
                player.audioPlayer.seek(Duration.seconds(0.0));
                return;
            }
            if (Player.MEDIA_END_BEHAVIOUR.equals(PlayerSettings.Media_End_Behaviour_Type.AUTO_PLAY.get())) {
                Utilities.stopPlayer(player);
                if (finalIndex.get() >= player.listView.getItems().size()) {
                    finalIndex.set(0);
                    File file = SongRegistry.getAndRegister(player.listView.getItems().get(0), directory + "/" + player.listView.getItems().get(0));
                    String songName = Utilities.correctSongName(file, true);
                    player.setSongName(songName, Player.ANIMATIONS, true);
                    player.listView.getSelectionModel().select(0);
                    checkAndRunNextSongNotification(finalIndex, player, songName);
                    player.playMedia(file, finalIndex.get(), directory);
                    return;
                }
                File file = SongRegistry.getAndRegister(player.listView.getItems().get(finalIndex.get()), directory + "/" + player.listView.getItems().get(finalIndex.get()));
                String songName = Utilities.correctSongName(file, true);
                player.setSongName(songName, Player.ANIMATIONS, true);
                checkAndRunNextSongNotification(finalIndex, player, songName);
                player.listView.getSelectionModel().select(finalIndex.get());
                player.playMedia(file, finalIndex.get(), directory);
            }
        };
    }
    private static void checkAndRunNextSongNotification(WrappedValue<Integer> finalIndex, Player player, String songName){
        if (!MainApp.stage1.isFocused() && Player.SLIDING_NOTIFICATIONS) {
            String upNext = (finalIndex.get() + 1 >= player.listView.getItems().size()) ?
                    player.listView.getItems().get(0) : player.listView.getItems().get(finalIndex.get() + 1);
            WindowsCommands.launchPowershellNotification(
                    "Music has been automatically changed",
                    """
                            Now playing: %now%
                            Up next: %next%
                            """
                            .replace("%now%", songName)
                            .replace("%next%", upNext)
                            .replace((char) 65372, '|')
                            .replace("|", " ")
            );
//            NextSong.launch(songName, upNext.replace((char) 65372, '|'));
        }
    }
    public static void disableVideoPlayer(){
        Player player = Player.instance;
        if(player.videoPlayer != null && player.videoPlayer.getStatus() != MediaPlayer.Status.DISPOSED){
            if(Player.ANIMATIONS){
                Animations.undoAnimateObjectAppear(player.mediaView, 20, true);
            }
            player.videoPlayer.dispose();
            Utilities.sleep(Duration.millis(1000), 1, run -> System.gc(), null);
            LOGGER.info("Video player has been disabled.");
        }
        refreshThumbnail();
        player.thumbnail.setVisible(true);
    }

    public static void refreshThumbnail() {
        Platform.runLater(() -> {
            Player player = Player.instance;
            String thumbnail = Playlists.getPlaylistPathByName(Player.SELECTED_PLAYLIST)+"\\"+player.currentlyPlayingSongUnmodified+"\\Thumbnail."+Playlists.thumbnailExt;
            try {
                if(!new File(thumbnail).exists()){
                    player.thumbnail.setImage(null);
                    return;
                }
                Image image = new Image(new FileInputStream(thumbnail));
                player.thumbnail.setImage(image);
                Utilities.adjustImageViewSize(
                        player.thumbnail, player.mediaView.getFitWidth(), player.mediaView.getFitHeight(), true
                );
            }catch (IOException exception){
                exception.printStackTrace(System.err);
            }
            Utilities.centralizeThumbnail(player);
        });
    }

    public static void enableVideoPlayer(){
        Player player = Player.instance;
        player.thumbnail.setImage(null);
        player.thumbnail.setVisible(false);
        String currentSong = player.currentlyPlayingSong.replace((char) 124, (char) 65372);
        File[] playlist = Playlists.getPlaylist(Player.SELECTED_PLAYLIST);
        WrappedValue<String> path = WrappedValue.of("");
        for(File f : playlist){
            if(f.isDirectory() && f.getName().endsWith(currentSong)){
                path.set(f.getAbsolutePath());
                break;
            }
        }

        if(!player.THREADS.isEmpty()){
            Task<Void> playerTask = new Task<>() {
                @Override
                protected Void call() {
                    player.videoMedia = MediaCache.getVideoMedia(path.get());
                    player.videoPlayer = new MediaPlayer(player.videoMedia);
                    player.videoPlayer.setMute(true);
                    player.mediaView.setMediaPlayer(player.videoPlayer);
                    if(Player.ANIMATIONS){
                        player.mediaView.setOpacity(0.0);
                        Animations.animateObjectAppear(player.mediaView, 20, 1);
                    }
                    player.videoPlayer.setStartTime(Duration.ZERO);
                    player.videoPlayer.setOnReady(() -> {
                        player.videoPlayer.play();
                        player.videoPlayer.seek(player.audioPlayer.getCurrentTime());
                    });
                    player.videoPlayer.setOnError(() -> {
                        player.FAILED_MEDIAS.add(player.videoPlayer);
                        LOGGER.warning("Video player has crashed while trying to re-enable. Retrying...");
                        enableVideoPlayer();
                    });
                    Utilities.centralizeMediaPlayer(player);
                    return null;
                }
            };
            player.THREADS.get(1).submit(playerTask);
            LOGGER.info("Video player has been enabled.");
            Utilities.sleep(Duration.millis(2000), 1, run -> System.gc(), null);
        }
    }
    public static void restoreMediaViewToOriginalSize() {
        Player player = Player.instance;
        player.mediaView.setFitWidth(Player.fullScreenMode.mediaViewBounds.getKey());
        player.mediaView.setFitHeight(Player.fullScreenMode.mediaViewBounds.getValue());
        player.thumbnail.setFitWidth(Player.fullScreenMode.thumbnailBounds.getKey());
        player.thumbnail.setFitHeight(Player.fullScreenMode.thumbnailBounds.getValue());
    }
    public static double getAVDifference() {
        Player player = Player.instance;
        if(player.audioPlayer == null){
            return -1;
        }
        double videoTime = player.videoPlayer == null ? 0 : player.videoPlayer.getCurrentTime().toMillis();
        if(PlayerSettings.HIDE_PLAYER || player.videoPlayer == null || player.videoPlayer.getStatus() == MediaPlayer.Status.DISPOSED || player.audioPlayer == null){
            return 0;
        }
        return player.audioPlayer.getCurrentTime().toMillis() - videoTime;
    }
}
