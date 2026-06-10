package team.model;

/**
 * A deterministic Level Devil style trap.
 *
 * The spike starts hidden, appears when the player crosses the trigger X,
 * then becomes dangerous after a short fixed delay. This keeps the trap
 * surprising on the first attempt but learnable on retries.
 */
public class Spike extends GameObject {
    public enum State {
        HIDDEN,
        TRIGGERED,
        DANGEROUS
    }

    private static final int ACTIVATION_DELAY_TICKS = 12;

    private final int triggerX;
    private State state;
    private int ticksSinceTriggered;

    public Spike(int x, int y, int width, int height, int triggerX) {
        super(x, y, width, height);
        this.triggerX = triggerX;
        reset();
    }

    /**
     * Updates the trap state according to the player's location.
     * This model-layer method owns the dynamic trap behavior.
     */
    public void update(Player player) {
        if (state == State.HIDDEN && player.getX() >= triggerX) {
            state = State.TRIGGERED;
            ticksSinceTriggered = 0;
            return;
        }

        if (state == State.TRIGGERED) {
            ticksSinceTriggered++;
            if (ticksSinceTriggered >= ACTIVATION_DELAY_TICKS) {
                state = State.DANGEROUS;
            }
        }
    }

    public void reset() {
        state = State.HIDDEN;
        ticksSinceTriggered = 0;
    }

    public boolean isVisible() {
        return state != State.HIDDEN;
    }

    public boolean isDangerous() {
        return state == State.DANGEROUS;
    }

    public State getState() {
        return state;
    }

    public int getTriggerX() {
        return triggerX;
    }
}