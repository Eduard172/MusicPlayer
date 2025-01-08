package me.eduard.musicplayer.Components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.Library.Animations.LabelAnimations;
import me.eduard.musicplayer.Library.CustomComponents.WindowTitleBar;
import me.eduard.musicplayer.Library.DirectorySelector;
import me.eduard.musicplayer.Library.LambdaObject;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Library.BackupManager;
import me.eduard.musicplayer.Utils.OutputUtilities;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;
import me.eduard.musicplayer.Utils.Utilities;

import java.io.File;

public class BackupWindow extends OutputUtilities {

    @FXML public AnchorPane corePane;
    @FXML public Label statusLabel;
    @FXML public Button restoreBackup, createBackup, browse, cancel;
    @FXML public TextField backupPathField;

    public static void launchWindow(){
        try {
            FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("Backup/BackupWindow")
                    .withStageBuilder(
                            StageBuilder.newBuilder()
                                    .title("Manage backups")
                                    .resizable(false)
                                    .icon("icons/icon.png")
                                    .styleSheet("ApplicationWindow")
                                    .removeUpperBar()
                    ).bindAllStagesCloseKeyCombination().addExitListenerWithEscape().finishBuilding();

            Stage stage = fxmlStageBuilder.getStage();

            WindowTitleBar windowTitleBar = new WindowTitleBar(stage)
                    .setTitleString("Setup or restore backups.")
                    .setButtonsViewType(WindowTitleBar.ButtonsViewType.IMAGED)
                    .setCloseImage("icons/TopBar/XButton2.png")
                    .setMinimizeImage("icons/TopBar/MinimizeIcon.png")
                    .setSupportHeight(24)
                    .setButtonsWidth(30);
            windowTitleBar.linkToStage();

            windowTitleBar.setSupportStyling("-fx-fill: #808080;").setGeneralButtonsStyling("-fx-background-color: transparent;");
            windowTitleBar.removeNode(windowTitleBar.getMaximizeButton());
            windowTitleBar.moveButtonNextTo(windowTitleBar.getMinimizeButton(), windowTitleBar.getCloseButton(), false);
            windowTitleBar.autoAdjustImageViews(true, true);

            MainApp.openStage(stage, Player.ANIMATIONS, true);

            BackupWindow backupWindow = fxmlStageBuilder.getFxmlLoader().getController();
            LambdaObject<File> result = LambdaObject.of(null);
            File desktopFile = new File(MainApp.DESKTOP_PATH);
            backupWindow.browse.setOnAction(event -> {
                DirectorySelector directorySelector = new DirectorySelector();
                directorySelector
                        .setInitialDirectory(desktopFile)
                        .setTitle("Select the directory where you want to save the backup to.")
                        .launch();
                result.set(directorySelector.getResult());
                if(result.get() == null){
                    backupWindow.backupPathField.setText("");
                }else{
                    backupWindow.backupPathField.setText(result.get().getAbsolutePath());
                }
            });
            backupWindow.createBackup.setOnAction(event -> {
                LabelAnimations.instance(backupWindow.statusLabel)
                        .text("Your backup is being created...")
                        .color(Color.AQUAMARINE)
                        .waitingTime(2500)
                        .startAnimation();
                Utilities.sleep(Duration.millis(50), 1, run -> {
                    String text = backupWindow.backupPathField.getText();
                    File backupDirectory = new File(text);
                    if(!backupDirectory.exists() || !backupDirectory.isDirectory() || text.isEmpty()){
                        LabelAnimations.instance(backupWindow.statusLabel)
                                .waitingTime(2500)
                                .text("The path provided is invalid. (File does not exist or is not a directory!)")
                                .color(Color.RED)
                                .startAnimation();
                        return;
                    }
                    if(BackupManager.backupExists(backupDirectory.getAbsolutePath())){
                        ScreenLauncher.launchWarningMessageBox(
                                "Backup already existing",
                                "Uhm... There's already another live backup there.",
                                """
                                        The directory where you want to store this backup has another one already.
                                        You can override it by pressing the 'OK' button.
                                        
                                        WARNING! Every file inside the old backup folder will be replaced with the ones from a fresh one.
                                        If you have important data there, please save it first.
                                        """, event2 -> BackupManager.createBackup(backupDirectory, true)
                        );
                        backupWindow.setOutputLabel(backupWindow.statusLabel, "Backup was aborted. Waiting for user approval to continue.", OutputUtilities.Level.INCOMPLETE, Player.ANIMATIONS);
                        return;
                    }
                    BackupManager.createBackup(backupDirectory, false);
                    backupWindow.setOutputLabel(backupWindow.statusLabel, "Your backup has been successfully created!", OutputUtilities.Level.SUCCESS);
                }, null);
            });
            backupWindow.restoreBackup.setOnAction(event -> {
                String text = backupWindow.backupPathField.getText();
                File directory = new File(text);
                if(!BackupManager.isVersionOkay(directory) && BackupManager.isActiveBackupDirectory(directory)){
                    ScreenLauncher.launchWarningMessageBox(
                            "Backup version not compatible!",
                            "Backup version is not compatible with the application!",
                            """
                                    Version incompatibility could lead to app not starting properly, errors or inconsistent user experience.
                                    
                                    Before proceeding, please keep in mind that before restoring, all the current files will be removed and \
                                    replaced with the ones from the backup. If after backup the application does not work properly, \
                                    there's nothing that could be done about it, and a clean app install could be required. Do it at your own risk.
                                    
                                    If you would still like to continue, press the 'OK' button below.
                                    """, event2 -> {
                                LabelAnimations.instance(backupWindow.statusLabel)
                                        .color(Color.ORANGE)
                                        .waitingTime(1500)
                                        .text("Restoring old Music Player backup...")
                                        .startAnimation();
                                Utilities.sleep(Duration.millis(50), 1, run -> {
                                    BackupManager.restoreBackup(result.get());
                                    LabelAnimations.instance(backupWindow.statusLabel)
                                            .color(Color.LIME)
                                            .waitingTime(1000)
                                            .text("Backup restored successfully!")
                                            .startAnimation();
                                }, null);
                            }
                    );
                    return;
                }else if(!BackupManager.isActiveBackupDirectory(result.get())){
                    LabelAnimations.instance(backupWindow.statusLabel)
                            .text("Backup cannot be restored because the path is invalid.")
                            .color(Color.RED)
                            .waitingTime(2500)
                            .startAnimation();
                    return;
                }
                LabelAnimations.instance(backupWindow.statusLabel)
                        .color(Color.SKYBLUE)
                        .waitingTime(1500)
                        .text("Restoring Music Player backup...")
                        .startAnimation();
                Utilities.sleep(Duration.millis(50), 1, run -> {
                    BackupManager.restoreBackup(result.get());
                    LabelAnimations.instance(backupWindow.statusLabel)
                            .color(Color.LIME)
                            .waitingTime(1000)
                            .text("Backup restored successfully!")
                            .startAnimation();
                }, null);
            });
            backupWindow.cancel.setOnAction(event -> MainApp.closeStage(stage, Player.ANIMATIONS));
        }catch (IllegalStateException exception){
            exception.printStackTrace(System.err);
        }
    }
}
