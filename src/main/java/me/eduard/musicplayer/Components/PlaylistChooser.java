package me.eduard.musicplayer.Components;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.Library.CustomComponents.WindowTitleBar;
import me.eduard.musicplayer.Library.SimplePair;
import me.eduard.musicplayer.Library.WrappedValue;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.*;
import me.eduard.musicplayer.Utils.PlaylistRelated.Playlists;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;

import java.io.File;
import java.util.List;

@SuppressWarnings("unused")
public class PlaylistChooser extends OutputUtilities {

    private static final Settings settings = Settings.of("settings.yml");

    @FXML private AnchorPane corePane;
    @FXML public Label titleLabel, statusLabel;
    @FXML public Button selectButton;
    @FXML public Button cancelButton;
    @FXML public ListView<String> listView;

    public void setNumberOfPlaylists(int n){
        this.titleLabel.setText("You have "+n+" playlists to choose from.");
    }
    public void forceSetWindowHeader(String string){
        this.titleLabel.setText(string);
    }
    public void setTitle(String string){
        this.titleLabel.setText(string);
    }
    public void addPlaylists(List<String> list){
        this.listView.getItems().addAll(list);
    }
    public String getSelectedItem(){
        return this.listView.getSelectionModel().getSelectedItem();
    }
    public int getSelectedIndex(){
        return this.listView.getSelectionModel().getSelectedIndex();
    }
    public void initializeListViewSelectionListener(){
        this.listView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) ->
                this.setOutputLabel(
                    this.statusLabel,
                    "Selected: "+newValue+" ("+Playlists.getPlaylist(newValue).length+" songs)",
                    Level.SUCCESS, false
        ));
    }
    @SuppressWarnings("all")
    public SimplePair<EventType<KeyEvent>, EventHandler<? super KeyEvent>>[] getEvents(){
        return new SimplePair[]{
                new SimplePair<EventType<KeyEvent>, EventHandler<? super KeyEvent>>(
                    KeyEvent.KEY_PRESSED, event -> {
                        if(event.getCode() == KeyCode.ENTER){
                            this.selectButton.fire();
                        }
                        WrappedValue<Integer> index = WrappedValue.of(this.getSelectedIndex());
                        if(event.getCode() == KeyCode.DOWN) {
                            Platform.runLater(() -> {
                                if(index.get() + 1 >= this.listView.getItems().size()){
                                    this.listView.getSelectionModel().select(0);
                                }
                            });
                        }else if(event.getCode() == KeyCode.UP){
                            Platform.runLater(() -> {
                                if(index.get() - 1 < 0){
                                    this.listView.getSelectionModel().select(this.listView.getItems().size() - 1);
                                }
                            });
                        }
                    }
                ),
        };
    }
    public static void launchWindow(boolean useCached){
        try {
            String playlistsPath = Playlists.PATH;
            File mainDirectory = new File(playlistsPath);
            if(mainDirectory.exists() && mainDirectory.isDirectory()){
                FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("PlaylistChooser")
                        .withStageBuilder(StageBuilder.newBuilder()
                                .styleSheet("PlaylistChooser")
                                .icon("icons/icon.png")
                                .title("Playlist Selector")
                                .removeUpperBar()
                                .resizable(false)
                        )
                        .bindAllStagesCloseKeyCombination()
                        .bindAllStagesCloseKeyCombination()
                        .addExitListenerWithEscape()
                        .requireModality().finishBuilding();
                Stage stage = fxmlStageBuilder.getStage();

                WindowTitleBar titleBar = new WindowTitleBar(stage).useDefaultPresets()
                        .setTitleString("Select a playlist")
                        .setTitleLabelStyling("-fx-text-fill: #000000")
                        .setOnClose(() -> MainApp.closeStage(stage, Player.ANIMATIONS))
                        .setButtonsArrangement(WindowTitleBar.ButtonsArrangement.CLOSE)
                        .setSupportStyling(MainApp.childWindowTitleBarTheme);
                titleBar.linkToStage();

                stage.initOwner(MainApp.stage1);

                MainApp.openStage(stage, true, true);

                File[] files = mainDirectory.listFiles();
                assert files != null;
                List<String> playlistsAvailable = Playlists.getPlaylists();
                PlaylistChooser playlistChooser = fxmlStageBuilder.getFxmlLoader().getController();

                GlobalAppStyle.applyToButtons(playlistChooser.cancelButton, playlistChooser.selectButton);

                fxmlStageBuilder.addKeyEvents(playlistChooser.getEvents());
                fxmlStageBuilder.addKeyEvents(
                        SimplePair.of(KeyEvent.KEY_PRESSED, event -> {
                            if(event.getCode() == KeyCode.ENTER)
                                playlistChooser.selectButton.fire();
                        })
                );

                playlistChooser.addPlaylists(playlistsAvailable);
                playlistChooser.initializeListViewSelectionListener();

                playlistChooser.setNumberOfPlaylists(playlistsAvailable.size());

                if(playlistChooser.listView.getItems().isEmpty()){
                    playlistChooser.forceSetWindowHeader("You do not currently have any active playlists.");
                    playlistChooser.listView.setVisible(false);
                }else{
                    playlistChooser.listView.setVisible(true);
                    playlistChooser.listView.getSelectionModel().select(0);
                }
                playlistChooser.listView.getSelectionModel().select(Player.SELECTED_PLAYLIST);
                playlistChooser.cancelButton.setOnAction(event -> MainApp.closeStage(stage, true));

                playlistChooser.selectButton.setOnAction(event -> {
                    String selectedPlaylist = playlistChooser.getSelectedItem();
                    if(playlistChooser.getSelectedItem() == null){
                        playlistChooser.setOutputLabel(playlistChooser.statusLabel, "You must select a playlist to continue.", OutputUtilities.Level.INCOMPLETE, Player.ANIMATIONS);
                        return;
                    }else if(playlistChooser.getSelectedItem().equals(Player.SELECTED_PLAYLIST)){
                        playlistChooser.setOutputLabel(playlistChooser.statusLabel, "This playlist is already selected.", OutputUtilities.Level.INCOMPLETE, Player.ANIMATIONS);
                        return;
                    }
                    Playlists.selectPlaylist(selectedPlaylist);
                    MainApp.closeStage(stage, Player.ANIMATIONS);
                });
            }
        }catch (IllegalStateException exception){
            ErrorHandler.launchWindow(exception);
        }
    }
}
