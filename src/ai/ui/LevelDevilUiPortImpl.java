package ai.ui;

import shared.ui_ports.LevelDevilUiPort;

/**
 * Concrete UI port for Level Devil. Forwards every backend command to the right
 * Swing widget (GamePanel / HudPanel / DialogManager / SoundPlayer). It is the
 * only class that knows both the abstract port and the concrete Swing widgets.
 */
public class LevelDevilUiPortImpl extends LevelDevilUiPort {

    private final GamePanel gamePanel;
    private final HudPanel hudPanel;
    private final DialogManager dialogManager;
    private final SoundPlayer soundPlayer;

    public LevelDevilUiPortImpl(GamePanel gamePanel, HudPanel hudPanel,
                                DialogManager dialogManager, SoundPlayer soundPlayer) {
        this.gamePanel = gamePanel;
        this.hudPanel = hudPanel;
        this.dialogManager = dialogManager;
        this.soundPlayer = soundPlayer;
    }

    @Override public void drawArena(int width, int height) { gamePanel.setArena(width, height); }
    @Override public void setPlayer(double x, double y, double w, double h, int facing) { gamePanel.setPlayer(x, y, w, h, facing); }

    @Override public void drawTile(int id, double x, double y, double w, double h, String kind) { gamePanel.setTile(id, x, y, w, h, kind); }
    @Override public void removeTile(int id) { gamePanel.removeTile(id); }

    @Override public void drawSpike(int id, double x, double y, double w, double h, String dir) { gamePanel.setSpike(id, x, y, w, h, dir); }
    @Override public void removeSpike(int id) { gamePanel.removeSpike(id); }

    @Override public void drawMover(int id, double x, double y, double w, double h, String kind) { gamePanel.setMover(id, x, y, w, h, kind); }
    @Override public void updateMover(int id, double x, double y) { gamePanel.moveMover(id, x, y); }
    @Override public void removeMover(int id) { gamePanel.removeMover(id); }

    @Override public void drawDoor(int id, double x, double y, double w, double h, boolean real) { gamePanel.setDoor(id, x, y, w, h, real); }
    @Override public void updateDoor(int id, double x, double y) { gamePanel.moveDoor(id, x, y); }

    @Override public void clearLevel() { gamePanel.clearLevel(); }
    @Override public void showFloatingText(String text, double x, double y, String colorKind) { gamePanel.spawnFloatingText(text, x, y, colorKind); }

    @Override public void updateLives(int count)     { hudPanel.setLives(count); }
    @Override public void updateDeaths(int count)    { hudPanel.setDeaths(count); }
    @Override public void updateLevel(int n)         { hudPanel.setLevel(n); }
    @Override public void updateStatusText(String t) { hudPanel.setStatusText(t); }
    @Override public void setMuted(boolean muted)    { soundPlayer.setEnabled(!muted); hudPanel.setMuted(muted); gamePanel.setMuted(muted); }

    @Override public void showPlaying()                          { gamePanel.showPlaying(); }
    @Override public void showMainMenu()                         { gamePanel.showMainMenuScreen(); }
    @Override public void showLevelSelect(int total, int unlock) { gamePanel.showLevelSelectScreen(total, unlock); }
    @Override public void showHighScores(String[][] rows, int hl){ gamePanel.showHighScoresScreen(rows, hl); }
    @Override public void showNameEntry(int level, boolean won)  { gamePanel.showNameEntryScreen(level, won); }
    @Override public void updateNameEntry(String name)           { gamePanel.updateNameText(name); }

    @Override public void showLevelStart(int n, String s) { dialogManager.showLevelStart(n, s); }
    @Override public void showLevelComplete(int n)        { dialogManager.showLevelComplete(n); }
    @Override public void showGameOver(int deaths)        { dialogManager.showGameOver(deaths); }
    @Override public void showVictory(int deaths)         { dialogManager.showVictory(deaths); }
    @Override public void clearBanner()                   { dialogManager.clearBanner(); }

    @Override public void flashDeath() { gamePanel.flashDeath(); }
    @Override public void playSound(String soundName) { soundPlayer.play(soundName); }
    @Override public void log(String message) { System.out.println("[UI] " + message); }
}
