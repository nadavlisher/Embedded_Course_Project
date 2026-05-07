package ai.ui;

import javax.swing.*;

import my_base.App;
import shared.MainRouter;
import shared.ui_ports.Ex3UiPort;

import java.awt.*;

public class Ui {
    private MainRouter mainRouter;
    private DrawingPanel drawingPanel;
    private Ex3UiPortImpl uiInstance;

    public void setUiPorts() {
        // Panel will be created in createAndShowWindow, so we defer this
    }

    public void start(MainRouter mainRouter) {
        this.mainRouter = mainRouter;
        createAndShowWindow();
        App.content().getBackend().setUiPort(uiInstance);
        
        SwingUtilities.invokeLater(() -> {
            mainRouter.route("/ex3/start", base.Params.of());
        });
    }

    private void createAndShowWindow() {
        JFrame frame = new JFrame("UI Window");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);  // Fullscreen
        frame.setLayout(new BorderLayout());

        drawingPanel = new DrawingPanel(mainRouter);
        frame.add(drawingPanel, BorderLayout.CENTER);

        uiInstance = new Ex3UiPortImpl(drawingPanel);
        Ex3UiPort.setInstance(uiInstance);

        frame.setVisible(true);
        drawingPanel.requestFocusInWindow();
    }
}
