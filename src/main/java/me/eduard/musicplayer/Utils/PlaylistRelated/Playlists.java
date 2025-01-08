package me.eduard.musicplayer.Utils.PlaylistRelated;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.util.Duration;
import me.eduard.musicplayer.AppState;
import me.eduard.musicplayer.Components.DownloadProgress;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.Components.Player.VideoQualityType;
import me.eduard.musicplayer.Library.BasicKeyValuePair;
import me.eduard.musicplayer.Library.Cache.PlaylistCache;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.DataStructures;
import me.eduard.musicplayer.Utils.FilesUtils;
import me.eduard.musicplayer.Utils.Logging.LoggerFormatter;
import me.eduard.musicplayer.Utils.Logging.LoggerHandler;
import me.eduard.musicplayer.Utils.Utilities;
import me.eduard.musicplayer.Utils.ValueParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

@SuppressWarnings("ALL")
public final class Playlists {

    private static final Logger LOGGER = Logger.getLogger("Playlist-Manager");

    private static final double[] ENCODE_FAIL_ADJUSTMENT = {
            1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2, 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 3.0
    };

    static {
        LoggerHandler handler = new LoggerHandler();
        handler.setFormatter(new LoggerFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
    }

    public static final String PATH = MainApp.MAIN_APP_PATH+"/Playlists";
    public static final String audioExt = "wav";
    public static final String videoExt = "mp4";

    public static File[] getPlaylist(String playlistName){
        File file = new File(PATH);
        if(!file.exists()){
            return new File[0];
        }
        File playlistFile = new File(PATH+"/"+playlistName);
        if(!playlistFile.exists()){
            return new File[0];
        }
        return playlistFile.listFiles();
    }

    public static void createPlaylistsDirectory(){
        File file = new File(PATH);
        if(!file.exists())
            file.mkdir();
    }

    public static String getSelectedPlaylist(){
        return Player.SELECTED_PLAYLIST;
    }
    public static String[] getPlaylistStringRepresentation(String playlistName){
        File[] filesList = getPlaylist(playlistName);
        return Arrays.stream(filesList).filter(file -> {
                    if(file.isDirectory()){
                        File[] files = file.listFiles();
                        return files.length == 2 &&
                                (files[0].getName().equals("Video."+audioExt) && files[1].getName().equals("Audio."+videoExt)) ||
                                (files[0].getName().equals("Audio."+audioExt) && files[1].getName().equals("Video."+videoExt));
                    }
                    return false;
                }
        ).map(file -> Utilities.correctSongName(file, false)).toArray(String[]::new);
    }
    public static void updatePlaylistContents(String playlistName, ListView<String> listView){
        listView.getItems().clear();
        listView.getItems().addAll(getPlaylistStringRepresentation(playlistName));
        listView.getSelectionModel().clearSelection();
        PlaylistCache.register(playlistName, new ArrayList<>(listView.getItems()));
    }
    public static List<String> getPlaylists(){
        File mainDirectory = new File(PATH);
        if(!mainDirectory.exists()){
            return new ArrayList<>();
        }
        File[] files = mainDirectory.listFiles();
        assert files != null;
        return Arrays.stream(files).filter(File::isDirectory).map(File::getName).toList();
    }
    @SuppressWarnings("all")
    public static void deletePlaylist(String playlist){
        FilesUtils.removeDirectoryFiles(getPlaylistPathByName(playlist), true);
    }
    public static boolean hasSongs(String playlist){
        File mainDirectory = new File(PATH);
        if(!mainDirectory.exists())
            return false;
        File playlistGiven = new File(PATH+"/"+playlist);
        if(!playlistGiven.exists())
            return false;
        File[] songFiles = playlistGiven.listFiles();
        assert songFiles != null;
        return !Arrays.stream(songFiles).filter(
                file -> {
                    if (file.isDirectory()) {
                        File[] files = file.listFiles();
                        return files.length == 2 &&
                                (files[0].getName().equals("Video." + audioExt) && files[1].getName().equals("Audio." + videoExt)) ||
                                (files[0].getName().equals("Audio." + audioExt) && files[1].getName().equals("Video." + videoExt));
                    }
                    return false;
                }
        ).toList().isEmpty();
    }
    public static int getPlaylistCount(){
        return getPlaylists().size();
    }
    public static String[] getDefaultVideoLinks(){
        return new String[] {
                "https://youtu.be/3myKtI0V7oM?si=aaZqZfED6GGh7QQB",
                "https://youtu.be/raRChkQXD6E?si=dnhOVisVKmHjmfc0",
                "https://youtu.be/rjBsQ9SygnE?si=WRa8wyXB54xG1ZOK",
                "https://youtu.be/K9HO23XjfZc?si=yzq0dU4n5-S97HGi"
        };
    }
    public static String[] convertToValidVideoLinks(String urls){
        return urls.trim().replace("\n", "").split(",");
    }
    public static void downloadSongs(VideoQualityType qualityType, String directory, boolean isNewPlaylist, String... url){
        MainApp.instance.setApplicationState(AppState.DOWNLOADING);
        Player.instance.refreshButton.setVisible(false);
        Player.instance.choosePlaylist.setVisible(false);
        Player.instance.searchSong.setVisible(false);
        DownloadProgress downloadProgress = DownloadProgress.launchWindow();
        new Thread(() -> {
            Platform.runLater(() -> {
                downloadProgress.setTotalSteps(url.length * 4, downloadProgress);
                downloadProgress.setTotalFilesToDownload(url.length, downloadProgress);
            });

            List<File> currentPlaylist = Arrays.asList(getPlaylist(Player.SELECTED_PLAYLIST));
            int downloaded = 0;
            for(String strings : url){
                Platform.runLater(() -> downloadProgress.setStatusInProgress("Downloading Video..."));
                String[] command = {
                        MainApp.APP_EXTERNAL_HELPERS + "\\downloader.exe",
                        "--no-keep-fragments", "--merge-output-format", "mp4",
                        "-f", (qualityType != VideoQualityType.HIGHEST_POSSIBLE ? "bv[height<="+qualityType.getQuality()+"]" : "bv"),
                        "-o", "%(title)s--Video.%(ext)s",
                        "-P", directory,
                        strings
                };
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder(command);
                    processBuilder.redirectErrorStream(true);
                    Process process = processBuilder.start();
                    DataStructures.PROCESSES.add(process);
                    try (InputStream inputStream = process.getInputStream()){
                        LOGGER.info("Downloading Sample Video...");
                        Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
                        while (scanner.hasNextLine()){
                            LOGGER.fine(scanner.nextLine());
                        }
                    }catch (IOException exception){
                        exception.printStackTrace(System.err);
                    }
                    int exitCode = process.waitFor();
                    if(exitCode == 0){
                        DataStructures.PROCESSES.remove(process);
                        process.destroy();
                        Platform.runLater(() -> {
                            downloadProgress.setStatusInProgress("Downloading Audio...");
                            downloadProgress.setStepsFinished(downloadProgress.getStepsFinished() + 1, downloadProgress);
                        });
                        downloadAudio(strings, directory);
                        Platform.runLater(() -> downloadProgress.setStepsFinished(downloadProgress.getStepsFinished() + 1, downloadProgress));
                        if(isNewPlaylist){
                            String[] dirParts = directory.split("[/\\\\]"); // '/' or '\'
                            String playlistName = dirParts[dirParts.length - 1]; //Last index
                            List<File> songs = Arrays.stream(getPlaylist(playlistName))
                                    .filter(file -> file.getName().endsWith(".mp4") || file.getName().endsWith(".m4a") ||
                                            file.getName().endsWith(".mp3") || file.getName().endsWith(".wav") ||
                                            file.getName().endsWith(".opus") || file.getName().endsWith(".aac") ||
                                            file.getName().endsWith(".webm")).toList();
                            if(songs.isEmpty()){
                                LOGGER.warning("There are no songs to re-encode in this new playlist");
                                return;
                            }
                            //Indexes: 0 - Audio, 1 - Video
                            createSongDirectory(songs.get(1).getAbsolutePath());
                            encodeVideo(
                                    songs.get(1).getAbsolutePath(),
                                    downloadProgress
                            );
                            Platform.runLater(() -> downloadProgress.setStepsFinished(downloadProgress.getStepsFinished() + 1, downloadProgress));
//                            encodeAudio(songs.get(0).getAbsolutePath(), downloadProgress, null);
                            encodeAudio(songs.get(0).getAbsolutePath(), downloadProgress, BasicKeyValuePair.of(.0, .0), 0);
                            //Take a look next time
                        }else{
                            List<File> newSongs = Arrays.stream(getPlaylist(Player.SELECTED_PLAYLIST))
                                    .filter(file -> (file.getName().endsWith(".mp4") || file.getName().endsWith(".m4a")
                                            || file.getName().endsWith(".mp3") || file.getName().endsWith(".wav")
                                            || file.getName().endsWith(".opus") || file.getName().endsWith(".aac")
                                            || file.getName().endsWith(".webm")) && !currentPlaylist.contains(file))
                                    .toList();
                            if(newSongs.isEmpty()){
                                LOGGER.warning("There are no songs to re-encode in this current playlist.");
                                return;
                            }
                            createSongDirectory(newSongs.get(1).getAbsolutePath());
                            encodeVideo(
                                    newSongs.get(1).getAbsolutePath(),
                                    downloadProgress
                            );
                            Platform.runLater(() -> downloadProgress.setStepsFinished(downloadProgress.getStepsFinished() + 1, downloadProgress));
                            encodeAudio(newSongs.get(0).getAbsolutePath(), downloadProgress, BasicKeyValuePair.of(.0, .0), 0);
                        }
                        downloaded++;
                    }
                    else
                        LOGGER.severe("Something didn't work as expected. Aborted");
                }catch (IOException | InterruptedException exception){
                    exception.printStackTrace(System.err);
                }
            }
            Platform.runLater( () -> downloadProgress.setStatusInProgress("Tasks successfully finished."));
            Platform.runLater( () -> MainApp.instance.setApplicationState(AppState.NORMAL));
            LOGGER.info("Finished downloading "+downloaded+" song(s).");
            Utilities.sleep(Duration.millis(1500), 1, run -> MainApp.closeStage(downloadProgress.getStage(), Player.ANIMATIONS), null);
            Player.instance.refreshButton.setVisible(true);
            Player.instance.choosePlaylist.setVisible(true);
            Player.instance.searchSong.setVisible(true);
            Platform.runLater(() -> Player.instance.onRefreshAction());
            return;
        }).start();
    }
    private static void createSongDirectory(String songAbsolutePath){
        try {
            String updatedPath = FilesUtils.getFileNameUpdated(songAbsolutePath, false, ".mp4", ".m4a", ".mp3", ".webm", ".opus", "--Video", "--Audio");
            File file = new File(updatedPath);
            if(!file.exists())
                file.mkdir();
            else{
                while (true){
                    updatedPath = updatedPath.concat(" [1]");
                    file = new File(updatedPath);
                    if(file.exists())
                        continue;
                    file.mkdir();
                    break;
                }
            }
        }catch (Exception exception){
            exception.printStackTrace(System.err);
        }
    }
    private static void downloadAudio(String url, String directory){
//        String[] command = {
//                MainApp.APP_EXTERNAL_HELPERS + "\\downloader.exe",
//                "--no-keep-fragments", "-x", "-f", "bestaudio", "--audio-quality", "0",
//                "--audio-format", "opus",
//                "-o", "%(title)s--Audio.%(ext)s",
//                "-P", directory,
//                url
//        };
        String[] command = {
                MainApp.APP_EXTERNAL_HELPERS+"\\downloader.exe",
                "--no-keep-fragments", "-x", "-f", "bestaudio",
                "-o", "%(title)s--Audio.%(ext)s",
                "-P", directory, url
        };
        LOGGER.info("Downloading Sample Audio...");
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            DataStructures.PROCESSES.add(process);
            try (InputStream inputStream = process.getInputStream()){
                Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
                while (scanner.hasNextLine()){
                    LOGGER.fine(scanner.nextLine());
                }
            }catch (IOException exception){
                exception.printStackTrace(System.err);
            }
            int processCode = process.waitFor();
            if(processCode == 0){
                DataStructures.PROCESSES.remove(process);
                process.destroy();
            }
        }catch (Exception exception){
            exception.printStackTrace(System.err);
        }
    }

    private static void encodeVideo(String videoAbsPath, DownloadProgress downloadProgress){
        File video = new File(videoAbsPath);
        String fileName = FilesUtils.getFileNameUpdated(videoAbsPath, true);
        String outputName = videoAbsPath.replace(fileName, "Video.mp4");
        LOGGER.info("Encoding Video file...");
        Platform.runLater(() -> downloadProgress.setStatusInProgress("Encoding Video..."));
        try {
            String[] command = {
                    MainApp.APP_EXTERNAL_HELPERS+"\\ffmpeg.exe", "-i",
                    videoAbsPath, "-c:v", "libx264", outputName
            };
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            DataStructures.PROCESSES.add(process);
            try(InputStream inputStream = process.getInputStream()) {
                Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
                while (scanner.hasNextLine()){
                    LOGGER.fine(scanner.nextLine());
                }
            }
            int exitCode = process.waitFor();
            if(exitCode == 0){
                DataStructures.PROCESSES.remove(process);
                process.destroy();
                LOGGER.info("Removing original video file and replacing with final one...");
                String newPath = FilesUtils.getFileNameUpdated(videoAbsPath, false, ".mp4", ".webm", "--Video")+"\\"+"Video.mp4";
                video.delete();
                new File(outputName).renameTo(new File(newPath));
            }
        }catch (IOException | InterruptedException exception){
            exception.printStackTrace(System.err);
        }
    }

    private static String[] getAudioEncodingCommand(BasicKeyValuePair<Double, Double> volumeParams, int encodeFailRetry, String audioAbsolutePath, String outputName){
        //Histogram range = 18000 - 78000
        //Mean value threshold: -12.5
        double lostVolume = volumeParams.getKey();
        double hist_val = volumeParams.getValue();
        if(lostVolume > -12.5 && hist_val > 78000 && encodeFailRetry == 1){
            return new String[] {
                    MainApp.APP_EXTERNAL_HELPERS+"\\ffmpeg.exe", "-i", audioAbsolutePath, "-hide_banner", "-y",
                    "-af", "volume=0.88", outputName
            };
        }
        if(lostVolume == 0 && encodeFailRetry == 0){
            return new String[] {
                    MainApp.APP_EXTERNAL_HELPERS + "\\ffmpeg.exe", "-i", audioAbsolutePath, "-hide_banner", "-y", outputName
            };
        }
        if(lostVolume < -12.5 && encodeFailRetry > 0){
            return new String[] {
                    MainApp.APP_EXTERNAL_HELPERS+"\\ffmpeg.exe", "-i", audioAbsolutePath, "-hide_banner", "-y",
                    "-af", "volume="+ENCODE_FAIL_ADJUSTMENT[encodeFailRetry - 1], outputName
            };
        }
        return null;
    }

    private static void encodeAudio(String audioAbsolutePath, DownloadProgress downloadProgress, BasicKeyValuePair<Double, Double> volumeParams, int encodeFailRetry){
        if(encodeFailRetry > ENCODE_FAIL_ADJUSTMENT.length){
            LOGGER.severe("fails = "+encodeFailRetry+" exceeded maximum value "+ENCODE_FAIL_ADJUSTMENT.length);
            downloadProgress.setStatusInProgress("Encode fail retries exceeded the maximum limit.");
            return;
        }
        LOGGER.info("Encoding Audio file...");
        boolean b = volumeParams.getKey() != 0 || volumeParams.getValue() != 0;
        String statusInProgress = b ? "Retrying a different audio encode approach... (Retry "+encodeFailRetry+"/"+(ENCODE_FAIL_ADJUSTMENT.length)+")" : "Encoding Audio with basic settings...";
        Platform.runLater(() -> downloadProgress.setStatusInProgress(statusInProgress));
        File file = new File(audioAbsolutePath);
        String output = "Audio."+audioExt;
        try {
            String fileName = FilesUtils.getFileNameUpdated(audioAbsolutePath, true);
            String outputName = audioAbsolutePath.replace(fileName, output);
            String[] command = getAudioEncodingCommand(volumeParams, encodeFailRetry, audioAbsolutePath, outputName);
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            DataStructures.PROCESSES.add(process);
            try (InputStream inputStream = process.getInputStream()){
                Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
                while (scanner.hasNextLine()){
                    LOGGER.fine(scanner.nextLine());
                }
            }
            int exitCode = process.waitFor();
            if(exitCode == 0){
                DataStructures.PROCESSES.remove(process);
                process.destroy();
                LOGGER.info("Removing original file and replacing with final one...");
                String newPath = FilesUtils.getFileNameUpdated(audioAbsolutePath, false,
                        ".mp4", ".m4a", ".mp3", ".wav", ".ogg", ".opus", ".aac", ".webm", "--Audio")+"\\"+output;
                BasicKeyValuePair<Double, Double> mean_volume = getMeanAndHistogramValues(outputName);
                if(mean_volume.getKey() < -12.5 || (mean_volume.getValue() > 78000 && encodeFailRetry == 0)){
                    LOGGER.warning("Detected audio volume too low.. Retrying encode with a different approach... (Value="+mean_volume.getKey()+", Hist="+mean_volume.getValue()+")");
                    LOGGER.warning("Adjusting with (Volume = "+ENCODE_FAIL_ADJUSTMENT[encodeFailRetry]+"");
                    encodeAudio(audioAbsolutePath, downloadProgress, mean_volume, encodeFailRetry + 1);
                    return;
                }
                LOGGER.info("Encoding "+outputName+" with a MEAN_VOLUME of "+mean_volume.getKey()+", HIST_VALUE of "+mean_volume.getValue());
                file.delete();
                new File(outputName).renameTo(new File(newPath));
                Platform.runLater(() -> downloadProgress.setFilesDownloaded(downloadProgress.getDownloadedFiles() + 1, downloadProgress));
                Platform.runLater(() -> downloadProgress.setStepsFinished(downloadProgress.getStepsFinished() + 1, downloadProgress));
                LOGGER.info("[Download and Encode finished for "+FilesUtils.getFileNameUpdated(audioAbsolutePath, true,
                        ".opus", "mp4", "m4a", "mp3", ".webm", "--Audio")+".]");
            }else{
                LOGGER.severe("Something went wrong. Task has failed.");
            }
        }catch (Exception exception){
            exception.printStackTrace(System.err);
        }
    }

    /**
     * Key - Mean Value, Value - Histogram_0dB
     */
    private static BasicKeyValuePair<Double, Double> getMeanAndHistogramValues(String audioAbsolutePath){
        String[] command = {
                MainApp.APP_EXTERNAL_HELPERS+"\\ffmpeg.exe", "-hide_banner",
                "-i", audioAbsolutePath, "-af", "volumedetect",
                "-f", "null", "-"
        };
        BasicKeyValuePair<Double, Double> pair = BasicKeyValuePair.of(-1.0, -1.0);
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
                parser.subtractFullString("Parsed_volumedetect_0", ValueParser.SubtractionDirection.LOWER, true, true);
                parser.setRemovableParts("dB");
                pair.key(Double.parseDouble(parser.getValue("mean_volume")))
                    .value(Double.parseDouble(parser.getValue("histogram_0db")));
            }
            return pair;
        }catch (IOException | InterruptedException exception){
            exception.printStackTrace();
            return pair;
        }
    }
    public static boolean isFFmpegPresent(){
        return FilesUtils.fileExists(MainApp.APP_EXTERNAL_HELPERS+"\\ffmpeg.exe");
    }
    public static boolean isDownloaderPresent(){
        return FilesUtils.fileExists(MainApp.APP_EXTERNAL_HELPERS+"\\downloader.exe");
    }
    public static String getPlaylistPathByName(String string){
        return Playlists.PATH+"/"+string;
    }
}
