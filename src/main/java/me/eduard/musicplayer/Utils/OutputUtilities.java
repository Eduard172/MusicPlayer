package me.eduard.musicplayer.Utils;

import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;
import me.eduard.musicplayer.Components.Player.Player;

@SuppressWarnings("all")
public class OutputUtilities {
    private Timeline timeline;
    public enum Level{
        DEFAULT,
        INCOMPLETE,
        SUCCESS,
        INFORMATIVE;
    }
    public OutputUtilities setOutputLabel(Label label, String string, OutputUtilities.Level level){
        if(level == null || string == null || label == null){
            return this;
        }
        switch (level){
            case DEFAULT -> label.setText(string);
            case INCOMPLETE -> {
                label.setStyle("-fx-text-fill: red");
                label.setText(string);
            }case SUCCESS -> {
                label.setStyle("-fx-text-fill: lime");
                label.setText(string);
            }case INFORMATIVE -> {
                label.setStyle("-fx-text-fill: #a18368");
                label.setText(string);
            }
        }
        return this;
    }
    public OutputUtilities setOutputLabel(Label label, String string, String HEXCode){
        if(label == null || string == null || HEXCode == null){
            return this;
        }
        label.setStyle("-fx-text-fill: "+HEXCode);
        label.setText(string);
        return this;
    }
    public OutputUtilities setOutputLabel(Label label, String string, OutputUtilities.Level level, boolean animate){
        label.setOpacity(1.0);
        switch (level){
            case DEFAULT -> {
                label.setText(string);
            }
            case SUCCESS -> {
                label.setStyle("-fx-text-fill: lime");
                label.setText(string);
            }case INCOMPLETE -> {
                label.setStyle("-fx-text-fill: red");
                label.setText(string);
            }case INFORMATIVE -> {
                label.setStyle("-fx-text-fill: #a18368");
                label.setText(string);
            }
        }
        if(this.timeline != null && this.timeline.getStatus() == Animation.Status.RUNNING){
            this.timeline.stop();
        }
        if(animate){
            if(Player.ANIMATIONS){
                this.timeline = Utilities.sleep(
                        Duration.seconds(2), 1,
                        run -> Utilities.sleep(
                                Duration.millis(20), (int) (1.0 / 0.025),
                                run2 -> {
                                    if(label.getOpacity() <= 0.0)
                                        return;
                                    label.setOpacity(label.getOpacity() - 0.025);
                                }, null
                        ), null
                );
            }else{
                this.timeline = Utilities.sleep(Duration.seconds(2), 1, run -> label.setOpacity(0.0), null);
            }
        }
        return this;
    }
}
