package team.control;

import my_base.App;
import shared.ui_ports.Ex3UiPort;
import team.model.Canvas;
import team.model.Circle;
import team.model.Point;

public class Ex3Backend {

    /**
     * Use ex3UiPort() as a function and not a variableto get the UI port
     * to avoid trying to get it before it was set up by the UI
     * (which happens at UI startup, but this backend is constructed at app
     * startup).
     */
    private Ex3UiPort ex3UiPort() {
        return Ex3UiPort.getInstance();
    }

    private boolean runPeriodic = false;

    // Called once at UI startup
    public void startScenario() {
        Canvas canvas = App.content().canvas();

        // Tell UI to render objects (IDs only)
        for (int i = 0; i < 3; i++) {
            Circle c = canvas.getCircle(i);
            ex3UiPort().addCircle(c.getId(), c.getCenter().getX(), c.getCenter().getY(), c.getR());
        }

        for (int i = 0; i < 2; i++) {
            Point p = canvas.getPoint(i);
            ex3UiPort().addPoint(p.getId(), p.getX(), p.getY());
        }

        ex3UiPort().log("Scenario started: 3 circles + 2 points.");
        evaluateAndCommandUi();
    }

    // UI input events call these via router
    public void movePoint(int pointId, double x, double y) {
        Point p = App.content().canvas().getPoint(pointId);
        p.setX(x);
        p.setY(y);
        ex3UiPort().updatePoint(pointId, x, y);
        evaluateAndCommandUi();
    }

    public void moveCircle(int circleId, double cx, double cy) {
        Circle c = App.content().canvas().getCircle(circleId);
        c.getCenter().setX(cx);
        c.getCenter().setY(cy);
        ex3UiPort().updateCircle(circleId, cx, cy, c.getR());
        evaluateAndCommandUi();
    }

    public void setCircleRadius(int circleId, double r) {
        Circle c = App.content().canvas().getCircle(circleId);
        c.setR(r);
        ex3UiPort().updateCircle(circleId, c.getCenter().getX(), c.getCenter().getY(), c.getR());
        evaluateAndCommandUi();
    }

    // Business logic: checks + UI commands
    private void evaluateAndCommandUi() {
        Circle c0 = App.content().canvas().getCircle(0);
        Circle c1 = App.content().canvas().getCircle(1);
        Circle c2 = App.content().canvas().getCircle(2);

        Point p0 = App.content().canvas().getPoint(0);
        Point p1 = App.content().canvas().getPoint(1);

        boolean c0InC1 = c0.intersects(c1); // expected true initially
        boolean c0InC2 = c0.intersects(c2); // expected false initially

        boolean p0Inside = c0.contains(p0); // expected true initially
        boolean p1Inside = c0.contains(p1); // expected false initially

        // Student → UI commands (IDs only)
        ex3UiPort().paintPoint(0, p0Inside ? "red" : "black");
        ex3UiPort().paintPoint(1, p1Inside ? "red" : "black");

        if (c0InC1)
            ex3UiPort().blinkCircle(1, 2);
        if (c0InC2)
            ex3UiPort().blinkCircle(2, 2);
        // ex3UiPort().log("Checks: c1∩c2=" + c1InC2 + " c1∩c3=" + c1InC3 + " p1∈c1=" +
        // p1Inside + " p2∈c1=" + p2Inside);
    }

    public void moveCircle2(double dx, double dy) {
        if (!runPeriodic)
            return;
        double cx = App.content().canvas().getCircle(2).getCenter().getX() + dx;
        double cy = App.content().canvas().getCircle(2).getCenter().getY() + dy;
        moveCircle(2, cx, cy);
    }

    public void toggleRunPeriodic() {
         this.runPeriodic = !this.runPeriodic;
    }
}