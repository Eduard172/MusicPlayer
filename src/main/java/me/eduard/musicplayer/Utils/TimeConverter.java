package me.eduard.musicplayer.Utils;

public class TimeConverter {
    public enum LevelType{
        SECONDS, MINUTES, HOURS, DAYS
    }

    private int timer;
    public TimeConverter(int timer){
        this.timer = timer;
    }
    public TimeConverter(){}
    public void setTimer(int timer){
        this.timer = timer;
    }
    public int getTimer(){
        return this.timer;
    }
    public int toSeconds(){
        return timer % 60;
    }
    public int toMinutes(){
        return timer / 60 % 60;
    }
    public int toHours(){
        return timer / (60 * 60) % 24;
    }
    public int toDays(){
        return timer / (60 * 60 * 24) % 7;
    }
    public String toString(LevelType levelType){
        return this.toString(levelType, ":");
    }
    public static String toString(int timer, LevelType levelType, String separator){
        return new TimeConverter(timer).toString(levelType, separator);
    }
    public static String toExplicitString(int timer, LevelType levelType, boolean abbreviations){
        return new TimeConverter(timer).toExplicitString(levelType, abbreviations);
    }
    public String toExplicitString(LevelType levelType, boolean abbreviations){
        StringBuilder builder = new StringBuilder();
        String s = String.valueOf(this.toSeconds());
        String m = (this.toMinutes() > 0) ? String.valueOf(this.toMinutes()) : "";
        String h = (this.toHours() > 0) ? String.valueOf(this.toHours()) : "";
        String d = (this.toDays() > 0) ? String.valueOf(this.toDays()) : "";
        String separator = " ";

        String ss = (abbreviations) ? "s" : (s.equals("1")) ? " second" : " seconds";
        String mm = (m.isEmpty()) ? "" : (abbreviations) ? "m" : (m.equals("1")) ? " minute," : " minutes,";
        String hh = (h.isEmpty()) ? "" : (abbreviations) ? "h" : (h.equals("1")) ? " hour," : " hours,";
        String dd = (d.isEmpty()) ? "" : (abbreviations) ? "d" : (d.equals("1")) ? " day," : " days,";

        switch (levelType){
            case SECONDS -> builder.append(s).append(ss);

            case MINUTES -> {
                if(this.toMinutes() > 0){
                    builder.append(m).append(mm).append(separator);
                }
                builder.append(s).append(ss);
            }
            case HOURS -> {
                if(this.toHours() > 0)
                    builder.append(h).append(hh).append(separator);
                if(this.toMinutes() > 0)
                    builder.append(m).append(mm).append(separator);
                builder.append(s).append(ss);
            }
            case DAYS ->{
                if(this.toDays() > 0)
                    builder.append(d).append(dd).append(separator);
                if(this.toHours() > 0)
                    builder.append(h).append(hh).append(separator);
                if(this.toMinutes() > 0)
                    builder.append(m).append(mm).append(separator);
                builder.append(s).append(ss);
            }
        }
        return builder.toString();

    }
    public String toString(LevelType levelType, String separator){
        StringBuilder builder = new StringBuilder();
        String s = this.extraConvert(this.toSeconds());
        String m = this.extraConvert(this.toMinutes());
        String h = this.extraConvert(this.toHours());
        String d = this.extraConvert(this.toDays());

        switch (levelType){
            case SECONDS -> builder.append(s);
            case MINUTES -> builder.append(m).append(separator).append(s);
            case HOURS ->   builder.append(h).append(separator).append(m).append(separator).append(s);
            case DAYS ->    builder.append(d).append(separator).append(h).append(separator).append(m).append(separator).append(s);
        }
        return builder.toString();
    }
    @Override
    public String toString(){
        return String.valueOf(this.getTimer());
    }
    public String extraConvert(int t){
        return String.valueOf((t < 10) ? "0"+t : t);
    }
}
