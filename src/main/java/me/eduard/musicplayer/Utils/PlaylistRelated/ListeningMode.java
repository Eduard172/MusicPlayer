package me.eduard.musicplayer.Utils.PlaylistRelated;

public enum ListeningMode {

    LOCAL_DESKTOP_DEVICE(-14, "Local Desktop"),
    EXTERNAL_DEVICE(-7.00, "TV, Speakers, Etc...");

    private final double normalizationLevel;
    private final String description;

    ListeningMode(double normalizationLevel, String description){
        this.normalizationLevel = normalizationLevel;
        this.description = description;
    }

    public double getNormalizationLevel() {
        return this.normalizationLevel;
    }

    public String getDescription(){
        return this.description;
    }

}
