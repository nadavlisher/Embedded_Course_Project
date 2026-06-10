package shared.ui_ports;

public abstract class LevelDevilUiPort {

    private static LevelDevilUiPort instance;

    public static void setInstance(LevelDevilUiPort ui) {
        if (ui == null) throw new IllegalArgumentException("LevelDevilUiPort instance cannot be null");
        if (instance != null) throw new IllegalStateException("LevelDevilUiPort instance already set");
        instance = ui;
    }

    public static LevelDevilUiPort getInstance() {
        if (instance == null) throw new IllegalStateException("LevelDevilUiPort instance not set yet");
        return instance;
    }

    // ===== World / static geometry =====
    public abstract void drawArena(int width, int height);
    public abstract void setPlayer(double x, double y, double width, double height, int facing);
    public abstract void drawTile(int id, double x, double y, double width, double height, String kind);
    public abstract void removeTile(int id);
    public abstract void drawSpike(int id, double x, double y, double width, double height, String dir);
    public abstract void removeSpike(int id);
    public abstract void drawMover(int id, double x, double y, double width, double height, String kind);
    public abstract void updateMover(int id, double x, double y);
    public abstract void removeMover(int id);
    public abstract void drawDoor(int id, double x, double y, double width, double height, boolean real);
    public abstract void updateDoor(int id, double x, double y);
    public abstract void clearLevel();
    public abstract void showFloatingText(String text, double x, double y, String colorKind);

    // ===== HUD =====
    public abstract void updateLives(int count);
    public abstract void updateDeaths(int count);
    public abstract void updateLevel(int levelNumber);
    public abstract void updateStatusText(String text);
    public abstract void setMuted(boolean muted);

    // ===== Screens (menu / map / scores / name entry) and in-play banners =====
    public abstract void showPlaying();
    public abstract void showMainMenu();
    public abstract void showLevelSelect(int totalLevels, int unlockedUpTo);
    public abstract void showHighScores(String[][] rows, int highlightSeq);
    public abstract void showNameEntry(int levelReached, boolean won);
    public abstract void updateNameEntry(String name);

    public abstract void showLevelStart(int levelNumber, String name);
    public abstract void showLevelComplete(int levelNumber);
    public abstract void showGameOver(int deaths);
    public abstract void showVictory(int deaths);
    public abstract void clearBanner();

    // ===== Effects / audio / debug =====
    public abstract void flashDeath();
    public abstract void playSound(String soundName);
    public abstract void log(String message);

}
