package team.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameLevel {
    private static final int WORLD_WIDTH = 900;
    private static final int WORLD_HEIGHT = 520;
    private static final int FLOOR_Y = 445;
    private static final int DEATH_PLANE_Y = WORLD_HEIGHT + 80;

    private int levelNumber;
    private Player player;
    private Door door;
    private List<Spike> spikes;
    private List<DisappearingFloor> disappearingFloors;

    private boolean moveLeft;
    private boolean moveRight;
    private boolean won;

    private int attemptCount;
    private String backgroundName;

    public GameLevel() {
        loadLevel(1);
    }

    public void loadLevel(int levelNumber) {
        this.levelNumber = clampLevel(levelNumber);
        this.attemptCount = 1;
        loadLevelObjects();
    }

    public void reset() {
        loadLevel(1);
    }

    public void resetCurrentLevel() {
        loadLevelObjects();
    }

    private void loadLevelObjects() {
        this.spikes = new ArrayList<>();
        this.disappearingFloors = new ArrayList<>();
        this.player = new Player(70, FLOOR_Y - 48, 34, 48);
        this.door = new Door(790, FLOOR_Y - 82, 48, 82);
        this.backgroundName = "level" + levelNumber;

        configureLevelObjects();

        this.moveLeft = false;
        this.moveRight = false;
        this.won = false;
    }

    private void configureLevelObjects() {
        switch (levelNumber) {
            case 1:
                spikes.add(new Spike(420, FLOOR_Y - 34, 44, 34, 340));
                break;
            case 2:
                spikes.add(new Spike(360, FLOOR_Y - 34, 44, 34, 260));
                spikes.add(new Spike(610, FLOOR_Y - 34, 44, 34, 520));
                break;
            case 3:
                disappearingFloors.add(new DisappearingFloor(345, FLOOR_Y - 18, 160, 18, 280));
                spikes.add(new Spike(665, FLOOR_Y - 34, 44, 34, 580));
                break;
            case 4:
                disappearingFloors.add(new DisappearingFloor(300, FLOOR_Y - 18, 130, 18, 250));
                spikes.add(new Spike(570, FLOOR_Y - 34, 44, 34, 480));
                break;
            case 5:
                spikes.add(new Spike(325, FLOOR_Y - 34, 44, 34, 235));
                spikes.add(new Spike(520, FLOOR_Y - 34, 44, 34, 445));
                disappearingFloors.add(new DisappearingFloor(655, FLOOR_Y - 18, 120, 18, 600));
                break;
            case 6:
                disappearingFloors.add(new DisappearingFloor(250, FLOOR_Y - 18, 150, 18, 210));
                disappearingFloors.add(new DisappearingFloor(560, FLOOR_Y - 18, 140, 18, 500));
                spikes.add(new Spike(450, FLOOR_Y - 34, 44, 34, 360));
                break;
            default:
                spikes.add(new Spike(420, FLOOR_Y - 34, 44, 34, 340));
                break;
        }
    }

    private void restartAfterDeath() {
        attemptCount++;
        loadLevelObjects();
    }

    public void tick() {
        if (won) {
            return;
        }

        for (DisappearingFloor floor : disappearingFloors) {
            floor.update(player);
        }

        player.tick(moveLeft, moveRight, FLOOR_Y, WORLD_WIDTH, getActiveFloorYForPlayer());

        for (Spike spike : spikes) {
            spike.update(player);
        }

        if (isPlayerDisqualified()) {
            restartAfterDeath();
            return;
        }

        if (player.intersects(door)) {
            won = true;
        }
    }

    private int getActiveFloorYForPlayer() {
        for (DisappearingFloor floor : disappearingFloors) {
            if (!floor.isSolid() && isPlayerOverFloor(floor)) {
                return DEATH_PLANE_Y;
            }
        }

        return FLOOR_Y;
    }

    private boolean isPlayerOverFloor(DisappearingFloor floor) {
        int playerLeft = player.getX();
        int playerRight = player.getX() + player.getWidth();
        return playerRight > floor.getX() && playerLeft < floor.getX() + floor.getWidth();
    }

    private boolean isPlayerDisqualified() {
        if (player.getY() > WORLD_HEIGHT) {
            return true;
        }

        for (Spike spike : spikes) {
            if (spike.isDangerous() && player.intersects(spike)) {
                return true;
            }
        }

        return false;
    }

    public void setMoveLeft(boolean moveLeft) {
        this.moveLeft = moveLeft;
    }

    public void setMoveRight(boolean moveRight) {
        this.moveRight = moveRight;
    }

    public void jump() {
        player.jump();
    }

    public Player getPlayer() {
        return player;
    }

    public Door getDoor() {
        return door;
    }

    public Spike getSpike() {
        return spikes.isEmpty() ? null : spikes.get(0);
    }

    public List<Spike> getSpikes() {
        return Collections.unmodifiableList(spikes);
    }

    public List<DisappearingFloor> getDisappearingFloors() {
        return Collections.unmodifiableList(disappearingFloors);
    }

    public DisappearingFloor getDisappearingFloor() {
        return disappearingFloors.isEmpty() ? null : disappearingFloors.get(0);
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

    public int getWorldWidth() {
        return WORLD_WIDTH;
    }

    public int getWorldHeight() {
        return WORLD_HEIGHT;
    }

    public int getFloorY() {
        return FLOOR_Y;
    }

    public String getBackgroundName() {
        return backgroundName;
    }

    private int clampLevel(int requestedLevel) {
        if (requestedLevel < 1) {
            return 1;
        }

        if (requestedLevel > 6) {
            return 6;
        }

        return requestedLevel;
    }
}
