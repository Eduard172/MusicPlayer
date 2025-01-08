package me.eduard.musicplayer.Components.ManagePlaylist;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.Library.ApplicationWindow;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.*;
import me.eduard.musicplayer.Utils.Logging.LoggerFormatter;
import me.eduard.musicplayer.Utils.Logging.LoggerHandler;
import me.eduard.musicplayer.Utils.PlaylistRelated.Playlists;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings("UnusedReturnValue")
public class RemoveSongs extends ApplicationWindow {

    private final Settings settings = Settings.of("settings.yml");
    private static final List<String> selectedSongs = new ArrayList<>();

    private static final Logger LOGGER = Logger.getLogger("Playlist-Media-Remover");

    static {
        LoggerHandler handler = new LoggerHandler();
        handler.setFormatter(new LoggerFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
    }

    @FXML private ListView<String> listView;
    @FXML public Button cancelButton, proceedButton, removeButton;
    @FXML private Label statusLabel, plannedToRemoveLabel, headerLabel;
    @FXML private CheckBox instantRemove;

    private int itemsEvidence = 0;
    private int listViewSize = 0;

    public ListView<String> getListView() {
        return this.listView;
    }
    public RemoveSongs setStatusLabel(String string, Level level){
        super.setOutputLabel(this.statusLabel, string, level);
        return this;
    }
    public RemoveSongs setPlannedToRemoveLabel(String string, Level level){
        super.setOutputLabel(this.plannedToRemoveLabel, string, level);
        return this;
    }
    public RemoveSongs updateItemEvidence(int newValue){
        this.itemsEvidence = newValue;
        super.setOutputLabel(this.statusLabel,
                "Planned to remove "+(newValue == this.listViewSize ? "all ("+newValue+")" : newValue)+" items.",
                (newValue < 1) ? Level.INCOMPLETE : Level.INFORMATIVE);
        return this;
    }
    public RemoveSongs setRemainingSongs(int songs){
        if(songs == 0){
            this.instantRemove.setVisible(false);
        }
        super.setOutputLabel(this.headerLabel,
                "Remove songs from playlist. (Remaining: "+(songs == 0 ? "None" : songs)+")",
                Level.DEFAULT);
        return this;
    }
    public String getSelectedItem(){
        return this.getListView().getSelectionModel().getSelectedItem();
    }
    public int getSelectedIndex(){
        return this.getListView().getSelectionModel().getSelectedIndex();
    }

    public void initializeListView(String selectedPlaylist){

        this.loadSettings();
        this.initializeComponents();

        this.setRemainingSongs(Playlists.getPlaylist(selectedPlaylist).length);
        this.setPlannedToRemoveLabel("", Level.INCOMPLETE);
        this.setStatusLabel("", Level.INCOMPLETE);
        this.getListView().getItems().addAll(Playlists.getPlaylistStringRepresentation(selectedPlaylist));
        this.listViewSize = this.getListView().getItems().size();

        this.getListView().getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            int selectedIndex = this.getSelectedIndex();
            if(this.getSelectedItem() == null){
                return;
            }
            if(this.instantRemove.isSelected()){
                Utilities.sleep(Duration.millis(50), 1, run -> {
                    selectedSongs.add(listView.getItems().get(selectedIndex));
                    this.getListView().getSelectionModel().clearSelection();
                    this.setPlannedToRemoveLabel("", Level.INCOMPLETE);
                    if(!this.getListView().getItems().isEmpty())
                        this.getListView().getItems().remove(selectedIndex);
                    this.itemsEvidence++;
                    this.updateItemEvidence(this.itemsEvidence);
                    this.setRemainingSongs(this.listView.getItems().size());
                }, null);
            }else{
                this.setStatusLabel("", Level.INCOMPLETE);
                this.setPlannedToRemoveLabel("Planned song to remove: "+newValue, Level.INFORMATIVE);
            }
        });
    }
    private void loadSettings(){
        this.instantRemove.setSelected(
                Boolean.parseBoolean(settings.getSettingValue("manage-instant-remove", false))
        );
    }
    private void initializeComponents(){
        this.instantRemove.setOnAction(event -> settings.saveSetting("manage-instant-remove",
                this.instantRemove.isSelected())
        );
    }
    public int getItemsEvidence(){
        return this.itemsEvidence;
    }
    public int getListViewSize(){
        return this.listViewSize;
    }
    @SuppressWarnings("all")
    public static void launchWindow(String selectedPlaylist){
        try {
            LOGGER.info("Selected songs storage was cleaned up.");
            selectedSongs.clear();
            FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("ManagePlaylists/RemoveSongs")
                    .withStageBuilder(StageBuilder.newBuilder()
                            .styleSheet("ManagePlaylists/RemoveSongs")
                            .removeUpperBar()
                            .title("Playlist Manager - Remove Songs")
                            .icon("icons/icon.png")
                            .resizable(false)
                    ).bindAllStagesCloseKeyCombination().addExitListenerWithEscape().finishBuilding();
            Stage stage = fxmlStageBuilder.getStage();

            MainApp.openStage(stage, true, true);
            RemoveSongs removeSongs = fxmlStageBuilder.getFxmlLoader().getController();

            removeSongs.title("Remove songs from playlist '"+selectedPlaylist+"'").setStage(stage).useDefaultFunctionalityPresets();

            removeSongs.initializeListView(selectedPlaylist);
            removeSongs.cancelButton.setOnAction(event -> MainApp.closeStage(stage, true));
            removeSongs.removeButton.setOnAction(event -> {
                if(removeSongs.getItemsEvidence() == removeSongs.getListViewSize()) {
                    return;
                }
                if(removeSongs.getSelectedItem() == null || removeSongs.getSelectedIndex() < 0){
                    removeSongs.setStatusLabel("You need to select a song to remove first.", OutputUtilities.Level.INCOMPLETE);
                    return;
                }
                int selectedIndex = removeSongs.getSelectedIndex();
                String item = removeSongs.listView.getItems().get(selectedIndex);
                selectedSongs.add(item);
                removeSongs.getListView().getSelectionModel().clearSelection();
                removeSongs.getListView().getItems().remove(selectedIndex);
                removeSongs.setPlannedToRemoveLabel("", OutputUtilities.Level.INCOMPLETE);
                removeSongs.setRemainingSongs(removeSongs.getListView().getItems().size());
                removeSongs.updateItemEvidence(removeSongs.getItemsEvidence() + 1);
            });
            removeSongs.proceedButton.setOnAction(event -> {
                if(removeSongs.getItemsEvidence() == 0){
                    removeSongs.setStatusLabel("You have to select at least 1 item from the list above to continue.", Level.INCOMPLETE);
                    return;
                }
                int removed = 0;
                File[] dir = Playlists.getPlaylist(selectedPlaylist);
                for(File f : dir){
                    String name = FilesUtils.getFileNameUpdated(f.getAbsolutePath(), true);
                    if(selectedSongs.contains(name)){
                        if(FilesUtils.removeDirectoryFiles(f.getAbsolutePath(), true)){
                            removed++;
                            LOGGER.info("Removed item "+removed+"/"+selectedSongs.size()+" - Name: '"+name+"'");
                        }else{
                            LOGGER.warning("Cannot remove '"+name+"': File is possibly in use.");
                            continue;
                        }
                    }
                }
                int diff = selectedSongs.size() - removed;
                if(diff != 0){
                    LOGGER.warning("Removal wasn't complete. "+diff+" item(s) was/ were aborted"+((removed > 0) ? ", but "+removed+" was/ were deleted." : "."));
                    removeSongs.setStatusLabel("Removal failed for "+diff+" items. They are probably being used by the Player or another process.", Level.INCOMPLETE);
                    return;
                }
                MainApp.closeStage(stage, Player.ANIMATIONS);
            });
        }catch (IllegalStateException exception){
            ErrorHandler.launchWindow(exception);
        }
    }
}
