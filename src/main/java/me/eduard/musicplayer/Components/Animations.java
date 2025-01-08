package me.eduard.musicplayer.Components;

import javafx.stage.Stage;
import javafx.util.Duration;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.Utils.Utilities;

public class Animations {
    public static void animateStageLaunch(Stage stage){
        if(!Player.ANIMATIONS)
            return;
        stage.setOpacity(0.0d);
        Utilities.sleep(
                Duration.millis(10), (int) (1.0/0.05), run ->
                    stage.setOpacity((stage.getOpacity() + 0.05d >= 1.0) ? stage.getOpacity() : stage.getOpacity() + 0.05d), null
        );
    }
    public static void animateStageClose(Stage stage){
        if(!Player.ANIMATIONS){
            stage.close();
            return;
        }
        Utilities.sleep(
                Duration.millis(10), (int) (1.0/0.05),
                run -> stage.setOpacity((stage.getOpacity() - 0.05d <= 0.0) ? stage.getOpacity() : stage.getOpacity() - 0.05d),
                end -> stage.close()
        );
    }
}
