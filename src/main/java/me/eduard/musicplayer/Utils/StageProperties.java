package me.eduard.musicplayer.Utils;

public class StageProperties {

    private String styleSheet;
    private String title;
    private String icon;
    private boolean resizable;
    private StageProperties(){}
    private StageProperties(String styleSheet, String title, String icon, boolean resizable){
        this.styleSheet = styleSheet;
        this.title = title;
        this.icon = icon;
        this.resizable = resizable;
    }
    public String getStyleSheet() {
        return this.styleSheet;
    }

    public String getTitle() {
        return this.title;
    }

    public String getIcon() {
        return this.icon;
    }

    public boolean isResizable() {
        return this.resizable;
    }
    public static StageProperties BasicProperties(String styleSheet, String title, String icon, boolean resizable){
        return new StageProperties(styleSheet, title, icon, resizable);
    }

}
