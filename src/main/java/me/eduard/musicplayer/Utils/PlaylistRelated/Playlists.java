package me.eduard.musicplayer.Utils.PlaylistRelated;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.util.Duration;
import me.eduard.musicplayer.AppState;
import me.eduard.musicplayer.Components.DownloadProgress;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.Components.Player.PlayerSettings;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.Library.*;
import me.eduard.musicplayer.Library.Cache.PlaylistCache;
import me.eduard.musicplayer.Library.Exceptions.OperationFailedException;
import me.eduard.musicplayer.Library.FFmpeg.NormalizationHelper;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.DataStructures;
import me.eduard.musicplayer.Utils.FilesUtils;
import me.eduard.musicplayer.Utils.Logging.LoggerFormatter;
import me.eduard.musicplayer.Utils.Logging.LoggerHandler;
import me.eduard.musicplayer.Utils.Settings;
import me.eduard.musicplayer.Utils.Utilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Future;
import java.util.logging.Logger;

@SuppressWarnings("all")
public final class Playlists {

    private static final Logger LOGGER = Logger.getLogger("Playlist-Manager");

    static {
        LoggerHandler handler = new LoggerHandler();
        handler.setFormatter(new LoggerFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
    }

    public static final String PATH = MainApp.MAIN_APP_PATH+"/Playlists";
    public static final String audioExt = "wav";
    public static final String videoExt = "mp4";
    public static final String thumbnailExt = "jpg";

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
        try {
            Files.createDirectory(Paths.get(PATH));
        }catch (IOException e){
            e.printStackTrace(System.err);
        }
    }

    public static String getSelectedPlaylist(){
        return Player.SELECTED_PLAYLIST;
    }
    public static String[] getPlaylistStringRepresentation(String playlistName){
        File[] filesList = getPlaylist(playlistName);
        return Arrays.stream(filesList).filter(file -> {
                    if(file.isDirectory()){
                        File[] files = file.listFiles();
                        Arrays.sort(files); // 1 -> Audio.?ext
                                            // 2 -> Data.txt
                                            // 3 -> Video.?ext
                        String[] names = null;
                        int length = files.length;
                        if(length == 1) {
                            names = new String[]{"Audio." + audioExt};
                        }else if(length == 2){
                            names = files[1].getName().contains("Thumbnail") ?
                                    new String[] {"Audio."+audioExt, "Thumbnail."+thumbnailExt} :
                                    new String[] {"Audio."+audioExt, "Video."+videoExt};
                        }else if(length == 3){
                            names = new String[] {"Audio."+audioExt, "Thumbnail."+thumbnailExt, "Video."+videoExt};
                        }
                        boolean isValid = true;
                        assert names != null;
                        for(int i = 0; i < length; i++) {
                            if(!files[i].getName().equals(names[i])){
                                isValid = false;
                            }
                        }
                        if(!isValid){
                            LOGGER.warning("Invalid playlist format: Length = "+length);
                        }
                        return (length == 1 || length == 2 || length == 3) && isValid;
                    }
                    return false;
                }
        ).map(file -> Utilities.correctSongName(file, false)).toArray(String[]::new);
    }

