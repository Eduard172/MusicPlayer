package me.eduard.musicplayer.Utils;

@SuppressWarnings("unused") public class Coords3D {
    private double x = 0, y = 0, z = 0;
    private double minX = 0, minY = 0, minZ = 0;
    private double maxX = 0, maxY = 0, maxZ = 0;
    public Coords3D(){}
    public Coords3D(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public void setX(double x){
        this.x = x;
    }
    public void setY(double y){
        this.y = y;
    }
    public void setZ(double z){
        this.z = z;
    }
    public void setMaxX(double maxX){
        this.maxX = maxX;
    }
    public void setMaxY(double maxY){
        this.maxY = maxY;
    }
    public void setMaxZ(double maxZ){
        this.maxZ = maxZ;
    }
    public void setMinX(double minX){
        this.minX = minX;
    }
    public void setMinY(double minY){
        this.minY = minY;
    }
    public void setMinZ(double minZ){
        this.minZ = minZ;
    }
    public double getX(){
        return this.x;
    }
    public double getY(){
        return this.y;
    }
    public double getZ(){
        return this.z;
    }
    public double getMinX(){
        return this.minX;
    }
    public double getMinY(){
        return this.minY;
    }
    public double getMinZ(){
        return this.minZ;
    }
    public double getMaxX(){
        return this.maxX;
    }
    public double getMaxY(){
        return this.maxY;
    }
    public double getMaxZ(){
        return this.maxZ;
    }
    public double getCenterX(){
        return (this.x + this.maxX) * 0.5;
    }
    public double getCenterY(){
        return (this.y + this.maxY) * 0.5;
    }
    public double getCenterZ(){
        return (this.z + this.maxZ) * 0.5;
    }

}
