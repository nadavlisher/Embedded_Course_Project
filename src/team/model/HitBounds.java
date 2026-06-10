package team.model;

/**
 * Tiny immutable axis-aligned bounding box used purely for collision math.
 *
 * Lives in the model package because Player/Tile/Spike/Door/Mover all expose
 * {@code getBounds()} returning a HitBounds, and the controllers need cheap
 * intersection tests without pulling in java.awt.Rectangle (which would couple
 * the model to AWT).
 */
public final class HitBounds {

    public final double x, y, width, height;

    public HitBounds(double x, double y, double width, double height) {
        this.x = x; this.y = y; this.width = width; this.height = height;
    }

    public double left()   { return x; }
    public double right()  { return x + width; }
    public double top()    { return y; }
    public double bottom() { return y + height; }
    public double cx()     { return x + width  / 2.0; }
    public double cy()     { return y + height / 2.0; }

    /** True if this box and {@code other} overlap (open intersection). */
    public boolean intersects(HitBounds other) {
        return this.left()   < other.right()
            && this.right()  > other.left()
            && this.top()    < other.bottom()
            && this.bottom() > other.top();
    }

    @Override
    public String toString() {
        return String.format("HitBounds(x=%.1f, y=%.1f, w=%.1f, h=%.1f)", x, y, width, height);
    }
}
