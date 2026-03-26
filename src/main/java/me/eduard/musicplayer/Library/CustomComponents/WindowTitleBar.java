package me.eduard.musicplayer.Library.CustomComponents;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.eduard.musicplayer.Library.BasicAnimator;
import me.eduard.musicplayer.Library.SimplePair;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.NodeUtils;
import me.eduard.musicplayer.Utils.Utilities;

/**
 * This window helps in creating automatic title bars in JavaFX applications.
 * <p>
 * Its purpose is to help reducing time-consuming tasks such as manual implementation of such feature.
 */
@SuppressWarnings({"unused", "UnusedReturnValue", "SpellCheckingInspection"})
public final class WindowTitleBar {

    public enum ButtonsArrangement {
        /**
         * Disables ALL buttons (Close, Minimize, Maximize)
         */
        NONE,
        /**
         * Enables the Close button only.
         */
        CLOSE,
        /**
         * Enables the Close and Minimize buttons only.
         */
        CLOSE_AND_MINIMIZE,
        /**
         * Enables ALL buttons (Close, Minimize, Maximize)
         */
        EVERYTHING
    }

    public enum ViewMode {
        ABOVE(-1), NORMAL(0), BEHIND(1);
        private final int value;
        ViewMode(int value){
            this.value = value;
        }
        public int getValue(){
            return this.value;
        }
    }

    private boolean isSetup = false;
    private final ChangeListener<Number> widthChange = (obs, old, newVal) -> this.fitComponents();

    public enum ButtonsViewType{
        IMAGED, TEXT
    }

    //Flags used for fullscreen
    private boolean ignoreDrag = false;
    private boolean centralizeOnDrag = false;
    private boolean prepareToFullScreen = false;
    //

    public static final String KEEP_AS_ORIGINAL_TITLE = "null";

    private Stage workingStage;

    private Rectangle support;
    private Label title;
    private Button closeButton, minimizeButton, maximizeButton;
    private ImageView closeImage, minimizeImage, maximizeImage, titleBarImage;
    private String closeText, minimizeText, maximizeText, titleString, fullScreenWarnText;
    private String generalBtnStyling, closeBtnStyling, minimizeBtnStyling, maximizeBtnStyling, titleStyling, supportStyling, fullScreenWarnStyling;
    private String onFSP_generalBtnStyling, onFSP_closeBtnStyling, onFSP_minimizeBtnStyling, onFSP_maximizeBtnStyling, onFSP_titleStyling, onFSP_supportStyling, onFSP_title;
    private double supportHeight = 30;
    private double buttonsWidth = 90;
    private ButtonsViewType buttonsViewType = ButtonsViewType.TEXT;
    private ButtonsArrangement buttonsArrangement = ButtonsArrangement.EVERYTHING;
    private ViewMode viewMode = ViewMode.NORMAL;
    private boolean isFullScreen = false;
    private final SimplePair<Double, Double> workingStageBoundaries = SimplePair.of(-1.0, -1.0);
    private final SimplePair<Double, Double> lastStageCoords = SimplePair.of(0.0, 0.0);
    private Runnable onFullScreenEnable = null;
    private Runnable onFullScreenDisable = null;
    private Runnable onMinimize = null;
    private Runnable onClose = null;
    private Runnable onFullScreenPrepare = null;
    private Runnable onFullScreenPrepareExit = null;
    private boolean onFSPTitleAsOriginal = false;
    private Label fullScreenWarn;
    private BasicAnimator fullScreenLabelWarn = null;

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
        this.titleBarImage = new ImageView((Image) null);
        this.fullScreenWarn = new Label("");
        this.fullScreenLabelWarn = BasicAnimator.of(this.fullScreenWarn, 20, 1.0, 0.1);

