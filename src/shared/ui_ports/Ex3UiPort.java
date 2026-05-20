package shared.ui_ports;

public abstract class Ex3UiPort {

    private static Ex3UiPort instance;

    public static void setInstance(Ex3UiPort ui) {
        if (ui == null) throw new IllegalArgumentException("Ex3UiPort instance cannot be null");
        if (instance != null) throw new IllegalStateException("Ex3UiPort instance already set");
        instance = ui;
    }

    public static Ex3UiPort getInstance() {
        if (instance == null) throw new IllegalStateException("Ex3UiPort instance not set yet");
        return instance;
    }

    public abstract void setBackground(int worldWidth, int worldHeight, int floorY, String backgroundName);

    public abstract void updatePlayer(int x, int y, int width, int height, double vx, double vy, boolean onGround);

    public abstract void updateDoor(int x, int y, int width, int height);

    public abstract void updateSpike(int x, int y, int width, int height, boolean visible, boolean dangerous);

    public abstract void setAttemptCount(int attemptCount);

    public abstract void setWinState(boolean won);

    public abstract void log(String message);
}
