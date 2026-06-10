package team.model;

/**
 * Kinds of dynamic block.
 *
 *  FALLING_BLOCK - drops under gravity; lethal while airborne, becomes a solid
 *                  ledge once it lands.
 *  PLATFORM_H    - solid platform that oscillates horizontally; carries the
 *                  player when ridden.
 *  PLATFORM_V    - solid platform that oscillates vertically.
 *  FALLING_SPIKE - drops from above like a block but is lethal at all times
 *                  (you can never stand on it).
 */
public enum MoverKind {
    FALLING_BLOCK,
    FALLING_SPIKE,
    PLATFORM_H,
    PLATFORM_V
}
