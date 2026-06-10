package ai.ui;

import javax.swing.*;

import my_base.App;
import shared.MainRouter;
import shared.ui_ports.Ex3UiPort;

import java.awt.*;

public class Ui {
    private static final int GAME_LOOP_MS = 16;

    private MainRouter mainRouter;
    private DrawingPanel drawingPanel;
    private UiPortImpl uiInstance;
    private Timer gameLoopTimer;

    public void setUiPorts() {
        // Panel will be created in createAndShowWindow, so we defer this
    }

    public void start(MainRouter mainRouter) {
        this.mainRouter = mainRouter;
        createAndShowWindow();
        App.content().getBackend().setUiPort(uiInstance);
        startGameLoop();
        
        SwingUtilities.invokeLater(() -> {
            mainRouter.route("/ex3/start", base.Params.of());
        });
    }

    private void startGameLoop() {
        if (gameLoopTimer != null && gameLoopTimer.isRunning()) {
            return;
        }

        gameLoopTimer = new Timer(GAME_LOOP_MS, e -> mainRouter.route("/ex3/tick", base.Params.of()));
        gameLoopTimer.start();
    }

    private void createAndShowWindow() {
        JFrame frame = new JFrame("UI Window");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);  // Fullscreen
        frame.setLayout(new BorderLayout());

        drawingPanel = new DrawingPanel(mainRouter);
        frame.add(drawingPanel, BorderLayout.CENTER);

        uiInstance = new UiPortImpl(drawingPanel);
        Ex3UiPort.setInstance(uiInstance);

        frame.setVisible(true);
        SwingUtilities.invokeLater(() -> drawingPanel.requestFocusInWindow());
    }
}