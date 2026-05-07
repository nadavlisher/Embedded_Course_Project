package shared.routers;

import base.Params;
import base.SubRouter;
import my_base.App;
import team.control.Ex3Backend;

public class Ex3Router implements SubRouter {

    private final Ex3Backend backend;

    public Ex3Router() {
		this.backend = App.content().getBackend();
    }

    @Override
    public Object route(String subPath, Params p) {
        String route = normalizeRoute(subPath);

        switch (route) {
            case "/start":
                backend.startGame();
                return null;
            case "/tick":
                backend.tick();
                return null;
            case "/player/left":
                backend.setMoveLeft(p.getBoolean(0));
                return null;
            case "/player/right":
                backend.setMoveRight(p.getBoolean(0));
                return null;
            case "/player/jump":
                backend.jump();
                return null;
            default:
				System.err.println("Unknown ex3 route: " + route + " (raw: " + subPath + ")");
				throw new RuntimeException("Unknown ex3 route: " + route);
        }
    }

    private String normalizeRoute(String subPath) {
        if (subPath == null || subPath.trim().isEmpty()) {
            return "/";
        }

        String route = subPath.trim();
        if (!route.startsWith("/")) {
            route = "/" + route;
        }
        return route;
    }
}