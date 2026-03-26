package me.eduard.musicplayer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.Library.CustomComponents.WindowTitleBar;
import me.eduard.musicplayer.Utils.GlobalAppStyle;
import me.eduard.musicplayer.Utils.OutputUtilities;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;
import me.eduard.musicplayer.Utils.Utilities;

@SuppressWarnings("unused")
public class ErrorHandler extends OutputUtilities {
    @FXML public Label errorLog, statusLabel;
    @FXML public Button okButton, copyImportantTrace, copyFullTrace, copyDescription;
    @FXML private AnchorPane corePane;

    public void setErrorLog(String string){
        this.errorLog.setText(string);
    }
    public static void launchWindow(Exception exception){
        exception.printStackTrace(System.out);
        try {
            FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("ErrorHandler")
                    .withStageBuilder(StageBuilder.newBuilder()
                            .styleSheet("ApplicationWindow")
                            .icon("icons/icon.png")
                            .removeUpperBar()
                            .resizable(false)
                            .title("An error has occurred.")
                    ).bindAllStagesCloseKeyCombination().addExitListenerWithEscape().finishBuilding();
            Stage stage = fxmlStageBuilder.getStage();

            WindowTitleBar titleBar = new WindowTitleBar(stage).useDefaultPresets()
                    .setTitleString("An error has occurred. Full stacktrace could be found inside the application terminal or by copying it.")
                    .setSupportStyling(MainApp.childWindowTitleBarTheme);
            titleBar.linkToStage();

            MainApp.openStage(stage, Player.ANIMATIONS, true);

            ErrorHandler errorHandler = fxmlStageBuilder.getFxmlLoader().getController();

            GlobalAppStyle.applyToButtons(
                    errorHandler.copyDescription,
                    errorHandler.copyFullTrace,
                    errorHandler.copyImportantTrace,
                    errorHandler.okButton
            );

            errorHandler.setErrorLog(Utilities.getStackTraceLog(exception, true));
            errorHandler.okButton.setOnAction(event -> MainApp.closeStage(stage, true));
            errorHandler.copyDescription.setOnAction(event -> {
                Utilities.copyToClipboard(exception.getMessage());
                errorHandler.setOutputLabel(errorHandler.statusLabel, "Problem description copied to clipboard.", OutputUtilities.Level.SUCCESS, true);
            });
            errorHandler.copyImportantTrace.setOnAction(event -> {
                Utilities.copyToClipboard(Utilities.getStackTraceLog(exception, true));
                errorHandler.setOutputLabel(errorHandler.statusLabel, "Problem important stack trace copied to clipboard.", OutputUtilities.Level.SUCCESS, true);
            });
            errorHandler.copyFullTrace.setOnAction(event -> {
                Utilities.copyToClipboard(Utilities.getStackTraceLog(exception, false));
                errorHandler.setOutputLabel(errorHandler.statusLabel, "Problem full stack trace copied to clipboard.", OutputUtilities.Level.SUCCESS, true);
            });
        }catch (IllegalStateException exception2){
            exception.printStackTrace(System.err);
        }
    }

}
