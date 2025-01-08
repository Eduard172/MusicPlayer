package me.eduard.musicplayer.Components;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.Components.Player.PlayerSettingsArgs;
import me.eduard.musicplayer.Library.ApplicationWindow;
import me.eduard.musicplayer.Components.Player.PlayerSettings;
import me.eduard.musicplayer.Library.BasicKeyValuePair;
import me.eduard.musicplayer.Library.Cache.Window.WindowRegistry;
import me.eduard.musicplayer.Library.CustomComponents.BetterLabel;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.*;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;

import java.net.URL;
import java.util.ResourceBundle;

@SuppressWarnings("unused")
public class SettingsPage extends ApplicationWindow implements Initializable {

    private final Settings settings = new Settings("settings.yml");

    @FXML private AnchorPane corePane;
    @FXML public Button saveButton, cancelButton, saveAndExitButton;
    @FXML private Label playerEndSelectedBehaviour, statusLabel; //soundSelectedBehaviour
    @FXML public CheckBox allowAnimations, allowSlidingNotification, autoStart, verboseStatus, startInFullScreen, hidePlayer, hideTitle;
    @FXML public ListView<String> playerEndBehaviour, soundBehaviour;
    @FXML public Hyperlink downloaderUpdates;
    private BetterLabel soundSelectedBehaviour; //13px, 727, 320, 200

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.initializePlayerEndBehaviourListView();
        this.initializeSoundBehaviourListView();
        this.initializeComponents();
    }
    public void initializePlayerEndBehaviourListView(){
        String[] playerEndBehaviour = {
                "Infinite-Play", "Auto-Play"
        };
        this.playerEndBehaviour.getItems().addAll(playerEndBehaviour);
    }

    public void initializeSoundBehaviourListView(){
        String[] soundBehaviour = {
                "Adjust for Low-End Devices", "Allow Full Sound Volume"
        };
        this.soundBehaviour.getItems().addAll(soundBehaviour);
    }
    public void setPlayerEndSelectedBehaviour(String string){
        this.playerEndSelectedBehaviour.setText("Currently Selected: "+string);
    }
    public void setSoundSelectedBehaviour(String string, BetterLabel soundSelectedBehaviour){
        this.soundSelectedBehaviour = soundSelectedBehaviour;
        this.soundSelectedBehaviour.setText("Currently Selected: "+string);
    }
    public void setStatusLabel(String string, Level level, boolean animate){
        this.setOutputLabel(this.statusLabel, string, level, animate);
    }
    public void initializeComponents(){
        this.autoStart.setSelected(PlayerSettings.AUTO_START);
        this.allowAnimations.setSelected(PlayerSettings.ANIMATIONS);
        this.allowSlidingNotification.setSelected(PlayerSettings.SLIDING_NOTIFICATIONS);
        this.verboseStatus.setSelected(PlayerSettings.VERBOSE_STATUS);
        this.startInFullScreen.setSelected(PlayerSettings.START_IN_FULLSCREEN);
        this.hidePlayer.setSelected(PlayerSettings.HIDE_PLAYER);
        this.hideTitle.setSelected(PlayerSettings.HIDE_TITLE);

        String soundBehaviour = PlayerSettings.SOUND_TYPE;
        String endBehaviour = PlayerSettings.MEDIA_END_BEHAVIOUR;

        this.playerEndBehaviour.getSelectionModel().clearSelection();
        this.soundBehaviour.getSelectionModel().clearSelection();

        this.setPlayerEndSelectedBehaviour(endBehaviour);
        this.playerEndBehaviour.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldValue, newValue) -> this.setPlayerEndSelectedBehaviour(newValue)
        );
        this.soundBehaviour.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldValue, newValue) -> this.setSoundSelectedBehaviour(newValue, this.soundSelectedBehaviour)
        );
    }
    public int getPlayerEndBehaviourIndex(){
        return this.playerEndBehaviour.getSelectionModel().getSelectedIndex();
    }
    public int getSoundBehaviourIndex(){
        return this.soundBehaviour.getSelectionModel().getSelectedIndex();
    }
    public void applyNewSettings(){
        String soundBehaviour = switch (this.getSoundBehaviourIndex()){
            case 0 -> "Adjusted";
            case 1 -> "Full";
            default -> PlayerSettings.SOUND_TYPE;
        };
        String playerEndBehaviour = switch (this.getPlayerEndBehaviourIndex()){
            case 0 -> "Infinite-Play";
            case 1 -> "Auto-Play";
            default -> PlayerSettings.MEDIA_END_BEHAVIOUR;
        };
        PlayerSettings.updateApplicationSettings(
                PlayerSettingsArgs.newInstance()
                        .setAutoStart(this.autoStart.isSelected())
                        .setAnimations(this.allowAnimations.isSelected())
                        .setSlidingNotifications(this.allowSlidingNotification.isSelected())
                        .setVerboseStatus(this.verboseStatus.isSelected())
                        .setStartFullscreen(this.startInFullScreen.isSelected())
                        .setHidePlayer(this.hidePlayer.isSelected())
                        .setHideTitle(this.hideTitle.isSelected())
                        .setSoundType(soundBehaviour)
                        .setMediaEndBehaviour(playerEndBehaviour)
        );
        settings.saveSetting("auto-start", PlayerSettings.AUTO_START);
        settings.saveSetting("sliding-notifications", PlayerSettings.SLIDING_NOTIFICATIONS);
        settings.saveSetting("animations", PlayerSettings.ANIMATIONS);
        settings.saveSetting("Sound-Type", PlayerSettings.SOUND_TYPE);
        settings.saveSetting("Media-End-Behaviour", PlayerSettings.MEDIA_END_BEHAVIOUR);
        settings.saveSetting("verbose-status", PlayerSettings.VERBOSE_STATUS);
        settings.saveSetting("fullscreen", PlayerSettings.START_IN_FULLSCREEN);
        settings.saveSetting("hide-player", PlayerSettings.HIDE_PLAYER);
        settings.saveSetting("hide-title", PlayerSettings.HIDE_TITLE);
    }

    public static void launchWindow(boolean useCached){
        FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder
                .newInstance("Settings/SettingsPage")
                .withStageBuilder(
                        StageBuilder.newBuilder()
                                .removeUpperBar()
                                .styleSheet("SettingsPage")
                                .title("Settings")
                                .icon("icons/icon.png")
                                .resizable(false)
                ).bindAllStagesCloseKeyCombination().addExitListenerWithEscape().finishBuilding();
        SettingsPage settingsPage = fxmlStageBuilder.getFxmlLoader().getController();

        Stage stage = (useCached) ?
                WindowRegistry.isInRegistry("SETTINGS") ?
                        WindowRegistry.getStage("SETTINGS") : WindowRegistry.getAndRegister("SETTINGS", fxmlStageBuilder.getStage())
                : fxmlStageBuilder.getStage();
        BetterLabel soundSelectedBehaviour = BetterLabel.of(stage)
                .setAlignment(BetterLabel.Alignment.LEFT)
                .setFont(13)
                .setLayout_X(727)
                .setLayout_Y(320)
                .setWidth(200)
                .setLabelStyling("-fx-text-fill: white")
                .setBackgroundStyling("-fx-background-color: #171717")
                .setMarginsStyling("-fx-fill: #171717")
                .setRelativeLabelDistance(40)
                .setViewMode(BetterLabel.ViewMode.BEHIND);
        soundSelectedBehaviour.linkToStage();
        settingsPage.setSoundSelectedBehaviour(PlayerSettings.SOUND_TYPE, soundSelectedBehaviour);

        MainApp.openStage(stage, Player.ANIMATIONS, true);

        settingsPage.title("Application Settings").setStage(stage).useDefaultFunctionalityPresets();

        settingsPage.cancelButton.setOnAction(event -> MainApp.closeStage(stage, true));
        settingsPage.saveButton.setOnAction(event -> {
            settingsPage.applyNewSettings();
            Player.instance.sendNewSettingsPing();
            settingsPage.setStatusLabel("Your new settings have been saved.", OutputUtilities.Level.SUCCESS, true);
        });
        settingsPage.saveAndExitButton.setOnAction(event -> {
            settingsPage.applyNewSettings();
            Player.instance.sendNewSettingsPing();
            Player.instance.setStatusLabel("Your new settings have been saved.", OutputUtilities.Level.SUCCESS);
            MainApp.closeStage(stage, Player.ANIMATIONS);
        });
        EventHandler<? super KeyEvent> eventHandler = event -> {
            KeyCombinationUtils.registerKey(event.getCode());
            if(KeyCombinationUtils.isKey(KeyCode.CONTROL, 0) && KeyCombinationUtils.isKey(KeyCode.S, 1)){
                settingsPage.applyNewSettings();
                Player.instance.sendNewSettingsPing();
                settingsPage.setStatusLabel("Your new settings have been saved.", OutputUtilities.Level.SUCCESS, true);
                Utilities.sleep(Duration.millis(450), 1, run -> MainApp.closeStage(stage, Player.ANIMATIONS), null);
            }
        };
        settingsPage.downloaderUpdates.setOnAction(event -> {
            Utilities.reDownloadExternalHelpers();
            Player.instance.setStatusLabel("Updating the external app helpers...", OutputUtilities.Level.SUCCESS);
            MainApp.closeStage(stage, Player.ANIMATIONS);
        });

        fxmlStageBuilder.addKeyEvents(
                new BasicKeyValuePair<>(KeyEvent.KEY_RELEASED, event -> KeyCombinationUtils.removeKey(event.getCode())),
                new BasicKeyValuePair<>(KeyEvent.KEY_PRESSED, eventHandler)
        );
    }
}