package me.eduard.musicplayer.Library.CustomComponents;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.NodeUtils;
import me.eduard.musicplayer.Utils.Utilities;

/**
 * This window helps in creating automatic title bars in JavaFX applications.
 * <p>
 * Its purpose is to help reducing time-consuming tasks such as manual implementation of such feature.
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class WindowTitleBar {

    private boolean isSetup = false;
    private final ChangeListener<Number> widthChange = (obs, old, newVal) -> this.fitComponents();

    public enum ButtonsViewType{
        IMAGED, TEXT
    }

    private Stage workingStage;

    private Rectangle support;
    private Label title;
    private Button closeButton, minimizeButton, maximizeButton;
    private ImageView closeImage, minimizeImage, maximizeImage;
    private String closeText, minimizeText, maximizeText, titleString;
    private double supportHeight = 30;
    private double buttonsWidth = 90;
    private ButtonsViewType buttonsViewType = ButtonsViewType.TEXT;

    private double xOffset = 0.0d;
    private double yOffset = 0.0d;

    public WindowTitleBar(){
        this.initializeComponents();
    }
    public WindowTitleBar(Stage workingStage){
        this();
        this.setWorkingStage(workingStage);
    }
    public void setWorkingStage(Stage workingStage){
        this.workingStage = workingStage;
    }

    private void initializeComponents(){
        this.support = new Rectangle();
        this.title = new Label();
        this.closeButton = new Button();
        this.maximizeButton = new Button();
        this.minimizeButton = new Button();
        this.closeImage = new ImageView();
        this.minimizeImage = new ImageView();
        this.maximizeImage = new ImageView();
    }

    public WindowTitleBar setTitleString(String title){
        if(this.isNonNull(title)){
            this.titleString = title;
            this.title.setText(title);
        }
        return this;
    }
    public String getTitleString(){
        return this.titleString;
    }

    public WindowTitleBar setCloseText(String text){
        if(this.isNonNull(text)){
            this.closeText = text;
            this.closeButton.setText(text);
        }
        return this;
    }
    public String getCloseText(){
        return this.closeText;
    }
    public WindowTitleBar setMinimizeText(String text){
        if(this.isNonNull(text)){
            this.minimizeText = text;
            this.minimizeButton.setText(text);
        }
        return this;
    }
    public String getMinimizeText(){
        return this.minimizeText;
    }
    public WindowTitleBar setMaximizeText(String text){
        if(this.isNonNull(text)){
            this.maximizeText = text;
            this.maximizeButton.setText(text);
        }
        return this;
    }
    public String getMaximizeText(){
        return this.maximizeText;
    }
    public WindowTitleBar setCloseImage(String path){
        if(this.isNonNull(path)){
            Utilities.setImageViewWithFullPath(this.closeImage, path);
            Utilities.centralizeImageViewOnButton(this.closeImage, this.closeButton);
        }
        return this;
    }
    public String getCloseImagePath(){
        return this.closeImage.getImage().getUrl();
    }
    public WindowTitleBar setMinimizeImage(String path){
        if(this.isNonNull(path)){
            Utilities.setImageViewWithFullPath(this.minimizeImage, path);
            Utilities.centralizeImageViewOnButton(this.minimizeImage, this.minimizeButton);
        }
        return this;
    }
    public String getMinimizeImagePath(){
        return this.minimizeImage.getImage().getUrl();
    }
    public WindowTitleBar setMaximizeImage(String path){
        if(this.isNonNull(path)){
            Utilities.setImageViewWithFullPath(this.maximizeImage, path);
            Utilities.centralizeImageViewOnButton(this.maximizeImage, this.maximizeButton);
        }
        return this;
    }
    public String getMaximizeImagePath(){
        return this.maximizeImage.getImage().getUrl();
    }
    public WindowTitleBar setButtonsViewType(ButtonsViewType buttonsViewType){
        this.buttonsViewType = buttonsViewType;
        this.adjustButtonAppearanceBasedOnViewType(this.buttonsViewType);
        return this;
    }

    /**
     * <h>This method should be used after the {@link WindowTitleBar#linkToStage()} method.</h>
     */
    public WindowTitleBar autoAdjustImageViews(boolean respectAspectRatio, boolean performCentralization){
        Platform.runLater(() -> Utilities.sleep(Duration.millis(50), 1, run -> {
            Utilities.adjustImageViewSize(this.closeImage, closeButton.getWidth() - 4, closeButton.getHeight() - 4, respectAspectRatio);
            Utilities.adjustImageViewSize(this.minimizeImage, minimizeButton.getWidth() - 4, minimizeButton.getHeight() - 4, respectAspectRatio);
            Utilities.adjustImageViewSize(this.maximizeImage, maximizeButton.getWidth() - 4, maximizeButton.getHeight() - 4, respectAspectRatio);
            if(performCentralization){
                Utilities.centralizeImageViewOnButton(this.closeImage, this.closeButton);
                Utilities.centralizeImageViewOnButton(this.minimizeImage, this.minimizeButton);
                Utilities.centralizeImageViewOnButton(this.maximizeImage, this.maximizeButton);
            }
        }, null));
        return this;
    }
    public WindowTitleBar centralizeImageToButtons(){
        Platform.runLater(() -> {
            Utilities.centralizeImageViewOnButton(this.closeImage, this.closeButton);
            Utilities.centralizeImageViewOnButton(this.minimizeImage, this.minimizeButton);
            Utilities.centralizeImageViewOnButton(this.maximizeImage, this.maximizeButton);
        });
        return this;
    }
    public ButtonsViewType getButtonsViewType(){
        return this.buttonsViewType;
    }
    public WindowTitleBar setSupportHeight(double height){
        this.supportHeight = height;
        return this;
    }
    public double getSupportHeight(){
        return this.supportHeight;
    }
    public WindowTitleBar setButtonsWidth(double width){
        this.buttonsWidth = width;
        return this;
    }
    public double getButtonsWidth(){
        return this.buttonsWidth;
    }
    public Button getCloseButton(){
        return this.closeButton;
    }
    public Button getMinimizeButton(){
        return this.minimizeButton;
    }
    public Button getMaximizeButton(){
        return this.maximizeButton;
    }
    public WindowTitleBar setGeneralButtonsStyling(String... css){
        String style = Utilities.stringFromArray(css, "", ";");
        this.closeButton.setStyle(style);
        this.minimizeButton.setStyle(style);
        this.maximizeButton.setStyle(style);
        return this;
    }
    public ImageView getCloseImage(){
        return this.closeImage;
    }
    public ImageView getMinimizeImage(){
        return this.minimizeImage;
    }
    public ImageView getMaximizeImage(){
        return this.maximizeImage;
    }
    public WindowTitleBar setCloseButtonStyling(String... css){
        String style = Utilities.stringFromArray(css, "", ";");
        this.closeButton.setStyle(style);
        return this;
    }
    public WindowTitleBar setMinimizeButtonStyling(String... css){
        String style = Utilities.stringFromArray(css, "", ";");
        this.minimizeButton.setStyle(style);
        return this;
    }
    public WindowTitleBar setMaximizeButtonStyling(String... css){
        String style = Utilities.stringFromArray(css, "", ";");
        this.maximizeButton.setStyle(style);
        return this;
    }
    public WindowTitleBar setTitleLabelStyling(String... css){
        String style = Utilities.stringFromArray(css, "", ";");
        this.title.setStyle(style);
        return this;
    }
    public WindowTitleBar setSupportStyling(String... css){
        String style = Utilities.stringFromArray(css, "", ";");
        this.support.setStyle(style);
        return this;
    }
    /**
     * <h>This method should be used after the {@link WindowTitleBar#linkToStage()} method.</h>
     */
    public WindowTitleBar moveButtonNextTo(Button node1, Button node2, boolean toRight){
        Platform.runLater(() -> Utilities.sleep(Duration.millis(50), 2, run -> {
            if(toRight)
                node1.setLayoutX(node2.getLayoutX() + node2.getPrefWidth());
            else
                node1.setLayoutX(node2.getLayoutX() - node1.getPrefWidth());
        }, null));
        return this;
    }
    /**
     * <h>This method should be used after the {@link WindowTitleBar#linkToStage()} method.</h>
     */
    public WindowTitleBar switchNodesPositions(Node node1, Node node2){
        Utilities.sleep(Duration.millis(50), 1, run -> {
            double node1LayoutX = node1.getLayoutX();
            node1.setLayoutX(node2.getLayoutX());
            node2.setLayoutX(node1LayoutX);
        }, null);
        return this;
    }
    public WindowTitleBar removeNode(Node node){
        ((Pane) this.workingStage.getScene().getRoot()).getChildren().remove(node);
        return this;
    }
    public WindowTitleBar addNode(Node node){
        ((Pane) this.workingStage.getScene().getRoot()).getChildren().add(node);
        return this;
    }

    public void linkToStage(){
        this.fitComponents();
        if(!this.isSetup){
            this.workingStage.widthProperty().addListener(this.widthChange);
            this.addEventListeners();
        }
        this.addToStageChildren();
    }

    public void unlinkFromStage(){
        this.workingStage.widthProperty().removeListener(this.widthChange);
        this.removeFromStageChildren();
    }


    private void fitComponents(){
        Runnable runnable = () -> {
            this.support.setWidth(this.workingStage.getWidth());
            this.support.setHeight(this.supportHeight);
            this.support.setLayoutY(0);
            this.support.setLayoutX(0);
            //Buttons binding
            this.adjustButtonAppearanceBasedOnViewType(this.buttonsViewType);
            this.closeButton.setLayoutY(0);
            this.closeButton.setLayoutX(this.support.getWidth() - this.buttonsWidth);
            this.maximizeButton.setLayoutY(0);
            this.maximizeButton.setLayoutX(this.support.getWidth() - (this.buttonsWidth * 2));
            this.minimizeButton.setLayoutY(0);
            this.minimizeButton.setLayoutX(this.support.getWidth() - (this.buttonsWidth * 3));
            //Label binding
            this.title.setPrefWidth(this.support.getWidth() - 10 - (buttonsWidth * 3));
            this.title.setLayoutX(10);
            // Old title LayoutX: 10
            this.title.setLayoutY((this.supportHeight / 2) - (this.title.getHeight() / 2));
            this.isSetup = true;
        };
        Utilities.sleep(Duration.millis(40), 1, run -> Platform.runLater(runnable), null);
    }

    private void adjustButtonAppearanceBasedOnViewType(ButtonsViewType buttonsViewType){
        this.closeButton.setPrefWidth(this.buttonsWidth);
        this.closeButton.setPrefHeight(this.supportHeight);
        this.minimizeButton.setPrefWidth(this.buttonsWidth);
        this.minimizeButton.setPrefHeight(this.supportHeight);
        this.maximizeButton.setPrefWidth(this.buttonsWidth);
        this.maximizeButton.setPrefHeight(this.supportHeight);
        if(buttonsViewType == ButtonsViewType.TEXT){
            this.closeImage.setVisible(false);
            this.minimizeImage.setVisible(false);
            this.maximizeImage.setVisible(false);
            this.closeButton.setText(this.closeText);
            this.minimizeButton.setText(this.minimizeText);
            this.maximizeButton.setText(this.maximizeText);
        }else{
            this.closeButton.setText("");
            this.minimizeButton.setText("");
            this.maximizeButton.setText("");
            this.closeImage.setVisible(true);
            this.minimizeImage.setVisible(true);
            this.maximizeImage.setVisible(true);
        }
    }
    private void addEventListeners(){
        this.setDefaultTaskbarButtonsBehaviour();
        this.support.setOnMousePressed(this::onMousePress);
        this.title.setOnMousePressed(this::onMousePress);
        this.support.setOnMouseDragged(this::onTaskbarDrag);
        this.title.setOnMouseDragged(this::onTaskbarDrag);
    }
    private void onMousePress(MouseEvent event){
        this.xOffset = event.getSceneX();
        this.yOffset = event.getSceneY();
    }
    private void onTaskbarDrag(MouseEvent event){
        this.workingStage.setX(event.getScreenX() - this.xOffset);
        this.workingStage.setY(event.getScreenY() - this.yOffset);
    }
    private void setDefaultTaskbarButtonsBehaviour(){
        this.closeButton.setOnAction(event -> MainApp.closeStage(this.workingStage, Player.ANIMATIONS));
        this.minimizeButton.setOnAction(event -> this.workingStage.setIconified(!this.workingStage.isIconified()));
        this.closeButton.setCursor(Cursor.HAND);
        this.minimizeButton.setCursor(Cursor.HAND);
        NodeUtils.setTooltip(this.closeButton, "Closes this window.", 200);
        NodeUtils.setTooltip(this.minimizeButton, "Minimizes this window.", 200);
    }

    private void addToStageChildren(){
        ((Pane) this.workingStage.getScene().getRoot()).getChildren().addAll(
                this.support,
                this.title,
                this.closeImage,
                this.minimizeImage,
                this.maximizeImage,
                this.closeButton,
                this.minimizeButton,
                this.maximizeButton
        );
    }
    private void removeFromStageChildren(){
        ((Pane) this.workingStage.getScene().getRoot()).getChildren().removeAll(
                this.support,
                this.title,
                this.closeButton,
                this.minimizeButton,
                this.maximizeButton,
                this.closeImage,
                this.minimizeImage,
                this.maximizeImage
        );
    }
    public WindowTitleBar useDefaultPresets(){
        this.setButtonsWidth(35)
            .setSupportHeight(30)
            .setButtonsViewType(WindowTitleBar.ButtonsViewType.IMAGED)
            .setTitleString("TitleBar")
            .setCloseImage("icons/TopBar/XButton2.png")
            .setMinimizeImage("icons/TopBar/MinimizeIcon.png");
        return this;
    }
    public WindowTitleBar applyAfterLinkPresets(){
        this.setSupportStyling("-fx-fill: #808080").setGeneralButtonsStyling("-fx-background-color: transparent");
        this.removeNode(this.getMaximizeButton());
        this.moveButtonNextTo(this.getMinimizeButton(), this.getCloseButton(), false);
        this.autoAdjustImageViews(true, true);
        return this;
    }

    private boolean isNonNull(Object object){
        return object != null;
    }
}
