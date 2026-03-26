package me.eduard.musicplayer.Library.FFmpeg;

import me.eduard.musicplayer.Library.Exceptions.OperationFailedException;
import me.eduard.musicplayer.Library.ProcessHelper;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.FilesUtils;
import me.eduard.musicplayer.Utils.Logging.LoggerFormatter;
import me.eduard.musicplayer.Utils.Logging.LoggerHandler;
import me.eduard.musicplayer.Utils.MathUtils;
import me.eduard.musicplayer.Utils.PlaylistRelated.ListeningMode;
import me.eduard.musicplayer.Utils.Utilities;
import me.eduard.musicplayer.Utils.ValueParser;

import java.io.File;
import java.util.logging.Logger;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class NormalizationHelper {

    public static final double DEFAULT_LRA = 7;
    public static final double DEFAULT_I = -24;
    public static final double DEFAULT_TP = -2;

    private long startGen = -1;
    private long endGen = -1;

    private static final Logger LOGGER = Logger.getLogger("Loudness-Normalization");
    private boolean values_generated = false;
    static {
        LOGGER.addHandler(new LoggerHandler(new LoggerFormatter()));
        LOGGER.setUseParentHandlers(false);
    }

    private double output_I;
    private double output_TP;
    private double output_LRA;
    private double output_thresh;
    private double offset;
    private double input_i;
    private double input_tp;
    private double input_lra;
    private double input_thresh;

    private final String file;

    private final String[] command = {
            MainApp.FFMPEG, "-i", "%file%", "-hide_banner", "-af",
            "loudnorm=print_format=json", "-f", "null", "-"
    };

    public NormalizationHelper(String file){
//        this.file = FilesUtils.getBelongingDirectory(file).concat("\\First.wav"); // Abs Path + First.wav
//        this.decodeOPUSToWave(file, this.file);
        this.file = file;
        LOGGER.info("Target file for first pass is '"+this.file+"'");
        this.modifyCommandWithFile(this.file);
    }

    public static NormalizationHelper forFile(String file){
        return new NormalizationHelper(file);
    }

    private void decodeOPUSToWave(String file, String result) {
        final String[] cmd = {
                MainApp.FFMPEG, "-i", file, "-c:a", "pcm_s16le", "-af", "aresample=ochl=stereo:dither_method=triangular:osf=s16:output_sample_bits=16", result
        };
        LOGGER.info("Decoding OPUS to WAVE");
        ProcessHelper.executeIndependent(false, true, cmd);
        if(new File(file).delete()){
            LOGGER.info("Removed raw OPUS file");
        }
        LOGGER.info("Successfully decoded OPUS to WAVE.");
    }

    public NormalizationHelper generateValues(){
        this.startGen = System.currentTimeMillis();
        ValueParser parser = ProcessHelper.executeIndependent(false, true, this.command);
        this.endGen = System.currentTimeMillis();
        parser.subtractFullString("{", ValueParser.SubtractionDirection.LOWER, false);
        parser.setRemovableParts("\"", ",");
        this.output_I = parser.toDoubleValue("output_i");
        this.output_TP = parser.toDoubleValue("output_tp");
        this.output_LRA = parser.toDoubleValue("output_lra");
        this.output_thresh = parser.toDoubleValue("output_thresh");
        this.offset = parser.toDoubleValue("target_offset");
        this.input_i = parser.toDoubleValue("input_i");
        this.input_tp = parser.toDoubleValue("input_tp");
        this.input_lra = parser.toDoubleValue("input_lra");
        this.input_thresh = parser.toDoubleValue("input_thresh");
        this.values_generated = true;
        return this;
    }

    public void testGeneratedValues() {
        if(!values_generated){
            LOGGER.warning("Values were not generated.");
            return;
        }
        LOGGER.info("Original non-normalized audio statistics:");
        LOGGER.info("");
        LOGGER.info("Output I = "+this.output_I);
        LOGGER.info("Output TP = "+this.output_TP);
        LOGGER.info("Output LRA = "+this.output_LRA);
        LOGGER.info("Output Thresh = "+this.output_thresh);
        LOGGER.info("Target Offset = "+this.offset);
        LOGGER.info("Input I = "+this.input_i);
        LOGGER.info("Input TP = "+this.input_tp);
        LOGGER.info("Input LRA = "+this.input_lra);
        LOGGER.info("Input Thresh = "+this.input_thresh);
    }

    public void normalize(double integrated_i, double integrated_tp, double integrated_lra, String resultName, boolean useOutput){
        if(!values_generated){
            throw new OperationFailedException("Cannot normalize without values. Use NormalizationHelper.generateValues() method before calling this.");
        }
        String[] command = this.getNormalizationCommand(integrated_i, integrated_tp, integrated_lra, resultName, useOutput);
        System.out.println(Utilities.stringFromArray(command, "", " "));
        ProcessHelper.executeIndependent(false, true, command);
    }

    public void normalizeAudio(String resultsName, ListeningMode listeningMode, boolean useOutputValues){
        listeningMode = listeningMode == null ? ListeningMode.LOCAL_DESKTOP_DEVICE : listeningMode;
        LOGGER.info("Using "+(useOutputValues ? "output" : "input")+" values.");

        double i = -14.00;
        double true_peak = useOutputValues ? Math.max(-9.0, MathUtils.negative(Math.abs(this.output_TP) - 1.5)) : Math.min(this.input_tp, -1.5);

        double delta = Math.abs(i) - Math.abs(this.input_i);
        double lra = Math.abs(useOutputValues ? this.output_LRA : this.input_lra) < 1 ? 1 : Math.abs(useOutputValues ? this.output_LRA : this.input_lra);
        this.testGeneratedValues();
        
        if(Math.abs(this.input_i) < Math.abs(i)) {
            if(listeningMode == ListeningMode.EXTERNAL_DEVICE){
                if(new File(this.file).renameTo(new File(resultsName))){
                    LOGGER.info("Ready for TV playback.");
                }else{
                    LOGGER.warning("Something went wrong in processing audio for TV playback.");
                    Thread.dumpStack();
                }
                return;
            }
            LOGGER.info("Starting normalization... (Level = "+i+")");
            long now = System.currentTimeMillis();
            this.normalize(
                    useOutputValues ? MathUtils.negative(Math.abs(this.output_I) + delta) : MathUtils.negative(i),
                    true_peak,
                    lra,
                    resultsName, useOutputValues
            );
            long end = System.currentTimeMillis();
            double endToEndRatio = (double) (this.endGen - this.startGen) / (end - now);
            if(endToEndRatio < 4 && !useOutputValues){
                LOGGER.severe("The normalization may have failed.");
                LOGGER.severe("An usual End-To-End ratio should be in a range of 4...N, while now it's "+endToEndRatio+".");
                LOGGER.severe("Retrying normalization based on their output values and ignoring the input ones...");
                this.normalizeAudio(resultsName, listeningMode, true);
                return;
            }
            final double normalizeFactor = MathUtils.shiftToNextFirstDecimal(Math.abs(i) - Math.abs(this.input_i));
            LOGGER.info("Normalization took "+(end - now)+"ms to finish. (Ratio = "+endToEndRatio+")");
            LOGGER.info("Conditioned normalization has been finished. (Normalized by "+Math.abs(normalizeFactor)+"dB, True Peak "+ true_peak +"dB, and LRA "+lra+" LUFS)");
            LOGGER.info("Final Integrated loudness is now "+i+" LUFS (Result = "+(Math.abs(i) - Math.abs(this.input_i))+")");

        }else{
            if(new File(this.file).renameTo(new File(FilesUtils.getBelongingDirectory(this.file).concat("\\Audio.wav")))){
                LOGGER.info("Considering the processing audio as playback");
            }
            LOGGER.warning("Skipped normalizing. (Input Level is "+this.input_i+")");
        }
    }

    private String[] getNormalizationCommand(
            double integrated_i,
            double integrated_tp,
            double integrated_lra,
            String resultName,
            boolean useOutput
    ) {
        return new String[] {
                MainApp.FFMPEG, "-i", this.file, "-hide_banner", "-y", "-af",
                "loudnorm="+
                        "measured_I="+(useOutput ? this.output_I : this.input_i)+
                        ":measured_tp="+(useOutput ? this.output_TP : this.input_tp)+
                        ":measured_lra="+(useOutput ? this.output_LRA : this.input_lra)+
                        ":measured_thresh="+(useOutput ? this.output_thresh : this.input_thresh)+
                        ":offset="+this.offset+
                        ":I="+integrated_i+
                        ":TP="+integrated_tp+
                        ":LRA="+integrated_lra+",aresample=ochl=stereo:dither_method=triangular:osf=s16:output_sample_bits=16",
                resultName
        };
    }

    public double getOutput_I() {
        return this.output_I;
    }

    public double getOutput_TP() {
        return this.output_TP;
    }

    public double getOutput_LRA() {
        return this.output_LRA;
    }

    public double getOutput_thresh() {
        return this.output_thresh;
    }

    public double getOffset() {
        return this.offset;
    }

    public double getInput_I() {
        return this.input_i;
    }

    public double getInput_TP() {
        return this.input_tp;
    }

    public double getInput_LRA() {
        return this.input_lra;
    }

    public double getInput_Thresh() {
        return this.input_thresh;
    }

    public String getFile() {
        return this.file;
    }

    private void modifyCommandWithFile(String file){
        for(int i = 0; i < command.length; i++){
            command[i] = command[i].equals("%file%") ? file : command[i];
        }
    }

}
