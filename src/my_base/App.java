package my_base;

import ai.ui.Ui;
import shared.MainRouter;
import shared.routers.LevelDevilRouter;

public class App {

    private static MainRouter mainRouter = new MainRouter();
    private static Ui ui;
    private static AppContent content = new AppContent();

    // Register all routers here.
    private static void registerRouters() {
        mainRouter.addRouter("level-devil", new LevelDevilRouter());
    }

    // Allows all classes in the system to access content entities.
    public static AppContent content() {
        return content;
    }

    public static Ui UI() {
        return ui;
    }

    public static void main(String[] args) throws Exception {
        content.initContent();
        ui = new Ui();
        ui.setUiPorts();
        registerRouters();
        ui.start(mainRouter);
    }
}
