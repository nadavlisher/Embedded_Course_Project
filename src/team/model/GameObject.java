package team.model;

/**
 * Base class for game objects that have a rectangular position in the game world.
 *
 * Why it exists:
 * - Player, Door and Spike all have x/y/width/height.
 * - Collision detection should not be duplicated in every class.
 * - This keeps the model cohesive and encapsulated.
 */
public abstract class GameObject {
    protected double x;
    protected double y;
    protected final int width;
    protected final int height;

    public GameObject(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Axis-Aligned Bounding Box collision check.
     * This is enough for the current 2D platformer POC because all current
     * objects can be approximated by rectangles.
     */
    public boolean intersects(GameObject other) {
        return x < other.x + other.width
            && x + width > other.x
            && y < other.y + other.height
            && y + height > other.y;
    }

    public int getX() {
        return (int) Math.round(x);
    }

    public int getY() {
        return (int) Math.round(y);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
