package me.eduard.musicplayer.Components.ManagePlaylist;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import me.eduard.musicplayer.AppState;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.Components.ScreenLauncher;
import me.eduard.musicplayer.Utils.PlaylistRelated.ListeningMode;
import me.eduard.musicplayer.Utils.PlaylistRelated.VideoQualityType;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.Library.CustomComponents.WindowTitleBar;
import me.eduard.musicplayer.Library.SimplePair;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.*;
import me.eduard.musicplayer.Utils.PlaylistRelated.Playlists;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;

import java.net.URL;
import java.util.ResourceBundle;

public class AddSongs extends OutputUtilities implements Initializable {

    @FXML private TextArea textArea;
    @FXML public Button proceedButton, cancelButton;
    @FXML public Label statusLabel, qualityLabel;
    @FXML public MenuItem quality0, quality1, quality2, quality3, quality4, quality5;
    @FXML public MenuItem localDeviceMode, externalDeviceMode;
    @FXML public CheckBox audioOnly;

    private static final String settingsLabelText = "Video Quality: %quality%, Planning to listen on %target_device%";

    private static VideoQualityType qualityType = VideoQualityType.MEDIUM;
    private static ListeningMode listeningMode = ListeningMode.LOCAL_DESKTOP_DEVICE;

    @SuppressWarnings("unused") @FXML private AnchorPane corePane;

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
        return this.textArea.getText().trim().replace("\n", "").split(",");
    }
    public void setStatusLabel(String string, Level level){
        super.setOutputLabel(this.statusLabel, string, level);
    }
    public boolean hasLinks(){
        for(String strings : this.getLinks()){
            if(!strings.contains("https://") && (!strings.contains("youtu.be") || !strings.contains("www.youtube.com"))){
                return false;
            }
        }
        return true;
    }

    public static void launchWindow(String selectedPlaylist){
        try {
            FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("ManagePlaylists/AddSongs")
                    .withStageBuilder(StageBuilder.newBuilder()
                            .styleSheet("ManagePlaylists/AddSongs")
                            .title("Playlist Manager - Add Songs")
                            .removeUpperBar()
                            .icon("icons/icon.png")
                            .resizable(false)
                    )
                    .bindAllStagesCloseKeyCombination()
                    .bindAllStagesCloseKeyCombination()
                    .addExitListenerWithEscape()
                    .requireModality()
                    .finishBuilding();

            Stage stage = fxmlStageBuilder.getStage();

            WindowTitleBar titleBar = new WindowTitleBar(stage).useDefaultPresets()
                    .setTitleString("Add songs to '"+selectedPlaylist+"' playlist.")
                    .setTitleLabelStyling("-fx-text-fill: #000000")
                    .setOnClose(() -> MainApp.closeStage(stage, Player.ANIMATIONS))
                    .setButtonsArrangement(WindowTitleBar.ButtonsArrangement.CLOSE)
                    .setSupportStyling(MainApp.childWindowTitleBarTheme);
            titleBar.linkToStage();

            MainApp.openStage(stage, true, true);

            AddSongs addSongs = fxmlStageBuilder.getFxmlLoader().getController();

            GlobalAppStyle.applyToButtons(addSongs.cancelButton, addSongs.proceedButton);
            GlobalAppStyle.applyToCheckbox(addSongs.audioOnly);

            fxmlStageBuilder.addKeyEvents(addSongs.getEvents());

            addSongs.cancelButton.setOnAction(event -> MainApp.closeStage(stage, true));
            addSongs.proceedButton.setOnAction(event -> {
                if(!addSongs.hasLinks()){
                    addSongs.setStatusLabel("One or more provided links are not valid.",
                            OutputUtilities.Level.INCOMPLETE);
                    return;
                }
                addSongs.setStatusLabel("Looking for internet connection...", OutputUtilities.Level.INFORMATIVE);
                if(!Utilities.hasInternetConnection()){
                    addSongs.setStatusLabel("You are currently disconnected, please try again later.", OutputUtilities.Level.INCOMPLETE);
                    return;
                }
                if(!Playlists.isDownloaderPresent()){
                    addSongs.setStatusLabel("'downloader.exe' is missing. Task was cancelled.", Level.INCOMPLETE);
                    return;
                }
                if(!Playlists.isFFmpegPresent()){
                    addSongs.setStatusLabel("'ffmpeg.exe' is missing. Task was cancelled.", Level.INCOMPLETE);
                    return;
                }
                if(MainApp.instance.getApplicationState() == AppState.DOWNLOADING){
                    addSongs.setStatusLabel("Another download is in process. Try again once it's done.", Level.INCOMPLETE);
                    return;
                }
                addSongs.setStatusLabel("Updating your playlist... Please, be patient.", OutputUtilities.Level.SUCCESS);
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
                                if(addSongs.audioOnly.isSelected()){
                                    Playlists.downloadAudioOnly(listeningMode, Playlists.getPlaylistPathByName(selectedPlaylist), false, addSongs.getLinks());
                                }else{
                                    Playlists.downloadSongs(qualityType, listeningMode, Playlists.getPlaylistPathByName(selectedPlaylist), false, addSongs.getLinks());
                                }
                            }
                    );
                }else{
                    if(addSongs.audioOnly.isSelected()){
                        Playlists.downloadAudioOnly(listeningMode, Playlists.getPlaylistPathByName(selectedPlaylist), false, addSongs.getLinks());
                    }else{
                        Playlists.downloadSongs(qualityType, listeningMode, Playlists.getPlaylistPathByName(selectedPlaylist), false, addSongs.getLinks());
                    }                }
                MainApp.closeStage(stage, Player.ANIMATIONS);
            });
        }catch (IllegalStateException exception){
            ErrorHandler.launchWindow(exception);
        }
    }

    public SimplePair<EventType<KeyEvent>, EventHandler<? super KeyEvent>>[] getEvents(){
        return SimplePair.parseArray(
                new SimplePair<EventType<KeyEvent>, EventHandler<? super KeyEvent>>(KeyEvent.KEY_PRESSED, event -> {
                    if(KeyCombinationUtils.isKey(KeyCode.SHIFT, 0) && KeyCombinationUtils.isKey(KeyCode.ENTER, 1)){
                        this.proceedButton.fire();
                    }
                }
            )
        );
    }

}
