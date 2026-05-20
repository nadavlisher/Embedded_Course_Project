package team.model;

public class Canvas {
    private final GameLevel level;

    public Canvas() {
        this.level = new GameLevel();
    }

    public GameLevel getLevel() {
        return level;
    }

    public void initCanvas() {
        level.reset();
    }
}