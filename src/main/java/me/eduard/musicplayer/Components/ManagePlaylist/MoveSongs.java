package me.eduard.musicplayer.Components.ManagePlaylist;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.Library.ApplicationWindow;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.FilesUtils;
import me.eduard.musicplayer.Utils.OutputUtilities;
import me.eduard.musicplayer.Utils.PlaylistRelated.Playlists;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;
import me.eduard.musicplayer.Utils.Utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class MoveSongs extends ApplicationWindow {
    @FXML private AnchorPane corePane;
    @FXML private ListView<String> fromPlaylist, toPlaylist, targetPlaylist;
    @FXML public Button startTransferButton, cancelButton;
    @FXML private Label fromLabel, toLabel, addFrom, addTo;
    @FXML public Label statusLabel;

    private int countFrom = 0;
    private int countTo = 0;
    private List<String> stringsFrom;
    private List<String> stringsTo;

    private String targetedPlaylist;

    public void setTo(String string){
        this.targetedPlaylist = string;
        this.toLabel.setText("To: "+string);
    }
    public void setFromCount(int count){
        this.countFrom = count;
        this.addFrom.setText("> "+count+" songs to add");
    }
    public void setToCount(int count){
        this.countTo = count;
        this.addTo.setText("> "+count+" songs to add");
    }
    public void setFrom(String string){
        this.fromLabel.setText("From: "+string);
    }
    public ListView<String> getToPlaylist(){
        return this.toPlaylist;
    }
    public ListView<String> getFromPlaylist(){
        return this.fromPlaylist;
    }
    public ListView<String> getTargetPlaylist(){
        return this.targetPlaylist;
    }
    public void initializeFunctionality(String selectedPlaylist){
        if(Playlists.getPlaylistCount() == 0){
            this.startTransferButton.setVisible(false);
            return;
        }
        this.stringsFrom = Arrays.asList(Playlists.getPlaylistStringRepresentation(selectedPlaylist));
        this.setFrom(selectedPlaylist);
        this.getFromPlaylist().getItems().addAll(Playlists.getPlaylistStringRepresentation(selectedPlaylist));
        List<String> playlists = Playlists.getPlaylists().stream().filter(string -> !string.equalsIgnoreCase(selectedPlaylist)).toList();
        this.getTargetPlaylist().getItems().addAll(playlists);
        this.getTargetPlaylist().getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if(this.getTargetPlaylist().getSelectionModel().getSelectedItem() == null){
                return;
            }
            Utilities.sleep(Duration.millis(50), 1, run -> {
                this.getTargetPlaylist().getSelectionModel().clearSelection();
                this.getToPlaylist().getItems().clear();
                this.getFromPlaylist().getItems().clear();
                this.setFromCount(0);
                this.setToCount(0);
                this.setTo(newValue);
                this.stringsTo = Arrays.asList(Playlists.getPlaylistStringRepresentation(this.getTargetedPlaylist()));
                this.getToPlaylist().getItems().addAll(Playlists.getPlaylistStringRepresentation(newValue));
                this.getFromPlaylist().getItems().addAll(Playlists.getPlaylistStringRepresentation(selectedPlaylist));
            }, null);
        });
        this.getFromPlaylist().getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if(this.getFromPlaylist().getSelectionModel().getSelectedItem() == null || this.getToPlaylist().getItems().isEmpty())
                return;
            Utilities.sleep(Duration.millis(50), 1, run -> {
                this.getFromPlaylist().getSelectionModel().clearSelection();
                if(this.getFromPlaylist().getItems().size() - 1 == 0){
                    this.setOutputLabel(this.statusLabel, "You cannot transfer all songs from this playlist.", OutputUtilities.Level.INCOMPLETE, true);
                    return;
                }
                this.getFromPlaylist().getItems().remove(newValue);
                this.getToPlaylist().getItems().add(newValue);
                if(!this.stringsTo.contains(newValue)){
                    this.setToCount(this.countTo + 1);
                }else{
                    this.setFromCount(this.countFrom - 1);
                }
            }, null);
        });
        this.getToPlaylist().getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if(this.getToPlaylist().getSelectionModel().getSelectedItem() == null || this.getFromPlaylist().getItems().isEmpty())
                return;
            Utilities.sleep(Duration.millis(50), 1, run -> {
                this.getToPlaylist().getSelectionModel().clearSelection();
                if(this.getToPlaylist().getItems().size() - 1 == 0){
                    this.setOutputLabel(this.statusLabel, "You cannot transfer all songs from this playlist.", OutputUtilities.Level.INCOMPLETE, true);
                    return;
                }
                this.getToPlaylist().getItems().remove(newValue);
                this.getFromPlaylist().getItems().add(newValue);
                if(!this.stringsFrom.contains(newValue)){
                    this.setFromCount(this.countFrom + 1);
                }else{
                    this.setToCount(this.countTo - 1);
                }
            }, null);
        });
    }
    @SuppressWarnings("all")
    public void startTransferOperation(String selectedPlaylist, Stage stage){
        if(this.countFrom == 0 && this.countTo == 0){
            this.setOutputLabel(this.statusLabel, "Transfer could not be done due to no changes made.", Level.INCOMPLETE, true);
            return;
        }

        File folderFrom = new File(Playlists.getPlaylistPathByName(selectedPlaylist));
        File folderTo = new File(Playlists.getPlaylistPathByName(this.getTargetedPlaylist()));

        System.out.println(folderFrom.getAbsolutePath());
        System.out.println(folderTo.getAbsolutePath());

        List<String> addToFrom = new ArrayList<>();
        List<String> addToTo = new ArrayList<>();
        List<String> everySelectedSong = new ArrayList<>();
        for(String strings : this.getFromPlaylist().getItems()){
            if(!stringsFrom.contains(strings) && stringsTo.contains(strings)){
                addToFrom.add(strings);
                everySelectedSong.add(strings);
            }
        }
        for(String strings : this.getToPlaylist().getItems()){
            if(!stringsTo.contains(strings) && stringsFrom.contains(strings)){
                addToTo.add(strings);
                everySelectedSong.add(strings);
            }
        }
        for(String strings : everySelectedSong){
            if(Utilities.isSongInUse(Player.instance.currentlyPlayingSong, strings)){
                this.setOutputLabel(this.statusLabel, "Transfer could not be done due to song being already in use by the Player.", Level.INCOMPLETE, true);
                return;
            }
        }
        File temp;
        for(String strings : addToFrom){
            temp = new File(folderTo+"/"+strings);
            if(temp.exists() && temp.isDirectory()){
                FilesUtils.moveDirectory(temp.getAbsolutePath(), folderFrom.getAbsolutePath());
            }
        }
        for(String strings : addToTo){
            temp = new File(folderFrom+"/"+strings);
            if(temp.exists() && temp.isDirectory()){
                FilesUtils.moveDirectory(temp.getAbsolutePath(), folderTo.getAbsolutePath());
            }
        }
        if(Playlists.getSelectedPlaylist().equals(selectedPlaylist)){
            Playlists.updatePlaylistContents(selectedPlaylist, Player.instance.listView);
        }else if(Playlists.getSelectedPlaylist().equals(this.getTargetedPlaylist())){
            Playlists.updatePlaylistContents(this.getTargetedPlaylist(), Player.instance.listView);
        }
        this.startTransferButton.setVisible(false);
        this.setOutputLabel(this.statusLabel, "Songs are being transferred. Auto-Closing in 2 seconds...", OutputUtilities.Level.SUCCESS, true);
        Utilities.sleep(Duration.seconds(2), 1, run -> MainApp.closeStage(stage, true), null);
    }
    public String getTargetedPlaylist(){
        return this.targetedPlaylist;
    }
    @SuppressWarnings("all")
    public static void launchWindow(String selectedPlaylist){
        try {
            FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("ManagePlaylists/MoveSongs")
                    .withStageBuilder(StageBuilder.newBuilder()
                            .resizable(false)
                            .title("Playlist Manager - Move Songs")
                            .icon("icons/icon.png")
                            .styleSheet("ApplicationWindow")
                            .removeUpperBar()
                    ).bindAllStagesCloseKeyCombination().addExitListenerWithEscape().finishBuilding();
            Stage stage = fxmlStageBuilder.getStage();

            MainApp.openStage(stage, true, true);

            MoveSongs moveSongs = fxmlStageBuilder.getFxmlLoader().getController();
            moveSongs.setStage(stage).title("Transfer songs from a playlist to another.").useDefaultFunctionalityPresets();

            moveSongs.initializeFunctionality(selectedPlaylist);
            moveSongs.cancelButton.setOnAction(event -> MainApp.closeStage(stage, true));

            moveSongs.startTransferButton.setOnAction(event -> moveSongs.startTransferOperation(selectedPlaylist, stage));

        }catch (IllegalStateException exception){
            ErrorHandler.launchWindow(exception);
        }
    }
}
