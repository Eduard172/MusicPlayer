package me.eduard.musicplayer.Components.ManagePlaylist;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.Library.ApplicationWindow;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.PlaylistRelated.Playlists;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("UnusedReturnValue")
public class PlaylistInfo extends ApplicationWindow {
    @FXML public Button okButton;
    @FXML private Label songsLinked, createdOnDate, playlistName;
    @SuppressWarnings("unused") @FXML private AnchorPane corePane;
    @SuppressWarnings("unused") @FXML private Line titleLine;

    private String selectedPlaylist;
    public PlaylistInfo setSelectedPlaylist(String selectedPlaylist){
        this.selectedPlaylist = selectedPlaylist;
        return this;
    }
    public String getSongsLinkedCount(){
        return String.valueOf(Playlists.getPlaylist(this.selectedPlaylist).length);
    }
    public String getCreationDate(){
        String path = Playlists.PATH+"/"+this.selectedPlaylist;
        try {
            FileTime creationTime = (FileTime) Files.getAttribute(Paths.get(path), "creationTime");
            LocalDateTime localDateTime = LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            return localDateTime.format(dateTimeFormatter);
        }catch (IOException exception){
            exception.printStackTrace(System.err);
        }
        return null;
    }
    public PlaylistInfo setSongsLinked(int songsLinked){
        super.setOutputLabel(this.songsLinked, "Songs linked to this playlist: "+songsLinked, Level.DEFAULT);
        return this;
    }
    public PlaylistInfo setCreationDate(String date){
        super.setOutputLabel(this.createdOnDate, "Created on date: "+date, Level.DEFAULT);
        return this;
    }
    public PlaylistInfo updatePlaylistName(){
        super.setOutputLabel(this.playlistName, this.selectedPlaylist, Level.DEFAULT);
        return this;
    }
    public static void launchWindow(String selectedPlaylist){
        try {
            FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("ManagePlaylists/PlaylistInfo")
                    .withStageBuilder(StageBuilder.newBuilder()
                            .styleSheet("ManagePlaylists/PlaylistInfo")
                            .icon("icons/icon.png")
                            .removeUpperBar()
                            .title("Playlist Manager - Playlist Info")
                    ).bindAllStagesCloseKeyCombination().addExitListenerWithEscape().finishBuilding();
            Stage stage = fxmlStageBuilder.getStage();

            MainApp.openStage(stage, true, true);
            PlaylistInfo playlistInfo = fxmlStageBuilder.getFxmlLoader().getController();

            playlistInfo.setStage(stage).title("Playlist information").updateTaskbarBehaviour();

            playlistInfo.okButton.setOnAction(event -> MainApp.closeStage(stage, true));
            playlistInfo
                    .setSelectedPlaylist(selectedPlaylist)
                    .updatePlaylistName()
                    .setSongsLinked(Integer.parseInt(playlistInfo.getSongsLinkedCount()))
                    .setCreationDate(playlistInfo.getCreationDate());
        }catch (IllegalStateException ex){
            ErrorHandler.launchWindow(ex);
        }
    }

}
