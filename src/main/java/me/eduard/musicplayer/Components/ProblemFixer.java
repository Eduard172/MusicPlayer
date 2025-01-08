package me.eduard.musicplayer.Components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.Library.ApplicationWindow;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.OutputUtilities;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;
import me.eduard.musicplayer.Utils.Utilities;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class ProblemFixer extends ApplicationWindow {
    @FXML private AnchorPane corePane;
    @FXML public Label problem_description, statusLabel, solutionLabel;
    @FXML public Button fix_button, cancel_button;

    public ProblemFixer setProblemDescription(String string){
        this.problem_description.setText(string);
        return this;
    }
    public ProblemFixer setSolution(String string){
        this.solutionLabel.setText("Quick Solution: "+string);
        return this;
    }
    public ProblemFixer setOnClickFixAction(EventHandler<ActionEvent> event){
        this.fix_button.setOnAction(event);
        return this;
    }
    public ProblemFixer setOnClickCancelAction(EventHandler<ActionEvent> event){
        this.cancel_button.setOnAction(event);
        return this;
    }
    public static void launchWindow(String description, String solution, EventHandler<ActionEvent> onFixPress){
        try {
            FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder
                    .newInstance("ProblemFixer")
                    .withStageBuilder(
                            StageBuilder.newBuilder()
                                    .icon("icons/icon.png")
                                    .title("An error has occurred")
                                    .resizable(false)
                                    .removeUpperBar()
                                    .styleSheet("ApplicationWindow")
                    ).bindAllStagesCloseKeyCombination().addExitListenerWithEscape().finishBuilding();
            Stage stage = fxmlStageBuilder.getStage();

            MainApp.openStage(stage, true, true);

            ProblemFixer problemFixer = fxmlStageBuilder.getFxmlLoader().getController();

            problemFixer.setStage(stage).title("An error has occurred after executing this action...").useDefaultFunctionalityPresets();

            EventHandler<ActionEvent> eventHandler = event -> {
                Utilities.sleep(Duration.millis(500), 1, run -> {
                    if(onFixPress != null){
                        onFixPress.handle(event);
                    }
                    fxmlStageBuilder.close();
                }, null);
                problemFixer.setOutputLabel(
                        problemFixer.statusLabel,
                        "Potential fix has been applied.",
                        OutputUtilities.Level.INFORMATIVE,
                        true
                );
            };

            problemFixer
                    .setProblemDescription(description)
                    .setSolution(solution)
                    .setOnClickFixAction(eventHandler)
                    .setOnClickCancelAction(event -> MainApp.closeStage(stage, true));

        }catch (IllegalStateException exception){
            ErrorHandler.launchWindow(exception);
        }
    }

}
