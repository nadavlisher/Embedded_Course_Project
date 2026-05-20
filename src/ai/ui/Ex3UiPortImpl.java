package ai.ui;

import shared.ui_ports.Ex3UiPort;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Rectangle;

public class Ex3UiPortImpl extends Ex3UiPort {
    private final JPanel panel;
    private final DrawingPanel drawingPanel;

    private Rectangle player;
    private Rectangle door;
    private SpikeView spike;
    private int worldWidth = 900;
    private int worldHeight = 520;
    private int floorY = 445;
    private String backgroundName = "level1";
    private boolean won = false;
    private int attemptCount = 1;

    public Ex3UiPortImpl(DrawingPanel drawingPanel) {
        this.panel = drawingPanel;
        this.drawingPanel = drawingPanel;
        this.drawingPanel.setGameUiPort(this);
    }

    @Override
    public void setBackground(int worldWidth, int worldHeight, int floorY, String backgroundName) {
        runOnEdt(() -> {
            this.worldWidth = worldWidth;
            this.worldHeight = worldHeight;
            this.floorY = floorY;
            this.backgroundName = backgroundName;
            repaintPanel();
        });
    }

    @Override
    public void updatePlayer(int x, int y, int width, int height, double vx, double vy, boolean onGround) {
        runOnEdt(() -> {
            this.player = new Rectangle(x, y, width, height);
            repaintPanel();
        });
    }

    @Override
    public void updateDoor(int x, int y, int width, int height) {
        runOnEdt(() -> {
            this.door = new Rectangle(x, y, width, height);
            repaintPanel();
        });
    }

    @Override
    public void updateSpike(int x, int y, int width, int height, boolean visible, boolean dangerous) {
        runOnEdt(() -> {
            this.spike = new SpikeView(x, y, width, height, visible, dangerous);
            repaintPanel();
        });
    }

    @Override
    public void setAttemptCount(int attemptCount) {
        runOnEdt(() -> {
            this.attemptCount = attemptCount;
            repaintPanel();
        });
    }

    @Override
    public void setWinState(boolean won) {
        runOnEdt(() -> {
            this.won = won;
            repaintPanel();
        });
    }

    @Override
    public void log(String message) {
        System.out.println(message);
    }

    public Rectangle getPlayer() {
        return player;
    }

    public Rectangle getDoor() {
        return door;
    }

    public SpikeView getSpike() {
        return spike;
    }

    public int getWorldWidth() {
        return worldWidth;
    }

    public int getWorldHeight() {
        return worldHeight;
    }

    public int getFloorY() {
        return floorY;
    }

    public String getBackgroundName() {
        return backgroundName;
    }

    public boolean isWon() {
        return won;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    private void repaintPanel() {
        if (panel != null) {
            panel.repaint();
        }
    }

    private void runOnEdt(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        } else {
            SwingUtilities.invokeLater(action);
        }
    }
}
