package team.model;

import base.IdentifiedObject;

/**
 * A lethal spike. Touching its (slightly inset) hit-box costs the player a
 * life. The inset makes near-misses survivable, which keeps a troll game feeling
 * "hard but fair" rather than arbitrary.
 */
public class Spike extends IdentifiedObject {

    private final double x, y, width, height;
    private final SpikeDir dir;

    public Spike(int id, double x, double y, double width, double height, SpikeDir dir) {
        super(id);
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.dir = dir;
    }

    public double getX()    { return x; }
    public double getY()    { return y; }
    public double getWidth(){ return width; }
    public double getHeight(){ return height; }
    public SpikeDir getDir(){ return dir; }

    /** Lethal area, inset from the drawn triangle so grazing the edge is safe. */
    public HitBounds getBounds() {
        double insetX = Math.min(6, width  / 3.0);
        double insetY = Math.min(6, height / 3.0);
        return new HitBounds(x + insetX, y + insetY, width - 2 * insetX, height - 2 * insetY);
    }
}
