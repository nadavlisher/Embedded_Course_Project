package team.model;

/**
 * Visual/semantic flavour of a static platform tile.
 *
 *  SOLID   - ordinary stone block.
 *  BRITTLE - cracked block; telegraphs that a disappearing-floor trap may
 *            remove it. Still fully solid until a trap actually fires.
 */
public enum TileKind {
    SOLID,
    BRITTLE
}
