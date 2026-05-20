package team.model;

/**
 * A dynamic trap object.
 *
 * Story implemented for Assignment 5:
 * When the player reaches a hidden trigger zone, the spike appears.
 * If the player touches it after it becomes dangerous, the level restarts.
 */
public class Spike extends GameObject {
    private final int triggerX;
    private boolean visible;
    private boolean dangerous;

    public Spike(int x, int y, int width, int height, int triggerX) {
        super(x, y, width, height);
        this.triggerX = triggerX;
        reset();
    }

    /**
     * Updates the trap state according to the player's location.
     * This is the exact point where the dynamic behavior enters the model.
     */
    public void update(Player player) {
        if (!visible && player.getX() >= triggerX) {
            visible = true;
            dangerous = true;
        }
    }

    public void reset() {
        visible = false;
        dangerous = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isDangerous() {
        return dangerous;
    }

    public int getTriggerX() {
        return triggerX;
    }
}
