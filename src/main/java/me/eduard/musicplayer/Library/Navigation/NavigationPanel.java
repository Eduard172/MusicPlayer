package me.eduard.musicplayer.Library.Navigation;

import javafx.beans.value.ChangeListener;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.Library.CustomComponents.WindowTitleBar;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;

@SuppressWarnings("unused")
public class NavigationPanel {

    private AnchorPane topSide;
    private AnchorPane leftSide;
    private AnchorPane mainSide;

    private WindowTitleBar titleBar;
    private Stage stage;

    private ChangeListener<Number> onWidthChange = null;
    private ChangeListener<Number> onHeightChange = null;

    public NavigationPanel() {
        this.init();
    }

    private void init() {
        StageBuilder builder = StageBuilder.newBuilder()
                .icon("icons/icon.png")
                .resizable(false)
                .color("#151515")
                .removeUpperBar();
        stage = builder.buildAndGet();
        assert stage != null;

        titleBar = new WindowTitleBar(stage).useDefaultPresets()
                .setTitleString("My Stage")
                .setSupportStyling(MainApp.childWindowTitleBarTheme)
                .setOnClose(() -> MainApp.closeStage(stage, Player.ANIMATIONS))
                .setButtonsArrangement(WindowTitleBar.ButtonsArrangement.EVERYTHING)
                .setTitleLabelStyling("-fx-text-fill: #000000");
        titleBar.linkToStage();

        stage.setWidth(1080);
        stage.setHeight(600);
        this.setupListeners();
        stage.setOnCloseRequest(event -> {
            stage.widthProperty().removeListener(onWidthChange);
            stage.heightProperty().removeListener(onHeightChange);
            this.removeChildren();
            System.out.println("Nav Panel closed.");
        });
        stage.widthProperty().addListener(onWidthChange);
        stage.heightProperty().addListener(onHeightChange);

        MainApp.openStage(stage, Player.ANIMATIONS, true);
        this.setupSides();
    }

    public AnchorPane getLeftSide() {
        return leftSide;
    }

    private void setupSides() {
        final double yOffset = this.titleBar.getSupportHeight();

        this.topSide = new AnchorPane();
        this.topSide.setBackground(Background.fill(Color.YELLOW));
        this.topSide.setPrefSize(stage.getWidth(), 60);
        this.topSide.setLayoutX(0);
        this.topSide.setLayoutY(yOffset);

        this.leftSide = new AnchorPane();
        this.leftSide.setBackground(Background.fill(Color.ORANGE));
        this.leftSide.setPrefSize(200 ,stage.getHeight() - yOffset - topSide.getPrefHeight());
        this.leftSide.setLayoutX(0);
        this.leftSide.setLayoutY(topSide.getLayoutY() + topSide.getPrefHeight());

        this.mainSide = new AnchorPane();
        this.mainSide.setBackground(Background.fill(Color.RED));
        this.mainSide.setPrefSize(stage.getWidth() - leftSide.getPrefWidth(), stage.getHeight() - yOffset - topSide.getPrefHeight());
        this.mainSide.setLayoutX(this.leftSide.getPrefWidth());
        this.mainSide.setLayoutY(yOffset + this.topSide.getPrefHeight());

        this.addChildren();
    }

    private void setupListeners() {
        this.onWidthChange = (obs, oldVal, newVal) -> {
            this.topSide.setPrefWidth(newVal.doubleValue());
            this.mainSide.setPrefWidth(newVal.doubleValue() - leftSide.getPrefWidth());
        };
        this.onHeightChange = (obs, oldVal, newVal) -> {
            this.leftSide.setPrefHeight(newVal.doubleValue() - topSide.getPrefHeight());
            this.mainSide.setPrefHeight(newVal.doubleValue() - topSide.getPrefHeight());
        };
    }

    private void addChildren(){
        ((Pane) stage.getScene().getRoot()).getChildren().addAll(
                this.topSide,
                this.leftSide,
                this.mainSide
        );
    }
    private void removeChildren() {
        ((Pane) (stage.getScene().getRoot())).getChildren().removeAll(
                this.topSide,
                this.leftSide,
                this.mainSide
        );
    }

}