    public static int getPlaylistLength() {
        return getPlaylistStringRepresentation(getSelectedPlaylist()).length;
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

    public static int getPlaylistsCount(){
        return getPlaylists().size();
    }
    public static String[] convertToValidVideoLinks(String urls){
        return urls.trim().replace("\n", "").split(",");
    }
    public static String getSongAbsPath(String currentlyPlayingSong) {
        return getPlaylistPathByName(Player.SELECTED_PLAYLIST)+"\\"+currentlyPlayingSong;
    }
    public static void downloadSongs(VideoQualityType qualityType, ListeningMode mode, String directory, boolean isNewPlaylist, String... url){
        LOGGER.info("Preparing to download "+url.length+" URLs...");
        AppMode.set(AppState.DOWNLOADING);
        DownloadProgress downloadProgress = DownloadProgress.launchWindow();
        Future<?> future = MainApp.executorService.submit(() -> {
            Platform.runLater(() -> {
                downloadProgress.setTotalSteps(url.length * 4, downloadProgress);
                downloadProgress.setTotalFilesToDownload(url.length, downloadProgress);
                downloadProgress.setCurrentlyEncoding(null);
            });

            List<File> currentPlaylist = Arrays.asList(getPlaylist(Player.SELECTED_PLAYLIST));
            final WrappedValue<Integer> downloaded = WrappedValue.of(0);
            downloaded.setOnValueChange(val -> {
                if(val == url.length){
                    Platform.runLater( () -> {
                        AppMode.set(AppState.NORMAL);
                        downloadProgress.cancelButton.setVisible(false);
                        downloadProgress.setStatusInProgress("Nothing for now.");
                        Utilities.sleep(Duration.millis(1500), 1, run -> MainApp.closeStage(downloadProgress.getStage(), Player.ANIMATIONS), null);
                        Player.instance.onRefreshAction();
                    });
                    WindowsCommands.launchPowershellNotification(downloaded.get() == 1 ? "Your song is ready." : "Your songs are ready", downloaded+" song(s) have been downloaded.");
                    LOGGER.info("Finished downloading "+downloaded.get()+" song(s).");
                }
            });
            for(String strings : url){
                LOGGER.info("Downloading URL: "+strings);
                Platform.runLater(() -> downloadProgress.setStatusInProgress("Trying to download video stream... (Format "+qualityType.getFormat()+")"));
                String[] command = {
                        MainApp.APP_EXTERNAL_HELPERS + "\\downloader.exe",
                        "-f", String.valueOf(qualityType.getFormat()),
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
                        LOGGER.info("Downloading Sample Video... ("+qualityType.getQuality()+"p)");
                        Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
                        while (scanner.hasNextLine()){
                            LOGGER.fine(scanner.nextLine());
                        }
                    }catch (IOException exception){
                        exception.printStackTrace(System.err);
                    }
                    int exitCode = process.waitFor();
                    if(exitCode == 0){
                        process.destroy();
                        DataStructures.PROCESSES.remove(process);
                        Platform.runLater(() -> {
                            downloadProgress.setStatusInProgress("Encoding Video...");
                            downloadProgress.setStepsFinished(downloadProgress.getStepsFinished() + 1, downloadProgress);
                        });

                        if(isNewPlaylist){
                            String[] dirParts = directory.split("[/\\\\]"); // '/' or '\'
                            String playlistName = dirParts[dirParts.length - 1]; //Last index
                            List<File> songs = Arrays.stream(getPlaylist(playlistName))
                                    .filter(file -> !file.isDirectory() && (file.getName().endsWith(".mp4") || file.getName().endsWith(".m4a") ||
                                            file.getName().endsWith(".mp3") || file.getName().endsWith(".wav") ||
                                            file.getName().endsWith(".opus") || file.getName().endsWith(".aac") ||
                                            file.getName().endsWith(".webm"))).toList();
                            if(songs.isEmpty()){
                                AppMode.set(AppState.NORMAL);
                                LOGGER.warning("There are no songs to re-encode in this new playlist");
                                return;
                            }
                            createSongDirectory(songs.get(0).getAbsolutePath(), downloadProgress);
                            encodeVideo(
                                    songs.get(0).getAbsolutePath(),
                                    downloadProgress, qualityType
                            );
                            Platform.runLater(() -> {
                                downloadProgress.setStepsFinished(downloadProgress.getStepsFinished() + 1, downloadProgress);
                                downloadProgress.setStatusInProgress("Trying to download audio stream... (Format 251)");
                            });
                            downloadAudio(strings, directory);
                            Platform.runLater(() -> downloadProgress.setStepsFinished(downloadProgress.getStepsFinished() + 1, downloadProgress));
                            File audio = getSongFileToEncode(FilesUtils.getFileNameUpdated(directory, true));
                            String newAudioPath = FilesUtils.getBelongingDirectory(audio.getAbsolutePath()).concat("\\Audio").concat(FilesUtils.getFileExtension(audio));
                            audio.renameTo(new File(newAudioPath));
                            encodeAudio(
                                    newAudioPath, audio.getAbsolutePath(),
                                    downloadProgress, mode
                            );
                            File thumbnail = getThumbnail(FilesUtils.getFileNameUpdated(directory, true));
                            processThumbnail(
                                    thumbnail.getAbsolutePath(),
                                    downloadProgress
                            );
                        }else{
                            File video = getSongFileToEncode(FilesUtils.getFileNameUpdated(directory, true));
                            if(video == null){
                                AppMode.set(AppState.NORMAL);
                                LOGGER.warning("There are no songs to re-encode in this current playlist.");
                                return;
                            }
                            try {
                                createSongDirectory(video.getAbsolutePath(), downloadProgress);
                                encodeVideo(
                                        video.getAbsolutePath(),
                                        downloadProgress, qualityType
                                );
                            }catch (ArrayIndexOutOfBoundsException exception){
                                AppMode.set(AppState.NORMAL);
                                exception.printStackTrace(System.err);
                            }
                            Platform.runLater(() -> {
                                downloadProgress.setStepsFinished(downloadProgress.getStepsFinished() + 1, downloadProgress);
                                downloadProgress.setStatusInProgress("Trying to download audio stream... (Format 251)");
                            });
                            downloadAudio(strings, directory);
                            File audio = getSongFileToEncode(FilesUtils.getFileNameUpdated(directory, true));
                            String newAudioPath = FilesUtils.getBelongingDirectory(audio.getAbsolutePath()).concat("\\Audio").concat(FilesUtils.getFileExtension(audio));
                            audio.renameTo(new File(newAudioPath));
                            Platform.runLater(() -> downloadProgress.setStepsFinished(downloadProgress.getStepsFinished() + 1, downloadProgress));
                            encodeAudio(
                                    newAudioPath, audio.getAbsolutePath(),
                                    downloadProgress, mode
                            );
                            File thumbnail = getThumbnail(FilesUtils.getFileNameUpdated(directory, true));
                            processThumbnail(
                                    thumbnail.getAbsolutePath(),
                                    downloadProgress
                            );
                        }
                        Platform.runLater(() -> downloadProgress.setCurrentlyEncoding(null));
                        downloaded.set(downloaded.get() + 1);
                    }
                    else{
                        LOGGER.severe("Something didn't work as expected. Aborted");
                        handleBadExitCode(downloadProgress);
                    }
                }catch (IOException | InterruptedException exception){
                    exception.printStackTrace(System.err);
                    AppMode.set(AppState.NORMAL);
                }
            }
        });
    }
    public static File getSongFileToEncode(String playlistName) {
        return Arrays.stream(getPlaylist(playlistName)).filter(file -> {
            return !file.isDirectory() && (file.getName().endsWith(".mp4") || file.getName().endsWith(".m4a")
                    || file.getName().endsWith(".opus")
                    || file.getName().endsWith(".webm")
            );
        }).findFirst().orElseThrow();
    }
    public static File getThumbnail(String playlistName) {
        return Arrays.stream(getPlaylist(playlistName)).filter(file -> {
            return !file.isDirectory() && (file.getName().endsWith(".png") || file.getName().endsWith(".jpg")
                    || file.getName().endsWith(".gif")
                    || file.getName().endsWith(".webp")
            );
        }).findFirst().orElseThrow();
    }
    public static void downloadAudioOnly(ListeningMode mode, String directory, boolean isNewPlaylist, String... urls){
        LOGGER.info("Downloading "+urls.length+" URLs...");
        AppMode.set(AppState.DOWNLOADING);
        DownloadProgress downloadProgress = DownloadProgress.launchWindow();
        ThreadHelper helper = new ThreadHelper();
        helper.runAsNewThread(() -> {
            Platform.runLater(() -> {
                downloadProgress.setTotalSteps(urls.length * 2, downloadProgress);
                downloadProgress.setTotalFilesToDownload(urls.length, downloadProgress);
                downloadProgress.setCurrentlyEncoding(null);
            });
            List<File> currentPlaylist = Arrays.asList(getPlaylist(FilesUtils.getFileNameUpdated(directory, true)));
            int downloaded = 0;
            for(String url : urls) {
                Platform.runLater(() -> downloadProgress.setStatusInProgress("Downloading Audio... (Format 251)"));
                downloadAudio(url, directory);
                File audio = getSongFileToEncode(FilesUtils.getFileNameUpdated(directory, true));
                if (audio == null && isNewPlaylist) {
                    LOGGER.warning("There are no songs to re-encode in this new playlist");
                    LOGGER.severe("Something didn't work as expected. Aborted");
                    handleBadExitCode(downloadProgress);
                } else if (audio == null && !isNewPlaylist) {
                    LOGGER.warning("There are no songs to re-encode in this current playlist.");
                    LOGGER.severe("Something didn't work as expected. Aborted");
                    handleBadExitCode(downloadProgress);
                } else {
                    createSongDirectory(audio.getAbsolutePath(), downloadProgress);
                    //File extension also returns the '.', example: '.opus'
                    String newAudioPath = FilesUtils.getBelongingDirectory(audio.getAbsolutePath()).concat("\\Audio").concat(FilesUtils.getFileExtension(audio));
                    audio.renameTo(new File(newAudioPath));
                    Platform.runLater(() -> downloadProgress.setStepsFinished(downloadProgress.getStepsFinished() + 1, downloadProgress));
                    encodeAudio(
                            newAudioPath, audio.getAbsolutePath(),
                            downloadProgress, mode
                    );
                    File thumbnail = getThumbnail(FilesUtils.getFileNameUpdated(directory, true));
                    processThumbnail(
                            thumbnail.getAbsolutePath(),
                            downloadProgress
                    );
                }
                downloaded++;
            }
            Platform.runLater( () -> {
                AppMode.set(AppState.NORMAL);
                downloadProgress.cancelButton.setVisible(false);
                downloadProgress.setStatusInProgress("Nothing for now.");
                Utilities.sleep(Duration.millis(1500), 1, run -> MainApp.closeStage(downloadProgress.getStage(), Player.ANIMATIONS), null);
                Player.instance.onRefreshAction();
            });
            if(Player.instance.fullScreenMode.isFullScreen){
                return;
            }
            WindowsCommands.launchPowershellNotification(downloaded == 1 ? "Your song is ready." : "Your songs are ready", downloaded+" song(s) have been downloaded.");
        });
    }
    private static void createSongDirectory(String songAbsolutePath, DownloadProgress downloadProgress){
        try {
            String updatedPath = FilesUtils.getFileNameUpdated(songAbsolutePath, false, ".mp4", ".m4a", ".mp3", ".webm", ".opus", "--Video", "--Audio");
            File file = new File(updatedPath);
            if(file.exists()){
                LOGGER.info("Song directory already exists... Removing it's contents...");
                FilesUtils.removeDirectoryFiles(file.getAbsolutePath(), false);
            }
            if(!file.exists())
                file.mkdir();
            Platform.runLater(() -> {
                String toEncode = Utilities.correctSongName(FilesUtils.getFileNameUpdated(updatedPath, true), true);
                downloadProgress.setCurrentlyEncoding(toEncode);
            });
        }catch (Exception exception){
            exception.printStackTrace(System.err);
        }
    }
    public static void selectPlaylist(String playlist){
        Settings settings = Settings.of("settings.yml");
        settings.saveSetting("selected-playlist", playlist);
        File[] songsList = getPlaylist(playlist);
        assert songsList != null;
        List<String> stringRepr = Arrays.asList(getPlaylistStringRepresentation(playlist));
        if(stringRepr.isEmpty()){
            return;
        }
        Player player = Player.instance;
        if(player != null){
            DataStructures.cleanupTimelines(DataStructures.TEXT_ANIMATIONS, true);
            player.currentPlaylistLabel.setText("Playlist: "+playlist);
            PlayerSettings.SELECTED_PLAYLIST = playlist;
            player.replaceSongs(stringRepr);
            player.initializeListView(playlist);
            player.listView.getSelectionModel().select(0);
        }

    }
    private static void downloadAudio(String url, String directory){
        String[] command = {
                MainApp.APP_EXTERNAL_HELPERS+"\\downloader.exe", "-x", "-f", "251", "--write-thumbnail",
                "-o", "%(title)s--Audio.%(ext)s",
                "-P", directory, url
        };
        LOGGER.info("Downloading Sample Audio... (opus 251)");
        ProcessHelper.executeIndependent(false, true, command);
    }

