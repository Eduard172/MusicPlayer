package me.eduard.musicplayer.Library;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.GlobalAppStyle;
import me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder.FXMLStageBuilder;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class MessageDialog {

    @FXML public Label titleLabel, messageLabel;
    @FXML public AnchorPane corePane;

    private static final class ButtonBuilder {
        private final Button button = new Button();
        public static ButtonBuilder of(){
            return new ButtonBuilder();
        }
        public ButtonBuilder setText(String text){
            this.button.setText(text);
            return this;
        }
        public ButtonBuilder setWidth(double width){
            this.button.setPrefWidth(width);
            return this;
        }
        public ButtonBuilder setHeight(double height){
            this.button.setPrefHeight(height);
            return this;
        }
        public ButtonBuilder setLayoutX(double x){
            this.button.setLayoutX(x);
            return this;
        }
        public ButtonBuilder setLayoutY(double y){
            this.button.setLayoutY(y);
            return this;
        }
        public ButtonBuilder setOnAction(EventHandler<ActionEvent> eventHandler){
            this.button.setOnAction(eventHandler);
            return this;
        }
        public Button build(){
            return this.button;
        }
    }

    public enum Level {
        INFORMATIVE, WARNING, CRITICAL
    }

    public enum AnswerType {
        YES_NO, BASIC_OK, YES_NO_CANCEL
    }

    public Rectangle levelBar = new Rectangle();
    private Level level = Level.INFORMATIVE;
    private AnswerType answerType = AnswerType.BASIC_OK;
    private String title, message;

    private EventHandler<ActionEvent> on_YES = null;
    private EventHandler<ActionEvent> on_NO = null;
    private EventHandler<ActionEvent> on_OK = null;
    private EventHandler<ActionEvent> on_CANCEL = null;

    public MessageDialog(){

    }

    public MessageDialog(Level level, AnswerType answerType){
        this.setLevel(level);
        this.setAnswerType(answerType);
    }

    public static MessageDialog newMessageDialog(){
        return new MessageDialog();
    }

    public static MessageDialog newMessageDialog(Level level, AnswerType answerType){
        return new MessageDialog(level, answerType);
    }

    public MessageDialog setLevel(Level level){
        this.level = level;
        return this;
    }

    public MessageDialog setAnswerType(AnswerType answerType){
        this.answerType = answerType;
        return this;
    }

    public MessageDialog setTitle(String title){
        this.title = title;
        return this;
    }

    public MessageDialog setMessage(String message){
        this.message = message;
        return this;
    }

    public MessageDialog setOnOK(EventHandler<ActionEvent> on_OK){
        this.on_OK = on_OK;
        return this;
    }

    public MessageDialog setOnYes(EventHandler<ActionEvent> on_YES){
        this.on_YES = on_YES;
        return this;
    }

    public MessageDialog setOnNo(EventHandler<ActionEvent> on_NO){
        this.on_NO = on_NO;
        return this;
    }

    public MessageDialog setOnCancel(EventHandler<ActionEvent> on_CANCEL){
        this.on_CANCEL = on_CANCEL;
        return this;
    }

    public String getTitle(){
        return this.title;
    }
    public String getMessage(){
        return this.message;
    }
    public Level getLevel() {
        return this.level;
    }
    public AnswerType getAnswerType() {
        return this.answerType;
    }

    public void launchDialog(){
        FXMLStageBuilder fxmlStageBuilder = FXMLStageBuilder.newInstance("Dialogs/MessageDialog")
                .withStageBuilder(
                        StageBuilder.newBuilder()
                                .icon("icons/icon.png")
                                .title("Notification")
                                .removeUpperBar()
                                .styleSheet("ApplicationWindow")
                                .resizable(false)
                ).bindAllStagesCloseKeyCombination()
                .requireModality()
                .finishBuilding();

        MessageDialog dialog = fxmlStageBuilder.getFxmlLoader().getController();

        Stage stage = fxmlStageBuilder.getStage();

        dialog.setTitle(this.title)
                .setMessage(this.message)
                .setLevel(this.level)
                .setAnswerType(this.answerType);
        dialog.titleLabel.setText(dialog.getTitle());
        dialog.messageLabel.setText(dialog.getMessage());
        this.bindActionButtons(dialog, stage);
        this.applyLevelRectangle(dialog, stage);
        MainApp.openStage(stage, Player.ANIMATIONS, false);
    }

    private void bindActionButtons(MessageDialog instance, Stage stage){
        final double buttonWidth = 75;
        final double buttonHeight = 25;
        if(this.answerType == AnswerType.YES_NO || this.answerType == AnswerType.YES_NO_CANCEL){
            Platform.runLater(() -> {
                Button yes = ButtonBuilder.of()
                        .setText("Yes")
                        .setLayoutX(stage.getWidth() - 15 - buttonWidth)
                        .setLayoutY(stage.getHeight() - 10 - buttonHeight)
                        .setWidth(buttonWidth)
                        .setHeight(buttonHeight)
                        .setOnAction(ev -> {
                            if(this.on_YES != null) this.on_YES.handle(ev);
                            stage.close();
                        }).build();
                Button no = ButtonBuilder.of()
                        .setText("No")
                        .setLayoutX(yes.getLayoutX() - 10 - buttonWidth)
                        .setLayoutY(yes.getLayoutY())
                        .setWidth(buttonWidth)
                        .setHeight(buttonHeight)
                        .setOnAction(ev -> {
                            if(this.on_NO != null) this.on_NO.handle(ev);
                            stage.close();
                        }).build();
                GlobalAppStyle.applyToButtons(yes, no);
                instance.corePane.getChildren().addAll(yes, no);
            });
        }else if(this.answerType == AnswerType.BASIC_OK){
            Platform.runLater(() -> {
                Button ok = ButtonBuilder.of()
                        .setText("OK")
                        .setLayoutX(stage.getWidth() - 15 - buttonWidth)
                        .setLayoutY(stage.getHeight() - 10 - buttonHeight)
                        .setWidth(buttonWidth)
                        .setHeight(buttonHeight)
                        .setOnAction(ev -> {
                            if(this.on_OK != null) this.on_OK.handle(ev);
                            stage.close();
                        }).build();
                GlobalAppStyle.applyToButtons(ok);
                instance.corePane.getChildren().add(ok);
            });
        }
        if(this.answerType == AnswerType.YES_NO_CANCEL){
            Platform.runLater(() -> {
                Button cancel = ButtonBuilder.of()
                        .setText("Cancel")
                        .setHeight(buttonHeight)
                        .setWidth(buttonWidth)
                        .setLayoutX(stage.getWidth() - (10 * 2 + 15 + buttonWidth * 3))
                        .setLayoutY(stage.getHeight() - 10 - buttonHeight)
                        .setOnAction(ev -> {
                            if(this.on_CANCEL != null) this.on_CANCEL.handle(ev);
                            stage.close();
                        }).build();
                GlobalAppStyle.applyToButtons(cancel);
                instance.corePane.getChildren().add(cancel);
            });
        }
    }

    private void applyLevelRectangle(MessageDialog instance, Stage stage){
        Platform.runLater(() -> {
            Color levelColor = Color.web(
                    switch (this.level){
                        case WARNING -> "0xbd7802";
                        case CRITICAL -> "0xbd1111";
                        case INFORMATIVE -> "0xcccccc";
                    }
            );
            this.levelBar.setFill(levelColor);
            this.levelBar.setWidth(stage.getWidth());
            this.levelBar.setHeight(20);
            this.levelBar.setLayoutX(0);
            this.levelBar.setLayoutY(0);
            instance.corePane.getChildren().add(this.levelBar);
        });
    }

}
