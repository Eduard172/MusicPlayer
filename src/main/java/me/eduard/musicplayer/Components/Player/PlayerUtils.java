package me.eduard.musicplayer.Components.Player;

import javafx.util.Duration;
import me.eduard.musicplayer.Components.Notifications.NextSong;
import me.eduard.musicplayer.Library.Cache.PlaylistCache;
import me.eduard.musicplayer.Library.Cache.SongRegistry;
import me.eduard.musicplayer.Library.LambdaObject;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.Utilities;

import java.io.File;
import java.util.List;

public class PlayerUtils{
    public static Runnable getOnEndOfMediaPlayer(LambdaObject<Integer> finalIndex, String directory){
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
                    player.setSongName(songName, Player.ANIMATIONS);
                    player.listView.getSelectionModel().select(0);
                    checkAndRunNextSongNotification(finalIndex, player, songName);
                    player.playMedia(file, finalIndex.get(), directory);
                    return;
                }
                File file = SongRegistry.getAndRegister(player.listView.getItems().get(finalIndex.get()), directory + "/" + player.listView.getItems().get(finalIndex.get()));
                String songName = Utilities.correctSongName(file, true);
                player.setSongName(songName, Player.ANIMATIONS);
                checkAndRunNextSongNotification(finalIndex, player, songName);
                player.listView.getSelectionModel().select(finalIndex.get());
                player.playMedia(file, finalIndex.get(), directory);
            }
        };
    }
    private static void checkAndRunNextSongNotification(LambdaObject<Integer> finalIndex, Player player, String songName){
        if (!MainApp.stage1.isFocused() && Player.SLIDING_NOTIFICATIONS) {
            String upNext = (finalIndex.get() + 1 >= player.listView.getItems().size()) ?
                    player.listView.getItems().get(0) : player.listView.getItems().get(finalIndex.get() + 1);
            NextSong.launch(songName, upNext.replace((char) 65372, '|'));
        }
    }
}
