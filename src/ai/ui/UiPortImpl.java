package ai.ui;

import shared.ui_ports.UiPort;
import team.control.GameScreen;
import team.model.LevelState;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Rectangle;

public class UiPortImpl extends UiPort {
    private final JPanel panel;
    private final DrawingPanel drawingPanel;

    private Rectangle player;
    private Rectangle door;
    private SpikeView[] spikes = new SpikeView[0];
    private DisappearingFloorView[] disappearingFloors = new DisappearingFloorView[0];
    private GameScreen screen = GameScreen.MAIN_MENU;
    private LevelState[] levelStates = new LevelState[0];
    private int worldWidth = 900;
    private int worldHeight = 520;
    private int floorY = 445;
    private String backgroundName = "level1";
    private boolean won = false;
    private int attemptCount = 1;
    private int levelNumber = 1;

    public UiPortImpl(DrawingPanel drawingPanel) {
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
        updateSpikes(
            new int[] { x },
            new int[] { y },
            new int[] { width },
            new int[] { height },
            new boolean[] { visible },
            new boolean[] { dangerous }
        );
    }

    @Override
    public void updateSpikes(int[] x, int[] y, int[] width, int[] height, boolean[] visible, boolean[] dangerous) {
        runOnEdt(() -> {
            SpikeView[] nextSpikes = new SpikeView[x.length];
            for (int i = 0; i < x.length; i++) {
                nextSpikes[i] = new SpikeView(x[i], y[i], width[i], height[i], visible[i], dangerous[i]);
            }
            this.spikes = nextSpikes;
            repaintPanel();
        });
    }

    @Override
    public void updateDisappearingFloors(int[] x, int[] y, int[] width, int[] height, String[] states) {
        runOnEdt(() -> {
            DisappearingFloorView[] nextFloors = new DisappearingFloorView[x.length];
            for (int i = 0; i < x.length; i++) {
                nextFloors[i] = new DisappearingFloorView(x[i], y[i], width[i], height[i], states[i]);
            }
            this.disappearingFloors = nextFloors;
            repaintPanel();
        });
    }

    @Override
    public void setScreen(GameScreen screen) {
        runOnEdt(() -> {
            this.screen = screen;
            repaintPanel();
        });
    }

    @Override
    public void setLevelStates(LevelState[] levelStates) {
        runOnEdt(() -> {
            this.levelStates = levelStates.clone();
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
    public void setLevelNumber(int levelNumber) {
        runOnEdt(() -> {
            this.levelNumber = levelNumber;
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
        return spikes.length == 0 ? null : spikes[0];
    }

    public SpikeView[] getSpikes() {
        return spikes.clone();
    }

    public DisappearingFloorView[] getDisappearingFloors() {
        return disappearingFloors.clone();
    }

    public GameScreen getScreen() {
        return screen;
    }

    public LevelState[] getLevelStates() {
        return levelStates.clone();
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

    public int getLevelNumber() {
        return levelNumber;
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
