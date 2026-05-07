package shared.routers;

import base.Params;
import base.SubRouter;
import my_base.App;
import team.control.Ex3Backend;

public class Ex3Router implements SubRouter {

    private final Ex3Backend backend;

    public Ex3Router() {
        this.backend = App.content().ex3Backend();
    }

    @Override
    public Object route(String subPath, Params p) {
        // Uncomment next line to see routing commands in console
        // System.out.println("Routing Ex3: " + subPath + " with params " + p);
        switch (subPath) {

            // UI calls once on startup
            case "/start":
                backend.startScenario();
                return null;

            // UI input: drag point
            case "/point/move": {
                int id = p.getInt(0);
                double x = p.getDouble(1);
                double y = p.getDouble(2);
                backend.movePoint(id, x, y);
                return null;
            }

            // UI input: drag circle
            case "/circle/move": {
                int id = p.getInt(0);
                double cx = p.getDouble(1);
                double cy = p.getDouble(2);
                backend.moveCircle(id, cx, cy);
                return null;
            }

            // UI input: resize circle
            case "/circle/radius": {
                int id = p.getInt(0);
                double r = p.getDouble(1);
                backend.setCircleRadius(id, r);
                return null;
            }

            // UI input: resize circle
            case "/periodic": {
                backend.toggleRunPeriodic();
                return null;
            }

            default:
                throw new RuntimeException("Unknown ex3 route: " + subPath);
        }
    }
}