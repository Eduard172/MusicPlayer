package me.eduard.musicplayer.Components.Cache.Window;

@SuppressWarnings({"SpellCheckingInspection"})
public class WindowIdentifier {
    public static final String MAIN = "PLAYER";
    public static final String SETTINGS = "SETTINGS";
    public static final String PLAYLIST_CHOOSER = "PLAYLIST_CHOOSER";
    public static final String CREATE_PLAYLIST = "CREATE_PLAYLIST";
    public static final String MANAGE_PLAYLISTS = "MANAGE_PLAYLISTS";
    public static final String KEYBINDS = "KEYBINDS";
    public static final String UNINSTALL = "UNINSTALL";
    public static final String PLANNED_TO_BE_REMOVED = "PLANNED_TO_BE_REMOVED";
    public static final String WARNING = "WARNING";
    public static final String NEXT_SONG = "NEXT_SONG";
    public static final String MESSAGE_BOX = "MESSAGE_BOX";
    public static final String SLIDING_NOTIFICATION = "SLIDING_NOTIFICATION";
    public static final String PROBLEM_FIXER = "PROBLEM_FIXER";
    public static final String ERROR_HANDLER = "ERROR_HANDLER";

    public static final class ManagePlaylists {
        public static final String ADD_SONGS = MANAGE_PLAYLISTS+"/ADD_SONGS";
        public static final String REMOVE_SONGS = MANAGE_PLAYLISTS+"/REMOVE_SONGS";
        public static final String FIX = MANAGE_PLAYLISTS+"/FIX";
        public static final String MOVE_SONGS = MANAGE_PLAYLISTS+"/MOVE_SONGS";
        public static final String PLAYLIST_INFO = MANAGE_PLAYLISTS+"/PLAYLIST_INFO";
        public static final String DELETE = MANAGE_PLAYLISTS+"/DELETE";
    }
}
