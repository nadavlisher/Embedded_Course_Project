package team.model;

import base.IdentifiedObject;

public class Point extends IdentifiedObject {
    private double x;
    private double y;

    public Point(int id, double x, double y) {
        super(id);
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
}