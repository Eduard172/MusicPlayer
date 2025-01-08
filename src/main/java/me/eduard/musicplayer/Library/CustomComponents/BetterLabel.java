package me.eduard.musicplayer.Library.CustomComponents;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.eduard.musicplayer.Library.Exceptions.OperationFailedException;
import me.eduard.musicplayer.Library.LambdaObject;
import me.eduard.musicplayer.Utils.Coords3D;
import me.eduard.musicplayer.Utils.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class is a util tool if you want to use large labels in a short width space.
 * It supports high customizability features such as CSS manipulation, label speed, text alignment, width, height and more.
 * <p>
 * The alignment will only happen if the label's width is smaller or equal to the overall width used in {@link BetterLabel#setWidth} method.
 * <p>
 * If the label is too high and the total width is smaller than label's width, it will be automatically moved to the left, and a smooth
 * animation will start with a given speed from left to right, showing the whole label's text content.
 * When the label reaches the beginning, it will stop for an amount of milliseconds also specified by the developer who uses this class.
 * <p>
 * The animation will run in a background thread using a {@link ExecutorService}, which uses a {@link Task<Void>}.
 * <p>
 * When the working stage is closed, the animation also stops to prevent background usage of resources.
 * <p>
 * If you want to use the label without animation, use a {@link Label} instead.
 */
@SuppressWarnings({"unused", "FieldMayBeFinal", "FieldCanBeLocal", "DuplicatedCode", "UnusedReturnValue"})
public final class BetterLabel {

    private volatile boolean layoutUpdatedAtFirstInstance = false;

    public enum Alignment {
        LEFT, CENTER, RIGHT
    }

    public enum ViewMode{
        /**
         * The node will be displayed behind every other node in the stage
         */
        BEHIND,
        /**
         * The node will be normally displayed in the normal order view
         */
        NORMAL,
        /**
         * The node will be displayed above all nodes in the stage
         */
        ABOVE

    }

    private Stage workingStage;
    private ScrollPane scrollPane;
    private Pane contentsBox;
    private Label label1;
    private Label label2;

    private Timeline stopWhenPointReached;

    private Alignment alignment = Alignment.CENTER;

    private Rectangle firstMargin, secondMargin;

    private ExecutorService parallelThread = Executors.newCachedThreadPool();
    private ViewMode viewMode;

    private String text = "Class Developed by Edward76.";
    private double height = 0;
    private double width = 100;

    private double speed = 0.4;
    private double timeoutWhenStampReached = 1500;

    private double layoutX = 0;
    private double layoutY = 0;

    private double relativeLabelDistance = 100;

    private boolean forceStop = false;
    private boolean isLinked = false;

    private AnimationTimer animationTimer, keepMoving;
    private boolean isPaused = false;
    private final List<AnimationTimer> ANIMATIONS = new ArrayList<>();

