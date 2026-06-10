package shared.routers;

import base.Params;
import base.SubRouter;
import my_base.App;
import team.control.Backend;

public class Router implements SubRouter {

    private final Backend backend;

    public Router() {
		this.backend = App.content().getBackend();
    }

    @Override
    public Object route(String subPath, Params p) {
        String route = normalizeRoute(subPath);

        switch (route) {
            case "/menu":
                backend.showMainMenu();
                return null;
            case "/menu/start":
                backend.startGame();
                return null;
            case "/menu/level-select":
                backend.showLevelSelect();
                return null;
            case "/menu/exit":
                backend.exitGame();
                return null;
            case "/level/select":
                backend.startLevel(p.getInt(0));
                return null;
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
				System.err.println("Unknown game route: " + route + " (raw: " + subPath + ")");
				throw new RuntimeException("Unknown game route: " + route);
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
