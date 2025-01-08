package me.eduard.musicplayer.Components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.Library.CustomComponents.WindowTitleBar;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;
import me.eduard.musicplayer.Utils.Utilities;

@SuppressWarnings("all")
public class DownloadProgress {

    private FXMLStageBuilder fxmlStageBuilder;

    public DownloadProgress(){

    }

    public DownloadProgress(FXMLStageBuilder fxmlStageBuilder){
        this.fxmlStageBuilder = fxmlStageBuilder;
    }

    private Stage stage;

    @FXML public AnchorPane corePane;
    @FXML public ProgressBar progressBar;
    @FXML public Label filesDownloaded, stepsExecuted, overallProgress, inProgress;

    private int stepsFinished = 0;
    private int totalSteps = 1;
    private int downloadedFiles = 0;
    private int totalFilesToDownload = 1;

    //Placeholders
    private final String FINISHED = "[f]";
    private final String TOTAL = "[t]";
    private final String PERCENTAGE = "[p]";
    private final String STATUS = "[status]";

    private String overAllProgressText = "[p]%";
    private String filesDownloadedText = "Ready to go: [f] / [t].";
    private String stepsExecutedText = "Steps executed: [f] / [t].";
    private String inProgressText = "In progress: [status]";

    public int getTotalSteps(){
        return this.totalSteps;
    }
    public int getStepsFinished(){
        return this.stepsFinished;
    }
    public int getDownloadedFiles(){
        return this.downloadedFiles;
    }
    public int getTotalFilesToDownload(){
        return this.totalFilesToDownload;
    }
    public void setStepsFinished(int stepsFinished, DownloadProgress instance){
        this.stepsFinished = stepsFinished;
        double percentage = Utilities.getPercentage(this.stepsFinished, this.totalSteps);
        instance.stepsExecuted.setText(stepsExecutedText
                .replace(FINISHED, Integer.toString(stepsFinished))
                .replace(TOTAL, Integer.toString(totalSteps))
        );
        instance.progressBar.setProgress(Utilities.getProgressBarPercentage(this.stepsFinished, this.totalSteps));
        instance.overallProgress.setText(
                overAllProgressText
                        .replace(PERCENTAGE, Integer.toString((int) percentage)
                )
        );
        instance.progressBar.setProgress(percentage / 100);
    }
    public void setStatusInProgress(String status){
        this.inProgress.setText(
                this.inProgressText.replace(STATUS, status)
        );
    }
    public void setTotalSteps(int totalSteps, DownloadProgress instance){
        double percentage = Utilities.getPercentage(this.stepsFinished, this.totalSteps);
        this.totalSteps = totalSteps;
        instance.stepsExecuted.setText(stepsExecutedText
                .replace(FINISHED, Integer.toString(stepsFinished))
                .replace(TOTAL, Integer.toString(totalSteps))
        );
        instance.progressBar.setProgress(Utilities.getProgressBarPercentage(this.stepsFinished, this.totalSteps));
        instance.overallProgress.setText(
                overAllProgressText
                        .replace(PERCENTAGE, Integer.toString((int) percentage)
                )
        );
        instance.progressBar.setProgress(percentage / 100);
    }
    public void setFilesDownloaded(int filesDownloaded, DownloadProgress instance){
        this.downloadedFiles = filesDownloaded;
        instance.filesDownloaded.setText(filesDownloadedText
                .replace(FINISHED, Integer.toString(downloadedFiles))
                .replace(TOTAL, Integer.toString(totalFilesToDownload)
            )
        );
    }
    private void setStage(Stage stage){
        this.stage = stage;
    }
    public Stage getStage(){
        return this.stage;
    }
    public void setTotalFilesToDownload(int totalFilesToDownload, DownloadProgress instance){
        this.totalFilesToDownload = totalFilesToDownload;
        instance.filesDownloaded.setText(filesDownloadedText
                .replace(FINISHED, Integer.toString(downloadedFiles))
                .replace(TOTAL, Integer.toString(totalFilesToDownload))
        );
    }

    public void close(DownloadProgress instance){
        MainApp.closeStage(instance.fxmlStageBuilder.getStage(), Player.ANIMATIONS);
    }

    public static DownloadProgress launchWindow(){
        DownloadProgress downloadProgress = new DownloadProgress(
                FXMLStageBuilder.newInstance("DownloadProgress")
                        .withStageBuilder(
                                StageBuilder.newBuilder()
                                        .icon("icons/icon.png")
                                        .styleSheet("ApplicationWindow")
                                        .resizable(false)
                                        .removeUpperBar()
                                        .title("Download in progress...")
                        ).bindAllStagesCloseKeyCombination().finishBuilding()
        );
        return downloadProgress.launch();
    }

    private DownloadProgress launch(){
        try {

            DownloadProgress downloadProgress = this.fxmlStageBuilder.getFxmlLoader().getController();

            Stage stage = this.fxmlStageBuilder.getStage();

            downloadProgress.setStage(stage);

            MainApp.openStage(downloadProgress.getStage(), Player.ANIMATIONS);

            WindowTitleBar windowTitleBar = new WindowTitleBar(stage).useDefaultPresets();
            windowTitleBar.linkToStage();
            windowTitleBar.applyAfterLinkPresets();
            windowTitleBar
                    .removeNode(windowTitleBar.getCloseButton())
                    .removeNode(windowTitleBar.getCloseImage())
                    .removeNode(windowTitleBar.getMinimizeButton())
                    .removeNode(windowTitleBar.getMinimizeImage())
                    .removeNode(windowTitleBar.getMaximizeButton())
                    .removeNode(windowTitleBar.getMaximizeImage());
            windowTitleBar.setTitleString("Download in progress...");

            return downloadProgress;

        }catch (IllegalStateException e){
            ErrorHandler.launchWindow(e);
            return null;
        }
    }

}
