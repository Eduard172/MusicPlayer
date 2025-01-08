package me.eduard.musicplayer.Components.Player;

public enum VideoQualityType {

    HIGHEST_POSSIBLE(-1, "Highest-Possible"),
    HIGH(1080, "High"),
    ABOVE_MEDIUM(720, "Above-Medium"),
    MEDIUM(480, "Medium"),
    LOW_MEDIUM(360, "Low-Medium"),
    LOW(240, "Low"),
    LOWEST(144, "Lowest");

    private final int quality;
    private final String name;

    VideoQualityType(int quality, String name){
        this.name = name;
        this.quality = quality;
    }

    public int getQuality(){
        return this.quality;
    }

    public String getName(){
        return this.name;
    }

}