    private static void encodeVideo(String videoAbsPath, DownloadProgress downloadProgress, VideoQualityType qualityType){
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
                process.destroy();
                DataStructures.PROCESSES.remove(process);
                LOGGER.info("Removing original video file and replacing with final one...");
                String newPath = FilesUtils.getFileNameUpdated(videoAbsPath, false, ".mp4", ".webm", "--Video")+"\\"+"Video.mp4";
                video.delete();
                new File(outputName).renameTo(new File(newPath));
            }
        }catch (IOException | InterruptedException exception){
            AppMode.set(AppState.NORMAL);
            exception.printStackTrace(System.err);
        }
    }

    private static void encodeAudio(String audioAbsolutePath, String originalAudioPath, DownloadProgress downloadProgress, ListeningMode listeningMode){
        LOGGER.info("Encoding Audio file...");
        Platform.runLater(() -> downloadProgress.setStatusInProgress("Scanning audio for I/O loudness data..."));
        File file = new File(audioAbsolutePath);
        String output = "Audio."+audioExt;
        String fileName = FilesUtils.getFileNameUpdated(originalAudioPath, true);
        String outputName = originalAudioPath.replace(fileName, output);
        String newPath = FilesUtils.getFileNameUpdated(originalAudioPath, false,
                ".mp4", ".m4a", ".opus", ".webm", "--Audio")+"\\"+output;
        System.out.println(newPath);
        NormalizationHelper normHelper = NormalizationHelper.forFile(audioAbsolutePath).generateValues();
        Platform.runLater(() -> downloadProgress.setStatusInProgress("Normalizing audio input..."));
        normHelper.normalizeAudio(outputName, listeningMode, false);

        file.delete();
        new File(outputName).renameTo(new File(newPath));
        System.out.println(file.getAbsolutePath()+"\n"+outputName+"\n"+newPath);
        Platform.runLater(() -> downloadProgress.setFilesDownloaded(downloadProgress.getDownloadedFiles() + 1, downloadProgress));
        Platform.runLater(() -> downloadProgress.setStepsFinished(downloadProgress.getStepsFinished() + 1, downloadProgress));
        LOGGER.info("[Normalization finished for "+FilesUtils.getFileNameUpdated(audioAbsolutePath, true,
                ".opus", "mp4", "m4a", ".webm", "--Audio")+".]");
    }

    private static void processThumbnail(String absPath, DownloadProgress downloadProgress) {
        Platform.runLater(() -> downloadProgress.setStatusInProgress("Processing thumbnail..."));
        String output = "Thumbnail."+thumbnailExt;
        String name = FilesUtils.getFileNameUpdated(absPath, true);
        String truncatedName = Utilities.removeStringParts(name, "--Audio", ".webp", ".jpg", ".png", ".gif");
        String baseNewPath = FilesUtils.getFileNameUpdated(absPath, false, name)
                .concat(truncatedName)
                .concat("\\")
                .concat(output);
        LOGGER.warning("Base Path = "+baseNewPath);
        String[] command = {
                MainApp.FFMPEG, "-i", absPath, baseNewPath
        };
        ProcessHelper.executeIndependent(false, false, command);
        LOGGER.warning("Processed thumbnail.");
        new File(absPath).delete();
        FilesUtils.removeNonPlaylistFiles(Player.SELECTED_PLAYLIST);
    }

    private static void handleBadExitCode(DownloadProgress downloadProgress){
        Platform.runLater(() -> {
            AppMode.set(AppState.NORMAL);
            downloadProgress.close(downloadProgress);
            Player player = Player.instance;
            ErrorHandler.launchWindow(new OperationFailedException("""
                    Download process has not been completed as expected and failed. This may be caused by an invalid \
                    video quality format or outdated downloader version. Please update the downloader through Settings \
                    or choose another video quality and try again.
                    """));
            player.onRefreshAction();
            FilesUtils.removeNonPlaylistFiles(Player.SELECTED_PLAYLIST);
        });
        LOGGER.severe("Something went wrong. Task has failed. Remaining junk files have been cleaned up.");
    }

    public static int getNonPlaylistFiles(String playlist){
        return Arrays
                .stream(Objects.requireNonNull(FilesUtils.getDirectoryFiles(getPlaylistPathByName(playlist))))
                .map(File::new)
                .filter(f -> {
                    File[] ff = f.listFiles();
                    if(ff == null)
                        return false;
                    return !f.isDirectory() || ff.length != 2 || (ff.length == 2 && (!ff[0].getName().endsWith(audioExt) || !ff[1].getName().endsWith(videoExt)));
                }).toList().size();
    }
    public static boolean isFFmpegPresent(){
        return FilesUtils.fileExists(MainApp.APP_EXTERNAL_HELPERS+"\\ffmpeg.exe");
    }
    private static String removeIllegalChars(String s){
        return s.replace("\uFF5C", String.valueOf((char) 124));
    }
    public static boolean isDownloaderPresent(){
        return FilesUtils.fileExists(MainApp.APP_EXTERNAL_HELPERS+"\\downloader.exe");
    }
    public static String getPlaylistPathByName(String string){
        return Playlists.PATH+"/"+string;
    }
}
