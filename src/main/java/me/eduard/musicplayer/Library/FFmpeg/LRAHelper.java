package me.eduard.musicplayer.Library.FFmpeg;

import me.eduard.musicplayer.Library.ProcessHelper;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.ValueParser;

public class LRAHelper {

    private double low_lra;
    private double high_lra;
    private double lra;

    private final String[] command = {
            MainApp.FFMPEG, "-i", "%file%", "-filter_complex", "ebur128", "-f", "null", "-"
    };

    public LRAHelper(String file){
        this.setFile(file);
    }

    public static LRAHelper forFile(String fileAbsPath) {
        return new LRAHelper(fileAbsPath);
    }

    public LRAHelper generateLRAValues(){
        ValueParser parser = ProcessHelper.executeIndependent(false, false, this.command);
        parser.subtractFullString("Summary:", ValueParser.SubtractionDirection.LOWER, false);
        parser.setRemovableParts("LUFS", "LU");
        this.low_lra = Math.abs(parser.toDoubleValue("LRA low"));
        this.high_lra = Math.abs(parser.toDoubleValue("LRA high"));
        this.lra = Math.abs(parser.toDoubleValue("LRA"));
        return this;
    }

    public void testGeneratedValues() {
        System.out.println("LOW LRA = "+this.low_lra);
        System.out.println("HIGH LRA = "+this.high_lra);
        System.out.println("LRA = "+this.lra);
    }

    private void setFile(String file) {
        for(int i = 0; i < command.length; i++){
            command[i] = command[i].equals("%file%") ? file : command[i];
        }
    }

    public static double getLRALimit(double LRA, double... limits){
        for(double d : limits){
            if(LRA + 0.5 < d){
                return d;
            }
        }
        return LRA;
    }

    public double getLRA() {
        return this.lra;
    }
    public double getLowLRA(){
        return this.low_lra;
    }
    public double getHigh_LRA() {
        return this.high_lra;
    }
    private double abs(double d){
        return Math.abs(d);
    }

}
