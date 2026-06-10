package shared.routers;

import base.Params;
import base.SubRouter;
import my_base.App;
import team.domain.LevelDevilBackend;

public class LevelDevilRouter implements SubRouter {

    private final LevelDevilBackend backend;

    public LevelDevilRouter() {
		this.backend = App.content().levelDevilBackend();
    }

    @Override
    public Object route(String subPath, Params p) {
        String route = normalizeRoute(subPath);

        switch (route) {
            case "/start":            backend.start();            return null;
            case "/menu/show":        backend.showMainMenu();     return null;
            case "/menu/levels":      backend.openLevelSelect();  return null;
            case "/menu/scores":      backend.openHighScores();   return null;

            case "/game/start":       backend.startNewGame();     return null;
            case "/game/restart":     backend.restart();          return null;

            case "/level/select":     backend.selectLevel(p.getInt(0)); return null;
            case "/level/next":       backend.jumpToNextLevel();  return null;

            case "/player/move":      backend.setMoveDir(p.getInt(0)); return null;
            case "/player/jump":      backend.jump();             return null;
            case "/game/tick":        backend.tick();             return null;

            case "/audio/toggleMute": backend.toggleMute();       return null;

            case "/name/type":        backend.typeNameChar(p.getString(0)); return null;
            case "/name/backspace":   backend.nameBackspace();    return null;
            case "/name/submit":      backend.submitName();       return null;

                
            default:
				System.err.println("Unknown Level Devil route: " + route + " (raw: " + subPath + ")");
				throw new RuntimeException("Unknown Level Devil route: " + route);
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
