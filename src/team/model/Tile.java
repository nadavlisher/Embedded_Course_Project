package team.model;

import base.IdentifiedObject;

/**
 * A static, solid platform block. Tiles never move; a "disappearing floor"
 * effect is modelled by a Trap removing tiles from the Arena, not by the tile
 * mutating itself.
 */
public class Tile extends IdentifiedObject {

    private final double x, y, width, height;
    private final TileKind kind;

    public Tile(int id, double x, double y, double width, double height, TileKind kind) {
        super(id);
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.kind = kind;
    }

    public double getX()      { return x; }
    public double getY()      { return y; }
    public double getWidth()  { return width; }
    public double getHeight() { return height; }
    public TileKind getKind() { return kind; }

    public HitBounds getBounds() {
        return new HitBounds(x, y, width, height);
    }
}
