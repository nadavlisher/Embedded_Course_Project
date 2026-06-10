package team.model;

/**
 * The little devil the player controls.
 *
 * Mutable kinematic body: position, velocity, grounded flag and facing.
 * PlayerController writes the velocity, CollisionController integrates and
 * resolves the position against the world.
 */
public class Player {

    private double x, y;
    private final double width, height;
    private double vx, vy;
    private boolean onGround = false;
    private int facing = 1; // +1 right, -1 left

    public Player(double x, double y, double width, double height) {
        this.x = x; this.y = y; this.width = width; this.height = height;
    }

    public double getX()      { return x; }
    public double getY()      { return y; }
    public double getWidth()  { return width; }
    public double getHeight() { return height; }
    public double getVx()     { return vx; }
    public double getVy()     { return vy; }
    public boolean isOnGround(){ return onGround; }
    public int getFacing()    { return facing; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setVx(double vx){ this.vx = vx; if (vx > 0) facing = 1; else if (vx < 0) facing = -1; }
    public void setVy(double vy){ this.vy = vy; }
    public void setOnGround(boolean v) { this.onGround = v; }
    public void setFacing(int f){ this.facing = f; }

    public void setPosition(double x, double y) { this.x = x; this.y = y; }

    public HitBounds getBounds() {
        return new HitBounds(x, y, width, height);
    }
}
