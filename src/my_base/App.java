package my_base;

import ai.ui.Ui;
import base.PeriodicScheduler;
import shared.MainRouter;
import shared.routers.Ex3Router;

public class App {

    private static MainRouter mainRouter = new MainRouter();
    private static Ui ui;
    private static AppContent content = new AppContent();

    // Register all routers here.
    private static void registerRouters() {
        mainRouter.addRouter("ex3", new Ex3Router());
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

        PeriodicScheduler scheduler = new PeriodicScheduler();
        scheduler.setPeriodicLoop(new MyPeriodicLoop());
        scheduler.setPeriodicInterval(30); // Smoother game loop than the original 300 ms.
        scheduler.start();
    }
}
