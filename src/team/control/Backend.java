package team.control;

import shared.ui_ports.UiPort;
import team.model.Canvas;
import team.model.DisappearingFloor;
import team.model.Door;
import team.model.GameLevel;
import team.model.LevelProgress;
import team.model.Player;
import team.model.Spike;

import java.util.List;

public class Backend {
    private final Canvas canvas;
    private final LevelProgress progress;
    private UiPort ui;

    private GameScreen currentScreen;
    private boolean winMessageAlreadySent;

    public Backend() {
        this(new Canvas());
    }

    public Backend(Canvas canvas) {
        this.canvas = canvas;
        this.progress = new LevelProgress();
        this.currentScreen = GameScreen.MAIN_MENU;
        this.winMessageAlreadySent = false;
    }

    public void setUiPort(UiPort ui) {
        this.ui = ui;
        render();
    }

    public void showMainMenu() {
        currentScreen = GameScreen.MAIN_MENU;
        render();
    }

    public void showLevelSelect() {
        currentScreen = GameScreen.LEVEL_SELECT;
        render();
    }

    public void startGame() {
        startLevel(1);
    }

    public void startLevel(int levelNumber) {
        if (!progress.isUnlocked(levelNumber)) {
            showLevelSelect();
            return;
        }

        canvas.loadLevel(levelNumber);
        currentScreen = GameScreen.PLAYING;
        winMessageAlreadySent = false;

        render();
        log("Level " + levelNumber + " started.");
    }

    public void exitGame() {
        System.exit(0);
    }

    public GameScreen getCurrentScreen() {
        return currentScreen;
    }

    public void tick() {
        if (currentScreen != GameScreen.PLAYING) {
            render();
            return;
        }

        GameLevel level = canvas.getLevel();
        level.tick();

        if (level.isWon()) {
            completeCurrentLevel(level);
        }

        render();
    }

    private void completeCurrentLevel(GameLevel level) {
        if (winMessageAlreadySent) {
            return;
        }

        winMessageAlreadySent = true;
        progress.markCompleted(level.getLevelNumber());
        log("Level " + level.getLevelNumber() + " complete.");

        if (level.getLevelNumber() == progress.getTotalLevels()) {
            currentScreen = GameScreen.VICTORY;
        } else {
            currentScreen = GameScreen.LEVEL_SELECT;
        }
    }

    public void setMoveLeft(boolean pressed) {
        if (currentScreen == GameScreen.PLAYING) {
            canvas.getLevel().setMoveLeft(pressed);
        }
    }

    public void setMoveRight(boolean pressed) {
        if (currentScreen == GameScreen.PLAYING) {
            canvas.getLevel().setMoveRight(pressed);
        }
    }

    public void jump() {
        if (currentScreen == GameScreen.PLAYING) {
            canvas.getLevel().jump();
        }
    }

    private void render() {
        if (ui == null) {
            return;
        }

        GameLevel level = canvas.getLevel();
        Player player = level.getPlayer();
        Door door = level.getDoor();

        ui.setScreen(currentScreen);
        ui.setLevelStates(progress.getLevelStates());
        ui.setBackground(level.getWorldWidth(), level.getWorldHeight(), level.getFloorY(), level.getBackgroundName());

        ui.updatePlayer(
            player.getX(),
            player.getY(),
            player.getWidth(),
            player.getHeight(),
            player.getVx(),
            player.getVy(),
            player.isOnGround()
        );

        ui.updateDoor(
            door.getX(),
            door.getY(),
            door.getWidth(),
            door.getHeight()
        );

        renderSpikes(level.getSpikes());
        renderDisappearingFloors(level.getDisappearingFloors());

        ui.setAttemptCount(level.getAttemptCount());
        ui.setLevelNumber(level.getLevelNumber());
        ui.setWinState(level.isWon());
    }

    private void renderSpikes(List<Spike> spikes) {
        int count = spikes.size();
        int[] x = new int[count];
        int[] y = new int[count];
        int[] width = new int[count];
        int[] height = new int[count];
        boolean[] visible = new boolean[count];
        boolean[] dangerous = new boolean[count];

        for (int i = 0; i < count; i++) {
            Spike spike = spikes.get(i);
            x[i] = spike.getX();
            y[i] = spike.getY();
            width[i] = spike.getWidth();
            height[i] = spike.getHeight();
            visible[i] = spike.isVisible();
            dangerous[i] = spike.isDangerous();
        }

        ui.updateSpikes(x, y, width, height, visible, dangerous);
    }

    private void renderDisappearingFloors(List<DisappearingFloor> floors) {
        int count = floors.size();
        int[] x = new int[count];
        int[] y = new int[count];
        int[] width = new int[count];
        int[] height = new int[count];
        String[] states = new String[count];

        for (int i = 0; i < count; i++) {
            DisappearingFloor floor = floors.get(i);
            x[i] = floor.getX();
            y[i] = floor.getY();
            width[i] = floor.getWidth();
            height[i] = floor.getHeight();
            states[i] = floor.getState().name();
        }

        ui.updateDisappearingFloors(x, y, width, height, states);
    }

    private void log(String message) {
        if (ui != null) {
            ui.log(message);
            return;
        }

        System.out.println(message);
    }
}
