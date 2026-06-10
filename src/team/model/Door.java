package team.model;

import base.IdentifiedObject;


public class Door extends IdentifiedObject {

    private double x, y;
    private final double width, height;
    private final boolean real;

    private boolean moving = false;
    private double targetX, targetY;
    private double slideSpeed = 0;

    public Door(int id, double x, double y, double width, double height, boolean real) {
        super(id);
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.real = real;
        this.targetX = x; this.targetY = y;
    }

    public double getX()      { return x; }
    public double getY()      { return y; }
    public double getWidth()  { return width; }
    public double getHeight() { return height; }
    public boolean isReal()   { return real; }
    public boolean isMoving() { return moving; }
    public double getTargetX(){ return targetX; }
    public double getTargetY(){ return targetY; }

    public void setPosition(double x, double y) { this.x = x; this.y = y; }

    /** Begin sliding toward (tx,ty) at {@code speed} pixels/second. */
    public void slideTo(double tx, double ty, double speed) {
        this.targetX = tx; this.targetY = ty;
        this.slideSpeed = speed;
        this.moving = true;
    }

    /** Advance the slide animation; returns true once it has arrived. */
    public boolean stepSlide(double dt) {
        if (!moving) return true;
        double dx = targetX - x, dy = targetY - y;
        double dist = Math.hypot(dx, dy);
        double step = slideSpeed * dt;
        if (dist <= step || dist == 0) {
            x = targetX; y = targetY; moving = false;
            return true;
        }
        x += dx / dist * step;
        y += dy / dist * step;
        return false;
    }

    /** Door area, inset a little so the player must really stand in the doorway. */
    public HitBounds getBounds() {
        double in = 6;
        return new HitBounds(x + in, y + in, width - 2 * in, height - 2 * in);
    }
}
