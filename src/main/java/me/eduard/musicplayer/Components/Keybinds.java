package me.eduard.musicplayer.Components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.Library.ApplicationWindow;
import me.eduard.musicplayer.Library.Cache.Window.WindowRegistry;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;
import me.eduard.musicplayer.Utils.Utilities;

@SuppressWarnings("all")
public class Keybinds extends ApplicationWindow {

    @FXML public AnchorPane corePane;
    @FXML public Label keybindsList;
    @FXML public Button okButton;

    public void setKeybindsList(String[] keybindsList){
        String stringFromArray = Utilities.stringFromArray(keybindsList, " * ", "\n");
        this.keybindsList.setText(stringFromArray);
    }
    public static void launchWindow(boolean useCached){
        try {
            FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("Keybinds")
                    .withStageBuilder(StageBuilder.newBuilder()
                            .icon("icons/icon.png")
                            .resizable(false)
                            .removeUpperBar()
                            .title("Application Keybinds")
                            .styleSheet("ApplicationWindow")
                    ).bindAllStagesCloseKeyCombination().addExitListenerWithEscape().finishBuilding();
            Stage stage = (useCached) ?
                    (WindowRegistry.isInRegistry("KEYBINDS")) ?
                            WindowRegistry.getStage("KEYBINDS") : WindowRegistry.getAndRegister("KEYBINDS", fxmlStageBuilder.getStage())
                    : fxmlStageBuilder.getStage();

            MainApp.openStage(stage, true, true);
            Keybinds keybinds = fxmlStageBuilder.getFxmlLoader().getController();

            keybinds.setStage(stage).title("Application Keybinds").useDefaultFunctionalityPresets();

            keybinds.okButton.setOnAction(event -> MainApp.closeStage(stage, true));
            String[] content = {
                    "RIGHT_ARROW - Turn the media volume forward by 5 seconds.",
                    "LEFT_ARROW - Turn the media volume back by 5 seconds.",
                    "F1 - Reset the key combination storage",
                    "UP_ARROW - Turn the volume up by 5%.",
                    "DOWN_ARROW - Turn the volume down by 5%.",
                    "SHIFT + RIGHT_ARROW - Play the next song.",
                    "SHIFT + LEFT_ARROW - Play the previous song.",
                    "CTRL + LEFT_ARROW - Play the current song from the beginning.",
                    "CTRL + F - Enable the search function.",
                    "ESC - Minimize the main window.",
                    "F11 - Enable / Disable the fullscreen.",
                    "ALT + S - Open the \"Settings\" page.",
                    "ALT + N - Open the \"Create Playlist\" page.",
                    "ALT + M - Open the \"Playlist Manager\" page.",
                    "ALT + K - Open this page.",
                    "ALT + E - Open the Main Applicationn Directory.",
                    "ALT + C - Open the \"Playlist Selector\" page.",
                    "ALT + B - Open the \"Backup Manager\" page.",
                    "ALT + F4 - Quit the Application."
            };
            keybinds.setKeybindsList(content);
        }catch (IllegalStateException exception){
            ErrorHandler.launchWindow(exception);
        }
    }
}
