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
        return this.STRINGS.isEmpty() ? this.fullString : this.getStringFromList();
    }

    public String getDelimiter(){
        return this.delimiter;
    }

    public String getValue(String label) {
        String val = Arrays.stream(this.getFullString().split("\n"))
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

    public String getRealLabel(String entry) {
        if(!entry.contains(this.delimiter)){
            throw new IllegalArgumentException("This entry is invalid. It doesn't contain '"+this.delimiter+"'. (Entry: "+entry+")");
        }
        return entry.split(this.delimiter)[0];
    }

    public boolean testIfLabelExists(String label){
        String[] lines = this.fullString.split("\n");
        String line = Arrays.stream(lines)
                .filter(s -> s.contains(label))
                .map(s -> s.replace(" ", ""))
                .findFirst().orElse("null").split(this.delimiter)[0];
        return line != null && !line.equals("null");
    }

    public void subtractFullString(String where, SubtractionDirection direction){
        this.subtractFullString(where, direction, false);
    }

    public void subtractFullStringFromMultipleCases(SubtractionDirection direction, String... references){
        String[] lines = this.getFullString().split("\n");
        for(String line : lines){
            for(String reference : references){
                if(line.contains(reference)){
                    this.subtractFullString(reference, direction, false);
                }
            }
        }
    }

    public void subtractFullString(String where, SubtractionDirection direction, boolean keepSeparatedLine){
        boolean found = false;
        String[] lines =  this.getFullString().split("\n");
        StringBuilder builder = new StringBuilder();
        switch (direction){
            case LOWER -> {
                for(String string : lines){
                    if(string.trim().contains(where)){
                        found = true;
                        if(!keepSeparatedLine)
                            continue;
                    }
                    if(found)
                        builder.append(string).append("\n");
                }
            }case UPPER -> {
                for(String s : lines){
                    if(s.trim().contains(where)){
                        if(keepSeparatedLine)
                            builder.append(s).append("\n");
                        break;
                    }
                    builder.append(s).append("\n");
                }
            }
        }
        this.setFullString(builder.toString().trim());
        STRINGS.clear();
        STRINGS.add(builder.toString().trim());
    }

    public int toIntValue(String value){
        return Integer.parseInt(this.getValue(value));
    }
    public double toDoubleValue(String value){
        return Double.parseDouble(this.getValue(value));
    }
    public boolean toBooleanValue(String value){
        return Boolean.parseBoolean(this.getValue(value));
    }


}
