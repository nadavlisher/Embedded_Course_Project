package team.model;

public class Player {
    private double x;
    private double y;
    private double vx;
    private double vy;

    private final int width;
    private final int height;

    private boolean onGround;

    private static final double MOVE_SPEED = 12.0;
    private static final double JUMP_SPEED = -13.0;
    private static final double GRAVITY = 0.75;
    private static final double MAX_FALL_SPEED = 18.0;

    public Player(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.vx = 0;
        this.vy = 0;
        this.onGround = false;
    }

    public void tick(boolean moveLeft, boolean moveRight, int floorY, int worldWidth) {
        vx = 0;

        if (moveLeft) {
            vx -= MOVE_SPEED;
        }

        if (moveRight) {
            vx += MOVE_SPEED;
        }

        vy += GRAVITY;

        if (vy > MAX_FALL_SPEED) {
            vy = MAX_FALL_SPEED;
        }

        x += vx;
        y += vy;

        if (x < 0) {
            x = 0;
        }

        if (x + width > worldWidth) {
            x = worldWidth - width;
        }

        if (y + height >= floorY) {
            y = floorY - height;
            vy = 0;
            onGround = true;
        } else {
            onGround = false;
        }
    }

    public void jump() {
        if (onGround) {
            vy = JUMP_SPEED;
            onGround = false;
        }
    }

    public boolean intersects(Door door) {
        return x < door.getX() + door.getWidth()
            && x + width > door.getX()
            && y < door.getY() + door.getHeight()
            && y + height > door.getY();
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