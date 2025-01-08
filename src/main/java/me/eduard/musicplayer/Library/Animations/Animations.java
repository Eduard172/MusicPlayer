package me.eduard.musicplayer.Library.Animations;

import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.eduard.musicplayer.Components.Player.Player;
import me.eduard.musicplayer.MainApp;
import me.eduard.musicplayer.Utils.Utilities;

public class Animations {

    private static Timeline playerImageOpen, playerImageClose;

    public static void animateStageLaunch(Stage stage){
        Player player = Player.instance;
        if(!Player.ANIMATIONS)
            return;
        stage.setOpacity(0.0d);
        Utilities.sleep(
                Duration.millis(10), (int) (1.0/0.05),
                run ->
                    stage.setOpacity(Math.min(stage.getOpacity() + 0.05d, 0.9)), null

        );
        if(player != null && !player.unfocusedScreen.isVisible() && !MainApp.STAGES.isEmpty())
            animateOverAllScreen(player.unfocusedScreen);
    }
    public static boolean isStageClosePending = false;
    public static void animateStageClose(Stage stage){
        Player player = Player.instance;
        if(!Player.ANIMATIONS){
            stage.close();
            return;
        }
        Utilities.sleep(
                Duration.millis(10), (int) (1.0/0.05),
                run -> {
                    isStageClosePending = true;
                    stage.setOpacity(Math.max(stage.getOpacity() - 0.05d, 0.0));
                },
                end -> {
                    isStageClosePending = false;
                    stage.close();
                }
        );
        if(player != null && player.unfocusedScreen.isVisible() && MainApp.STAGES.isEmpty()){
            undoAnimateOverAllScreen(player, true);
        }
    }

    public static void animatePlayerHoverImageOnEnter(Rectangle rectangle){
        if(Player.instance == null || Player.instance.videoPlayer == null)
            return;
        if(!Player.ANIMATIONS){
            rectangle.setVisible(true);
            rectangle.setOpacity(0.0);
            return;
        }
        checkAndStopAnimations(playerImageOpen);
        rectangle.setVisible(true);
        playerImageOpen = Utilities.sleep(
                Duration.millis(10), (int) (1.0/0.05),
                run -> rectangle.setOpacity((rectangle.getOpacity() + 0.05d >= 0.7) ? rectangle.getOpacity() : rectangle.getOpacity() + 0.05d),
                null
        );
    }
    public static void undoAnimatePlayerHoverImageOnEnter(Rectangle rectangle){
        if(!Player.ANIMATIONS){
            rectangle.setOpacity(0.0);
            rectangle.setVisible(false);
        }else{
            checkAndStopAnimations(playerImageClose);
            playerImageClose = Utilities.sleep(
                    Duration.millis(10), (int) (1.0/0.05),
                    run -> rectangle.setOpacity((rectangle.getOpacity() - 0.05d < 0.0) ? rectangle.getOpacity() : rectangle.getOpacity() - 0.05d),
                    end -> rectangle.setVisible(false)
            );
        }
    }

    private static Timeline disappearTimeline;

    public static void undoAnimateObjectAppear(Node node, int millisSpeed, boolean makeInvisible){
        checkAndStopAnimations(disappearTimeline);
        disappearTimeline = Utilities.sleep(Duration.millis(millisSpeed), (int) (1.0 / 0.05),
                run -> node.setOpacity((node.getOpacity() - 0.05 < 0) ? 0 : node.getOpacity() - 0.05),
                event -> node.setVisible(!makeInvisible)
        );
    }

    private static Timeline appearTimeLine;

    public static void animateObjectAppear(Node node, int millisSpeed){
        checkAndStopAnimations(appearTimeLine);
        node.setVisible(true);
        appearTimeLine = Utilities.sleep(Duration.millis(millisSpeed), (int) (1.0 / 0.05),
                run -> node.setOpacity(Math.min(node.getOpacity() + 0.05, 1.0)), null
        );
    }