        //Disable buttons visibility to avoid huge icons before app ticking happens
        this.closeImage.setVisible(false);
        this.minimizeImage.setVisible(false);
        this.maximizeImage.setVisible(false);
        this.fullScreenWarn.setVisible(false);
        this.titleBarImage.setVisible(false);
    }

    public WindowTitleBar setTitleString(String title){
        if(this.isNonNull(title)){
            this.titleString = title;
            if(!this.isNonNull(this.onFSP_title))
                this.onFSP_title = "";
            if(this.onFSPTitleAsOriginal)
                this.onFSP_title = this.titleString;
            this.title.setText(title);
        }
        return this;
    }
    public String getTitleString(){
        return this.titleString;
    }
    public BasicAnimator getFullScreenLabelAnimator(){
        return this.fullScreenLabelWarn;
    }
    public WindowTitleBar setFullScreenWarnText(String fullScreenWarnText){
        if(this.isNonNull(fullScreenWarnText)){
            this.fullScreenWarnText = fullScreenWarnText;
            this.fullScreenWarn.setText(fullScreenWarnText);
        }
        return this;
    }
    public WindowTitleBar setFullScreenWarnFont(Font font){
        if(this.isNonNull(font))
            this.fullScreenWarn.setFont(font);
        return this;
    }
    public WindowTitleBar setFullScreenWarnFont(double fontSize){
        if(fontSize > 1){
            this.fullScreenWarn.setFont(Font.font(fontSize));
        }
        return this;
    }
    public String getFullScreenWarnText(){
        return this.fullScreenWarnText;
    }

    public WindowTitleBar setCloseText(String text){
        if(this.isNonNull(text)){
            this.closeText = text;
            this.closeButton.setText(text);
        }
        return this;
    }

    public WindowTitleBar setButtonsArrangement(ButtonsArrangement buttonsArrangement){
        this.buttonsArrangement = buttonsArrangement;
        if(this.isSetup)
            this.arrangeButtons();
        return this;
    }

    public WindowTitleBar setViewMode(ViewMode viewMode){
        this.viewMode = viewMode;
        return this;
    }

    public ViewMode getViewMode(){
        return this.viewMode;
    }

    public ButtonsArrangement getButtonsArrangement(){
        return this.buttonsArrangement;
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
    public WindowTitleBar setOnFullScreenEnable(Runnable onFullScreenEnable){
        this.onFullScreenEnable = onFullScreenEnable;
        return this;
    }
    public WindowTitleBar setOnFullScreenPrepare(Runnable onFullScreenPrepare){
        this.onFullScreenPrepare = onFullScreenPrepare;
        return this;
    }
    public WindowTitleBar setOnFullScreenPrepareExit(Runnable onFullScreenPrepareExit){
        this.onFullScreenPrepareExit = onFullScreenPrepareExit;
        return this;
    }
    public WindowTitleBar setOnFullScreenDisable(Runnable onFullScreenDisable){
        this.onFullScreenDisable = onFullScreenDisable;
        return this;
    }
    public WindowTitleBar setOnMinimize(Runnable onMinimize){
        this.onMinimize = onMinimize;
        return this;
    }
    public WindowTitleBar setOnClose(Runnable onClose){
        this.onClose = onClose;
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
    public WindowTitleBar setTitleBarImage(String path) {
        if(this.isNonNull(path)){
            Utilities.setImageViewWithFullPath(this.titleBarImage, path);
        }else{
            this.titleBarImage.setImage(null);
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
    public String getGeneralButtonsStyling(){
        return this.generalBtnStyling;
    }
    public String getCloseButtonStyling(){
        return this.closeBtnStyling;
    }
    public String getMinimizeButtonsStyling(){
        return this.minimizeBtnStyling;
    }
    public String getMaximizeButtonsStyling(){
        return this.maximizeBtnStyling;
    }
    public String getTitleStyling(){
        return this.titleStyling;
    }
    public String getSupportStyling(){
        return this.supportStyling;
    }
    public WindowTitleBar setFullScreenWarnLabelStyling(String... css){
        this.fullScreenWarnStyling = Utilities.stringFromArray(css, "", ";");
        return this;
    }
    public String getFullScreenWarnStyling(){
        return this.fullScreenWarnStyling;
    }
    public WindowTitleBar setOnFSPGeneralButtonsStyling(String... css){
        this.onFSP_generalBtnStyling = Utilities.stringFromArray(css, "", ";");
        return this;
    }
    public String getOnFSPGeneralButtonStyling(){
        return this.onFSP_generalBtnStyling;
    }
    public WindowTitleBar setOnFSPCloseButtonStyling(String... css){
        this.onFSP_closeBtnStyling = Utilities.stringFromArray(css, "", ";");
        return this;
    }
    public String getOnFSPCloseButtonStyling(){
        return this.onFSP_generalBtnStyling;
    }
    public WindowTitleBar setOnFSPMinimizeButtonStyling(String... css){
        this.onFSP_minimizeBtnStyling = Utilities.stringFromArray(css, "", ";");
        return this;
    }
    public String getOnFSPMinimizeButtonStyling(){
        return this.onFSP_minimizeBtnStyling;
    }
    public WindowTitleBar setOnFSPMaximizeButtonStyling(String... css){
        this.onFSP_maximizeBtnStyling = Utilities.stringFromArray(css, "", ";");
        return this;
    }
    public String getOnFSPMaximizeButtonStyling(){
        return this.onFSP_maximizeBtnStyling;
    }
    public WindowTitleBar setOnFSPTitleStyling(String... css){
        this.onFSP_titleStyling = Utilities.stringFromArray(css, "", ";");
        return this;
    }
    public String getOnFSPTitleStyling(){
        return this.onFSP_titleStyling;
    }
    public WindowTitleBar setOnFSPSupportStyling(String... css){
        this.onFSP_supportStyling = Utilities.stringFromArray(css, "", ";");
        return this;
    }
    public String getOnFSPSupportStyling(){
        return this.onFSP_supportStyling;
    }
    public WindowTitleBar setOnFSPTitleString(String title){
        this.onFSPTitleAsOriginal = title != null && title.equals(KEEP_AS_ORIGINAL_TITLE);
        this.onFSP_title = this.onFSPTitleAsOriginal ? this.titleString : title;
        return this;
    }
    public String getOnFSPTitle(){
        return this.onFSP_title;
    }
    public WindowTitleBar setGeneralButtonsStyling(String... css){
        this.generalBtnStyling = Utilities.stringFromArray(css, "", ";");
        if(!this.isNonNull(this.onFSP_generalBtnStyling))
            this.onFSP_generalBtnStyling = this.generalBtnStyling;
        this.closeButton.setStyle(this.generalBtnStyling);
        this.minimizeButton.setStyle(this.generalBtnStyling);
        this.maximizeButton.setStyle(this.generalBtnStyling);
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
    public ImageView getTitleBarImage() {
        return this.titleBarImage;
    }
    public WindowTitleBar setCloseButtonStyling(String... css){
        this.closeBtnStyling = Utilities.stringFromArray(css, "", ";");
        if(!this.isNonNull(this.onFSP_closeBtnStyling))
            this.onFSP_closeBtnStyling = this.closeBtnStyling;
        this.closeButton.setStyle(this.closeBtnStyling);
        return this;
    }
    public WindowTitleBar setMinimizeButtonStyling(String... css){
        this.minimizeBtnStyling = Utilities.stringFromArray(css, "", ";");
        if(!this.isNonNull(this.onFSP_minimizeBtnStyling))
            this.onFSP_minimizeBtnStyling = this.minimizeBtnStyling;
        this.minimizeButton.setStyle(this.minimizeBtnStyling);
        return this;
    }
    public WindowTitleBar setMaximizeButtonStyling(String... css){
        this.maximizeBtnStyling = Utilities.stringFromArray(css, "", ";");
        if(!this.isNonNull(this.onFSP_maximizeBtnStyling))
            this.onFSP_maximizeBtnStyling = this.maximizeBtnStyling;
        this.maximizeButton.setStyle(this.maximizeBtnStyling);
        return this;
    }
    public WindowTitleBar setTitleLabelStyling(String... css){
        this.titleStyling = Utilities.stringFromArray(css, "", ";");
        if(!this.isNonNull(this.onFSP_titleStyling))
            this.onFSP_titleStyling = this.titleStyling;
        this.title.setStyle(this.titleStyling);
        return this;
    }
    public WindowTitleBar setSupportStyling(String... css){
        this.supportStyling = Utilities.stringFromArray(css, "", ";");
        if(!this.isNonNull(this.onFSP_supportStyling))
            this.onFSP_supportStyling = this.supportStyling;
        this.support.setStyle(this.supportStyling);
        return this;
    }
    public WindowTitleBar removeNodes(Node... nodes){
        for(Node node : nodes){
            ((Pane) this.workingStage.getScene().getRoot()).getChildren().remove(node);
        }
        return this;
    }
    public WindowTitleBar addNode(Node node){
        ((Pane) this.workingStage.getScene().getRoot()).getChildren().add(node);
        return this;
    }
    public Label getTitleLabel() {
        return this.title;
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

    public void requestDefaultStyling(){
        this.support.setStyle(this.supportStyling);
        this.title.setText(this.titleString);
        this.title.setStyle(this.titleStyling);
        this.closeButton.setStyle(this.generalBtnStyling != null ? this.generalBtnStyling : this.closeBtnStyling);
        this.maximizeButton.setStyle(this.generalBtnStyling != null ? this.generalBtnStyling : this.maximizeBtnStyling);
        this.minimizeButton.setStyle(this.generalBtnStyling != null ? this.generalBtnStyling : this.minimizeBtnStyling);
    }

    public void requestOnFullScreenPrepareStyling(){
        this.support.setStyle(this.onFSP_supportStyling);
        this.fullScreenWarn.setStyle(this.fullScreenWarnStyling);
        this.title.setText(this.onFSP_title);
        this.title.setStyle(this.onFSP_titleStyling);
        this.closeButton.setStyle(this.onFSP_generalBtnStyling != null ? this.onFSP_generalBtnStyling : this.onFSP_closeBtnStyling);
        this.maximizeButton.setStyle(this.onFSP_generalBtnStyling != null ? this.onFSP_generalBtnStyling : this.onFSP_maximizeBtnStyling);
        this.minimizeButton.setStyle(this.onFSP_generalBtnStyling != null ? this.onFSP_generalBtnStyling : this.onFSP_minimizeBtnStyling);
    }


    private void fitComponents(){
        Platform.runLater(() -> {
            if(!this.isSetup){
                this.workingStageBoundaries.setKey(workingStage.getWidth()).setValue(workingStage.getHeight());
            }
            this.support.setWidth(this.workingStage.getWidth());
            this.support.setHeight(this.supportHeight);
            this.support.setLayoutY(0);
            this.support.setLayoutX(0);
            this.fullScreenWarn.setLayoutX(0);
            this.fullScreenWarn.setLayoutY(this.workingStage.getHeight() / 2 - this.fullScreenWarn.getHeight() / 2);
            this.fullScreenWarn.setPrefWidth(this.workingStage.getWidth());
            //Buttons binding
            this.arrangeButtons();
            this.applyComponentsViewMode();
            //Label binding
            this.title.setPrefWidth(this.support.getWidth() - 10 - (buttonsWidth * 3));
            this.title.setLayoutX(
                    this.titleBarImage.getImage() == null ?
                            10 : this.titleBarImage.getLayoutX() + this.titleBarImage.getBoundsInLocal().getWidth() + 3
            );
            // Old title LayoutX: 10
            this.title.setLayoutY((this.supportHeight / 2) - (this.title.getHeight() / 2));
            this.isSetup = true;
        });
    }

    private void adjustButtonAppearanceBasedOnViewType(){
        this.closeButton.setPrefWidth(this.buttonsWidth);
        this.closeButton.setPrefHeight(this.supportHeight);
        this.minimizeButton.setPrefWidth(this.buttonsWidth);
        this.minimizeButton.setPrefHeight(this.supportHeight);
        this.maximizeButton.setPrefWidth(this.buttonsWidth);
        this.maximizeButton.setPrefHeight(this.supportHeight);
        if(this.buttonsViewType == ButtonsViewType.TEXT){
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
            this.titleBarImage.setVisible(true);
        }
    }

    private void applyComponentsViewMode(){
        this.support.setViewOrder(this.viewMode.getValue());
        this.title.setViewOrder(this.viewMode.getValue());
        this.closeImage.setViewOrder(this.viewMode.getValue());
        this.closeButton.setViewOrder(this.viewMode.getValue());
        this.maximizeImage.setViewOrder(this.viewMode.getValue());
        this.maximizeButton.setViewOrder(this.viewMode.getValue());
        this.minimizeImage.setViewOrder(this.viewMode.getValue());
        this.minimizeButton.setViewOrder(this.viewMode.getValue());
        this.titleBarImage.setViewOrder(this.viewMode.getValue());
    }

    private void arrangeButtons(){
        Stage st = this.workingStage;
        double subtract = 4;
        Utilities.adjustImageViewSize(
                this.titleBarImage,
                20,
                this.support.getHeight() - 4,
                true
        );
        this.titleBarImage.setLayoutX(3);
        this.titleBarImage.setLayoutY((this.supportHeight / 2) - (this.titleBarImage.getBoundsInLocal().getHeight() / 2));
        if(this.buttonsArrangement == ButtonsArrangement.NONE){
            this.removeNodes(
                    this.closeButton, this.closeImage,
                    this.maximizeButton, this.maximizeImage,
                    this.minimizeButton, this.minimizeImage
            );
            this.adjustButtonAppearanceBasedOnViewType();
            return;
        }
        this.closeButton.setLayoutX(st.getWidth() - this.closeButton.getPrefWidth()); // CLOSE BUTTON
        Utilities.adjustImageViewSize(this.closeImage,
                this.closeButton.getPrefWidth() - subtract,
                this.closeButton.getPrefHeight() - subtract,
                true);
        Utilities.centralizeImageViewOnButton(this.closeImage, this.closeButton);
        if(this.buttonsArrangement == ButtonsArrangement.CLOSE){
            this.removeNodes(
                    this.maximizeButton, this.maximizeImage,
                    this.minimizeButton, this.minimizeImage
            );
        }else if(this.buttonsArrangement == ButtonsArrangement.CLOSE_AND_MINIMIZE){
            this.removeNodes(
                    this.maximizeButton, this.maximizeImage
            );
            this.minimizeButton.setLayoutX(st.getWidth() - (2 * this.minimizeButton.getPrefWidth())); // MINIMIZE BUTTON
            Utilities.adjustImageViewSize(this.minimizeImage,
                    this.minimizeButton.getPrefWidth() - subtract,
                    this.minimizeButton.getPrefHeight() - subtract,
                    true);
            Utilities.centralizeImageViewOnButton(this.minimizeImage, this.minimizeButton);
        }else if(this.buttonsArrangement == ButtonsArrangement.EVERYTHING){
            this.maximizeButton.setLayoutX(st.getWidth() - (2 * this.maximizeButton.getPrefWidth())); // MINIMIZE BUTTON
            Utilities.adjustImageViewSize(this.maximizeImage,
                    this.maximizeButton.getPrefWidth() - subtract,
                    this.maximizeButton.getPrefHeight() - subtract,
                    true);
            Utilities.centralizeImageViewOnButton(this.maximizeImage, this.maximizeButton);
            this.minimizeButton.setLayoutX(st.getWidth() - (3 * this.minimizeButton.getPrefWidth())); // MINIMIZE BUTTON
            Utilities.adjustImageViewSize(this.minimizeImage,
                    this.minimizeButton.getPrefWidth() - subtract,
                    this.minimizeButton.getPrefHeight() - subtract,
                    true);
            Utilities.centralizeImageViewOnButton(this.minimizeImage, this.minimizeButton);
        }
        this.adjustButtonAppearanceBasedOnViewType();
    }

    private void addCloseButtonListeners(){
        this.closeButton.setOnMouseEntered(e ->
            Utilities.setImageViewWithFullPath(this.closeImage, "icons/TopBar/Hover/XButton2_hover.png")
        );
        this.closeButton.setOnMouseExited(e ->
                Utilities.setImageViewWithFullPath(this.closeImage, "icons/TopBar/XButton2.png")
        );
        this.closeButton.setOnMousePressed(e ->
                Utilities.setImageViewWithFullPath(this.closeImage, "icons/TopBar/Clicked/XButton2_clicked.png")
        );
    }

    private void addMinimizeButtonListeners(){
        this.minimizeButton.setOnMouseEntered(e ->
                Utilities.setImageViewWithFullPath(this.minimizeImage, "icons/TopBar/Hover/MinimizeIcon_hover.png")
        );
        this.minimizeButton.setOnMouseExited(e ->
                Utilities.setImageViewWithFullPath(this.minimizeImage, "icons/TopBar/MinimizeIcon.png")
        );
        this.minimizeButton.setOnMousePressed(e ->
                Utilities.setImageViewWithFullPath(this.minimizeImage, "icons/TopBar/Clicked/MinimizeIcon_clicked.png")
        );
    }

    private void addMaximizeButtonListeners(){
        if(this.buttonsArrangement == ButtonsArrangement.EVERYTHING){
            this.maximizeButton.setOnMouseEntered(e ->
                    Utilities.setImageViewWithFullPath(this.maximizeImage,
                            this.isFullScreen ? "icons/TopBar/Hover/exit_fullscreen_hover.png" : "icons/TopBar/Hover/go_fullscreen_hover.png"
                    )
            );
            this.maximizeButton.setOnMouseExited(e ->
                    Utilities.setImageViewWithFullPath(this.maximizeImage,
                            this.isFullScreen ? "icons/TopBar/exit_fullscreen.png" : "icons/TopBar/go_fullscreen.png"
                    )
            );
            this.maximizeButton.setOnMousePressed(e ->
                    Utilities.setImageViewWithFullPath(this.maximizeImage,
                            this.isFullScreen ? "icons/TopBar/Clicked/exit_fullscreen_clicked.png" : "icons/TopBar/Clicked/go_fullscreen_clicked.png"
                    )
            );
        }
    }

    private void addEventListeners(){
        this.setDefaultTaskbarButtonsBehaviour();
        this.addCloseButtonListeners();
        this.addMinimizeButtonListeners();
        this.addMaximizeButtonListeners();
        this.support.setOnMousePressed(this::onMousePress);
        this.title.setOnMousePressed(this::onMousePress);
        this.titleBarImage.setOnMousePressed(this::onMousePress);
        this.support.setOnMouseDragged(this::onTaskbarDrag);
        this.title.setOnMouseDragged(this::onTaskbarDrag);
        this.titleBarImage.setOnMouseDragged(this::onTaskbarDrag);
        this.support.setOnMouseReleased(this::onMouseRelease);
        this.title.setOnMouseReleased(this::onMouseRelease);
        this.titleBarImage.setOnMouseReleased(this::onMouseRelease);
    }
    private void onMousePress(MouseEvent event){
        this.xOffset = event.getSceneX();
        this.yOffset = event.getSceneY();
        if(this.buttonsArrangement == ButtonsArrangement.EVERYTHING && event.getClickCount() == 2) {
            this.ignoreDrag = true;
            if(this.isFullScreen) this.disableFullScreen();
            else {
                this.requestOnFullScreenPrepareStyling();
                Utilities.sleep(Duration.millis(75), 1, run -> this.enableFullScreen(), null);
            }
        }
    }
    private void onMouseRelease(MouseEvent event){
        if(this.workingStage.getY() < 0){
            this.workingStage.setY(0);
        }
        this.ignoreDrag = false;
        this.centralizeOnDrag = false;
        if(this.prepareToFullScreen) this.enableFullScreen();
    }
    private void onTaskbarDrag(MouseEvent event){
        double resultX = event.getScreenX() - this.xOffset;
        double resultY = event.getScreenY() - this.yOffset;
        if(this.ignoreDrag){
            return;
        }
        if(resultY < 0 && !this.isFullScreen && this.buttonsArrangement == ButtonsArrangement.EVERYTHING && !this.prepareToFullScreen){
            if(this.onFullScreenPrepare != null) Platform.runLater(this.onFullScreenPrepare);
            this.requestOnFullScreenPrepareStyling();
            Utilities.runAsynchronously(() -> {
                this.fullScreenLabelWarn.stop();
                this.fullScreenLabelWarn.markToDisappear(false).start();
            });
            this.prepareToFullScreen = true;
            //enable, ignoreDrag = true, return
        }else if(resultY > 0 && !this.isFullScreen && this.buttonsArrangement == ButtonsArrangement.EVERYTHING && this.prepareToFullScreen){
            if(this.onFullScreenPrepareExit != null) Platform.runLater(this.onFullScreenPrepareExit);
            Utilities.runAsynchronously(() -> {
                this.fullScreenLabelWarn.stop();
                this.fullScreenLabelWarn.markToDisappear(true).start();
            });
            this.requestDefaultStyling();
            this.prepareToFullScreen = false;
        }
        if(this.isFullScreen && this.buttonsArrangement == ButtonsArrangement.EVERYTHING){
            this.disableFullScreen();
            this.centralizeOnDrag = true;
        }
        this.workingStage.setX(this.centralizeOnDrag ? event.getScreenX() - this.workingStage.getWidth() / 2 : resultX);
        this.workingStage.setY(resultY);
    }
    private void setDefaultTaskbarButtonsBehaviour(){
        this.closeButton.setOnAction(event -> {
            if(this.onClose != null) Platform.runLater(this.onClose);
        });
        this.maximizeButton.setOnAction(event -> {
            if(!this.isFullScreen){
                this.enableFullScreen();
            }else{
                this.disableFullScreen();
            }
        });
        this.minimizeButton.setOnAction(event -> {
            if(this.onMinimize != null) Platform.runLater(this.onMinimize);
            this.workingStage.setIconified(!this.workingStage.isIconified());
        });
        this.closeButton.setCursor(Cursor.HAND);
        this.maximizeButton.setCursor(Cursor.HAND);
        this.minimizeButton.setCursor(Cursor.HAND);
        NodeUtils.setTooltip(this.closeButton, "Closes this window.", 200);
        NodeUtils.setTooltip(this.maximizeButton, "Makes this window fullscreen.", 200);
        NodeUtils.setTooltip(this.minimizeButton, "Minimizes this window.", 200);
    }

    private void addToStageChildren(){
        ((Pane) this.workingStage.getScene().getRoot()).getChildren().addAll(
                this.support,
                this.title,
                this.fullScreenWarn,
                this.closeImage,
                this.titleBarImage,
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
                this.fullScreenWarn,
                this.closeButton,
                this.minimizeButton,
                this.maximizeButton,
                this.closeImage,
                this.minimizeImage,
                this.maximizeImage,
                this.titleBarImage
        );
    }

    public void enableFullScreen() {
        this.isFullScreen = true;
        this.prepareToFullScreen = false;
        this.fullScreenLabelWarn.clear();
        this.requestDefaultStyling();
        this.lastStageCoords.setKey(this.workingStage.getX());
        this.lastStageCoords.setValue(this.workingStage.getY() <= 0 ? 1 : this.workingStage.getY());
        this.workingStage.setX(0);
        this.workingStage.setY(0);
        this.workingStage.setWidth(MainApp.VISUAL_SCREEN_WIDTH);
        this.workingStage.setHeight(MainApp.VISUAL_SCREEN_HEIGHT);
        NodeUtils.setTooltip(this.maximizeButton, "Returns this window to it's original size.", 200);
        this.setMaximizeImage("icons/TopBar/exit_fullscreen.png");
        if(this.onFullScreenEnable != null)
            Platform.runLater(this.onFullScreenEnable);
    }

    public void disableFullScreen() {
        this.isFullScreen = false;
        this.requestDefaultStyling();
        this.workingStage.setWidth(this.workingStageBoundaries.getKey());
        this.workingStage.setHeight(this.workingStageBoundaries.getValue());
        this.workingStage.setX(this.lastStageCoords.getKey());
        this.workingStage.setY(this.lastStageCoords.getValue());
        NodeUtils.setTooltip(this.maximizeButton, "Makes this window fullscreen.", 200);
        this.setMaximizeImage("icons/TopBar/go_fullscreen.png");
        if(this.onFullScreenDisable != null)
            Platform.runLater(this.onFullScreenDisable);
    }

    public boolean isFullScreen() {
        return this.isFullScreen;
    }

    public WindowTitleBar useDefaultPresets(){
        this.setButtonsWidth(35)
            .setSupportHeight(30)
            .setViewMode(ViewMode.BEHIND)
            .setButtonsViewType(ButtonsViewType.IMAGED)
            .setButtonsArrangement(ButtonsArrangement.CLOSE_AND_MINIMIZE)
            .setSupportStyling("-fx-fill: #808080")
            .setTitleBarImage("icons/icon.png")
            .setGeneralButtonsStyling("-fx-background-color: transparent")
            .setTitleString("")
            .setCloseImage("icons/TopBar/XButton2.png")
            .setMaximizeImage("icons/TopBar/go_fullscreen.png")
            .setMinimizeImage("icons/TopBar/MinimizeIcon.png");
        return this;
    }

    private boolean isNonNull(Object object){
        return object != null;
    }
}