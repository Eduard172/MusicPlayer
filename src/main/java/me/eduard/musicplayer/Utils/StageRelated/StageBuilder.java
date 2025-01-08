package me.eduard.musicplayer.Utils.StageRelated;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import me.eduard.musicplayer.Utils.Utilities;


@SuppressWarnings("unused") public class StageBuilder extends Pane {
    private final Scene scene;
    private Stage stage;
    private String CSS = null;

    private StageBuilder(){
        this.scene = new Scene(this);
        this.stage = new Stage();
        this.stage.setScene(this.scene);
    }

    public static StageBuilder newBuilder(){
        return new StageBuilder();
    }
    public StageBuilder styleSheet(String styleSheet){
        this.CSS = Utilities.getResource("StyleSheets/"+styleSheet+".css").toExternalForm();
        this.stage.getScene().getStylesheets().add(CSS);
        return this;
    }
    public StageBuilder withScene(Scene scene){
        this.stage.setScene(scene);
        if(this.CSS != null)
            this.stage.getScene().getStylesheets().add(this.CSS);
        return this;
    }
    public StageBuilder setMaxHeightAndWidth(double height, double width){
        this.stage.setMaxHeight(height);
        this.stage.setMaxWidth(width);
        return this;
    }
    public StageBuilder removeUpperBar(){
        this.stage.initStyle(StageStyle.UNDECORATED);
        return this;
    }
    public StageBuilder addOnKeyPressEvent(EventHandler<? super KeyEvent> eventHandler){
        this.scene.setOnKeyPressed(eventHandler);
        return this;
    }
    public StageBuilder onCloseRequest(EventHandler<WindowEvent> eventHandler){
        this.stage.setOnCloseRequest(eventHandler);
        return this;
    }
    public StageBuilder addComponent(Node node){
        this.getChildren().add(node);
        return this;
    }
    public StageBuilder setMinHeightAndWidth(double height, double width){
        this.stage.setMinHeight(height);
        this.stage.setMinWidth(width);
        return this;
    }
    public StageBuilder setCentralized(){
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        double centerX = screenBounds.getMinX() - screenBounds.getWidth() / 2;
        double centerY = screenBounds.getMinY() - screenBounds.getHeight() / 2;
        this.stage.setX(centerX - this.stage.getWidth() / 2);
        this.stage.setY(centerY - this.stage.getHeight() / 2);
        return this;
    }
    public StageBuilder setRenderScales(double X, double Y){
        this.stage.setRenderScaleX(X);
        this.stage.setRenderScaleY(Y);
        return this;
    }
    public StageBuilder setXY(double X, double Y){
        this.stage.setX(X);
        this.stage.setY(Y);
        return this;
    }
    public StageBuilder setHeightWidth(double height, double width){
        this.stage.setHeight(height);
        this.stage.setWidth(width);
        return this;
    }
    public StageBuilder fullScreenHint(String hint){
        this.stage.setFullScreenExitHint(hint);
        return this;
    }
    public StageBuilder fullScreen(boolean setFullScreen){
        this.stage.setFullScreen(setFullScreen);
        return this;
    }
    public StageBuilder maximized(boolean maximizable){
        this.stage.setMaximized(maximizable);
        return this;
    }
    public StageBuilder resizable(boolean resizable){
        this.stage.setResizable(resizable);
        return this;
    }
    public StageBuilder alwaysOnTop(boolean alwaysOnTop){
        this.stage.setAlwaysOnTop(alwaysOnTop);
        return this;
    }
    public StageBuilder title(String title){
        this.stage.setTitle(title);
        return this;
    }
    public StageBuilder exitKeyCombination(String combination){
        this.stage.setFullScreenExitKeyCombination(KeyCombination.valueOf(combination));
        return this;
    }
    public StageBuilder icon(String iconPath){
        Image image = new Image(Utilities.getResourceAsStream(iconPath));
        this.stage.getIcons().add(image);
        return this;
    }
    public StageBuilder opacity(double opacity){
        this.stage.setOpacity(opacity);
        return this;
    }

    public StageBuilder color(String color){
        this.setStyle("-fx-background-color: "+color+";");
        return this;
    }
    public StageBuilder byActionEvent(ActionEvent event){
        if(event != null)
            this.stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        return this;
    }
    public StageBuilder byAnotherStage(Stage stage){
        if(stage != null)
            this.stage = (Stage) stage.getScene().getWindow();
        return this;
    }
    public Stage buildAndGet(boolean excludeScene){
        if(!excludeScene)
            this.stage.setScene(this.scene);
        return this.stage;
    }
    public Stage buildAndGet(){
        return this.buildAndGet(this.scene != null);
    }
    public Stage getStage(){
        return this.stage;
    }
    public Pane getRoot(){
        return this;
    }
    public Scene get_Scene(){
        return this.scene;
    }
}