    public BetterLabel(){
        this.scrollPane = new ScrollPane();
        this.label1 = new Label();
        this.label2 = new Label();
        this.label1.textProperty().addListener((obs, oldVal, newVal) -> this.startAnimation(this.label1, true, true, 1));
        this.contentsBox = new Pane();
        this.firstMargin = new Rectangle();
        this.secondMargin = new Rectangle();
        this.animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                //Just initialize it without putting any code inside.
            }
        };
    }

    public static BetterLabel of(Stage workingStage){
        return new BetterLabel(workingStage);
    }
    public static BetterLabel of(){
        return new BetterLabel();
    }
    public BetterLabel setOnMouseEntered(EventHandler<? super MouseEvent> event){
        this.label1.setOnMouseEntered(event);
        this.label2.setOnMouseEntered(event);
        return this;
    }

    public BetterLabel setOnMouseExited(EventHandler<? super MouseEvent> event){
        this.label1.setOnMouseExited(event);
        this.label2.setOnMouseExited(event);
        return this;
    }

    public BetterLabel setOnMouseClicked(EventHandler<? super MouseEvent> event){
        this.label1.setOnMouseClicked(event);
        this.label2.setOnMouseClicked(event);
        return this;
    }

    public BetterLabel(Stage workingStage){
        this();
        this.setWorkingStage(workingStage);
        this.workingStage.showingProperty().addListener((obs, oldValue, newValue) -> {
            if(!newValue){
                this.unlinkFromStage();
                this.stopAllAnimations(true);
                this.forceStop = true;
            }
        });
    }

    public void setWorkingStage(Stage workingStage){
        this.workingStage = workingStage;
    }

    public BetterLabel setAlignment(Alignment alignment){
        this.alignment = alignment;
        return this;
    }

    public BetterLabel setSpeed(double speed){
        if(speed < 0)
            throw new IllegalArgumentException("The speed argument cannot be less than 0.");
        this.speed = speed;
        return this;
    }

    public BetterLabel setTimeoutWhenStampReached(double timeoutWhenStampReached){
        this.timeoutWhenStampReached = timeoutWhenStampReached;
        return this;
    }

    public BetterLabel setFont(Font font){
        this.label1.setFont(font);
        this.label2.setFont(font);
        if(this.isLinked)
            this.updateLayout();
        return this;
    }

    public BetterLabel setFont(double size){
        this.label1.setFont(Font.font(size));
        this.label2.setFont(Font.font(size));
        if(this.isLinked)
            this.updateLayout();
        return this;
    }

    public BetterLabel setBackgroundStyling(String... css){
        String style = Utilities.stringFromArray(css, "", ";");
        this.scrollPane.setStyle(style);
        this.contentsBox.setStyle(style);
        return this;
    }

    public BetterLabel setMarginsStyling(String... css){
        String style = Utilities.stringFromArray(css, "", ";");
        this.firstMargin.setStyle(style);
        this.secondMargin.setStyle(style);
        return this;
    }

    public BetterLabel setLabelStyling(String... css){
        String style = Utilities.stringFromArray(css, "", ";");
        this.label1.setStyle(style);
        this.label2.setStyle(style);
        return this;
    }

    public BetterLabel setViewMode(ViewMode viewMode){
        this.viewMode = viewMode;
        this.adjustBasedOnViewMode(this.viewMode);
        return this;
    }

    private void adjustBasedOnViewMode(ViewMode viewMode){
        switch (viewMode){
            case BEHIND -> this.setViewOrder(1);
            case NORMAL -> this.setViewOrder(0);
            case ABOVE -> this.setViewOrder(-1);
        }
    }

    public BetterLabel setText(String text){
        this.text = text;
        if(this.isLinked){
            this.updateText(text);
        }
        return this;
    }

    public BetterLabel setRelativeLabelDistance(double relativeLabelDistance){
        this.relativeLabelDistance = relativeLabelDistance;
        return this;
    }

    public BetterLabel setHeight(double height){
        this.height = height;
        if(this.isLinked)
            this.updateLayout();
        return this;
    }

    public BetterLabel setWidth(double width){
        this.width = width;
        if(this.isLinked)
            this.updateLayout();
        return this;
    }

    public BetterLabel setLayout_X(double layoutX){
        this.layoutX = layoutX;
        if(this.isLinked)
            this.updateLayout();
        return this;
    }

    public BetterLabel setLayout_Y(double layoutY){
        this.layoutY = layoutY;
        if(this.isLinked)
            this.updateLayout();
        return this;
    }

    public BetterLabel setToolTip(Tooltip tooltip){
        this.label1.setTooltip(tooltip);
        this.label2.setTooltip(tooltip);
        return this;
    }

    public Tooltip getToolTip(){
        return this.label1.getTooltip();
    }
    public BetterLabel setViewOrder(int viewOrder){
        this.scrollPane.setViewOrder(viewOrder);
        this.contentsBox.setViewOrder(viewOrder);
        this.label1.setViewOrder(viewOrder);
        this.label2.setViewOrder(viewOrder);
        this.firstMargin.setViewOrder(viewOrder);
        this.secondMargin.setViewOrder(viewOrder);
        return this;
    }

    public String getText(){
        return this.text;
    }
    public double getHeight(){
        return this.height;
    }

    public double getWidth(){
        return this.width;
    }

    public double getSpeed(){
        return this.speed;
    }

    public double getLayoutX(){
        return this.layoutX;
    }

    public double getLayoutY(){
        return this.layoutY;
    }

    public double getTimeoutWhenStampReached(){
        return this.timeoutWhenStampReached;
    }

    public boolean isForceStopped(){
        return this.forceStop;
    }

    public Alignment getAlignment(){
        return this.alignment;
    }

    public boolean isLinked(){
        return this.isLinked;
    }

    public Label getLabel1(){
        return this.label1;
    }

    public Label getLabel2(){
        return this.label2;
    }

    public Rectangle getLeftMargin(){
        return this.firstMargin;
    }

    public Rectangle getRightMargin(){
        return this.secondMargin;
    }

    private void pauseTimer(AnimationTimer animationTimer){
        this.isPaused = true;
        animationTimer.stop();
    }

    private void resumeTimer(AnimationTimer animationTimer){
        this.isPaused = false;
        animationTimer.start();
    }

    public void linkToStage(){
        if(this.isLinked)
            throw new OperationFailedException(
                    "The operation could not complete because this node is already linked to another stage. ("+this.workingStage+
                            ", Stage Title: "+this.workingStage.getTitle()+")"
            );
        this.fitComponents();
        this.isLinked = true;
        ((Pane) this.workingStage.getScene().getRoot()).getChildren().addAll(
                this.scrollPane,
                this.firstMargin,
                this.secondMargin
        );
    }

    public void unlinkFromStage(){
        this.isLinked = false;
        ((Pane) this.workingStage.getScene().getRoot()).getChildren().removeAll(
                this.scrollPane,
                this.firstMargin,
                this.secondMargin
        );
    }

    private void fitComponents(){
        Platform.runLater(() -> {
            //ScrollPane bind
            this.scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            this.scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            this.scrollPane.setPannable(false);
            this.scrollPane.setPrefWidth(this.width);
            this.scrollPane.setMinHeight(this.height);

            this.label1.setText(this.text);
            this.label2.setText(this.text);

            this.label2.setVisible(false);
            this.contentsBox.getChildren().addAll(this.label1, this.label2);
            this.contentsBox.setPrefWidth(this.width);
            this.contentsBox.setPrefHeight(this.label1.getFont().getSize() * 1.66);

            this.scrollPane.setLayoutX(this.layoutX);
            this.scrollPane.setLayoutY(this.layoutY);
            //First margin
            this.firstMargin.setWidth(1);
            this.firstMargin.setLayoutX(this.scrollPane.getLayoutX());
            this.firstMargin.setLayoutY(this.scrollPane.getLayoutY());
            //Second margin
            this.secondMargin.setWidth(1);
            this.secondMargin.setLayoutX(this.scrollPane.getLayoutX() + this.scrollPane.getPrefWidth());
            this.secondMargin.setLayoutY(this.scrollPane.getLayoutY());
            this.scrollPane.setContent(this.contentsBox);

            Utilities.sleep(Duration.millis(30), 1, run -> {
                this.firstMargin.setHeight(this.scrollPane.getHeight());
                this.secondMargin.setHeight(this.scrollPane.getHeight());
                this.startAnimation(this.label1, true , true, 1);
            }, null);
        });
    }

    private void updateText(String newText){
        Runnable runnable = () -> {
            if(!this.label1.getText().equals(newText))
                this.stopAllAnimations(false);
            this.label1.setVisible(true);
            this.label1.setText(newText);
            this.label2.setText(newText);
            if(this.label1.getText().equals(newText))
                return;
            this.startAnimation(this.label1, true, true, 1);
        };
        Utilities.sleep(Duration.millis(20), 1, run -> Platform.runLater(runnable), null);
    }

    public void resetPosition(){
        for(AnimationTimer animationTimers : ANIMATIONS){
            animationTimers.stop();
        }
        this.label2.setVisible(false);
        this.label1.setVisible(true);
        Utilities.sleep(Duration.millis(30), 1, run -> Platform.runLater(() -> this.startAnimation(this.label1, true, true, 1)), null);
    }

    private void updateLayout(){
        Runnable runnable = () -> {
            if(!this.label1.getText().equals(this.text)){
                this.stopAllAnimations(false);
            }
            this.scrollPane.setPrefWidth(this.width);
            this.scrollPane.setMinHeight(this.height);
            this.contentsBox.setPrefWidth(this.width);
            this.contentsBox.setPrefHeight(this.label1.getFont().getSize() * 1.66);
            this.scrollPane.setLayoutX(this.layoutX);
            this.scrollPane.setLayoutY(this.layoutY);
            this.firstMargin.setLayoutX(this.scrollPane.getLayoutX());
            this.firstMargin.setLayoutY(this.scrollPane.getLayoutY());
            this.firstMargin.setHeight(this.scrollPane.getMinHeight());
            this.label1.setVisible(true);
            this.label1.setText(this.text);
            this.label2.setText(this.text);
            this.secondMargin.setLayoutX(this.scrollPane.getLayoutX() + this.scrollPane.getPrefWidth() - 1);
            this.secondMargin.setLayoutY(this.scrollPane.getLayoutY());
            this.secondMargin.setHeight(this.scrollPane.getMinHeight());
        };
        Utilities.sleep(Duration.millis(30), 1, run -> Platform.runLater(runnable), null);
    }

    private Coords3D getExactLabel_X_Coords(Label label){
        Coords3D coords3D = new Coords3D();
        coords3D.setMinX(label.translateXProperty().get());
        coords3D.setMaxX(label.translateXProperty().get() + label.getWidth());
        return coords3D;
    }
    private void centralizeIfNeeded(Label label) {
        double labelWidth = label.getWidth();
        double totalSpace = this.scrollPane.getPrefWidth();
        if(totalSpace < labelWidth){
            label.setTranslateX(0);
        }else{
            this.positionBasedOnAlignment(this.alignment, label);
        }
    }

    private void positionBasedOnAlignment(Alignment alignment, Label label){
        switch (alignment){
            case LEFT -> label.setTranslateX(0);
            case CENTER -> label.setTranslateX(this.scrollPane.getPrefWidth() / 2 - label.getWidth() / 2);
            case RIGHT -> label.setTranslateX(this.scrollPane.getPrefWidth() - label.getWidth());
            default -> throw new IllegalArgumentException("Alignment parameter needs to be 1 of these: LEFT, CENTER, RIGHT");
        }
    }

    private void moveToRight(Label label){
        label.translateXProperty().set(this.scrollPane.getPrefWidth() + 1);
    }

    private void checkAndStopTimeline(Timeline timeline){
        if(timeline != null && (timeline.getStatus() == Animation.Status.RUNNING || timeline.getStatus() == Animation.Status.PAUSED)){
            timeline.stop();
        }
    }

    private void checkAndStopAnimation(AnimationTimer animation){
        if(animation != null){
            animation.stop();
        }
    }

    private void stopAllAnimations(boolean stopThread){
        this.checkAndStopTimeline(this.stopWhenPointReached);
        for(AnimationTimer timers : ANIMATIONS){
            if(timers != null)
                timers.stop();
        }
        if(stopThread){
            this.parallelThread.shutdown();
        }
    }

    private void startAnimation(Label label, boolean stopCurrent, boolean centralize, int labelNo){
        Runnable runnable = () -> {
            if(!layoutUpdatedAtFirstInstance){
                this.updateLayout();
                layoutUpdatedAtFirstInstance = true;
            }
            if(forceStop)
                return;
            if(stopCurrent){
                this.label2.setVisible(false);
                this.checkAndStopAnimation(this.animationTimer);
                this.checkAndStopAnimation(this.keepMoving);
            }
            this.checkAndStopTimeline(this.stopWhenPointReached);
            boolean b = labelNo == 1;
            if(centralize)
                this.centralizeIfNeeded(label);
            //Fit check
            double labelWidth = label.getWidth();
            double totalSpace = this.scrollPane.getPrefWidth();
            if(labelWidth <= totalSpace)
                return;

            LambdaObject<Boolean> stackUpCheck = LambdaObject.of(true);
            LambdaObject<Boolean> reachedPointCheck = LambdaObject.of(true);
            Task<Void> task = new Task<>() {
                @Override
                protected Void call(){
                    animationTimer = new AnimationTimer() {
                        @Override
                        public void handle(long now) { //the 'now' var is in nanos.
                            Coords3D exactPosition = getExactLabel_X_Coords(label);
                            if(stackUpCheck.get() && ((exactPosition.getMaxX() + relativeLabelDistance) <= scrollPane.getPrefWidth())){
                                stackUpCheck.set(false);
                                moveToRight(b ? label2 : label1);
                                if(b){ //Means Label 1
                                    label2.setVisible(true);
                                    keepMoving = new AnimationTimer() {
                                        @Override
                                        public void handle(long now) {
                                            Coords3D prevPos = getExactLabel_X_Coords(label1);
                                            if(prevPos.getMaxX() <= 0){
                                                label1.setVisible(false);
                                                keepMoving.stop();
                                                return;
                                            }
                                            label1.translateXProperty().set(label1.translateXProperty().subtract(speed).get());
                                        }
                                    };
                                    keepMoving.start();
                                }else{ // Means label 2
                                    label1.setVisible(true);
                                    keepMoving = new AnimationTimer() {
                                        @Override
                                        public void handle(long now) {
                                            Coords3D prevPos = getExactLabel_X_Coords(label2);
                                            if(prevPos.getMaxX() <= 0){
                                                label2.setVisible(false);
                                                keepMoving.stop();
                                                return;
                                            }
                                            label2.translateXProperty().set(label2.translateXProperty().subtract(speed).get());
                                        }
                                    };
                                    keepMoving.start();
                                }
                                animationTimer.stop();
                                startAnimation(b ? label2 : label1, false, false, b ? 2 : 1);
                            }else if((stackUpCheck.get() && reachedPointCheck.get()) && exactPosition.getMinX() <= 1){
                                reachedPointCheck.set(false);
                                if(timeoutWhenStampReached > 0){
                                    pauseTimer(animationTimer);
                                    stopWhenPointReached = Utilities.sleep(
                                            Duration.millis(timeoutWhenStampReached), 1, run -> resumeTimer(animationTimer), null
                                    );
                                }
                            }else{
                                label.translateXProperty().set(label.translateXProperty().subtract(speed).get());
                            }
                        }
                    };
                    for(AnimationTimer timers : ANIMATIONS){
                        if(timers != null)
                            timers.stop();
                    }
                    ANIMATIONS.add(animationTimer);
                    animationTimer.start();
                    return null;
                }
            };
            this.parallelThread.submit(task);
        };
        Utilities.sleep(Duration.millis(5), 1, run -> Platform.runLater(runnable), null);
    }

    /**
     * Presets a default value for basic properties. This includes:
     * <li>Relative Distance between labels: 30</li>
     * <li>Speed: 0.4</li>
     * <li>Alignment type: CENTER</li>
     * <li>View Mode: BEHIND</li>
     * <li>Timeout when Stamp reached: 1500ms (1.5s)</li>
     * <li>Label Styling: Text-Fill: White</li>
     * <li>Background Styling: Color: #171717</li>
     * <li>Margins Styling: Fill: #171717; Effect: DropShadow(Gaussian, #171717, 12, 0.6, 0, 0)</li>
     * <p>.</p>
     * <p>Some more properties such as Layout X, Y, Width, Height, etc may need manual handle</p>
     */
    public BetterLabel useDefaultPresets(){
        return this.setRelativeLabelDistance(30)
                .setSpeed(0.4)
                .setAlignment(Alignment.CENTER)
                .setViewMode(ViewMode.BEHIND)
                .setTimeoutWhenStampReached(1500)
                .setLabelStyling("-fx-text-fill: white")
                .setBackgroundStyling("-fx-background-color: #171717")
                .setMarginsStyling("-fx-fill: #171717", "-fx-effect: dropshadow(gaussian, #171717, 12, 0.5, 0, 0)");
    }

}