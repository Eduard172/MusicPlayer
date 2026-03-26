package me.eduard.musicplayer.Library.FFmpeg;

import me.eduard.musicplayer.Library.WrappedValue;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.DataStructures;
import me.eduard.musicplayer.Utils.ValueParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@SuppressWarnings("unused")
public class VolumeDetectHelper {

    private final WrappedValue<Double> mean_value = WrappedValue.of(0.0);
    private final WrappedValue<Double> max_value = WrappedValue.of(0.0);
    private final WrappedValue<Double> histogram = WrappedValue.of(0.0);

    public static final double MEAN_THRESH = -12.5;
    public static final double BOOST_MAXIMUM_HIST = 75_000;
    public static final double LOWER_MAXIMUM_HIST = 30_000;

    public static VolumeDetectHelper of(){
        return new VolumeDetectHelper();
    }

    public VolumeDetectHelper generateValuesFor(String path){
        String[] command = {
                MainApp.APP_EXTERNAL_HELPERS+"\\ffmpeg.exe", "-hide_banner",
                "-i", path, "-af", "volumedetect",
                "-f", "null", "-"
        };
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            DataStructures.PROCESSES.add(process);
            ValueParser parser = new ValueParser();
            try (InputStream stream = process.getInputStream()){
                Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8);
                while (scanner.hasNextLine()){
                    parser.addString(scanner.nextLine());
                }
            }
            int exitCode = process.waitFor();
            if(exitCode == 0){
                parser.subtractFullString("Parsed_volumedetect_0", ValueParser.SubtractionDirection.LOWER, true);
                parser.setRemovableParts("dB");
                this.mean_value.set(parser.toDoubleValue("mean_volume"));
                this.max_value.set(parser.toDoubleValue("max_volume"));
                this.histogram.set(parser.toDoubleValue("histogram"));
            }
        }catch (IOException | InterruptedException exception){
            exception.printStackTrace(System.err);
        }
        return this;
    }

    public void testValues(){
        System.out.println(this.getMeanValue());
        System.out.println(this.getMaxValue());
        System.out.println(this.getHistogram_0dB());
    }

    public VolumeDetectHelper setMeanValue(double meanValue){
        this.mean_value.set(meanValue);
        return this;
    }
    public VolumeDetectHelper setMaxValue(double maxValue){
        this.max_value.set(maxValue);
        return this;
    }
    public VolumeDetectHelper setHistogramValue(double histogramValue){
        this.histogram.set(histogramValue);
        return this;
    }

    public double getMeanValue() {
        return this.mean_value.get();
    }

    public double getMaxValue() {
        return this.max_value.get();
    }

    public double getHistogram_0dB() {
        return this.histogram.get();
    }
}
