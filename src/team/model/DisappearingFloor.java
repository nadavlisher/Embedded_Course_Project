package team.model;

public class DisappearingFloor extends GameObject {
    public enum State {
        VISIBLE,
        TRIGGERED,
        GONE
    }

    private static final int DISAPPEAR_DELAY_TICKS = 15;

    private final int triggerX;
    private State state;
    private int ticksSinceTriggered;

    public DisappearingFloor(int x, int y, int width, int height, int triggerX) {
        super(x, y, width, height);
        this.triggerX = triggerX;
        reset();
    }

    public void update(Player player) {
        if (state == State.VISIBLE && player.getX() >= triggerX) {
            state = State.TRIGGERED;
            ticksSinceTriggered = 0;
            return;
        }

        if (state == State.TRIGGERED) {
            ticksSinceTriggered++;
            if (ticksSinceTriggered >= DISAPPEAR_DELAY_TICKS) {
                state = State.GONE;
            }
        }
    }

    public void reset() {
        state = State.VISIBLE;
        ticksSinceTriggered = 0;
    }

    public boolean isSolid() {
        return state != State.GONE;
    }

    public boolean isVisible() {
        return state != State.GONE;
    }

    public boolean isTriggered() {
        return state == State.TRIGGERED;
    }

    public State getState() {
        return state;
    }

    public int getTriggerX() {
        return triggerX;
    }
}
