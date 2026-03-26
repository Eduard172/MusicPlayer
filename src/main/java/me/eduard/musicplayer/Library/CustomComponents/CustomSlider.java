package me.eduard.musicplayer.Library.CustomComponents;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class CustomSlider {

    private Rectangle base;
    private Rectangle progressed;
    private Rectangle thumb;

    private Pane pane;
    private final Stage targetStage;

    private final DoubleProperty widthProperty = new SimpleDoubleProperty(500);
    private final DoubleProperty heightProperty = new SimpleDoubleProperty(40);
    private final DoubleProperty valueProperty = new SimpleDoubleProperty(0);
    private final DoubleProperty layoutXProperty = new SimpleDoubleProperty(0);
    private final DoubleProperty layoutYProperty = new SimpleDoubleProperty(0);

    private final DoubleProperty thumbWidthProperty = new SimpleDoubleProperty(10);

    private ChangeListener<Number> onWidthChange = null;
    private ChangeListener<Number> onHeightChange = null;
    private ChangeListener<Number> onValueChange = null;
    private ChangeListener<Number> onLayoutXChange = null;
    private ChangeListener<Number> onLayoutYChange = null;
    private ChangeListener<Number> onThumbWidthChange = null;

    public CustomSlider(Stage targetStage){
        this.targetStage = targetStage;
        this.init();
    }

    private void init() {
        this.base = new Rectangle();
        this.progressed = new Rectangle();
        this.thumb = new Rectangle();
        this.pane = new Pane();
        this.setupListeners();
        this.arrangeComponents();
    }

    public void setThumbWidth(double width) {
        this.thumbWidthProperty.set(width);
    }
    public void setWidth(double width) {
        this.widthProperty.set(width);
    }
    public void setHeight(double height){
        this.heightProperty.set(height);
    }
    public void setValue(double value){
        this.valueProperty.set(value);
    }
    public void setLayoutX(double layoutX){
        this.layoutXProperty.set(layoutX);
    }
    public void setLayoutY(double layoutY){
        this.layoutYProperty.set(layoutY);
    }
    private void arrangeComponents() {
        this.base.setLayoutX(0);
        this.base.setLayoutY(0);
        this.progressed.setLayoutX(0);
        this.progressed.setLayoutY(0);
        this.thumb.setLayoutY(0);
        this.base.setFill(Color.LIME);
        this.progressed.setFill(Color.RED);
        this.thumb.setFill(Color.BLACK);
    }
    public void linkToStage() {
        this.bindListeners();
        this.pane.getChildren().addAll(
                this.base, this.progressed, this.thumb
        );
        ((Pane) this.targetStage.getScene().getRoot()).getChildren().addAll(
                this.pane
        );
    }
    public void unlinkFromStage() {
        this.unbindListeners();
        this.pane.getChildren().removeAll(
                this.base, this.progressed, this.thumb
        );
        ((Pane) this.targetStage.getScene().getRoot()).getChildren().remove(this.pane);
    }

    private void setupListeners() {
        //TotalWidth ... 100%
        //? Width ... X%
        this.onValueChange = (obs, oldVal, newVal) -> {
            double layoutX = ((newVal.doubleValue() * this.widthProperty.get()) / 100) - (this.thumbWidthProperty.get() / 2);
            this.thumb.setLayoutX(layoutX);
            this.progressed.setWidth(this.thumb.getLayoutX());
        };
        //Create listeners
    }
    private void bindListeners() {
        this.base.widthProperty().bind(this.widthProperty);
        this.pane.prefWidthProperty().bind(this.widthProperty);

        this.base.heightProperty().bind(this.heightProperty);
        this.pane.prefHeightProperty().bind(this.heightProperty);
        this.progressed.heightProperty().bind(this.heightProperty);
        this.thumb.heightProperty().bind(this.heightProperty);

        this.pane.layoutXProperty().bind(this.layoutXProperty);
        this.pane.layoutYProperty().bind(this.layoutYProperty);

        this.thumb.widthProperty().bind(this.thumbWidthProperty);

        this.valueProperty.addListener(this.onValueChange);
    }
    private void unbindListeners() {
        this.base.widthProperty().unbind();
        this.base.heightProperty().unbind();
        this.pane.prefWidthProperty().unbind();
        this.pane.prefHeightProperty().unbind();
        this.progressed.heightProperty().unbind();
        this.thumb.heightProperty().unbind();
        this.pane.layoutXProperty().unbind();

        this.onWidthChange = null;
        this.onHeightChange = null;
        this.onValueChange = null;
        this.onThumbWidthChange = null;
        this.onLayoutXChange = null;
        this.onLayoutYChange = null;
    }


}
