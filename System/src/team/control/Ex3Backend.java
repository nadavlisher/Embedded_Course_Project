package team.control;

import shared.ui_ports.Ex3UiPort;
import team.model.Canvas;
import team.model.Door;
import team.model.GameLevel;
import team.model.Player;

public class Ex3Backend {
    private final Canvas canvas;
    private Ex3UiPort ui;

    private boolean started;
    private boolean winMessageAlreadySent;

    public Ex3Backend() {
        this(new Canvas());
    }

    public Ex3Backend(Canvas canvas) {
        this.canvas = canvas;
        this.started = false;
        this.winMessageAlreadySent = false;
    }

    public void setUiPort(Ex3UiPort ui) {
        this.ui = ui;
    }

    public void startGame() {
        canvas.getLevel().reset();
        started = true;
        winMessageAlreadySent = false;

        render();
        log("Game started. Use A/D or Left/Right to move, Space/W/Up to jump.");
    }

    public void tick() {
        if (!started) {
            return;
        }

        GameLevel level = canvas.getLevel();
        level.tick();

        render();

        if (level.isWon() && !winMessageAlreadySent) {
            winMessageAlreadySent = true;
            log("Level complete. Player reached the door.");
        }
    }

    public void setMoveLeft(boolean pressed) {
        canvas.getLevel().setMoveLeft(pressed);
    }

    public void setMoveRight(boolean pressed) {
        canvas.getLevel().setMoveRight(pressed);
    }

    public void jump() {
        canvas.getLevel().jump();
    }

    private void render() {
        if (ui == null) {
            return;
        }

        GameLevel level = canvas.getLevel();
        Player player = level.getPlayer();
        Door door = level.getDoor();

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

        ui.setWinState(level.isWon());
    }

    private void log(String message) {
        if (ui != null) {
            ui.log(message);
            return;
        }

        System.out.println(message);
    }

    // Legacy methods retained for compile compatibility with the old circle/point demo.
    public void startScenario() {
        startGame();
    }

    public void movePoint(int pointId, double x, double y) {
    }

    public void moveCircle(int circleId, double cx, double cy) {
    }

    public void setCircleRadius(int circleId, double r) {
    }

    public void moveCircle2(double dx, double dy) {
    }

    public void toggleRunPeriodic() {
    }
}