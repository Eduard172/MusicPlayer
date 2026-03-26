package me.eduard.musicplayer.Components.Player.Wave;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class WaveProperties {

    private double layoutX, layoutY, outlineWidth, amplitude, length, width, height, opacity;
    /**
     * Current LayoutY is being modified and calculated to match the exact needed Y value. This variable just shows the visual representation of the needed value,
     * whereas the real Y value is subtracted by 96.
     */
    private double realLayoutY;
    /**
     * This variable keeps track of the local "Amplitude" variable which updates 'on-spot' when the 'amplitude(double)' method is called.
     * <p>
     * The difference between this variable and the 'amplitude' variable is that the 'amplitude' variable updates before the change of the 'layoutY' which allows
     * to update the calculation of the needed Y coordinate properly to avoid any errors, whereas this variable updates after the 'layoutY' update.
     * <p>
     * The purpose of this variable is to successfully execute a condition in order to calculate the 'layoutY' coordinate to correspond to user's needs.
     */
    private double tempAmplitude;

    private Paint outlineColor, interiorColor;

    private final GraphicsContext graphicsContext;

    private final Canvas canvas;
    private final AnchorPane anchorPane;

    public WaveProperties(AnchorPane anchorPane){
        this.anchorPane = anchorPane;
        this.canvas = new Canvas();
        this.layoutX = 0;
        this.layoutY = 0;
        this.outlineWidth = 1;
        this.amplitude = 20;
        this.length = 100;
        this.width = 0;
        this.height = 0;
        this.outlineColor = Color.rgb(255, 105, 180);
        this.interiorColor = Color.rgb(255, 105, 180, 0.3);
        this.graphicsContext = this.applyCurrentValues();
    }

    /**
     * Sets the layout X coordinate of the Wave object starting from the beginning of it.
     */
    public WaveProperties layoutX(double layoutX){
        this.layoutX = layoutX;
        this.anchorPane.setLayoutX(this.layoutX);
        return this;
    }

    /**
     * Sets the layout Y coordinate of the Wave object starting from the bottom of it.
     */
    public WaveProperties layoutY(double layoutY){
        this.layoutY = layoutY - 96;
        this.anchorPane.setLayoutY(this.layoutY);
        this.realLayoutY = layoutY;
        return this;
    }

    /**
     * Sets the opacity of the Wave.
     */
    public WaveProperties opacity(double opacity){
        this.opacity = opacity;
        this.anchorPane.setOpacity(this.opacity);
        return this;
    }

    /**
     * Sets the outline thickness of the Wave.
     */
    public WaveProperties outlineWidth(double outlineWidth){
        this.outlineWidth = outlineWidth;
        return this;
    }

    /**
     * Sets the top height of the Wave.
     */
    public WaveProperties amplitude(double amplitude){
        this.amplitude = amplitude;
        this.height = Math.max(this.height, this.amplitude * 2);
        this.layoutY = (this.tempAmplitude > amplitude) ? this.realLayoutY - (this.height / 2 + this.amplitude) : this.realLayoutY - this.height;
        this.tempAmplitude = amplitude;
        return this;
    }

    /**
     * Sets the length of the Wave.
     */
    public WaveProperties length(double length){
        this.length = length;
        this.width = Math.max(this.width, this.length);
        return this;
    }

    /**
     * Sets the overall Object width which contains the Wave.
     * <p>
     *  If the width is lower than the length then there might occur some graphics errors.
     *  <p>
     *  This method is not recommended to be manually used by the user since it's automatically
     *  managed by the {@link WaveProperties#length(double)} method, unless you have a good-defined reason to do so.
     */
    public WaveProperties width(double width){
        this.width = width;
        return this;
    }

    /**
     * Sets the overall Object height which contains the Wave.
     * <p>
     * If the height is lower than the amplitude then there might occur some graphics errors.
     * <p>
     * This method is not recommended to be manually used by the user since it's automatically
     * managed by the {@link WaveProperties#amplitude(double)} method, unless you have a good-defined reason to do so.
     */
    public WaveProperties height(double height){
        this.height = height;
        return this;
    }

    /**
     * Sets the outline color of the Wave. This method does not interact with the interior color, which is managed by {@link WaveProperties#interiorColor(Paint)}.
     */
    public WaveProperties outlineColor(Paint outlineColor){
        this.outlineColor = outlineColor;
        return this;
    }

    /**
     * Sets the interior color of the Wave. This method does not interact with the outline color, which is managed by {@link WaveProperties#outlineColor(Paint)}.
     */
    public WaveProperties interiorColor(Paint interiorColor){
        this.interiorColor = interiorColor;
        return this;
    }

    /**
     * Applies the modified values and returns the final result as a {@link GraphicsContext}.
     * This method is only used by the {@link Wave} class, so you don't really have to worry about it.
     */
    public GraphicsContext applyCurrentValues(){
        this.anchorPane.setLayoutX(this.layoutX);
        this.anchorPane.setLayoutY(this.layoutY);
        this.anchorPane.setOpacity(this.opacity);
        this.canvas.setWidth(this.width);
        this.canvas.setHeight(this.height);
        GraphicsContext graphicsContext = this.canvas.getGraphicsContext2D();
        graphicsContext.setFill(this.interiorColor);
        graphicsContext.setStroke(this.outlineColor);
        graphicsContext.setLineWidth(this.outlineWidth);
        return graphicsContext;
    }

    public GraphicsContext getGraphicsContext(){
        return this.graphicsContext;
    }

    /**
     * Resets the current variables to their default representation of their values.
     */
    public WaveProperties resetValues(){
        return new WaveProperties(this.anchorPane);
    }

    public Canvas getCanvas(){
        return this.canvas;
    }

    public double getLayoutX() {
        return this.layoutX;
    }

    public double getLayoutY() {
        return this.realLayoutY;
    }

    public double getOutlineWidth() {
        return this.outlineWidth;
    }

    public double getAmplitude() {
        return this.amplitude;
    }

    public double getOpacity(){
        return this.opacity;
    }

    public double getLength() {
        return this.length;
    }

    public double getWidth() {
        return this.width;
    }

    public double getHeight() {
        return this.height;
    }

    public Paint getOutlineColor() {
        return this.outlineColor;
    }

    public Paint getInteriorColor() {
        return this.interiorColor;
    }
}