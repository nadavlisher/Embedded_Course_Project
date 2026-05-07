package ai.ui;

import javax.swing.*;

import base.Params;
import shared.MainRouter;
import shared.ui_ports.Ex3UiPort;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Ui {
    private MainRouter mainRouter;
    private Map<String, Point> points = new HashMap<>();
    private Map<String, Circle> circles = new HashMap<>();
    private DrawingPanel drawingPanel;
    private Ex3UiPortImpl uiInstance;

    public void setUiPorts() {
        // Panel will be created in createAndShowWindow, so we defer this
    }

    public void start(MainRouter mainRouter) {
        this.mainRouter = mainRouter;
        createAndShowWindow();
        mainRouter.route("/ex3/start", Params.of());
    }

    private void createAndShowWindow() {
        JFrame frame = new JFrame("UI Window");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        drawingPanel = new DrawingPanel(points, circles, mainRouter);
        frame.add(drawingPanel, BorderLayout.CENTER);

        // Add control panel with buttons
        JPanel controlPanel = createControlPanel();
        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        // Initialize Ex3UiPortImpl with references to points, circles, and panel
        uiInstance = new Ex3UiPortImpl(points, circles, drawingPanel);
        Ex3UiPort.setInstance((Ex3UiPort) uiInstance);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        JButton periodicButton = new JButton("Run Periodic");
        periodicButton.setFont(new Font("Arial", Font.PLAIN, 24));
        periodicButton.setPreferredSize(new Dimension(300, 80));
        boolean[] isRunning = { false };

        periodicButton.addActionListener(e -> {
            isRunning[0] = !isRunning[0];
            periodicButton.setText(isRunning[0] ? "Stop Periodic" : "Run Periodic");
            mainRouter.route("/ex3/periodic", Params.of());
        });

        panel.add(periodicButton);
        return panel;
    }
}
