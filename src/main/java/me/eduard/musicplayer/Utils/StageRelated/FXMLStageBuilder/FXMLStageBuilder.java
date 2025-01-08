package me.eduard.musicplayer.Utils.StageRelated.FXMLStageBuilder;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.ErrorHandler;
import me.eduard.musicplayer.Library.Animations.Animations;
import me.eduard.musicplayer.Library.Exceptions.OperationFailedException;
import me.eduard.musicplayer.Library.HoveringBooleans;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.FXMLUtils;
import me.eduard.musicplayer.Library.BasicKeyValuePair;
import me.eduard.musicplayer.Utils.KeyCombinationUtils;
import me.eduard.musicplayer.Utils.StageRelated.StageBuilder;

import java.io.IOException;

@SuppressWarnings("unused")
public class FXMLStageBuilder {

    private final String FXML_PATH;
    private final FXMLLoader fxmlLoader;
    private Stage stage;
    private Pane pane;
    private StageBuilder stageBuilder;

    private boolean defaultEventsAdded = false;

    private final BasicKeyValuePair<EventType<MouseEvent>, EventHandler<? super MouseEvent>> mouseEvent = BasicKeyValuePair.of(MouseEvent.MOUSE_MOVED, event -> {
        HoveringBooleans.isMouseOverMainStage = false;
        if(Animations.isStageClosePending){
            return;
        }
        if(Player.instance == null)
            return;
        Animations.animateOverAllScreen(Player.instance.unfocusedScreen);
    });

    private FXMLStageBuilder(String FXML_PATH){
        this.FXML_PATH = FXML_PATH;
        this.fxmlLoader = FXMLUtils.getFXMLLoader(this.FXML_PATH);
        try {
            this.pane = this.fxmlLoader.load();
        }catch (IOException exception){
            exception.printStackTrace(System.err);
        }
    }
    public void close(){
        boolean animations = Player.instance == null || Player.ANIMATIONS;
        MainApp.closeStage(this.getStage(), animations);
    }
    public FXMLStageBuilder withStageBuilder(StageBuilder stageBuilder){
        this.stageBuilder = stageBuilder;
        this.stageBuilder = this.stageBuilder.withScene(new Scene(this.pane));
        return this;
    }

    public FXMLStageBuilder addExitListenerWithEscape(){
        this.addKeyEvents(
                BasicKeyValuePair.of(KeyEvent.KEY_PRESSED, event -> {
                    if(event.getCode() == KeyCode.ESCAPE)
                        this.close();
                })
        );
        return this;
    }
    public final FXMLStageBuilder bindAllStagesCloseKeyCombination(){
        this.addKeyEvents(
                BasicKeyValuePair.of(KeyEvent.KEY_PRESSED, event -> {
                    KeyCombinationUtils.registerKey(event.getCode());
                    if(KeyCombinationUtils.isKey(KeyCode.SHIFT, 0) && KeyCombinationUtils.isKey(KeyCode.F4, 1)){
                        MainApp.closeAllStages();
                    }
                }),
                BasicKeyValuePair.of(KeyEvent.KEY_RELEASED, event -> KeyCombinationUtils.removeKey(event.getCode()))
        );
        return this;
    }

    public Stage getStage(){
        return this.stage;
    }

    public FXMLStageBuilder finishBuilding(){
        if(this.stageBuilder == null){
            this.stageBuilder = StageBuilder.newBuilder().withScene(new Scene(this.pane));
        }
        this.stage = this.stageBuilder.buildAndGet();
        this.stage.onCloseRequestProperty().setValue(event -> MainApp.closeStage(stage, Player.ANIMATIONS));
        if(!this.defaultEventsAdded){
            this.addMouseEvents(this.mouseEvent);
            this.defaultEventsAdded = true;
        }
        //Add
        return this;
    }

    @SafeVarargs
    public final void addKeyEvents(BasicKeyValuePair<EventType<KeyEvent>, EventHandler<? super KeyEvent>>... basicKeyValuePairs){
        this.throwOperationFailedIf(this.stageBuilder == null);
        for(BasicKeyValuePair<EventType<KeyEvent>, EventHandler<? super KeyEvent>> pair : basicKeyValuePairs){
            this.stageBuilder.getStage().getScene().addEventFilter(pair.getKey(), pair.getValue());
        }
    }
    @SafeVarargs
    public final void addMouseEvents(BasicKeyValuePair<EventType<MouseEvent>, EventHandler<? super MouseEvent>>... basicKeyValuePairs){
        this.throwOperationFailedIf(this.stageBuilder == null);
        for(BasicKeyValuePair<EventType<MouseEvent>, EventHandler<? super MouseEvent>> eachPair : basicKeyValuePairs){
            this.stageBuilder.getStage().getScene().addEventFilter(eachPair.getKey(), eachPair.getValue());
        }
    }
    public Scene getScene(){
        return this.stageBuilder.get_Scene();
    }
    public FXMLLoader getFxmlLoader(){
        return this.fxmlLoader;
    }
    public Pane getRoot(){
        return this.pane;
    }
    public String getFxmlPath(){
        return this.FXML_PATH;
    }

    public static FXMLStageBuilder newInstance(String FXML_PATH){
        return new FXMLStageBuilder(FXML_PATH);
    }
    public StageBuilder getLocalStageBuilder(){
        return this.stageBuilder;
    }
    private void throwOperationFailedIf(boolean condition){
        if(condition){
            ErrorHandler.launchWindow(new OperationFailedException("The StageBuilder variable is null. Action failed."));
        }
    }

}
