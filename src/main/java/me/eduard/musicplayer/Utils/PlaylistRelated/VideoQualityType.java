package me.eduard.musicplayer.Utils.PlaylistRelated;

public enum VideoQualityType {

    HIGH(1080, "High", 248),
    ABOVE_MEDIUM(720, "Above-Medium", 247),
    MEDIUM(480, "Medium", 244),
    LOW_MEDIUM(360, "Low-Medium", 243),
    LOW(240, "Low", 242),
    LOWEST(144, "Lowest", 278);

    private final int quality;
    private final String name;
    private final int format;

    VideoQualityType(int quality, String name, int format){
        this.name = name;
        this.quality = quality;
        this.format = format;
    }

    public int getQuality(){
        return this.quality;
    }

    public int getFormat() {
        return this.format;
    }

    public String getName(){
        return this.name;
    }

}
