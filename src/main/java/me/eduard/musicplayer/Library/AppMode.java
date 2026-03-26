package me.eduard.musicplayer.Library;

import me.eduard.musicplayer.AppState;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.MainApp;

public class AppMode {

    private static AppState state = AppState.NORMAL;

    public static void set(AppState appState) {
        Player player = Player.instance;
        MainApp.instance.setApplicationState(appState);
        switch (appState) {
            case DOWNLOADING -> {
                player.refreshButton.setVisible(false);
                player.choosePlaylist.setVisible(false);
                player.searchSong.setVisible(false);
            }
            case NORMAL -> {
                if(state == AppState.DOWNLOADING) {
                    Player.instance.refreshButton.setVisible(true);
                    Player.instance.choosePlaylist.setVisible(true);
                    Player.instance.searchSong.setVisible(true);
                }
            }
            case SEARCHING_FOR_MEDIA -> {

            }
            case WAITING_FOR_UPDATE_APPROVAL -> {

            }
        }
        state = appState;
    }

    public static AppState get() {
        return state;
    }

}
