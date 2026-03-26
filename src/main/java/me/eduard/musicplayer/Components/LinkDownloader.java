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
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.Utils.PlaylistRelated.ListeningMode;
import me.eduard.musicplayer.Utils.PlaylistRelated.VideoQualityType;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.Library.CustomComponents.WindowTitleBar;
import me.eduard.musicplayer.Library.SimplePair;
import me.eduard.musicplayer.Library.Cache.Window.WindowRegistry;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.GlobalAppStyle;
import me.eduard.musicplayer.Utils.KeyCombinationUtils;
import me.eduard.musicplayer.Utils.OutputUtilities;
import me.eduard.musicplayer.Utils.PlaylistRelated.Playlists;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;
import me.eduard.musicplayer.Utils.Utilities;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class LinkDownloader extends OutputUtilities implements Initializable {
    @FXML public AnchorPane corePane;
    @FXML public Label playListNameLabel;
    @FXML public TextArea linkArea;
    @FXML public Button createButton;
    @FXML public TextField playlistName;
    @FXML public Button cancelButton;
    @FXML public Label statusLabel, qualityLabel, titleLabel;
    @FXML public MenuItem quality0, quality1, quality2, quality3, quality4, quality5;
    @FXML public MenuItem localDeviceMode, externalDeviceMode;
    @FXML public CheckBox audioOnly;

    private static VideoQualityType qualityType = VideoQualityType.MEDIUM;
    private static ListeningMode listeningMode = ListeningMode.LOCAL_DESKTOP_DEVICE;

    private static final String settingsLabelText = "Video Quality: %quality%, Planning to listen on %target_device%";

    private static final List<String> BANNED_CHARACTERS = Arrays.asList("\\", "/", ":", "*", "?", "\"", "<", ">", "|");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.initializeMenuBar();
        this.setQualityTypeAndLabel(qualityType, qualityLabel);
    }

    @SuppressWarnings("DuplicatedCode")
    private void initializeMenuBar(){

        this.audioOnly.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal){
                qualityLabel.setText("Planning to listen on "+listeningMode.getDescription());
            }else{
                this.setQualityTypeAndLabel(qualityType, qualityLabel);
            }
        });

        quality0.setOnAction(event -> this.setQualityTypeAndLabel(VideoQualityType.LOWEST, qualityLabel));
        quality1.setOnAction(event -> this.setQualityTypeAndLabel(VideoQualityType.LOW, qualityLabel));
        quality2.setOnAction(event -> this.setQualityTypeAndLabel(VideoQualityType.LOW_MEDIUM, qualityLabel));
        quality3.setOnAction(event -> this.setQualityTypeAndLabel(VideoQualityType.MEDIUM, qualityLabel));
        quality4.setOnAction(event -> this.setQualityTypeAndLabel(VideoQualityType.ABOVE_MEDIUM, qualityLabel));
        quality5.setOnAction(event -> this.setQualityTypeAndLabel(VideoQualityType.HIGH, qualityLabel));

        localDeviceMode.setOnAction(event -> this.setListeningMode(ListeningMode.LOCAL_DESKTOP_DEVICE, qualityLabel));
        externalDeviceMode.setOnAction(event -> this.setListeningMode(ListeningMode.EXTERNAL_DEVICE, qualityLabel));
    }
    private void setQualityTypeAndLabel(VideoQualityType videoQualityType, Label label){
        if(this.audioOnly.isSelected()){
            return;
        }
        qualityType = videoQualityType;
        String basic = qualityType.getName()+" ("+qualityType.getQuality()+"p)";
        label.setText(settingsLabelText
                .replace("%quality%", basic)
                .replace("%target_device%", listeningMode.getDescription())
        );
    }
    private void setListeningMode(ListeningMode mode, Label label){
        listeningMode = mode;
        if(this.audioOnly.isSelected()){
            label.setText("Planning to listen on "+listeningMode.getDescription());
            return;
        }
        String basicVideoQuality = qualityType.getName()+" ("+qualityType.getQuality()+"p)";
        label.setText(settingsLabelText
                .replace("%quality%", basicVideoQuality)
                .replace("%target_device%", listeningMode.getDescription())
        );
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
    public SimplePair<EventType<KeyEvent>, EventHandler<? super KeyEvent>>[] getEvents(TextField linkedArea){
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
        return SimplePair.parseArray(
                new SimplePair<EventType<KeyEvent>, EventHandler<? super KeyEvent>>(KeyEvent.KEY_PRESSED, event -> {
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
                    )
                    .bindAllStagesCloseKeyCombination()
                    .addExitListenerWithEscape()
                    .requireModality()
                    .finishBuilding();
            Stage stage = (useCached) ?
                    (WindowRegistry.isInRegistry("CREATE_PLAYLIST")) ?
                            WindowRegistry.getStage("CREATE_PLAYLIST") : WindowRegistry.getAndRegister("CREATE_PLAYLIST", fxmlStageBuilder.getStage())
                    : fxmlStageBuilder.getStage();

            WindowTitleBar titleBar = new WindowTitleBar(stage).useDefaultPresets()
                    .setTitleString("Create a new playlist")
                    .setTitleLabelStyling("-fx-text-fill: #000000")
                    .setOnClose(() -> MainApp.closeStage(stage, Player.ANIMATIONS))
                    .setButtonsArrangement(WindowTitleBar.ButtonsArrangement.CLOSE)
                    .setSupportStyling(MainApp.childWindowTitleBarTheme);
            titleBar.linkToStage();

            MainApp.openStage(stage, Player.ANIMATIONS, true);

            LinkDownloader linkDownloader = fxmlStageBuilder.getFxmlLoader().getController();

            GlobalAppStyle.applyToButtons(linkDownloader.cancelButton, linkDownloader.createButton);
            GlobalAppStyle.applyToCheckbox(linkDownloader.audioOnly);

            fxmlStageBuilder.addKeyEvents(linkDownloader.getEvents(linkDownloader.playlistName));

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
                    if(listeningMode == ListeningMode.EXTERNAL_DEVICE) {
                        ScreenLauncher.launchWarningMessageBox(
                                "Warning", "A few words about your configuration",
                                """
                                        You have selected to listen on an external device such as TV, Speakers or any other \
                                        device that's getting audio shared from your PC. It's highly recommended that you listen to \
                                        these medias only while sharing audio from this PC because listening locally may result in \
                                        distortions and / or clippings of the audio. However, you can still listen to them. \
                                        For an optimal listening experience, you can go to Settings and Enable "Adjusted" option from \
                                        "Sound Behaviour" category.
                                        
                                        To proceed to download process, click the 'OK' button at bottom right.
                                        """,
                                event1 ->{
                                    if(linkDownloader.audioOnly.isSelected()){
                                        Playlists.downloadAudioOnly(listeningMode, directory, true, links);
                                    }else{
                                        Playlists.downloadSongs(qualityType, listeningMode, directory, true, links);
                                    }
                                }
                        );
                    }else{
                        if(linkDownloader.audioOnly.isSelected()){
                            Playlists.downloadAudioOnly(listeningMode, directory, true, links);
                        }else{
                            Playlists.downloadSongs(qualityType, listeningMode, directory, true, links);
                        }
                    }
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
