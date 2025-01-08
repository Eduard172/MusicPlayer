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
import me.eduard.musicplayer.Components.ManagePlaylist.AddSongs;
import me.eduard.musicplayer.Components.ManagePlaylist.MoveSongs;
import me.eduard.musicplayer.Components.ManagePlaylist.PlaylistInfo;
import me.eduard.musicplayer.Components.ManagePlaylist.RemoveSongs;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.Library.ApplicationWindow;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.Library.BasicKeyValuePair;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.KeyCombinationUtils;
import me.eduard.musicplayer.Utils.PlaylistRelated.Playlists;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;
import me.eduard.musicplayer.Utils.Utilities;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@SuppressWarnings("all")
public class ManagePlaylists extends ApplicationWindow implements Initializable {

    public enum Actions{
        ADD_SONGS, REMOVE_SONGS, PLAYLIST_INFO, DELETE_PLAYLIST, MOVE_SONGS, CLEANUP_PLAYLIST
    }

    private Actions currentAction = null;

    @FXML private ListView<String> listView;
    @FXML private MenuItem addSongs, removeSongs, playlistInfo, deletePlaylist, fixPlaylist, moveSongs, cleanupPlaylist;
    @FXML private Label currentlySelectedLabel, headerLabel, actionLabel, statusLabel;
    @FXML private AnchorPane corePane;
    @FXML public Menu menuBar;
    @FXML public Button proceedToActionButton, cancelButton;

