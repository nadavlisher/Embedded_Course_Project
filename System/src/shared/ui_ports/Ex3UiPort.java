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

    // Your UI commands here, for example:
    public abstract void addPoint(int id, double x, double y);
    public abstract void updatePoint(int id, double x, double y);

    public abstract void addCircle(int id, double cx, double cy, double r);
    public abstract void updateCircle(int id, double cx, double cy, double r);

    public abstract void paintPoint(int pointId, String colorName);
    public abstract void blinkCircle(int circleId, int times);
    public abstract void log(String message);
}