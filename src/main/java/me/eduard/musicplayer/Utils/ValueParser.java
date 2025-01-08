package me.eduard.musicplayer.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ValueParser {

    public enum SubtractionDirection{
        UPPER, LOWER
    }

    private final List<String> STRINGS = new ArrayList<>();
    private String fullString = "[None]";
    private String delimiter = ":";

    private String[] removableParts = {};

    public void setRemovableParts(String... parts){
        this.removableParts = parts;
    }

    public String[] getRemovableParts(){
        return this.removableParts;
    }

    public void setDelimiter(String delimiter){
        this.delimiter = delimiter;
    }

    public void addString(String string){
        this.STRINGS.add(string);
    }

    public void clearStrings(){
        this.STRINGS.clear();
    }

    public void setFullString(String fullString){
        this.fullString = fullString;
    }

    public String getStringFromList(){
        return Utilities.stringFromList(this.STRINGS, "", "\n");
    }

    public String getFullString(){
        return this.fullString;
    }

    public String getDelimiter(){
        return this.delimiter;
    }

    public String getValue(String label){
        String[] lines = this.fullString.split("\n");
        String val = Arrays.stream(lines)
                .filter(s -> s.contains(label))
                .map(s -> s.replace(" ", ""))
                .findFirst().orElseThrow().split(this.delimiter)[1];
        for(String s : removableParts){
            val = val.replace(s, "");
        }
        return val;
    }

    public void removeBannedPartsFromFullString(){
        for(String s : this.removableParts){
            this.fullString = this.fullString.replace(s, "");
        }
    }

    public void subtractFullString(String where, SubtractionDirection direction){
        this.subtractFullString(where, direction, false, false);
    }

    public void subtractFullString(String where, SubtractionDirection direction, boolean keepSeparatedLine){
        this.subtractFullString(where, direction, keepSeparatedLine, false);
    }

    public void subtractFullString(String where, SubtractionDirection direction, boolean keepSeparatedLine, boolean fromList){
        boolean found = false;
        String[] lines =  fromList ? this.getStringFromList().split("\n") : this.fullString.split("\n");
        StringBuilder builder = new StringBuilder();
        switch (direction){
            case LOWER -> {
                for(String string : lines){
                    if(string.contains(where)){
                        found = true;
                        if(!keepSeparatedLine)
                            continue;
                    }
                    if(found)
                        builder.append(string).append("\n");
                }
            }case UPPER -> {
                for(String s : lines){
                    if(s.contains(where)){
                        if(keepSeparatedLine)
                            builder.append(s).append("\n");
                        break;
                    }
                    builder.append(s).append("\n");
                }
            }
        }
        this.setFullString(builder.toString().trim());
    }

}
