package me.eduard.musicplayer.Library.CustomComponents.ComplexStage;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public final class LeftPanelElement{

    Button button = new Button();
    private ComplexStage reference;
    private final Pane scrollPaneSupport;

    private EventHandler<MouseEvent> onMouseMove = event -> {};
    private EventHandler<MouseEvent> onMousePress = event -> {};
    private EventHandler<MouseEvent> onMouseRelease = event -> {};
    private EventHandler<MouseEvent> onMouseClicked = event -> {};
    private EventHandler<MouseEvent> onMouseExit = event -> {};

    private String text = "Example Text";
    public double height = -1;

    public static final double DEFAULT_HEIGHT = 45;

    private String style = null;

    private LeftPanelElement(ComplexStage reference) {
        this.reference = reference;
        this.scrollPaneSupport = reference.leftPanelSupport;
    }

    public static LeftPanelElement newBuilder(ComplexStage reference) {
        return new LeftPanelElement(reference);
    }

    public LeftPanelElement setText(String text) {
        this.text = text;
        return this;
    }

    public LeftPanelElement setReference(ComplexStage refference) {
        this.reference = refference;
        return this;
    }

    public LeftPanelElement setHeight(double height) {
        this.height = height;
        return this;
    }

    public LeftPanelElement setStyle(String style) {
        this.style = style;
        this.button.setStyle(style);
        return this;
    }

    public LeftPanelElement setOnMouseMove(EventHandler<MouseEvent> onMouseMove){
        this.onMouseMove = onMouseMove;
        return this;
    }
    public LeftPanelElement setOnMousePress(EventHandler<MouseEvent> onMousePress){
        this.onMousePress = onMousePress;
        return this;
    }
    public LeftPanelElement setOnMouseRelease(EventHandler<MouseEvent> onMouseRelease){
        this.onMouseRelease = onMouseRelease;
        return this;
    }
    public LeftPanelElement setOnMouseClicked(EventHandler<MouseEvent> onMouseClicked) {
        this.onMouseClicked = onMouseClicked;
        return this;
    }
    public LeftPanelElement setOnMouseExit(EventHandler<MouseEvent> onMouseExit) {
        this.onMouseExit = onMouseExit;
        return this;
    }

    public LeftPanelElement unbind() {
        this.button.setOnMouseClicked(null);
        this.onMouseClicked = null;
        this.button.setOnMouseMoved(null);
        this.onMouseMove = null;
        this.button.setOnMousePressed(null);
        this.onMousePress = null;
        this.button.setOnMouseReleased(null);
        this.onMouseRelease = null;
        this.button.setOnMouseExited(null);
        this.onMouseExit = null;
        this.scrollPaneSupport.getChildren().remove(this.button);
        this.reference.leftPanelElements.remove(this);
        double previousY = 0;
        for(LeftPanelElement element : reference.leftPanelElements){
            element.button.setLayoutY(previousY);
            element.button.setLayoutX(0);
            previousY += element.height == -1 ? DEFAULT_HEIGHT : element.height;
        }
        return this;
    }

    public void build() {
        double layoutY = 0;
        for(LeftPanelElement element : this.reference.leftPanelElements){
            layoutY += element.height == -1 ? DEFAULT_HEIGHT : element.height;
        }
        this.button.setStyle(this.style);
        this.button.setLayoutX(0);
        this.button.setText(this.text);
        this.button.setLayoutY(layoutY + this.button.getPrefHeight());
        this.button.setWrapText(true);
        this.button.setOnMouseClicked(this.onMouseClicked);
        this.button.setOnMouseMoved(this.onMouseMove);
        this.button.setOnMousePressed(this.onMousePress);
        this.button.setOnMouseReleased(this.onMouseRelease);
        this.button.setOnMouseExited(this.onMouseExit);
        this.button.setPrefWidth(this.reference.separator.getStartX() - (this.reference.separator.getStrokeWidth() / 2));
        this.button.setPrefHeight(this.height == -1 ? DEFAULT_HEIGHT : this.height);
        this.scrollPaneSupport.getChildren().add(this.button);
        this.reference.leftPanelElements.add(this);
    }
}
