package team.model;

import base.IdentifiedObject;

/**
 * A dynamic rectangle: either a falling block or an oscillating platform.
 *
 * Motion is integrated by TrapController; collision/landing is resolved by
 * CollisionController. Keeping both the kinematic fields and the patrol bounds
 * here lets the controllers stay stateless about individual movers.
 */
public class Mover extends IdentifiedObject {

    private final MoverKind kind;
    private double x, y;
    private final double width, height;

    private double vx, vy;
    private boolean landed = false;

    // Patrol parameters (platforms): travel between min..max on one axis.
    private double rangeMin, rangeMax;
    private double speed;     // pixels/second
    private int    dir = 1;   // +1 / -1 along the patrol axis

    public Mover(int id, MoverKind kind, double x, double y, double width, double height) {
        super(id);
        this.kind = kind;
        this.x = x; this.y = y; this.width = width; this.height = height;
    }

    public MoverKind getKind() { return kind; }
    public double getX()       { return x; }
    public double getY()       { return y; }
    public double getWidth()   { return width; }
    public double getHeight()  { return height; }
    public double getVx()      { return vx; }
    public double getVy()      { return vy; }
    public boolean isLanded()  { return landed; }
    public double getSpeed()   { return speed; }
    public int    getDir()     { return dir; }
    public double getRangeMin(){ return rangeMin; }
    public double getRangeMax(){ return rangeMax; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setVx(double vx){ this.vx = vx; }
    public void setVy(double vy){ this.vy = vy; }
    public void setLanded(boolean v) { this.landed = v; }
    public void setDir(int d)  { this.dir = d; }

    public void configurePatrol(double rangeMin, double rangeMax, double speed, int dir) {
        this.rangeMin = rangeMin; this.rangeMax = rangeMax;
        this.speed = speed; this.dir = dir;
    }

    /** Platforms are always solid; a falling block is solid only after it lands; spikes are never solid. */
    public boolean isSolid() {
        if (kind == MoverKind.FALLING_SPIKE) return false;
        return kind != MoverKind.FALLING_BLOCK || landed;
    }

    /** A falling block is lethal while airborne; a falling spike is always lethal. */
    public boolean isLethalNow() {
        if (kind == MoverKind.FALLING_SPIKE) return true;
        return kind == MoverKind.FALLING_BLOCK && !landed;
    }

    public HitBounds getBounds() {
        return new HitBounds(x, y, width, height);
    }
}
