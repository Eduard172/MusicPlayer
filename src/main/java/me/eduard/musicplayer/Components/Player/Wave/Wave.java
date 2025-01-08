package me.eduard.musicplayer.Components.Player.Wave;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;

@SuppressWarnings("unused")
public class Wave extends AnchorPane{

    private Canvas canvas;
    private GraphicsContext graphicsContext;

    private WaveProperties waveProperties;

    public Wave(){
        this.waveProperties = new WaveProperties(this);
        this.setWidth(this.waveProperties.getCanvas().getWidth());
        this.setHeight(this.waveProperties.getCanvas().getHeight());
        this.setWaveProperties(this.waveProperties);
    }

    private void drawWave(GraphicsContext graphicsContext) {
        graphicsContext.clearRect(0, 0, this.waveProperties.getWidth(), this.waveProperties.getHeight());

        graphicsContext.setStroke(this.waveProperties.getOutlineColor());
        graphicsContext.setFill(this.waveProperties.getInteriorColor());

        graphicsContext.setLineWidth(this.waveProperties.getOutlineWidth());

        double startX = (this.waveProperties.getWidth() - this.waveProperties.getLength()) / 2;
        double baselineY = (waveProperties.getHeight() / 2);
        graphicsContext.beginPath();

        for (double x = 0; x <= this.waveProperties.getLength(); x++) {
            double cosine = Math.cos(x * 2 * Math.PI / this.waveProperties.getLength());
            double y = baselineY + this.waveProperties.getAmplitude() * cosine;
            graphicsContext.lineTo(startX + x, y);
        }
        graphicsContext.closePath();
        graphicsContext.fill();
        graphicsContext.stroke();
    }

    /**
     * Updates the Wave's current variables to the new {@link WaveProperties} values.
     */
    public void setWaveProperties(WaveProperties waveProperties){
        this.getChildren().clear();
        this.waveProperties = waveProperties;
        if(this.graphicsContext != null)
            this.graphicsContext.clearRect(0, 0, this.waveProperties.getCanvas().getWidth(), this.waveProperties.getCanvas().getHeight());
        this.graphicsContext = this.waveProperties.applyCurrentValues();
        this.drawWave(this.graphicsContext);
        this.getChildren().add(this.waveProperties.getCanvas());
    }

    public WaveProperties getWaveProperties(){
        return this.waveProperties;
    }
    public GraphicsContext getGraphicsContext(){
        return this.graphicsContext;
    }
    public AnchorPane getRoot(){
        return this;
    }
    public void delete(){
        this.getChildren().remove(this.waveProperties.getCanvas());
    }

}
