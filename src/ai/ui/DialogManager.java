package ai.ui;

import javax.swing.Timer;

/**
 * Tracks the current "banner" the GamePanel paints over the play field
 * (main menu, level start/complete, game over, victory). It stores banner text
 * and triggers repaints; it does no drawing itself. Transient banners
 * (level start/complete) auto-hide after a short delay.
 */
public class DialogManager {

    private final GamePanel gamePanel;
    private boolean visible = false;
    private String title = "";
    private String subtitle = "";
    private Timer autoHideTimer;

    public DialogManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.gamePanel.attachBannerSource(this);
    }

    public void install() { /* GamePanel already holds a reference to us. */ }

    public boolean isVisible()   { return visible; }
    public String  getTitle()    { return title; }
    public String  getSubtitle() { return subtitle; }

    public void showMainMenu() {
        showBanner("LEVEL  DEVIL",
                "SPACE start    •    ← → / A D move    •    SPACE jump    •    R restart    •    M menu", -1);
    }
    public void showLevelStart(int n, String name) {
        showBanner("Level " + n + "  –  " + name, "The floor is lava. And lying.", 1500);
    }
    public void showLevelComplete(int n) {
        showBanner("Level " + n + " cleared!", "Do not get comfortable...", 1300);
    }
    public void showGameOver(int deaths) {
        showBanner("GAME  OVER", "The devil wins. Deaths: " + deaths + "    •    R = try again    •    M = menu", -1);
    }
    public void showVictory(int deaths) {
        showBanner("YOU  ESCAPED!", "You beat the devil with " + deaths + " death(s)    •    R = again    •    M = menu", -1);
    }

    public void clearBanner() {
        if (autoHideTimer != null && autoHideTimer.isRunning()) autoHideTimer.stop();
        visible = false; title = ""; subtitle = "";
        gamePanel.repaint();
    }

    private void showBanner(String title, String subtitle, int autoHideMs) {
        if (autoHideTimer != null && autoHideTimer.isRunning()) autoHideTimer.stop();
        this.title = title; this.subtitle = subtitle; this.visible = true;
        gamePanel.repaint();
        if (autoHideMs > 0) {
            autoHideTimer = new Timer(autoHideMs, e -> clearBanner());
            autoHideTimer.setRepeats(false);
            autoHideTimer.start();
        }
    }
}
