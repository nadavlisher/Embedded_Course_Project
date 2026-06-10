package team.model;

public class Canvas {
    private final GameLevel level;

    public Canvas() {
        this.level = new GameLevel();
    }

    public GameLevel getLevel() {
        return level;
    }

    public void loadLevel(int levelNumber) {
        level.loadLevel(levelNumber);
    }

    public void resetCurrentLevel() {
        level.resetCurrentLevel();
    }

    public void initCanvas() {
        level.reset();
    }
}
