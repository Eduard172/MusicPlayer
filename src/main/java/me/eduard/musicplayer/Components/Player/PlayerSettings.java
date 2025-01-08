package me.eduard.musicplayer.Components.Player;

public class PlayerSettings {

    public enum SoundType{
        ADJUSTED("Adjusted"),
        FULL("Full");
        private final String s;
        SoundType(String s){
            this.s = s;
        }
        public String get(){
            return this.s;
        }
    }
    public enum Media_End_Behaviour_Type{
        INFINITE_PLAY("Infinite-Play"),
        AUTO_PLAY("Auto-Play");
        private final String s;
        Media_End_Behaviour_Type(String s){
            this.s = s;
        }
        public String get(){
            return this.s;
        }
    }
    //Variables
    public static boolean AUTO_START = false;
    public static double VOLUME = 100.0d;
    public static String SELECTED_PLAYLIST = "[None]";
    public static boolean MANAGE_INSTANT_REMOVE = false;
    public static String SOUND_TYPE = SoundType.FULL.get();
    public static String MEDIA_END_BEHAVIOUR = Media_End_Behaviour_Type.AUTO_PLAY.get();
    public static boolean ANIMATIONS = true;
    public static boolean SLIDING_NOTIFICATIONS = true;
    public static boolean VERBOSE_STATUS = false;
    public static boolean START_IN_FULLSCREEN = false;
    public static boolean HIDE_PLAYER = false;
    public static boolean HIDE_TITLE = false;

    public static void initializeApplicationSettings(){
        PlayerSettingsArgs playerSettings = PlayerSettingsArgs.newInstance();
        AUTO_START = playerSettings.isAutoStart();
        VOLUME = playerSettings.getVolume();
        SELECTED_PLAYLIST = playerSettings.getSelectedPlaylist();
        MANAGE_INSTANT_REMOVE = playerSettings.isManageInstantRemove();
        SOUND_TYPE = playerSettings.getSoundType();
        MEDIA_END_BEHAVIOUR = playerSettings.getMediaEndBehaviour();
        ANIMATIONS = playerSettings.isAnimations();
        SLIDING_NOTIFICATIONS = playerSettings.isSlidingNotifications();
        VERBOSE_STATUS = playerSettings.isVerboseStatus();
        START_IN_FULLSCREEN = playerSettings.isStartFullScreen();
        HIDE_PLAYER = playerSettings.isHidePlayer();
        HIDE_TITLE = playerSettings.isHideTitle();
    }
    public static void updateApplicationSettings(PlayerSettingsArgs newSettings){
        AUTO_START = newSettings.isAutoStart();
        VOLUME = newSettings.getVolume();
        SELECTED_PLAYLIST = newSettings.getSelectedPlaylist();
        MANAGE_INSTANT_REMOVE = newSettings.isManageInstantRemove();
        SOUND_TYPE = newSettings.getSoundType();
        MEDIA_END_BEHAVIOUR = newSettings.getMediaEndBehaviour();
        ANIMATIONS = newSettings.isAnimations();
        SLIDING_NOTIFICATIONS = newSettings.isSlidingNotifications();
        VERBOSE_STATUS = newSettings.isVerboseStatus();
        START_IN_FULLSCREEN = newSettings.isStartFullScreen();
        HIDE_PLAYER = newSettings.isHidePlayer();
        HIDE_TITLE = newSettings.isHideTitle();
    }
}
