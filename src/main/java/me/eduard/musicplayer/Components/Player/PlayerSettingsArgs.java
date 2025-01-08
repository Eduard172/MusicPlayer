package me.eduard.musicplayer.Components.Player;

import me.eduard.musicplayer.Utils.Settings;

@SuppressWarnings("unused")
public class PlayerSettingsArgs {

    private final Settings playerSettings = Settings.of("settings.yml");

    private boolean
            autoStart,
            manageInstantRemove,
            animations,
            slidingNotifications,
            verboseStatus,
            startFullScreen,
            hidePlayer,
            hideTitle;

    private String
            selectedPlaylist,
            soundType,
            mediaEndBehaviour;

    private double volume;

    private void initValues(){
        this.autoStart = Boolean.parseBoolean(this.playerSettings.getSettingValue("auto-start", false));
        this.manageInstantRemove = Boolean.parseBoolean(this.playerSettings.getSettingValue("manage-instant-remove", false));
        this.animations = Boolean.parseBoolean(this.playerSettings.getSettingValue("animations", false));
        this.slidingNotifications = Boolean.parseBoolean(this.playerSettings.getSettingValue("sliding-notifications", false));
        this.verboseStatus = Boolean.parseBoolean(this.playerSettings.getSettingValue("verbose-status", false));
        this.startFullScreen = Boolean.parseBoolean(this.playerSettings.getSettingValue("fullscreen", false));
        this.hideTitle = Boolean.parseBoolean(this.playerSettings.getSettingValue("hide-player", false));
        this.hideTitle = Boolean.parseBoolean(this.playerSettings.getSettingValue("hide-title", false));
        this.selectedPlaylist = this.playerSettings.getSettingValue("selected-playlis", false);
        this.soundType = this.playerSettings.getSettingValue("Sound-Type", false);
        this.mediaEndBehaviour = this.playerSettings.getSettingValue("Media-End-Behaviour", false);
        this.volume = Double.parseDouble(this.playerSettings.getSettingValue("app-volume", false));
    }

    private PlayerSettingsArgs(){
        this.initValues();
    }

    public static PlayerSettingsArgs newInstance() {
        return new PlayerSettingsArgs();
    }

    //Booleans
    public PlayerSettingsArgs setAutoStart(boolean autoStart){
        this.autoStart = autoStart;
        return this;
    }

    public PlayerSettingsArgs setManageInstantRemove(boolean manageInstantRemove){
        this.manageInstantRemove = manageInstantRemove;
        return this;
    }

    public PlayerSettingsArgs setAnimations(boolean animations){
        this.animations = animations;
        return this;
    }

    public PlayerSettingsArgs setSlidingNotifications(boolean slidingNotifications){
        this.slidingNotifications = slidingNotifications;
        return this;
    }

    public PlayerSettingsArgs setVerboseStatus(boolean verboseStatus){
        this.verboseStatus = verboseStatus;
        return this;
    }

    public PlayerSettingsArgs setStartFullscreen(boolean startFullScreen){
        this.startFullScreen = startFullScreen;
        return this;
    }

    public PlayerSettingsArgs setHidePlayer(boolean hidePlayer){
        this.hidePlayer = hidePlayer;
        return this;
    }

    public PlayerSettingsArgs setHideTitle(boolean hideTitle){
        this.hideTitle = hideTitle;
        return this;
    }

    //Strings
    public PlayerSettingsArgs setSelectedPlaylist(String selectedPlaylist){
        this.selectedPlaylist = selectedPlaylist == null ? this.selectedPlaylist : selectedPlaylist;
        return this;
    }

    public PlayerSettingsArgs setSoundType(String soundType){
        this.soundType = soundType == null ? this.soundType : soundType;
        return this;
    }

    public PlayerSettingsArgs setMediaEndBehaviour(String mediaEndBehaviour){
        this.mediaEndBehaviour = mediaEndBehaviour == null ? this.mediaEndBehaviour : mediaEndBehaviour;
        return this;
    }

    //Doubles
    public PlayerSettingsArgs setVolume(double volume){
        this.volume = volume < 0 || volume > 100 ? this.volume : volume;
        return this;
    }

    //Return Booleans
    public boolean isAutoStart(){
        return this.autoStart;
    }

    public boolean isManageInstantRemove(){
        return this.manageInstantRemove;
    }

    public boolean isAnimations(){
        return this.animations;
    }

    public boolean isSlidingNotifications(){
        return this.slidingNotifications;
    }

    public boolean isVerboseStatus(){
        return this.verboseStatus;
    }

    public boolean isStartFullScreen(){
        return this.startFullScreen;
    }

    public boolean isHidePlayer(){
        return this.hidePlayer;
    }

    public boolean isHideTitle() {
        return this.hideTitle;
    }

    //Returning Strings

    public String getSelectedPlaylist() {
        return this.selectedPlaylist;
    }

    public String getSoundType() {
        return this.soundType;
    }

    public String getMediaEndBehaviour() {
        return this.mediaEndBehaviour;
    }

    //Returning Doubles

    public double getVolume() {
        return this.volume;
    }
}
