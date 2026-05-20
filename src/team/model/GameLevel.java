package team.model;

/**
 * Represents one playable level.
 *
 * Responsibility:
 * - Own the level objects: Player, Door and Spike.
 * - Manage the main level state: movement flags, win flag and attempt counter.
 * - Run the per-frame game logic in tick().
 */
public class GameLevel {
    private static final int WORLD_WIDTH = 900;
    private static final int WORLD_HEIGHT = 520;
    private static final int FLOOR_Y = 445;

    private Player player;
    private Door door;
    private Spike spike;

    private boolean moveLeft;
    private boolean moveRight;
    private boolean won;

    private int attemptCount;
    private String backgroundName;

    public GameLevel() {
        reset();
    }

    /**
     * Full reset used when starting a new game.
     */
    public void reset() {
        this.attemptCount = 1;
        loadLevelObjects();
    }

    /**
     * Creates or recreates the actual objects in the level.
     * This method is also used after death, but without resetting the attempt counter.
     */
    private void loadLevelObjects() {
        this.player = new Player(70, FLOOR_Y - 48, 34, 48);
        this.door = new Door(790, FLOOR_Y - 82, 48, 82);

        // The spike is located on the floor at x=395.
        // It becomes visible when the player reaches x=300.
        this.spike = new Spike(395, FLOOR_Y - 34, 44, 34, 300);

        this.moveLeft = false;
        this.moveRight = false;
        this.won = false;
        this.backgroundName = "level1";
    }

    /**
     * Restart caused by a dangerous collision.
     * The attempt counter is kept and incremented, because this is part of the gameplay feedback.
     */
    private void restartAfterDeath() {
        attemptCount++;
        loadLevelObjects();
    }

    public void tick() {
        if (won) {
            return;
        }

        player.tick(moveLeft, moveRight, FLOOR_Y, WORLD_WIDTH);

        // Dynamic object update: the spike reacts to player location.
        spike.update(player);

        // Loss condition: the player touches an active dangerous spike.
        if (spike.isDangerous() && player.intersects(spike)) {
            restartAfterDeath();
            return;
        }

        // Win condition: the player reaches the door.
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

    public Spike getSpike() {
        return spike;
    }

    public boolean isWon() {
        return won;
    }

    public int getAttemptCount() {
        return attemptCount;
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
