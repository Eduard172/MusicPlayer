package me.eduard.musicplayer.Library.Animations;

import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import me.eduard.musicplayer.Utils.Settings;
import me.eduard.musicplayer.Utils.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class LabelAnimations {

    private final Settings settings = Settings.of("settings.yml");

    private static final List<LabelAnimations> labelAnimations = new ArrayList<>();
    private final String defaultNumericalValueText = " (x[n])";
    private String IDENTIFIER;
    private String text = "Eduard is cool man.";
    private final Label label;
    private int numericalValue = 0;
    private int callCount = 0;
    private String numericalValueText = this.defaultNumericalValueText;
    private double waitingTime = 5000.0;
    private double speed = 15.0;
    private double fromValue = 1.0;
    private double toValue = 0.0;
    private double difference = Math.abs(this.fromValue - this.toValue);
    private double steps = 0.025;
    private Timeline animationTimeline = null, timeout = null;
    private boolean incrementedNumericalValue = false;

    public LabelAnimations getByIdentifier(String IDENTIFIER){
        for(LabelAnimations labelAnimation : labelAnimations){
            if(labelAnimation.getIdentifier().equals(IDENTIFIER)){
                return labelAnimation;
            }
        }
        this.IDENTIFIER = IDENTIFIER;
        labelAnimations.add(this);
        return this;
    }
    private LabelAnimations(Label label){
        this.label = label;
    }
    public String getIdentifier(){
        return this.IDENTIFIER;
    }
    public String getText(){
        return this.text;
    }
    public String getNumericalValueText(){
        return this.numericalValueText;
    }
    public int getNumericalValue(){
        return this.numericalValue;
    }
    public double getSpeed(){
        return this.speed;
    }
    public double getFromValue(){
        return this.fromValue;
    }
    public double getToValue(){
        return this.toValue;
    }
    public double getSteps(){
        return this.steps;
    }
    public double getWaitingTime(){
        return this.waitingTime;
    }
    public double getDifference(){
        return this.difference;
    }
    public int getCallCounts(){
        return this.callCount;
    }
    public Label getLabel(){
        return this.label;
    }
    public LabelAnimations incrementedNumericalValue(boolean incrementedNumericalValue){
        this.incrementedNumericalValue = incrementedNumericalValue;
        return this;
    }
    public LabelAnimations numericalValueText(String numericalValueText){
        this.numericalValueText =
                Objects.requireNonNullElse(numericalValueText, this.defaultNumericalValueText).replace("[n]", String.valueOf(this.numericalValue));
        return this;
    }
    public LabelAnimations numericalValue(int numericalValue, boolean considerCallCount){
        this.numericalValue = numericalValue;
        this.numericalValueText(null);
        this.text(this.getText() + ((considerCallCount && this.callCount > 0) ? this.numericalValueText : ""));
        return this;
    }

    public LabelAnimations color(Paint paint){
        this.label.setTextFill(paint);
        return this;
    }
    public LabelAnimations speed(double speed){
        this.speed = speed;
        return this;
    }
    public LabelAnimations fromValue(double fromValue){
        this.checkValue(fromValue, 0.0, 1.0);
        this.fromValue = fromValue;
        this.difference = this.updateDifference(this.fromValue, this.toValue);
        return this;
    }
    public LabelAnimations toValue(double toValue){
        this.checkValue(toValue, 0.0, 1.0);
        this.toValue = toValue;
        this.difference = this.updateDifference(this.fromValue, this.toValue);
        return this;
    }
    public LabelAnimations steps(double steps){
        this.checkValue(steps, 0.0, 1.0);
        this.steps = steps;
        return this;
    }
    public LabelAnimations waitingTime(double waitingTime){
        this.waitingTime = waitingTime;
        return this;
    }
    public LabelAnimations text(String text){
        this.text = text;
        this.label.setText(this.text);
        return this;
    }
    public static LabelAnimations instance(Label label){
        return new LabelAnimations(label);
    }

    public void startAnimation(){
        if(this.incrementedNumericalValue){
            this.numericalValue(this.numericalValue + 1, true);
        }
        this.callCount++;
        boolean animationsAllowed = Boolean.parseBoolean(settings.getSettingValue("animations", false));
        this.checkAndStopTimelines(this.timeout);
        this.checkAndStopTimelines(this.animationTimeline);
        this.label.setOpacity(1.0);
        this.timeout = Utilities.sleep(Duration.millis(this.waitingTime), 1, run -> {
            if(!animationsAllowed){
                this.label.setOpacity(this.toValue);
                labelAnimations.remove(this);
            }else{
                this.animationTimeline = Utilities.sleep(Duration.millis(this.speed), (int) ((this.fromValue - this.toValue) / this.steps), run2 -> {
                    if (this.label.getOpacity() > this.fromValue || this.label.getOpacity() < this.toValue) {
                        return;
                    }
                    this.label.setOpacity(this.label.getOpacity() - this.steps);
                }, event -> labelAnimations.remove(this));
            }
        }, null);

    }
    private void checkValue(double value, double... expectedValues){
        if(expectedValues.length == 0 || expectedValues.length > 2)
            return;
        if(expectedValues.length == 1 && value < expectedValues[0]){
            throw new IllegalArgumentException("The value '"+value+"' is lower than expected "+expectedValues[0]+".");
        }else if(expectedValues.length == 2 && (value < expectedValues[0] || value > expectedValues[1])){
            throw new IllegalArgumentException("The value '"+value+"' is lower than expected "+expectedValues[0]+" or higher than "+expectedValues[1]);
        }
    }
    private double updateDifference(double fromValue, double toValue){
        return Math.abs(fromValue - toValue);
    }
    private void checkAndStopTimelines(Timeline timeline){
        if(timeline != null && timeline.getStatus() == Animation.Status.RUNNING){
            timeline.stop();
        }
    }
}