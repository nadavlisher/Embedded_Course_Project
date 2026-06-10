package shared.ui_ports;

public abstract class UiPort {

    private static UiPort instance;

    public static void setInstance(UiPort ui) {
        if (ui == null) throw new IllegalArgumentException("UiPort instance cannot be null");
        if (instance != null) throw new IllegalStateException("UiPort instance already set");
        instance = ui;
    }

    public static UiPort getInstance() {
        if (instance == null) throw new IllegalStateException("UiPort instance not set yet");
        return instance;
    }

    public abstract void setBackground(int worldWidth, int worldHeight, int floorY, String backgroundName);

    public abstract void updatePlayer(int x, int y, int width, int height, double vx, double vy, boolean onGround);

    public abstract void updateDoor(int x, int y, int width, int height);

    public abstract void updateSpike(int x, int y, int width, int height, boolean visible, boolean dangerous);

    public abstract void setAttemptCount(int attemptCount);

    public abstract void setLevelNumber(int levelNumber);

    public abstract void setWinState(boolean won);

    public abstract void log(String message);
}
