package me.eduard.musicplayer.Library.CustomComponents.ComplexStage;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.Library.CustomComponents.WindowTitleBar;
import me.eduard.musicplayer.Library.SimplePair;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.Logging.LoggerUtils;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ComplexStage {

    private static final Logger LOGGER = LoggerUtils.createOrGet("ComplexStage");

    private ChangeListener<Number> onStageWidthChange = null;
    private ChangeListener<Number> onStageHeightChange = null;
    private SimplePair<EventType<KeyEvent>, EventHandler<KeyEvent>> onESCPress = null;
    protected final List<LeftPanelElement> leftPanelElements = new ArrayList<>();

    protected Pane secondaryRoot;
    protected Stage secondaryRootStage;
    protected Pane leftPanelSupport;
    protected ScrollPane leftPanel;

    protected Stage stage;
    protected Line separator;

    private String title = "";
    private String backgroundColor = "#171717;";
    private WindowTitleBar titleBar = null;

    public ComplexStage(){
        this.initComponents();
    }

    private void initComponents() {
        this.secondaryRoot = new Pane();
        this.secondaryRootStage = new Stage(StageStyle.UNDECORATED);
        this.leftPanelSupport = new Pane();
        this.leftPanel = new ScrollPane();

        this.stage = StageBuilder.newBuilder()
                .title("Nothing to see here.")
                .icon("icons/icon.png")
                .removeUpperBar()
                .resizable(false)
                .color(this.backgroundColor)
                .buildAndGet();
        this.stage.initOwner(MainApp.stage1);
        this.stage.setOnCloseRequest(event -> this.unbindListeners());
        this.configureComponents();
        this.configureListeners();
    }

    public WindowTitleBar getTitleBar() {
        return this.titleBar;
    }

    public static ComplexStage defineProperties() {
        return new ComplexStage();
    }

    public ComplexStage setTitle(String title) {
        this.title = title;
        return this;
    }

    public ComplexStage setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public ComplexStage addLeftPanelElement(LeftPanelElement element) {
        element.build();
        return this;
    }

    public List<LeftPanelElement> getLeftPanelElements() {
        return this.leftPanelElements;
    }

    private void bindListeners() {
        this.stage.widthProperty().addListener(this.onStageWidthChange);
        this.stage.heightProperty().addListener(this.onStageHeightChange);
        this.stage.getScene().addEventFilter(this.onESCPress.getKey(), this.onESCPress.getValue());
        LOGGER.info("Listeners have been bound to "+this.stage.getTitle());
    }
    private void unbindListeners() {
        this.stage.widthProperty().removeListener(this.onStageWidthChange);
        this.stage.heightProperty().removeListener(this.onStageHeightChange);
        this.stage.getScene().removeEventFilter(this.onESCPress.getKey(), this.onESCPress.getValue());

        this.onESCPress = SimplePair.of(null, null);
        this.onStageWidthChange = null;
        this.onStageHeightChange = null;
        LOGGER.info("Listeners have been removed from "+this.stage.getTitle());
    }

    private void configureListeners(){
        this.onStageWidthChange = (obs, oldVal, newVal) -> {
            double newWidth = newVal.doubleValue();

            //Separator
            this.separator.setStartX(newWidth / 5);
            this.separator.setEndX(newWidth / 5);

            for(LeftPanelElement element : this.getLeftPanelElements()){
                element.button.setPrefWidth(this.separator.getStartX());
            }

            //Second Pane
            this.secondaryRoot.setLayoutX(this.separator.getStartX());
            this.secondaryRoot.setPrefWidth(this.stage.getWidth() - (this.separator.getStrokeWidth() / 2));
            this.secondaryRootStage.setX(this.separator.getStartX());
            this.secondaryRootStage.setWidth(this.stage.getWidth() - (this.separator.getStrokeWidth() / 2));

            //Left Panel
            this.leftPanel.setLayoutX(0);
            this.leftPanel.setPrefWidth(this.separator.getStartX() - (this.separator.getStrokeWidth() / 2));

            //Left Panel Support
            this.leftPanelSupport.setPrefWidth(this.separator.getStartX() - (this.separator.getStrokeWidth() / 2));
        };
        this.onStageHeightChange = (obs, oldVal, newVal) -> {
            double newHeight = newVal.doubleValue();
            this.separator.setStartY(this.titleBar.getSupportHeight());
            this.separator.setEndY(newHeight);

            //Secondary Pane
            this.secondaryRoot.setLayoutY(this.titleBar.getSupportHeight());
            this.secondaryRoot.setPrefHeight(this.stage.getHeight());
            this.secondaryRootStage.setY(this.titleBar.getSupportHeight());
            this.secondaryRootStage.setHeight(this.stage.getHeight());

            //Left Panel
            this.leftPanel.setLayoutY(this.titleBar.getSupportHeight());
            this.leftPanel.setPrefHeight(this.stage.getHeight() - this.titleBar.getSupportHeight());

            //Left Panel Support
            double height = 0;
            for(LeftPanelElement element : this.leftPanelElements){
                height += element.height == -1 ? LeftPanelElement.DEFAULT_HEIGHT : element.height;
            }
            this.leftPanelSupport.setPrefHeight(Math.max(this.stage.getHeight() - this.titleBar.getSupportHeight(), height));
        };

        this.onESCPress = SimplePair.of(
                KeyEvent.KEY_PRESSED, event -> {
                    if(event.getCode() == KeyCode.ESCAPE){
                        this.unbindListeners();
                        MainApp.closeStage(this.stage, Player.ANIMATIONS);
                    }
                }
        );

        this.bindListeners();
    }

    private void configureComponents() {
        //Separator
        this.separator = new Line(0, 0, 0, 0);
        this.separator.setStrokeWidth(5);
        this.separator.setFill(Color.WHITE);
        this.separator.setStroke(Color.WHITE);

        //Secondary Pane
        this.secondaryRoot.setStyle("-fx-background-color: red;");

        //Left Panel
        this.leftPanelSupport.setStyle("-fx-background-color: blue; -fx-border-color: blue;");
        this.leftPanel.setStyle("-fx-background: transparent; -fx-border-color: transparent;");
        this.leftPanel.setPannable(false);
        this.leftPanel.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.leftPanel.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.leftPanel.setContent(this.leftPanelSupport);

        this.fitComponentsToParentStage();
    }

    private void fitComponentsToParentStage() {
        Pane pane = (Pane) this.stage.getScene().getRoot();
        pane.getChildren().addAll(this.leftPanel, this.secondaryRoot, this.separator);
    }

    public void setMainContent(Stage stage) {
        this.secondaryRoot.getChildren().add(stage.getScene().getRoot());
        stage.show();
    }

    public void show() {
        this.titleBar = new WindowTitleBar(this.stage).useDefaultPresets()
                .setTitleString(this.title)
                .setButtonsArrangement(WindowTitleBar.ButtonsArrangement.EVERYTHING)
                .setOnClose(() -> {
                    this.unbindListeners();
                    MainApp.closeStage(this.stage, Player.ANIMATIONS);
                })
                .setSupportStyling(MainApp.childWindowTitleBarTheme);
        titleBar.linkToStage();
        this.stage.setWidth(850);
        this.stage.setHeight(500);
        MainApp.openStage(this.stage, Player.ANIMATIONS, true);
    }



}
