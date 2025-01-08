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
import javafx.util.Duration;
import me.eduard.musicplayer.AppState;
import me.eduard.musicplayer.Components.Player.VideoQualityType;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.Library.ApplicationWindow;
import me.eduard.musicplayer.Library.BasicKeyValuePair;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.*;
import me.eduard.musicplayer.Utils.PlaylistRelated.Playlists;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;

import java.net.URL;
import java.util.ResourceBundle;

public class AddSongs extends ApplicationWindow implements Initializable {

    @FXML private TextArea textArea;
    @FXML public Button proceedButton, cancelButton;
    @FXML public Label statusLabel, qualityLabel;
    @FXML public MenuItem quality0, quality1, quality2, quality3, quality4, quality5, quality6;

    private static VideoQualityType qualityType = VideoQualityType.LOW_MEDIUM;

    @SuppressWarnings("unused") @FXML private AnchorPane corePane;

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
                    ).bindAllStagesCloseKeyCombination().bindAllStagesCloseKeyCombination().addExitListenerWithEscape().finishBuilding();

            Stage stage = fxmlStageBuilder.getStage();

            MainApp.openStage(stage, true, true);

            AddSongs addSongs = fxmlStageBuilder.getFxmlLoader().getController();

            fxmlStageBuilder.addKeyEvents(addSongs.getEvents());

            addSongs.setStage(stage).title("Add songs to playlist '"+selectedPlaylist+"'").useDefaultFunctionalityPresets();

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
                Utilities.sleep(Duration.millis(300), 1, run ->
                    Playlists.downloadSongs(qualityType,Playlists.PATH+"/"+selectedPlaylist, false, addSongs.getLinks()),
            null);
                Utilities.sleep(Duration.seconds(1), 1, run -> MainApp.closeStage(stage, true), null);
            });
        }catch (IllegalStateException exception){
            ErrorHandler.launchWindow(exception);
        }
    }

    public BasicKeyValuePair<EventType<KeyEvent>, EventHandler<? super KeyEvent>>[] getEvents(){
        return BasicKeyValuePair.parseArray(
                new BasicKeyValuePair<EventType<KeyEvent>, EventHandler<? super KeyEvent>>(KeyEvent.KEY_PRESSED, event -> {
                    if(KeyCombinationUtils.isKey(KeyCode.SHIFT, 0) && KeyCombinationUtils.isKey(KeyCode.ENTER, 1)){
                        this.proceedButton.fire();
                    }
                }
            )
        );
    }

}