    public ManagePlaylists setCurrentlySelectedPlaylist(String playlist){
        this.currentlySelectedLabel.setText("Currently selected: "+playlist);
        return this;
    }
    public ManagePlaylists setAmountOfPlaylists(int amount){
        this.headerLabel.setText("You have "+amount+" created playlists. Choose what to do with them.");
        return this;
    }
    public ManagePlaylists forceSetWindowHeader(String string){
        this.headerLabel.setText(string);
        return this;
    }
    public ManagePlaylists setActionTaken(String actionTaken){
        this.actionLabel.setText("Action taken: "+actionTaken);
        return this;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.initializeOptionsMenu();
        this.initializeListView();
    }
    public ManagePlaylists setAction(Actions actions){
        this.currentAction = actions;
        return this;
    }
    public ManagePlaylists setStatusLabel(String string, Level level, boolean animate){
        super.setOutputLabel(this.statusLabel, string, level, animate);
        return this;
    }
    public Actions getAction(){
        return this.currentAction;
    }
    private void initializeListView(){
        List<String> allPlaylists = Playlists.getPlaylists();
        this.listView.getItems().addAll(allPlaylists);
        this.listView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            this.setCurrentlySelectedPlaylist(newValue);
        });
        Utilities.sleep(Duration.millis(80), 1, run -> {
            this.listView.getSelectionModel().select(Player.SELECTED_PLAYLIST);
            this.setCurrentlySelectedPlaylist(Player.SELECTED_PLAYLIST);
        }, null);
    }
    public String getSelectedPlaylist(){
        return this.listView.getSelectionModel().getSelectedItem();
    }
    public int getSelectedIndex(){
        return this.listView.getSelectionModel().getSelectedIndex();
    }
    public ListView<String> getListView(){
        return this.listView;
    }
    private void initializeOptionsMenu(){
        this.addSongs.setOnAction(event -> {
            this.currentAction = Actions.ADD_SONGS;
            this.setActionTaken("Add Songs.");
        });
        this.removeSongs.setOnAction(event -> {
            this.currentAction = Actions.REMOVE_SONGS;
            this.setActionTaken("Remove Songs.");
        });
        this.playlistInfo.setOnAction(event -> {
            this.currentAction = Actions.PLAYLIST_INFO;
            this.setActionTaken("See Playlist Information.");
        });
        this.deletePlaylist.setOnAction(event -> {
            this.currentAction = Actions.DELETE_PLAYLIST;
            this.setActionTaken("Delete Playlist.");
        });
        this.moveSongs.setOnAction(event -> {
            this.currentAction = Actions.MOVE_SONGS;
            this.setActionTaken("Move songs from a playlist to another.");
        });
        this.cleanupPlaylist.setOnAction(event -> {
            this.currentAction = Actions.CLEANUP_PLAYLIST;
            this.setActionTaken("Remove files that do not consist of a workable media.");
        });
    }
    public void proceedToAction(Actions actions, String selectedPlaylist){
        if(actions == null || (selectedPlaylist == null || selectedPlaylist.isEmpty()) || this.listView.getItems().isEmpty()){
            this.setStatusLabel("Action or selected playlist is missing...", Level.INCOMPLETE, Player.ANIMATIONS);
            return;
        }
        switch (actions){
            case ADD_SONGS -> AddSongs.launchWindow(selectedPlaylist);
            case REMOVE_SONGS -> RemoveSongs.launchWindow(selectedPlaylist);
            case PLAYLIST_INFO -> PlaylistInfo.launchWindow(selectedPlaylist);
            case DELETE_PLAYLIST -> ScreenLauncher.launchDeletePlaylistScreen(selectedPlaylist);
            case MOVE_SONGS -> {
                if(Playlists.getPlaylists().size() < 2){
                    this.setStatusLabel("At least 2 playlists are required to access this feature.", Level.INCOMPLETE, Player.ANIMATIONS);
                    return;
                }
                MoveSongs.launchWindow(selectedPlaylist);
            }
            case CLEANUP_PLAYLIST -> ScreenLauncher.launchPlaylistCleanupScreen(selectedPlaylist);
        }
    }
    public BasicKeyValuePair<EventType<KeyEvent>, EventHandler<? super KeyEvent>>[] getEvents(){
        return new BasicKeyValuePair[]{
                new BasicKeyValuePair<EventType<KeyEvent>, EventHandler<? super KeyEvent>>(
                        KeyEvent.KEY_PRESSED, event -> {
                            KeyCombinationUtils.registerKey(event.getCode());
                            if(KeyCombinationUtils.isKey(KeyCode.SHIFT, 0) && KeyCombinationUtils.isKey(KeyCode.ENTER, 1)){
                                this.menuBar.show();
                                return;
                            }
                            if(event.getCode() == KeyCode.ENTER){
                                KeyCombinationUtils.removeKey(event.getCode());
                                if(this.currentAction == null){
                                    this.menuBar.show();
                                }else{
                                    this.proceedToActionButton.fire();
                                }
                            }
                        }
                ), new BasicKeyValuePair<EventType<KeyEvent>, EventHandler<? super KeyEvent>>(
                        KeyEvent.KEY_RELEASED, event -> KeyCombinationUtils.removeKey(event.getCode())
                )
        };
    }
    public static void launchWindow(){
        try {
            FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("ManagePlaylists/ManagePlaylists")
                    .withStageBuilder(StageBuilder.newBuilder()
                            .styleSheet("ManagePlaylists/MainMenu")
                            .icon("icons/icon.png")
                            .title("Playlist Manager")
                            .removeUpperBar()
                            .resizable(false)
                    ).bindAllStagesCloseKeyCombination().bindAllStagesCloseKeyCombination().addExitListenerWithEscape().finishBuilding();
            Stage stage = fxmlStageBuilder.getStage();

            MainApp.openStage(stage, Player.ANIMATIONS, true);

            ManagePlaylists managePlaylists = fxmlStageBuilder.getFxmlLoader().getController();
            managePlaylists.setStage(stage).title("Manage your playlists.").useDefaultFunctionalityPresets();
            managePlaylists.setAmountOfPlaylists(Playlists.getPlaylists().size());

            fxmlStageBuilder.addKeyEvents(managePlaylists.getEvents());

            if(managePlaylists.getListView().getItems().isEmpty()){
                managePlaylists.getListView().setVisible(false);
                managePlaylists.forceSetWindowHeader("You do not currently have any active playlists.");
            }else{
                managePlaylists.getListView().setVisible(true);
            }

            managePlaylists.setActionTaken(" !! [Not selected.]");
            managePlaylists.setCurrentlySelectedPlaylist(" !! [Not selected.]");

            managePlaylists.cancelButton.setOnAction(event -> MainApp.closeStage(stage, true));
            managePlaylists.proceedToActionButton.setOnAction(event ->
                    managePlaylists.proceedToAction(managePlaylists.getAction(), managePlaylists.getSelectedPlaylist()));
        }catch (IllegalStateException exception){
            ErrorHandler.launchWindow(exception);
        }
    }
}
