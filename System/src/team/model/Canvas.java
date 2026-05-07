package team.model;

public class Canvas {
    private Point[] points;
    private Circle[] circles;

    public void initCanvas() {
        points = new Point[2]; // Assuming 2 points
        circles = new Circle[3]; // Assuming 3 circles

        points[0] = new Point(0,110, 110); // inside circle1 (expected true)
        points[1] = new Point(1, 200, 200); // outside circle1 (expected false)

        // Circle centers (internal objects, but still ID'd)
        Point c1Center = new Point(0, 100, 100);
        Point c2Center = new Point(1, 140, 100);
        Point c3Center = new Point(2, 250, 100);

        // Circles (IDs 1..3)
        circles[0] = new Circle(0, c1Center, 50);
        circles[1] = new Circle(1, c2Center, 30);
        circles[2] = new Circle(2, c3Center, 20);
    }

    public Circle getCircle(int index) {
        return circles[index];
    }
    public Point getPoint(int index) {
        return points[index];
    }   
}
