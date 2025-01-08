package me.eduard.musicplayer.Components;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.Components.Player.VideoQualityType;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.Library.ApplicationWindow;
import me.eduard.musicplayer.Library.BasicKeyValuePair;
import me.eduard.musicplayer.Library.Cache.Window.WindowRegistry;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.KeyCombinationUtils;
import me.eduard.musicplayer.Utils.PlaylistRelated.Playlists;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;
import me.eduard.musicplayer.Utils.Utilities;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class LinkDownloader extends ApplicationWindow implements Initializable {
    @FXML public AnchorPane corePane;
    @FXML public Label playListNameLabel;
    @FXML public TextArea linkArea;
    @FXML public Button createButton;
    @FXML public TextField playlistName;
    @FXML public Button cancelButton;
    @FXML public Label statusLabel, qualityLabel, titleLabel;
    @FXML public MenuItem quality0, quality1, quality2, quality3, quality4, quality5, quality6;

    private static VideoQualityType qualityType = VideoQualityType.LOW_MEDIUM;

    private static final List<String> BANNED_CHARACTERS = Arrays.asList("\\", "/", ":", "*", "?", "\"", "<", ">", "|");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.initializeMenuBar();
        this.setQualityTypeAndLabel(qualityType, qualityLabel);
    }

    @SuppressWarnings("DuplicatedCode")
    private void initializeMenuBar(){
        quality0.setOnAction(event -> this.setQualityTypeAndLabel(VideoQualityType.LOWEST, qualityLabel));
        quality1.setOnAction(event -> this.setQualityTypeAndLabel(VideoQualityType.LOW, qualityLabel));
        quality2.setOnAction(event -> this.setQualityTypeAndLabel(VideoQualityType.LOW_MEDIUM, qualityLabel));
        quality3.setOnAction(event -> this.setQualityTypeAndLabel(VideoQualityType.MEDIUM, qualityLabel));
        quality4.setOnAction(event -> this.setQualityTypeAndLabel(VideoQualityType.ABOVE_MEDIUM, qualityLabel));
        quality5.setOnAction(event -> this.setQualityTypeAndLabel(VideoQualityType.HIGH, qualityLabel));
        quality6.setOnAction(event -> this.setQualityTypeAndLabel(VideoQualityType.HIGHEST_POSSIBLE, qualityLabel));
    }
    private void setQualityTypeAndLabel(VideoQualityType videoQualityType, Label label){
        qualityType = videoQualityType;
        String basic = qualityType.getName()+" ("+qualityType.getQuality()+"p)";
        String highest = "Highest Possible";
        label.setText("Video Quality: "+(qualityType != VideoQualityType.HIGHEST_POSSIBLE ? basic : highest));
    }

    public String[] getLinks(){
        return this.linkArea.getText().trim().replace("\n", "").split(",");
    }
    public String getPlaylistName(){
        return this.playlistName.getText();
    }
    public void setStatusLabel(String string){
        this.statusLabel.setText(string);
    }
    @SuppressWarnings("all")
    public boolean hasLinks(){
        if(this.getLinks().length < 1){
            return false;
        }
        for(String strings : this.getLinks()){
            if(!strings.contains("https://") && (!strings.contains("youtu.be") || !strings.contains("www.youtube.com"))){
                return false;
            }
        }
        return true;
    }
    public BasicKeyValuePair<EventType<KeyEvent>, EventHandler<? super KeyEvent>>[] getEvents(TextField linkedArea){
        linkedArea.setOnKeyTyped(event -> {
            if(linkedArea.getText().length() > 256){
                String currentText = linkedArea.getText();
                linkedArea.setText(currentText.substring(0, 255));
                linkedArea.positionCaret(255);
                return;
            }
            int pos = linkedArea.getCaretPosition();
            if(BANNED_CHARACTERS.contains(event.getCharacter())){
                linkedArea.setText(linkedArea.getText().replace(event.getCharacter(), "_"));
                linkedArea.positionCaret(pos);
            }
        });
        return BasicKeyValuePair.parseArray(
                new BasicKeyValuePair<EventType<KeyEvent>, EventHandler<? super KeyEvent>>(KeyEvent.KEY_PRESSED, event -> {
                    if(KeyCombinationUtils.isKey(KeyCode.SHIFT, 0) && KeyCombinationUtils.isKey(KeyCode.ENTER, 1)){
                        this.createButton.fire();
                    }
                }
            )
        );
    }
    public static void launchWindow(boolean useCached){
        try {
            final String playlistPath = Playlists.PATH;
            final String downloaderPath = "%USERPROFILE%\\Desktop\\MusicPlayer\\Playlists\\";
            File file = new File(playlistPath);
            if(!file.exists() || !file.isDirectory()){
                return;
            }
            FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("LinkDownloader")
                    .withStageBuilder(StageBuilder.newBuilder()
                            .removeUpperBar()
                            .styleSheet("LinkDownloader")
                            .title("Create own playlist.")
                            .icon("icons/icon.png")
                            .resizable(false)
                    ).bindAllStagesCloseKeyCombination().addExitListenerWithEscape().finishBuilding();
            Stage stage = (useCached) ?
                    (WindowRegistry.isInRegistry("CREATE_PLAYLIST")) ?
                            WindowRegistry.getStage("CREATE_PLAYLIST") : WindowRegistry.getAndRegister("CREATE_PLAYLIST", fxmlStageBuilder.getStage())
                    : fxmlStageBuilder.getStage();

            MainApp.openStage(stage, Player.ANIMATIONS, true);

            LinkDownloader linkDownloader = fxmlStageBuilder.getFxmlLoader().getController();

            fxmlStageBuilder.addKeyEvents(linkDownloader.getEvents(linkDownloader.playlistName));

            linkDownloader.setStage(stage).title("Create own playlist.").useDefaultFunctionalityPresets();

            linkDownloader.xButton.setOnAction(event -> MainApp.closeStage(stage, true));
            linkDownloader.minimizeButton.setOnAction(event -> stage.setIconified(!stage.isIconified()));

            linkDownloader.cancelButton.setOnAction(event -> MainApp.closeStage(stage, true));
            linkDownloader.createButton.setOnAction(event -> {
                if(!linkDownloader.hasLinks()){
                    linkDownloader.setStatusLabel("You did not specify any valid link.");
                    return;
                }
                String[] links = linkDownloader.getLinks();

                String playListName = linkDownloader.getPlaylistName();
                if(playListName == null || playListName.isEmpty()){
                    linkDownloader.playlistName.requestFocus();
                    linkDownloader.setStatusLabel("You need to specify a playlist name.");
                    return;
                }

                File playListFolder = new File(playlistPath+"\\"+playListName);
                if(playListFolder.exists()){
                    linkDownloader.setStatusLabel("This playlist already exists. Try a different name.");
                    return;
                }
                linkDownloader.setStatusLabel("Looking for internet connection...");
                if(!Utilities.hasInternetConnection()){
                    linkDownloader.setStatusLabel("You are currently disconnected, try again later...");
                    return;
                }
                linkDownloader.linkArea.setText("");
                linkDownloader.playlistName.setText("");
                try {
                    linkDownloader.setStatusLabel("Your songs are being downloaded. Please be patient.");
                    if(!Playlists.isDownloaderPresent()){
                        linkDownloader.setStatusLabel("'downloader.exe' is missing. Task was cancelled.");
                        return;
                    }
                    if(!Playlists.isFFmpegPresent()){
                        linkDownloader.setStatusLabel("'ffmpeg.exe' is missing. Task was cancelled.");
                        return;
                    }
                    playListFolder.mkdir();
                    String directory = playlistPath+"\\"+playListName;
                    Utilities.sleep(Duration.millis(2000), 1, run -> {
                        Playlists.downloadSongs(qualityType, directory, true, links);
                    }, null);
                    Player.instance.noPlaylistsLabel.setVisible(false);
                    Player.instance.noPlaylistsLink.setVisible(false);
                    MainApp.closeStage(stage, true);
                }catch (Exception exception){
                    ErrorHandler.launchWindow(exception);
                }
            });
        }catch (IllegalStateException exception){
            ErrorHandler.launchWindow(exception);
        }
    }
}
