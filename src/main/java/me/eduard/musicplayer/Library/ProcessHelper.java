package me.eduard.musicplayer.Library;

import me.eduard.musicplayer.Utils.Logging.LoggerFormatter;
import me.eduard.musicplayer.Utils.Logging.LoggerHandler;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Logger;

public class ProcessHelper {

    private static final Logger LOGGER = Logger.getLogger("Process-Helper");

    static {
        LoggerHandler loggerHandler = new LoggerHandler();
        loggerHandler.setFormatter(new LoggerFormatter());
        LOGGER.addHandler(loggerHandler);
        LOGGER.setUseParentHandlers(false);
    }

    private Process process;
    private ProcessBuilder builder;
    private String[] arguments;
    private final StringBuilder stringBuilder = new StringBuilder();
    private boolean verbose = false;
    private boolean autoDestroy = true;

    public ProcessHelper(){

    }

    public ProcessHelper(String... arguments){
        this.arguments = arguments;
        this.setupProcessBuilder();
    }

    private void setupProcessBuilder(){
        this.builder = new ProcessBuilder(this.arguments);
        this.builder.redirectErrorStream(true);
    }

    public void setArguments(String... arguments){
        this.arguments = arguments;
    }

    public void setAutoDestroy(boolean autoDestroy){
        this.autoDestroy = autoDestroy;
    }

    public void start(){
        try {
            this.clearOutput();
            this.process = this.builder.start();
            try (InputStream inputStream = this.process.getInputStream()) {
                Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    this.stringBuilder.append(line).append("\n");
                    if (this.verbose)
                        LOGGER.info(line);
                }
            }
            if(this.autoDestroy){
                this.process.destroy();
            }
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public void clearOutput(){
        this.stringBuilder.setLength(0);
    }

    public String getOutput(){
        return this.stringBuilder.toString();
    }

    public void setVerbose(boolean verbose){
        this.verbose = verbose;
    }

    public Process getProcess(){
        return this.process;
    }

    public boolean isAutoDestroy(){
        return this.autoDestroy;
    }

    public ProcessBuilder getProcessBuilder(){
        return this.builder;
    }

    public boolean isVerbose(){
        return this.verbose;
    }

    public String[] getArguments(){
        return this.arguments;
    }

    public int getExitCode(){
        try {
            return this.process.waitFor();
        }catch (Exception e){
            LOGGER.severe(e.getMessage());
            return -1;
        }
    }

}
