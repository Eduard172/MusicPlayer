package me.eduard.musicplayer.Utils;

import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import me.eduard.musicplayer.MainApp;

public class LabelUtilities {
    public static Coords3D getLabelXCoords(Label label){
        Coords3D coords3D = new Coords3D();
        coords3D.setX(361);
        coords3D.setMaxX(label.getWidth() + 361 - 1);
        coords3D.setY(label.getLayoutY());
        return coords3D;
    }
    public static Coords3D getExactLabelXCoords(Label label){
        Coords3D coords3D = new Coords3D();
        coords3D.setX(label.getLayoutX());
        coords3D.setMaxX(label.getLayoutX() + label.getWidth());
        coords3D.setY(label.getLayoutY());
        return coords3D;
    }
    public static void centralizeLabel(Label label, Rectangle rectangle1, Rectangle rectangle2){
        Coords3D labelCoords = getLabelXCoords(label);
        double labelWidth = labelCoords.getMaxX() - labelCoords.getX();
        double point1 = rectangle1.getWidth() - 4;
        double point2 = MainApp.WIDTH - rectangle2.getWidth() + 1;
        double midPoint = (point1 + point2) * 0.5;
        label.setLayoutX(midPoint - (labelWidth * 0.5));
        label.setLayoutY(585);
    }
    public static double getLabelMidPoint(Rectangle rectangle1, Rectangle rectangle2){
        double point1 = rectangle1.getWidth() - 4;
        double point2 = MainApp.WIDTH - rectangle2.getWidth() + 1;
        return (point1 + point2) * 0.5;
    }
    public static void moveLabelToLeft(Label label, Rectangle rectangle1){
        double leftPoint = MainApp.WIDTH - rectangle1.getWidth() + 2;
        label.setLayoutY(585);
        label.setLayoutX(leftPoint);
    }
    public static void vanishLabels(Label... labels){
        for(Label label : labels){
            label.setVisible(false);
        }
    }
    public static void clearLabel(Duration duration, Label label){
        Utilities.sleep(duration, 1, run -> label.setText(""), null);
    }
}