    public static void animateNotification(Stage stage, boolean animate, double duration){
        MainApp.executorService.submit(() -> {
            Rectangle2D rectangle2D = Screen.getPrimary().getVisualBounds();
            double wholeScreenWidth = rectangle2D.getWidth();                       //Also known as starting point
            double wholeScreenHeight = rectangle2D.getHeight();
            double endXPoint = wholeScreenWidth - stage.getWidth();
            double endYPoint = wholeScreenHeight - stage.getHeight();
            stage.setY(endYPoint);
            stage.setOpacity(1.0);
            if(animate){
                stage.setX(wholeScreenWidth);
                Utilities.sleep(Duration.millis(200), 1, run4 -> { //Delay of 1/5 of a second before starting the animation.
                    Utilities.sleep(
                            Duration.millis(2), (int) (stage.getWidth() + 5),
                            run -> { //Moving animation
                                if(stage.getX() <= endXPoint){
                                    Utilities.sleep(Duration.seconds(duration), 1, run2 -> Utilities.sleep( //5-seconds delay
                                            Duration.millis(60), (int) (1.0 / 0.025),
                                            run3 -> { //Setting the opacity gradually until it reaches the value 0.0
                                                if(stage.getOpacity() <= 0.0){
                                                    MainApp.closeStage(stage, false);
                                                }else{
                                                    stage.setOpacity((stage.getOpacity() - 0.025f < 0.0) ? 0.0f : stage.getOpacity() - 0.025f );
                                                }
                                            }, null),null);
                                    return;
                                }
                                stage.setX(stage.getX() - 1);
                            }, null);
                }, null);
            }else{
                stage.setX(endXPoint);
                Utilities.sleep(Duration.seconds(duration), 1, run -> MainApp.closeStage(stage, false), null);
            }
        });
    }

    private static Timeline animate, undoAnimate;
    public static boolean allowedToUndoAnimation = false, isWholeScreenAnimationOn = false, isWholeScreenAnimationOff = false;

    public static void animateOverAllScreen(Rectangle rectangle){
        double maxAllowedOpacity = 0.7;
        if(!Player.ANIMATIONS || isWholeScreenAnimationOn){
            return;
        }
        checkAndStopAnimations(animate);
        isWholeScreenAnimationOn = true;
        rectangle.setOpacity((rectangle.getOpacity() == 1.0) ? 0.0 : rectangle.getOpacity());
        rectangle.setVisible(true);
        rectangle.setLayoutY(Player.instance.titleBar.getHeight());
        animate = Utilities.sleep(Duration.millis(10), (int) (1.0/0.1),
                run -> rectangle.setOpacity(Math.min(rectangle.getOpacity() + 0.1d, maxAllowedOpacity)),
                end -> isWholeScreenAnimationOff = false
        );
        Utilities.sleep(Duration.millis(1100), 1, null, end -> allowedToUndoAnimation = true);
    }
    public static void undoAnimateOverAllScreen(Player player, boolean force){
        Rectangle unfocused = player.unfocusedScreen;
        if(((!unfocused.isVisible() || !allowedToUndoAnimation) && !force) || isWholeScreenAnimationOff){
            return;
        }
        checkAndStopAnimations(undoAnimate);
        isWholeScreenAnimationOff = true;
        undoAnimate = Utilities.sleep(Duration.millis(10), (int) (1.0/0.1),
                run -> unfocused.setOpacity(Math.max(unfocused.getOpacity() - 0.1d, 0.0)),
                end -> {
                    unfocused.setOpacity(0.0d);
                    unfocused.setVisible(false);
                    isWholeScreenAnimationOn = false;
                }
        );
    }

    public static void checkAndStopAnimations(Timeline... timelines){
        for(Timeline timeline : timelines){
            if(timeline != null && timeline.getStatus() == Animation.Status.RUNNING){
                timeline.stop();
            }
        }
    }
}
