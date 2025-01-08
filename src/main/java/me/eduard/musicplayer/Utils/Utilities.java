package me.eduard.musicplayer.Utils;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.EqualizerBand;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import me.eduard.musicplayer.Components.Player.Equalizers;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.Library.BasicKeyValuePair;
import me.eduard.musicplayer.Library.FFmpegUtils;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.Logging.LoggerFormatter;
import me.eduard.musicplayer.Utils.Logging.LoggerHandler;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class Utilities {

    private static final Logger LOGGER = Logger.getLogger("General-Utilities");

    static {
        LoggerHandler handler = new LoggerHandler();
        handler.setFormatter(new LoggerFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
    }

    @SuppressWarnings("all")
    public static Timeline sleep(Duration duration, int cycleCount, EventHandler<ActionEvent> whileRunning, EventHandler<ActionEvent> onFinished){
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(duration, whileRunning));
        timeline.setCycleCount(cycleCount);
        timeline.setOnFinished(onFinished);
        timeline.play();
        return timeline;
    }
    public static String stringFromArray(String[] array, String suffix, String prefix){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < array.length; i++){
            if(i == array.length - 1){
                builder.append(suffix).append(array[i]);
            }else{
                builder.append(suffix).append(array[i]).append(prefix);
            }
        }
        return builder.toString();
    }
    public static List<String> reverseList(List<String> list){
        List<String> arr = new ArrayList<>(list.size());
        for(int i = list.size() - 1; i >= 0; i--){
            arr.add(list.get(i));
        }
        return arr;
    }
    public static String[] reverseArray(String[] arr){
        String[] list = new String[arr.length];
        for(int i = arr.length - 1, a = 0; i >= 0; i--, a++){
            list[a] = arr[i];
        }
        return list;
    }
    public static String stringFromList(List<String> list, String suffix, String prefix){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < list.size(); i++){
            if(i == list.size() - 1){
                builder.append(suffix).append(list.get(i));
            }else{
                builder.append(suffix).append(list.get(i)).append(prefix);
            }
        }
        return builder.toString();
    }
    @SuppressWarnings("all")
    public static boolean hasInternetConnection(){
        try {
            URL url = new URL("https://google.com");
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();
            return true;
        }catch (IOException e){
            return false;
        }
    }
    public static String concatStrings(String... strings){
        StringBuilder builder = new StringBuilder();
        for(String string : strings){
            if(string == null)
                continue;
            builder.append(string);
        }
        return builder.toString();
    }
    public static URL getResource(String string){
        return MainApp.class.getResource(string);
    }
    public static InputStream getResourceAsStream(String string){
        return MainApp.class.getResourceAsStream(string);
    }
    public static InputStream getMainClassResourceStream(String string){
        return MainApp.class.getResourceAsStream(string);
    }
    public static double getPercentage(double portion, double total){
        return (portion*100)/total;
    }
    public static double getPercentageOfFixedLimits(double minLimit, double maxLimit, double value){
        return ((value - minLimit) / (maxLimit - minLimit)) * 100;
    }
    public static double getValueOfFixedLimits(double minLimit, double maxLimit, double percentage){
        return minLimit + (percentage / 100) * (maxLimit - minLimit);
    }
    public static double getProgressBarPercentage(double t1, double total){
        return ((t1 * 100) / total) / 100;
    }
    public static double getPortionOfTotal(double procent, double total){
        double result = (procent * total) / 100;
        return Math.min(result, total);
    }
    public static String betterDouble(double value, int decimals){
        String s = String.valueOf(value);
        if(!s.contains("."))
            return s;
        return s.substring(0, s.indexOf(".") + 1 + decimals);
    }
    public static String getSongURI(File file){
        return file.toURI().toString();
    }

    public static String getDecodedURI(String URI){
        return URLDecoder.decode(URI, StandardCharsets.UTF_8);
    }

    public static void setImageViewWithFullPath(ImageView imageView, String path){
        InputStream inputStream = getResourceAsStream(path);
        if(inputStream == null)
            return;
        Image image1 = new Image(inputStream);
        imageView.setImage(image1);
    }

    public static void setImageView(ImageView imageView, String image){
        InputStream inputStream = getResourceAsStream("icons/"+image);
        if(inputStream == null)
            return;
        Image image1 = new Image(inputStream);
        imageView.setImage(image1);
    }
    public static void setImageView2(ImageView imageView, String image){
        imageView.setVisible(image != null);
        InputStream inputStream = getResourceAsStream("images/"+image);
        if(image == null || inputStream == null)
            return;
        Image image1 = new Image(inputStream);
        imageView.setImage(image1);
    }
    public static int getRandomNumber(int min, int max){
        return (int) (Math.random() * (max - min + 1) + min);
    }
    public static String getStackTraceLog(Exception exception, boolean getImportantInformation){
        StackTraceElement[] stackTraceElements = exception.getStackTrace();
        StringBuilder builder = new StringBuilder();
        builder.append(exception.getClass().getName()).append(" - ").append(exception.getMessage()).append("\n");
        if(getImportantInformation){
            for(StackTraceElement stackTraceElement : stackTraceElements){
                if(stackTraceElement.toString().contains("me.eduard")){
                    builder.append(stackTraceElement).append("\n");
                }
            }
        }else{
            for(StackTraceElement stackTraceElement : stackTraceElements){
                builder.append(stackTraceElement).append("\n");
            }
        }
        return builder.toString();
    }
    public static void copyToClipboard(String message){
        StringSelection stringSelection = new StringSelection(message);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }
    public static boolean isSongInUse(String currentlyPlayingSong, String actualString){
        char[] first = currentlyPlayingSong.toCharArray();
        char[] second = actualString.toCharArray();
        if(first.length != second.length)
            return false;
        for(int i = 0; i < currentlyPlayingSong.length(); i++){
            if(((int) first[i] == 124 && (int) second[i] == 65372) || ((int) first[i] == 65372 && (int) second[i] == 124))
                continue;
            if((int) first[i] != (int) second[i]){
                return false;
            }
        }
        return true;
    }
    /**
     * This method is used to adjust the song file name, which originally may be looking hilarious.
     *
     * @param file                     The file that is supposed to be played.
     * @param removeSpecialCharacters Whether to exclude the special "|" character or not. It's recommended to set it
     *                                 to 'false' if the returned string will be reused to get the file name back.
     * @return The corrected string representing the song file name more readable.
     */
    public static String correctSongName(File file, boolean removeSpecialCharacters) {
        String replace = file.getName().replace(".mp4", "").replace(".mp3", "");
        return (removeSpecialCharacters) ? replace
                .replace((char) 65372, (char) 124)
                .trim() : replace // '\\[.*?]' > remove everything between []
                .trim();
    }
    public static void stopPlayer(Player player){
        try {
            if(player.videoPlayer != null && player.videoPlayer.getStatus() != MediaPlayer.Status.DISPOSED){
                player.videoPlayer.stop();
                player.audioPlayer.stop();
                player.videoPlayer.dispose();
                player.audioPlayer.dispose();
                LOGGER.info("Video & Audio player was stopped and disposed.");
            }
        }catch (NullPointerException exception){
            LOGGER.severe("Avoided NPE.");
        }finally {
            if(player.videoPlayer != null){
                player.audioPlayer.dispose();
                player.videoPlayer.dispose();
            }
        }
    }
    public static void forceStopPlayer(Player player){
        LOGGER.info("Force-Stopping video player...");
        while (player.videoPlayer != null && player.videoPlayer.getStatus() != MediaPlayer.Status.DISPOSED){
            player.videoPlayer.dispose();
        }
        LOGGER.info("Video player was stopped.");
        LOGGER.info("Force-Stopping audio player...");
        while (player.audioPlayer != null && player.audioPlayer.getStatus() != MediaPlayer.Status.DISPOSED){
            player.audioPlayer.dispose();
        }
        LOGGER.info("Audio player was stopped.");
    }
    public static void centralizeMediaPlayer(Player player){
        player.mediaView.setLayoutX(15); //Default value in Scene Builder.
        player.mediaView.setLayoutY(80); //Default value in Scene Builder.
        BasicKeyValuePair<Double, Double> layoutX = BasicKeyValuePair.of(
                player.mediaView.getLayoutX(), player.listView.getLayoutX() - 4
        );
        BasicKeyValuePair<Double, Double> layoutY = BasicKeyValuePair.of(
                player.mediaView.getLayoutY(), player.mediaView.getLayoutY() + player.mediaView.getFitHeight()
        );
        Utilities.sleep(Duration.millis(50), 1, run -> {
            double videoWidth = player.mediaView.getBoundsInLocal().getWidth();
            double videoHeight = player.mediaView.getBoundsInLocal().getHeight();
            double resultPosition = (layoutX.getKey() + layoutX.getValue()) / 2 - (videoWidth / 2);
            double resultYPos = (layoutY.getKey() + layoutY.getValue()) / 2 - (videoHeight / 2);
            player.mediaView.setLayoutX(resultPosition);
            player.mediaView.setLayoutY(resultYPos);
            player.playerHoverImage.setWidth(videoWidth);
            player.playerHoverImage.setHeight(videoHeight);
            player.playerHoverImage.setLayoutX(player.mediaView.getLayoutX());
            player.playerHoverImage.setLayoutY(player.mediaView.getLayoutY());
        }, null);
    }
    public static void adjustImageViewSize(ImageView imageView, double width, double height, boolean respectAspectRatio){
        imageView.setPreserveRatio(respectAspectRatio);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
    }
    public static void centralizeImageViewOnButton(ImageView imageView, Button targetButton){
        double targetButtonWidthSecondLimit = targetButton.getLayoutX() + targetButton.getPrefWidth();
        double targetButtonHeightSecondLimit = targetButton.getLayoutY() + targetButton.getPrefHeight();
        BasicKeyValuePair<Double, Double> layoutsX = BasicKeyValuePair.of(targetButton.getLayoutX(), targetButtonWidthSecondLimit);
        BasicKeyValuePair<Double, Double> layoutsY = BasicKeyValuePair.of(targetButton.getLayoutY(), targetButtonHeightSecondLimit);
        Utilities.sleep(Duration.millis(50), 1, run -> {
            double imageWidth = imageView.getBoundsInLocal().getWidth();
            double imageHeight = imageView.getBoundsInLocal().getHeight();
            double resultWidth = (layoutsX.getKey() + layoutsX.getValue()) / 2 - (imageWidth / 2);
            double resultHeight = (layoutsY.getKey() + layoutsY.getValue()) / 2 - (imageHeight / 2);
            imageView.setLayoutX(resultWidth);
            imageView.setLayoutY(resultHeight);
        }, null);
    }
    public static void openApplicationDirectory(){
        Desktop desktop = Desktop.getDesktop();
        File mainDirectory = new File(MainApp.MAIN_APP_PATH);
        if (!mainDirectory.exists() || !mainDirectory.isDirectory()) {
            return;
        }
        try {
            desktop.open(mainDirectory);
        } catch (IOException exception) {
            ErrorHandler.launchWindow(exception);
        }
    }
    public static void openBrowserURL(String url){
        if(Desktop.isDesktopSupported()){
            Desktop desktop = Desktop.getDesktop();
            try {
                if(desktop.isSupported(Desktop.Action.BROWSE)){
                    desktop.browse(new URI(url));
                }
            }catch (IOException | URISyntaxException exception){
                exception.printStackTrace(System.err);
            }
        }
    }

    public static void reDownloadExternalHelpers(){
        Player player = Player.instance;
        new Thread(() -> {
            try {
                Runtime.getRuntime().exec(MainApp.APP_EXTERNAL_HELPERS+"\\downloader.exe -U");
            }catch (IOException exception){
                LOGGER.severe("An error has occurred while trying to update 'downloader.exe'...");
                exception.printStackTrace(System.err);
            }
            File ffmpeg = new File(MainApp.APP_EXTERNAL_HELPERS+"\\ffmpeg.exe");
            if(ffmpeg.delete()){
                FFmpegUtils.downloadZipFileIfNecessary(player.statusLabel);
            }else{
                LOGGER.severe("'ffmpeg.exe' wasn't able to get updated. A thread stack dump will be shown below.");
                Thread.dumpStack();
            }
        }).start();
    }

    /**
     * Default JavaFX EqualizerBand array provided by Oracle with some additions or/ and adjustments.
     * <a href="https://github.com/openjdk/jfx/blob/master/modules/javafx.media/src/main/java/javafx/scene/media/AudioEqualizer.java">
     *     Source here
 *     </a>
     */
    public static EqualizerBand[] getDefaultAudioEqualizers(){
        return Equalizers.EQUALIZERS;
    }
}
