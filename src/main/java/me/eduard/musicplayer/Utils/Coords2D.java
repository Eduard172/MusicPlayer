package me.eduard.musicplayer.Utils;

@SuppressWarnings("unused") public class Coords2D {
    private double x = 0, y = 0;
    private double minX = 0, minY = 0;
    private double maxX = 0, maxY = 0;
    public Coords2D(){}
    public Coords2D(double x, double y){
        this.x = x;
        this.y = y;
    }
    public void setX(double x){
        this.x = x;
    }
    public void setY(double y){
        this.y = y;
    }
    public void setMaxX(double maxX){
        this.maxX = maxX;
    }
    public void setMaxY(double maxY){
        this.maxY = maxY;
    }
    public void setMinX(double minX){
        this.minX = minX;
    }
    public void setMinY(double minY){
        this.minY = minY;
    }
    public double getX(){
        return this.x;
    }
    public double getY(){
        return this.y;
    }
    public double getMinX(){
        return this.minX;
    }
    public double getMinY(){
        return this.minY;
    }
    public double getMaxX(){
        return this.maxX;
    }
    public double getMaxY(){
        return this.maxY;
    }
    public double getCenterX(){
        return (this.x + this.maxX) * 0.5;
    }
    public double getCenterY(){
        return (this.y + this.maxY) * 0.5;
    }

}
