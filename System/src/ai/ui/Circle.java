package ai.ui;

public class Circle {
    public int cx, cy, radius;
    public boolean isBlinking;

    public Circle(int cx, int cy, int radius) {
        this.cx = cx;
        this.cy = cy;
        this.radius = radius;
        this.isBlinking = false;
    }

    public void update(int cx, int cy, int radius) {
        this.cx = cx;
        this.cy = cy;
        this.radius = radius;
    }
}
