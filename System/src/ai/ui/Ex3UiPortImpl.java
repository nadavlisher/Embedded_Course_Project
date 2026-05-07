package ai.ui;

import shared.ui_ports.Ex3UiPort;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class Ex3UiPortImpl extends Ex3UiPort {
    private final JPanel panel;
    private final DrawingPanel drawingPanel;
    private final Map<String, Point> points;
    private final Map<String, Circle> circles;
    private final Map<Integer, Timer> blinkTimers = new HashMap<>();

    private PlayerView player;
    private DoorView door;
    private int worldWidth = 900;
    private int worldHeight = 520;
    private int floorY = 445;
    private String backgroundName = "level1";
    private boolean won = false;

    public Ex3UiPortImpl(DrawingPanel drawingPanel) {
        this.points = new HashMap<>();
        this.circles = new HashMap<>();
        this.panel = drawingPanel;
        this.drawingPanel = drawingPanel;
        this.drawingPanel.setGameUiPort(this);
    }

    public Ex3UiPortImpl(Map<String, Point> points, Map<String, Circle> circles, JPanel panel) {
        this.points = points != null ? points : new HashMap<>();
        this.circles = circles != null ? circles : new HashMap<>();
        this.panel = panel;
        this.drawingPanel = panel instanceof DrawingPanel ? (DrawingPanel) panel : null;
        if (this.drawingPanel != null) {
            this.drawingPanel.setGameUiPort(this);
        }
    }

    @Override
    public void setBackground(int worldWidth, int worldHeight, int floorY, String backgroundName) {
        runOnEdt(() -> {
            this.worldWidth = worldWidth;
            this.worldHeight = worldHeight;
            this.floorY = floorY;
            this.backgroundName = backgroundName;
            repaintPanel();
        });
    }

    @Override
    public void updatePlayer(int x, int y, int width, int height, double vx, double vy, boolean onGround) {
        runOnEdt(() -> {
            this.player = new PlayerView(x, y, width, height, vx, vy, onGround);
            repaintPanel();
        });
    }

    @Override
    public void updateDoor(int x, int y, int width, int height) {
        runOnEdt(() -> {
            this.door = new DoorView(x, y, width, height);
            repaintPanel();
        });
    }

    @Override
    public void setWinState(boolean won) {
        runOnEdt(() -> {
            this.won = won;
            repaintPanel();
        });
    }

    @Override
    public void addPoint(int pointId, double x, double y) {
        points.put(String.valueOf(pointId), new Point((int) x, (int) y));
        repaintPanel();
    }

    @Override
    public void updatePoint(int pointId, double x, double y) {
        Point point = points.get(String.valueOf(pointId));
        if (point != null) {
            point.x = (int) x;
            point.y = (int) y;
            repaintPanel();
        }
    }

    @Override
    public void addCircle(int circleId, double cx, double cy, double radius) {
        circles.put(String.valueOf(circleId), new Circle((int) cx, (int) cy, (int) radius));
        repaintPanel();
    }

    @Override
    public void updateCircle(int circleId, double cx, double cy, double radius) {
        Circle circle = circles.get(String.valueOf(circleId));
        if (circle != null) {
            circle.update((int) cx, (int) cy, (int) radius);
            repaintPanel();
        }
    }

    @Override
    public void paintPoint(int pointId, String color) {
        Point point = points.get(String.valueOf(pointId));
        if (point != null) {
            point.color = parseColor(color);
            repaintPanel();
        }
    }

    @Override
    public void blinkCircle(int circleId, int count) {
        Circle circle = circles.get(String.valueOf(circleId));
        if (circle != null) {
            Timer existingTimer = blinkTimers.get(circleId);
            if (existingTimer != null) {
                existingTimer.stop();
            }

            circle.isBlinking = true;
            int[] blinkCount = { 0 };

            Timer blinkTimer = new Timer(250, e -> {
                circle.isBlinking = !circle.isBlinking;
                blinkCount[0]++;
                repaintPanel();

                if (blinkCount[0] >= count * 2) {
                    ((Timer) e.getSource()).stop();
                    circle.isBlinking = false;
                    blinkTimers.remove(circleId);
                    repaintPanel();
                }
            });
            blinkTimer.setRepeats(true);
            blinkTimer.start();
            blinkTimers.put(circleId, blinkTimer);
            repaintPanel();
        }
    }

    @Override
    public void log(String message) {
        System.out.println(message);
    }

    public PlayerView getPlayer() {
        return player;
    }

    public DoorView getDoor() {
        return door;
    }

    public int getWorldWidth() {
        return worldWidth;
    }

    public int getWorldHeight() {
        return worldHeight;
    }

    public int getFloorY() {
        return floorY;
    }

    public String getBackgroundName() {
        return backgroundName;
    }

    public boolean isWon() {
        return won;
    }

    private Color parseColor(String colorStr) {
        try {
            switch (colorStr.toLowerCase()) {
                case "red":
                    return Color.RED;
                case "green":
                    return Color.GREEN;
                case "blue":
                    return Color.BLUE;
                case "yellow":
                    return Color.YELLOW;
                case "black":
                    return Color.BLACK;
                case "white":
                    return Color.WHITE;
                case "cyan":
                    return Color.CYAN;
                case "magenta":
                    return Color.MAGENTA;
                default:
                    return Color.BLACK;
            }
        } catch (Exception e) {
            return Color.BLACK;
        }
    }

    private void repaintPanel() {
        if (panel != null) {
            panel.repaint();
        }
    }

    private void runOnEdt(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        } else {
            SwingUtilities.invokeLater(action);
        }
    }
}