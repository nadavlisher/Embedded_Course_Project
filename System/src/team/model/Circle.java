package team.model;

import base.IdentifiedObject;

public class Circle extends IdentifiedObject{
    private Point center;
    private double r;

    public Circle(int id, Point center, double r) {
        super(id);
        this.center = center;
        setR(r);
    }

    public Point getCenter() { return center; }
    public double getR() { return r; }

    public void setR(double r) {
        if (r <= 0) throw new IllegalArgumentException("Radius must be > 0");
        this.r = r;
    }

    public boolean intersects(Circle other) {
        double dx = center.getX() - other.center.getX();
        double dy = center.getY() - other.center.getY();
        double dist2 = dx * dx + dy * dy;
        double sum = r + other.r;
        return dist2 <= sum * sum;
    }

    public boolean contains(Point p) {
        double dx = center.getX() - p.getX();
        double dy = center.getY() - p.getY();
        double dist2 = dx * dx + dy * dy;
        return dist2 <= r * r;
    }
}