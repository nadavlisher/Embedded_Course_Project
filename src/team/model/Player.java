package team.model;

/**
 * The controllable player character.
 *
 * Responsibility:
 * - Hold player position and velocity.
 * - Apply simple platformer physics: horizontal movement, gravity and jump.
 * - Keep the player inside world bounds and on the floor.
 */
public class Player extends GameObject {
    private double vx;
    private double vy;

    private boolean onGround;

    private static final double MOVE_SPEED = 12.0;
    private static final double JUMP_SPEED = -13.0;
    private static final double GRAVITY = 0.75;
    private static final double MAX_FALL_SPEED = 18.0;

    public Player(double x, double y, int width, int height) {
        super(x, y, width, height);
        this.vx = 0;
        this.vy = 0;
        this.onGround = false;
    }

    public void tick(boolean moveLeft, boolean moveRight, int floorY, int worldWidth) {
        tick(moveLeft, moveRight, floorY, worldWidth, floorY);
    }

    public void tick(boolean moveLeft, boolean moveRight, int normalFloorY, int worldWidth, int activeFloorY) {
        vx = 0;

        if (moveLeft) {
            vx -= MOVE_SPEED;
        }

        if (moveRight) {
            vx += MOVE_SPEED;
        }

        // Gravity is applied every frame. vy is capped so the fall does not become uncontrollable.
        vy += GRAVITY;
        if (vy > MAX_FALL_SPEED) {
            vy = MAX_FALL_SPEED;
        }

        x += vx;
        y += vy;

        // Keep the player inside the horizontal world boundaries.
        if (x < 0) {
            x = 0;
        }

        if (x + width > worldWidth) {
            x = worldWidth - width;
        }

        // Floor collision. GameLevel may lower activeFloorY to create a temporary gap.
        if (y + height >= activeFloorY) {
            y = activeFloorY - height;
            vy = 0;
            onGround = activeFloorY == normalFloorY;
        } else {
            onGround = false;
        }
    }

    public void jump() {
        // The player can jump only when standing on the floor.
        if (onGround) {
            vy = JUMP_SPEED;
            onGround = false;
        }
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public boolean isOnGround() {
        return onGround;
    }
}
