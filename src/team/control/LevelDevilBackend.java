package team.control;

import shared.ui_ports.LevelDevilUiPort;
import team.model.Canvas;
import team.model.Door;
import team.model.GameLevel;
import team.model.Player;
import team.model.Spike;

public class LevelDevilBackend {
    private final Canvas canvas;
    private LevelDevilUiPort ui;

    private boolean started;
    private boolean winMessageAlreadySent;

    public LevelDevilBackend() {
        this(new Canvas());
    }

    public LevelDevilBackend(Canvas canvas) {
        this.canvas = canvas;
        this.started = false;
        this.winMessageAlreadySent = false;
    }

    public void setUiPort(LevelDevilUiPort ui) {
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
        Spike spike = level.getSpike();

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

        ui.updateSpike(
            spike.getX(),
            spike.getY(),
            spike.getWidth(),
            spike.getHeight(),
            spike.isVisible(),
            spike.isDangerous()
        );

        ui.setAttemptCount(level.getAttemptCount());
        ui.setWinState(level.isWon());
    }

    private void log(String message) {
        if (ui != null) {
            ui.log(message);
            return;
        }

        System.out.println(message);
    }
}
