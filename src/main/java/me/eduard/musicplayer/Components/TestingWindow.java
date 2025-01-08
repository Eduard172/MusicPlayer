package me.eduard.musicplayer.Components;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class TestingWindow implements Initializable {

    @FXML public Label devLabel;
    @FXML public AnchorPane corePane;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.draw();
    }

    private void draw(){
        final double WIDTH = 1500;
        final double HEIGHT = 800;
        final Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        final double bottomLineWidth = 50;
        final double scaleFactor = 25;
        final double waveLength = 100;
        final double offset = 200;

        gc.beginPath();
        gc.moveTo(offset, 400);
        gc.setFill(Color.BLACK);


        for(double x = 0; x <= 200; x++){
            double cos = scaleFactor * Math.cos(Math.PI * (x / waveLength));
            gc.lineTo(x + offset, 400 + cos);
        }

        gc.stroke();

        corePane.getChildren().add(canvas);

    }

}
