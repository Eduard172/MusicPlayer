package me.eduard.musicplayer.Components.VersionManagement;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import me.eduard.musicplayer.AppState;
import me.eduard.musicplayer.Consent;
import me.eduard.musicplayer.Library.AppMode;
import me.eduard.musicplayer.Library.CustomComponents.WindowTitleBar;
import me.eduard.musicplayer.Library.Uninstaller;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.GlobalAppStyle;
import me.eduard.musicplayer.Utils.Logging.LoggerUtils;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;

import java.io.File;
import java.util.logging.Logger;

@SuppressWarnings("CodeBlock2Expr")
public class VersionUpdater {

    private final Stage stage;

    private double offsetHeight = -1.0;

    private final Label updateHeader = new Label();
    private ChangeListener<Number> onUpdateHeaderBoundsChange = null;

    private final ScrollPane updateInfoPane = new ScrollPane();
    private final Label updateInfo = new Label();

    private final Button cleanInstall = new Button();
    private ChangeListener<Number> onCleanInstallWidthChange = null;
    private EventHandler<ActionEvent> onCleanInstallAction = null;

    private final Button updateOnly = new Button();
    private ChangeListener<Number> onUpdateOnlyWidthChange = null;
    private EventHandler<ActionEvent> onUpdateOnlyAction = null;

    private static final Logger LOGGER = LoggerUtils.createOrGet("VersionUpdater");

    public VersionUpdater() {
        this.stage = StageBuilder.newBuilder()
                .color("#151515")
                .title("Update Available")
                .resizable(false)
                .removeUpperBar()
                .icon("icons/icon.png")
                .buildAndGet();
        this.stage.setOnHidden(e -> this.release());
        this.init();
    }

    private void init(){
        WindowTitleBar titleBar = new WindowTitleBar(this.stage).useDefaultPresets();
        titleBar.setTitleBarImage("icons/icon.png")
                .setTitleString("Update Available")
                .setOnClose(() -> {
                    AppMode.set(AppState.NORMAL);
                    stage.close();
                })
                .setSupportStyling("-fx-fill: transparent")
                .setTitleLabelStyling("-fx-text-fill: white")
                .setButtonsArrangement(WindowTitleBar.ButtonsArrangement.CLOSE);
        titleBar.linkToStage();
        stage.setWidth(900);
        stage.setHeight(500);
        this.offsetHeight = titleBar.getSupportHeight();
        this.setupElements();

        this.stage.show();
    }

    private void addElements(Node... nodes) {
        ((Pane) stage.getScene().getRoot()).getChildren().addAll(nodes);
    }
    private void removeElements(Node... nodes) {
        ((Pane) stage.getScene().getRoot()).getChildren().removeAll(nodes);
    }

    private void setupElements() {
        initListeners();
        updateHeader.widthProperty().addListener(onUpdateHeaderBoundsChange);
        updateHeader.setFont(Font.font(27));
        updateHeader.setText("This app build is outdated.");
        updateHeader.setTextFill(Color.WHITE);
        addElements(updateHeader);

        updateInfoPane.setLayoutX(10);
        updateInfoPane.setLayoutY(offsetHeight + updateHeader.getLayoutY() + updateHeader.getHeight() + 60);
        updateInfoPane.setPrefWidth(stage.getWidth() - 20);
        updateInfoPane.setPrefHeight(stage.getHeight() - (updateInfoPane.getLayoutY() + 100));
        updateInfoPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        updateInfoPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        updateInfoPane.setPannable(false);
        updateInfoPane.setStyle("""
                -fx-background: #151515;
                -fx-focus-color: transparent;
                -fx-padding: 0px;
                -fx-faint-focus-color: transparent;
                -fx-border-width: 0px;
                -fx-border-color: transparent;
                """);

        updateInfo.setFont(Font.font(18));
        updateInfo.setWrapText(true);
        updateInfo.setStyle("""
                -fx-focus-color: transparent;
                -fx-faint-focus-color: transparent;
                -fx-border-width: 0px;
                -fx-border-color: transparent;
                """);
        updateInfo.setTextFill(Color.WHITE);
        updateInfo.setPrefWidth(updateInfoPane.getPrefWidth());
        updateInfo.setBackground(Background.fill(Color.web("#151515")));
        System.out.println("Wrap text = "+updateInfo.isWrapText());
        updateInfo.setText("""
                This is a small update that introduces that new update system.
                
                The only thing it does is to write a simple version to a text file that the app will \
                keep track of every time it starts up. You can choose 'Update what's necessary' this time \
                since it isn't a major app update.
                
                A few more details about this update:
                - Improved audio encoding settings that make it more clean and enjoyable to listen.
                """);
        updateInfoPane.setContent(updateInfo);
        addElements(updateInfoPane);

        cleanInstall.setPrefHeight(20);
        cleanInstall.setLayoutY(stage.getHeight() - cleanInstall.getPrefHeight() - 10);
        cleanInstall.widthProperty().addListener(onCleanInstallWidthChange);
        cleanInstall.setText("Do a clean install");
        cleanInstall.setOnAction(onCleanInstallAction);
        addElements(cleanInstall);
        GlobalAppStyle.applyToButtons(cleanInstall);

        updateOnly.setPrefHeight(20);
        updateOnly.setLayoutY(cleanInstall.getLayoutY());
        updateOnly.widthProperty().addListener(onUpdateOnlyWidthChange);
        updateOnly.setText("Update what's necessary");
        updateOnly.setOnAction(onUpdateOnlyAction);
        addElements(updateOnly);
        GlobalAppStyle.applyToButtons(updateOnly);
    }

    private void release() {
        updateHeader.widthProperty().removeListener(onUpdateHeaderBoundsChange);
        onUpdateHeaderBoundsChange = null;
        cleanInstall.widthProperty().removeListener(onCleanInstallWidthChange);
        onCleanInstallWidthChange = null;
        updateOnly.widthProperty().removeListener(onUpdateOnlyWidthChange);
        onUpdateOnlyWidthChange = null;
        cleanInstall.setOnAction(null);
        onCleanInstallAction = null;
        updateOnly.setOnAction(null);
        onUpdateOnlyAction = null;
    }

    private void initListeners() {
        final double stageWidth = stage.getWidth();
        final double stageHeight = stage.getHeight();
        onUpdateHeaderBoundsChange = (obs, oldVal, newVal) -> {
            updateHeader.setLayoutX(stageWidth / 2 - updateHeader.getWidth() / 2);
            updateHeader.setLayoutY(offsetHeight + 5);
        };
        onCleanInstallWidthChange = (obs, oldVal, newVal) -> {
            cleanInstall.setLayoutX(stageWidth - cleanInstall.getWidth() - 10);
        };
        onUpdateOnlyWidthChange = (obs, oldVal, newVal) -> {
            updateOnly.setLayoutX(cleanInstall.getLayoutX() - (10 + updateOnly.getWidth()));
        };
        onCleanInstallAction = event -> {
            LOGGER.info("User has chosen to do a clean update install.");
            updateInfo.setText("Uninstalling then launching Consent screen...");
            Uninstaller.uninstall();
            new Consent().launchConsentScreen(new File(MainApp.MAIN_APP_PATH));
            stage.close();
            LOGGER.info("App Update finished.");
        };
        onUpdateOnlyAction = event -> {
            LOGGER.info("The user has chosen to only update essential parts of the app.");
            new VersionHandler().writeVersion();
            MainApp.instance.setupComponents();
            MainApp.instance.startApp();
            stage.close();
            LOGGER.info("App Update finished.");
        };
    }

}
