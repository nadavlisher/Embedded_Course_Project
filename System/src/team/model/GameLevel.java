package team.model;

public class GameLevel {
    private static final int WORLD_WIDTH = 900;
    private static final int WORLD_HEIGHT = 520;
    private static final int FLOOR_Y = 445;

    private Player player;
    private Door door;

    private boolean moveLeft;
    private boolean moveRight;
    private boolean won;

    private String backgroundName;

    public GameLevel() {
        reset();
    }

    public void reset() {
        this.player = new Player(70, FLOOR_Y - 48, 34, 48);
        this.door = new Door(790, FLOOR_Y - 82, 48, 82);

        this.moveLeft = false;
        this.moveRight = false;
        this.won = false;
        this.backgroundName = "level1";
    }

    public void tick() {
        if (won) {
            return;
        }

        player.tick(moveLeft, moveRight, FLOOR_Y, WORLD_WIDTH);

        if (player.intersects(door)) {
            won = true;
        }
    }

    public void setMoveLeft(boolean moveLeft) {
        this.moveLeft = moveLeft;
    }

    public void setMoveRight(boolean moveRight) {
        this.moveRight = moveRight;
    }

    public void jump() {
        player.jump();
    }

    public Player getPlayer() {
        return player;
    }

    public Door getDoor() {
        return door;
    }

    public boolean isWon() {
        return won;
    }

    public int getWorldWidth() {
        return WORLD_WIDTH;
    }

    public int getWorldHeight() {
        return WORLD_HEIGHT;
    }

    public int getFloorY() {
        return FLOOR_Y;
    }

    public String getBackgroundName() {
        return backgroundName;
    }
}