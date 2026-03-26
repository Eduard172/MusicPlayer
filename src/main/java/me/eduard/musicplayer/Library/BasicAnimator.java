package me.eduard.musicplayer.Library;

import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;
import me.eduard.musicplayer.Utils.Utilities;

public final class BasicAnimator {

    private int millis;
    private double maxOpacity;
    private double speed;
    private Node nodeToAnimate = null;
    private boolean disappear;

    private Timeline timeline;

    public BasicAnimator(){
        this.speed = 0.05;
    }

    public BasicAnimator(Node nodeToAnimate){
        this();
        this.nodeToAnimate = nodeToAnimate;
    }

    public BasicAnimator(Node nodeToAnimate, int millis, double maxOpacity, double speed){
        this(nodeToAnimate);
        this.millis = millis;
        this.maxOpacity = maxOpacity;
        this.speed = speed;
    }

    public BasicAnimator markToDisappear(boolean disappear){
        this.disappear = disappear;
        return this;
    }

    public BasicAnimator setMillis(int millis){
        this.millis = millis;
        return this;
    }

    public BasicAnimator setMaxOpacity(double maxOpacity){
        this.maxOpacity = maxOpacity;
        return this;
    }

    public BasicAnimator setSpeed(double speed){
        this.speed = speed;
        return this;
    }

    public BasicAnimator clear(){
        this.nodeToAnimate.setOpacity(0.0);
        this.nodeToAnimate.setVisible(false);
        return this;
    }

    public void start(){
        if(!this.disappear){
            this.nodeToAnimate.setVisible(true);
            this.nodeToAnimate.setOpacity(0.0);
        }
        this.timeline = !this.disappear ?
                Utilities.sleep(Duration.millis(this.millis), (int) (1.0 / this.speed), run -> {
                    if(this.nodeToAnimate.getOpacity() >= maxOpacity){
                        this.nodeToAnimate.setOpacity(maxOpacity);
                        this.timeline.stop();
                    }
                    this.nodeToAnimate.setOpacity(Math.min(this.nodeToAnimate.getOpacity() + this.speed, 1.0));
                }, null
        ) : Utilities.sleep(Duration.millis(this.millis), (int) (1.0 / this.speed) + 1, run -> {
            this.nodeToAnimate.setOpacity(this.nodeToAnimate.getOpacity() - this.speed < 0 ? 0 : this.nodeToAnimate.getOpacity() - this.speed);
        }, event -> {
            this.clear();
            this.timeline.stop();
        });
    }

    public void stop(){
        if(this.timeline != null){
            this.timeline.stop();
        }
    }

    public static BasicAnimator of(){
        return new BasicAnimator();
    }

    public static BasicAnimator of(Node nodeToAnimate){
        return new BasicAnimator(nodeToAnimate);
    }

    public static BasicAnimator of(Node nodeToAnimate, int millis, double maxOpacity, double speed){
        return new BasicAnimator(nodeToAnimate, millis, maxOpacity, speed);
    }

}
