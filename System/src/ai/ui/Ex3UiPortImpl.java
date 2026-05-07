package ai.ui;

import shared.ui_ports.Ex3UiPort;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class Ex3UiPortImpl extends Ex3UiPort {
    private Map<String, Point> points;
    private Map<String, Circle> circles;
    private JPanel panel;
    private Map<Integer, Timer> blinkTimers = new HashMap<>();

    public Ex3UiPortImpl(Map<String, Point> points, Map<String, Circle> circles, JPanel panel) {
        this.points = points;
        this.circles = circles;
        this.panel = panel;
    }

    @Override
    public void addPoint(int pointId, double x, double y) {
        points.put(String.valueOf(pointId), new Point((int) x, (int) y));
        panel.repaint();
    }

    @Override
    public void updatePoint(int pointId, double x, double y) {
        Point point = points.get(String.valueOf(pointId));
        if (point != null) {
            point.x = (int) x;
            point.y = (int) y;
            panel.repaint();
        }
    }

    @Override
    public void addCircle(int circleId, double cx, double cy, double radius) {
        circles.put(String.valueOf(circleId), new Circle((int) cx, (int) cy, (int) radius));
        panel.repaint();
    }

    @Override
    public void updateCircle(int circleId, double cx, double cy, double radius) {
        Circle circle = circles.get(String.valueOf(circleId));
        if (circle != null) {
            circle.update((int) cx, (int) cy, (int) radius);
            panel.repaint();
        }
    }

    @Override
    public void paintPoint(int pointId, String color) {
        Point point = points.get(String.valueOf(pointId));
        if (point != null) {
            point.color = parseColor(color);
            panel.repaint();
        }
    }

    @Override
    public void blinkCircle(int circleId, int count) {
        Circle circle = circles.get(String.valueOf(circleId));
        if (circle != null) {
            // Cancel any existing blink timer for this circle
            Timer existingTimer = blinkTimers.get(circleId);
            if (existingTimer != null) {
                existingTimer.stop();
            }

            circle.isBlinking = true;
            int[] blinkCount = { 0 };

            Timer blinkTimer = new Timer(250, e -> {
                circle.isBlinking = !circle.isBlinking;
                blinkCount[0]++;
                panel.repaint();

                // Stop blinking after count blinks
                if (blinkCount[0] >= count * 2) {
                    ((Timer) e.getSource()).stop();
                    circle.isBlinking = false;
                    blinkTimers.remove(circleId);
                    panel.repaint();
                }
            });
            blinkTimer.setRepeats(true);
            blinkTimer.start();
            blinkTimers.put(circleId, blinkTimer);

            panel.repaint();
        }
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

    @Override
    public void log(String message) {
        System.out.println(message);
    }
}