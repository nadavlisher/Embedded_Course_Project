package ai.ui;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom JPanel that renders the play field AND the menu screens (main menu,
 * level-progression map, high-score table, name entry). It also owns the
 * clickable button geometry: the Ui hit-tests a mouse click here and routes the
 * matching action - the panel itself never calls the backend. It is a pure renderer that stores the latest snapshot pushed by
 * LevelDevilUiPortImpl and decides only how to draw, not what.
 */
public class GamePanel extends JPanel {

    // ---- screen modes ----
    public static final int PLAY = 0, MENU = 1, LEVELS = 2, SCORES = 3, NAME = 4;
    private int screen = MENU;

    private int arenaWidth = 960;
    private int arenaHeight = 576;

    private double playerX = 48, playerY = 444, playerW = 30, playerH = 42;
    private int playerFacing = 1;

    private final Map<Integer, TileView>  tiles  = new LinkedHashMap<>();
    private final Map<Integer, SpikeView> spikes = new LinkedHashMap<>();
    private final Map<Integer, MoverView> movers = new LinkedHashMap<>();
    private final Map<Integer, DoorView>  doors  = new LinkedHashMap<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();

    private long deathFlashUntilMs = 0;
    private DialogManager bannerSource;
    private final Timer animationTimer;

    // ---- menu-screen data ----
    private boolean muted = false;
    private int lsTotal = 6, lsUnlocked = 0;
    private String[][] scoreRows = new String[0][];
    private int scoreHighlight = -1;
    private int nameLevel = 1;
    private boolean nameWon = false;
    private String nameText = "";
    private final List<Button> buttons = new ArrayList<>();

    private static final String[] LEVEL_NAMES = {
        "First Steps", "Trust Issues", "Devil's Final",
        "Stairway to Hell", "Hall of Lies", "Grand Finale"
    };

    public GamePanel() {
        setPreferredSize(new Dimension(arenaWidth, arenaHeight));
        setBackground(new Color(12, 5, 9));
        setFocusable(true);
        animationTimer = new Timer(33, e -> {
            long now = System.currentTimeMillis();
            synchronized (floatingTexts) {
                Iterator<FloatingText> it = floatingTexts.iterator();
                while (it.hasNext()) if (now > it.next().expiresAtMs) it.remove();
            }
            repaint();
        });
        animationTimer.start();
    }

    void attachBannerSource(DialogManager source) { this.bannerSource = source; }

    public void setArena(int width, int height) {
        this.arenaWidth = width; this.arenaHeight = height;
        setPreferredSize(new Dimension(width, height));
        revalidate(); repaint();
    }

    public void setPlayer(double x, double y, double w, double h, int facing) {
        this.playerX = x; this.playerY = y; this.playerW = w; this.playerH = h;
        if (facing != 0) this.playerFacing = facing;
        repaint();
    }

    public void setTile(int id, double x, double y, double w, double h, String kind) { tiles.put(id, new TileView(x, y, w, h, kind)); repaint(); }
    public void removeTile(int id) { tiles.remove(id); repaint(); }
    public void setSpike(int id, double x, double y, double w, double h, String dir) { spikes.put(id, new SpikeView(x, y, w, h, dir)); repaint(); }
    public void removeSpike(int id) { spikes.remove(id); repaint(); }
    public void setMover(int id, double x, double y, double w, double h, String kind) { movers.put(id, new MoverView(x, y, w, h, kind)); repaint(); }
    public void moveMover(int id, double x, double y) { MoverView m = movers.get(id); if (m != null) { m.x = x; m.y = y; repaint(); } }
    public void removeMover(int id) { movers.remove(id); repaint(); }
    public void setDoor(int id, double x, double y, double w, double h, boolean real) { doors.put(id, new DoorView(x, y, w, h, real)); repaint(); }
    public void moveDoor(int id, double x, double y) { DoorView d = doors.get(id); if (d != null) { d.x = x; d.y = y; repaint(); } }
    public void clearLevel() { tiles.clear(); spikes.clear(); movers.clear(); doors.clear(); repaint(); }

    public void spawnFloatingText(String text, double x, double y, String colorKind) {
        synchronized (floatingTexts) { floatingTexts.add(new FloatingText(text, x, y, colorKind, System.currentTimeMillis())); }
        repaint();
    }

    public void flashDeath() { deathFlashUntilMs = System.currentTimeMillis() + 380; repaint(); }

    // ===================== menu screens (driven by the UI port) =====================

    /** True while the score-name entry screen is showing. Lets the input layer
     *  route typed characters to the name and suppress global key shortcuts. */
    public boolean isNameEntry() { return screen == NAME; }

    public void showPlaying() { screen = PLAY; buttons.clear(); repaint(); }

    public void showMainMenuScreen() { screen = MENU; buildMainMenuButtons(); repaint(); }

    public void showLevelSelectScreen(int total, int unlockedUpTo) {
        screen = LEVELS; this.lsTotal = total; this.lsUnlocked = unlockedUpTo; buildLevelButtons(); repaint();
    }

    public void showHighScoresScreen(String[][] rows, int highlightSeq) {
        screen = SCORES; this.scoreRows = rows != null ? rows : new String[0][]; this.scoreHighlight = highlightSeq;
        buttons.clear();
        buttons.add(new Button((arenaWidth - 180) / 2, arenaHeight - 70, 180, 44, "Back", "/leveldevil/menu/show", 0, true));
        repaint();
    }

    public void showNameEntryScreen(int level, boolean won) {
        screen = NAME; this.nameLevel = level; this.nameWon = won; this.nameText = "";
        buttons.clear();
        buttons.add(new Button((arenaWidth - 200) / 2, arenaHeight / 2 + 70, 200, 48, "OK", "/leveldevil/name/submit", 0, true));
        repaint();
    }

    public void updateNameText(String name) { this.nameText = name == null ? "" : name; repaint(); }

    public void setMuted(boolean m) { this.muted = m; if (screen == MENU) buildMainMenuButtons(); repaint(); }

    /** Hit-test a mouse click; returns the enabled button at (px,py) or null. */
    public Button hitTest(int px, int py) {
        for (Button b : buttons) if (b.enabled && b.contains(px, py)) return b;
        return null;
    }

    private void buildMainMenuButtons() {
        buttons.clear();
        int bw = 300, bh = 48, gap = 14, cx = arenaWidth / 2 - bw / 2, y0 = arenaHeight / 2 - 36;
        buttons.add(new Button(cx, y0,              bw, bh, "Start Game",   "/leveldevil/game/start",      0, true));
        buttons.add(new Button(cx, y0 + (bh + gap), bw, bh, "Level Select", "/leveldevil/menu/levels",     0, true));
        buttons.add(new Button(cx, y0 + 2*(bh+gap), bw, bh, "High Scores",  "/leveldevil/menu/scores",     0, true));
        buttons.add(new Button(cx, y0 + 3*(bh+gap), bw, bh, muted ? "Sound: OFF" : "Sound: ON", "/leveldevil/audio/toggleMute", 0, true));
    }

    private void buildLevelButtons() {
        buttons.clear();
        int cols = 3, rows = 2, bw = 244, bh = 100, gx = 26, gy = 22;
        int totalW = cols * bw + (cols - 1) * gx;
        int startX = (arenaWidth - totalW) / 2, startY = 150;
        int maxUnlocked = Math.min(lsTotal, lsUnlocked + 1);
        for (int i = 0; i < lsTotal; i++) {
            int r = i / cols, c = i % cols, level = i + 1;
            int x = startX + c * (bw + gx), y = startY + r * (bh + gy);
            buttons.add(new Button(x, y, bw, bh, "Level " + level, "/leveldevil/level/select", level, level <= maxUnlocked));
        }
        buttons.add(new Button((arenaWidth - 180) / 2, startY + rows * (bh + gy) + 8, 180, 44, "Back", "/leveldevil/menu/show", 0, true));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        paintBackground(g2);
        for (TileView t : tiles.values())   paintTile(g2, t);
        for (MoverView m : movers.values())  paintMover(g2, m);
        for (DoorView d : doors.values())    paintDoor(g2, d);
        for (SpikeView s : spikes.values())  paintSpike(g2, s);
        paintImp(g2, playerX, playerY, playerW, playerH, playerFacing, true);

        long now = System.currentTimeMillis();
        synchronized (floatingTexts) { for (FloatingText ft : floatingTexts) paintFloatingText(g2, ft, now); }

        if (now < deathFlashUntilMs) {
            float a = (deathFlashUntilMs - now) / 380.0f;
            Composite oc = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, a * 0.6f)));
            g2.setColor(new Color(190, 18, 22));
            g2.fillRect(0, 0, arenaWidth, arenaHeight);
            g2.setComposite(oc);
        }

        if (bannerSource != null && bannerSource.isVisible())
            paintBanner(g2, bannerSource.getTitle(), bannerSource.getSubtitle());

        if (screen != PLAY) paintScreen(g2);
    }

    // ===================== menu rendering =====================

    private void paintScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 195));
        g2.fillRect(0, 0, arenaWidth, arenaHeight);
        switch (screen) {
            case MENU:   paintMainMenu(g2);   break;
            case LEVELS: paintLevelSelect(g2);break;
            case SCORES: paintHighScores(g2); break;
            case NAME:   paintNameEntry(g2);  break;
        }
    }

    private void paintMainMenu(Graphics2D g2) {
        paintImp(g2, arenaWidth / 2.0 - 18, 96, 36, 50, 1, true);
        paintTitle(g2, "LEVEL  DEVIL", 168, 46);
        for (Button b : buttons) paintButton(g2, b);
    }

    private void paintLevelSelect(Graphics2D g2) {
        paintTitle(g2, "LEVEL  SELECT", 96, 38);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g2.setColor(new Color(220, 200, 200));
        drawCentered(g2, "Replay any level you have solved - locked levels stay locked", arenaWidth / 2, 128);
        for (Button b : buttons) {
            if (b.param >= 1) paintLevelBox(g2, b);
            else paintButton(g2, b);
        }
    }

    private void paintLevelBox(Graphics2D g2, Button b) {
        int level = b.param;
        boolean unlocked = b.enabled;
        boolean solved = level <= lsUnlocked;
        Color top = unlocked ? new Color(70, 22, 30) : new Color(30, 28, 32);
        Color bot = unlocked ? new Color(40, 12, 18) : new Color(18, 16, 20);
        g2.setPaint(new GradientPaint(b.x, b.y, top, b.x, b.y + b.h, bot));
        g2.fillRoundRect(b.x, b.y, b.w, b.h, 16, 16);
        g2.setColor(unlocked ? new Color(220, 120, 120) : new Color(90, 86, 92));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(b.x, b.y, b.w, b.h, 16, 16);
        g2.setStroke(new BasicStroke(1f));

        g2.setColor(unlocked ? new Color(255, 200, 120) : new Color(120, 116, 122));
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        drawCentered(g2, "Level " + level, b.x + b.w / 2, b.y + 36);
        g2.setColor(unlocked ? new Color(235, 220, 220) : new Color(110, 106, 112));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        drawCentered(g2, LEVEL_NAMES[level - 1], b.x + b.w / 2, b.y + 60);

        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        if (!unlocked) { g2.setColor(new Color(150, 146, 152)); drawCentered(g2, "Locked", b.x + b.w / 2, b.y + 84); }
        else if (solved) { g2.setColor(new Color(120, 220, 130)); drawCentered(g2, "Solved - click to replay", b.x + b.w / 2, b.y + 84); }
        else { g2.setColor(new Color(255, 210, 120)); drawCentered(g2, "Click to play", b.x + b.w / 2, b.y + 84); }
    }

    private void paintHighScores(Graphics2D g2) {
        paintTitle(g2, "HIGH  SCORES", 70, 38);
        int colX[] = { arenaWidth / 2 - 250, arenaWidth / 2 - 150, arenaWidth / 2 + 150 };
        int y = 150;
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.setColor(new Color(255, 200, 120));
        g2.drawString("#", colX[0], y);
        g2.drawString("Name", colX[1], y);
        drawCentered(g2, "Level", colX[2], y);
        g2.setColor(new Color(150, 90, 90));
        g2.drawLine(colX[0] - 10, y + 8, arenaWidth / 2 + 250, y + 8);

        if (scoreRows.length == 0) {
            g2.setColor(new Color(220, 210, 210));
            g2.setFont(new Font("SansSerif", Font.ITALIC, 18));
            drawCentered(g2, "No scores yet - be the first!", arenaWidth / 2, y + 60);
        }
        g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
        int ry = y + 36;
        for (String[] row : scoreRows) {
            boolean hl = scoreHighlight >= 0 && row.length > 3 && row[3].equals(String.valueOf(scoreHighlight));
            if (hl) {
                g2.setColor(new Color(120, 60, 30, 160));
                g2.fillRoundRect(colX[0] - 12, ry - 18, arenaWidth / 2 + 262 - colX[0], 26, 8, 8);
            }
            g2.setColor(hl ? new Color(255, 215, 120) : new Color(235, 225, 225));
            g2.drawString(row[0], colX[0], ry);
            g2.drawString(row[1], colX[1], ry);
            drawCentered(g2, row[2], colX[2], ry);
            ry += 30;
        }
        for (Button b : buttons) paintButton(g2, b);
    }

    private void paintNameEntry(Graphics2D g2) {
        paintTitle(g2, nameWon ? "YOU  ESCAPED!" : "GAME  OVER", arenaHeight / 2 - 150, 44);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 20));
        g2.setColor(new Color(235, 220, 220));
        drawCentered(g2, "You reached level " + nameLevel + " of 6", arenaWidth / 2, arenaHeight / 2 - 92);
        g2.setColor(new Color(255, 200, 120));
        drawCentered(g2, "Enter your name:", arenaWidth / 2, arenaHeight / 2 - 50);

        int bw = 320, bh = 46, bx = (arenaWidth - bw) / 2, by = arenaHeight / 2 - 24;
        g2.setColor(new Color(25, 12, 16));
        g2.fillRoundRect(bx, by, bw, bh, 10, 10);
        g2.setColor(new Color(220, 120, 120));
        g2.drawRoundRect(bx, by, bw, bh, 10, 10);
        boolean caret = (System.currentTimeMillis() / 500) % 2 == 0;
        g2.setFont(new Font("Monospaced", Font.BOLD, 22));
        g2.setColor(Color.WHITE);
        g2.drawString(nameText + (caret ? "_" : ""), bx + 14, by + 31);

        for (Button b : buttons) paintButton(g2, b);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2.setColor(new Color(200, 190, 190));
        drawCentered(g2, "(type your name, then press ENTER or click OK)", arenaWidth / 2, arenaHeight / 2 + 140);
    }

    private void paintButton(Graphics2D g2, Button b) {
        Color top = b.enabled ? new Color(86, 26, 36) : new Color(40, 38, 42);
        Color bot = b.enabled ? new Color(48, 14, 22) : new Color(24, 22, 26);
        g2.setPaint(new GradientPaint(b.x, b.y, top, b.x, b.y + b.h, bot));
        g2.fillRoundRect(b.x, b.y, b.w, b.h, 12, 12);
        g2.setColor(b.enabled ? new Color(230, 120, 120) : new Color(90, 86, 92));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(b.x, b.y, b.w, b.h, 12, 12);
        g2.setStroke(new BasicStroke(1f));
        g2.setFont(new Font("SansSerif", Font.BOLD, 19));
        g2.setColor(b.enabled ? new Color(255, 230, 220) : new Color(120, 116, 122));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(b.label, b.x + (b.w - fm.stringWidth(b.label)) / 2, b.y + (b.h + fm.getAscent() - fm.getDescent()) / 2);
    }

    private void paintTitle(Graphics2D g2, String text, int y, int size) {
        g2.setFont(new Font("SansSerif", Font.BOLD, size));
        FontMetrics fm = g2.getFontMetrics();
        int x = (arenaWidth - fm.stringWidth(text)) / 2;
        g2.setColor(new Color(0, 0, 0, 200));
        g2.drawString(text, x + 2, y + 2);
        g2.setColor(new Color(255, 96, 92));
        g2.drawString(text, x, y);
    }

    private void drawCentered(Graphics2D g2, String text, int cx, int y) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, cx - fm.stringWidth(text) / 2, y);
    }

    // ===================== world rendering (unchanged) =====================

    private void paintBackground(Graphics2D g2) {
        long t = System.currentTimeMillis();
        g2.setPaint(new GradientPaint(0, 0, new Color(34, 12, 18), 0, arenaHeight, new Color(6, 2, 5)));
        g2.fillRect(0, 0, arenaWidth, arenaHeight);
        Composite oc = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
        for (int i = 0; i < 7; i++) {
            int cx = (int) ((i + 0.5) * arenaWidth / 7.0);
            int r = 120 + (int) (30 * Math.sin(t / 500.0 + i));
            RadialGradientPaint rg = new RadialGradientPaint(cx, arenaHeight, r,
                    new float[]{0f, 1f}, new Color[]{new Color(255, 110, 40), new Color(255, 110, 40, 0)});
            g2.setPaint(rg);
            g2.fillRect(cx - r, arenaHeight - r, r * 2, r);
        }
        g2.setComposite(oc);
        for (int i = 0; i < 34; i++) {
            int ex = (int) (((i * 149) + (t / 38)) % arenaWidth);
            int ey = (int) (arenaHeight - ((t / 16 + i * 71) % (arenaHeight + 40)));
            int alpha = 25 + (int) (35 * (0.5 + 0.5 * Math.sin(t / 200.0 + i)));
            int sz = 1 + (i % 3);
            g2.setColor(new Color(255, 140, 50, alpha));
            g2.fillOval(ex, ey, sz, sz);
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        RadialGradientPaint vig = new RadialGradientPaint(arenaWidth / 2f, arenaHeight / 2f,
                Math.max(arenaWidth, arenaHeight) * 0.7f, new float[]{0.55f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 170)});
        g2.setPaint(vig);
        g2.fillRect(0, 0, arenaWidth, arenaHeight);
        g2.setComposite(oc);
        g2.setColor(new Color(90, 35, 45));
        g2.drawRect(0, 0, arenaWidth - 1, arenaHeight - 1);
    }

    private void paintTile(Graphics2D g2, TileView tv) {
        int x = (int) tv.x, y = (int) tv.y, w = (int) tv.w, h = (int) tv.h;
        boolean brittle = "BRITTLE".equals(tv.kind);
        Color top = brittle ? new Color(133, 96, 78) : new Color(92, 96, 112);
        Color bot = brittle ? new Color(74, 48, 38)  : new Color(50, 52, 66);
        g2.setPaint(new GradientPaint(x, y, top, x, y + h, bot));
        g2.fillRect(x, y, w, h);
        g2.setColor(new Color(255, 255, 255, 45));
        g2.fillRect(x + 1, y + 1, w - 2, 3);
        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillRect(x + 1, y + h - 3, w - 2, 2);
        g2.setColor(new Color(0, 0, 0, 40));
        for (int i = 0; i < 4; i++) g2.fillRect(x + 6 + (i * 13) % (w - 10), y + 7 + (i * 11) % (h - 12), 2, 2);
        g2.setColor(new Color(28, 28, 38));
        g2.drawRect(x, y, w - 1, h - 1);
        if (brittle) {
            g2.setColor(new Color(40, 22, 16, 220));
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawLine(x + w / 2, y + 3, x + w / 3, y + h - 4);
            g2.drawLine(x + w / 2, y + 3, x + (2 * w) / 3, y + h - 6);
            g2.drawLine(x + w / 4, y + h / 2, x + (3 * w) / 4, y + h / 2 + 5);
            g2.setStroke(new BasicStroke(1f));
        }
    }

    private void paintSpike(Graphics2D g2, SpikeView s) {
        int x = (int) s.x, y = (int) s.y, w = (int) s.w, h = (int) s.h, teeth = 3;
        for (int i = 0; i < teeth; i++) {
            Polygon p = new Polygon();
            switch (s.dir) {
                case "DOWN":
                    p.addPoint(x + i * w / teeth, y); p.addPoint(x + (i + 1) * w / teeth, y);
                    p.addPoint(x + i * w / teeth + w / (2 * teeth), y + h); break;
                case "LEFT":
                    p.addPoint(x + w, y + i * h / teeth); p.addPoint(x + w, y + (i + 1) * h / teeth);
                    p.addPoint(x, y + i * h / teeth + h / (2 * teeth)); break;
                case "RIGHT":
                    p.addPoint(x, y + i * h / teeth); p.addPoint(x, y + (i + 1) * h / teeth);
                    p.addPoint(x + w, y + i * h / teeth + h / (2 * teeth)); break;
                default:
                    p.addPoint(x + i * w / teeth, y + h); p.addPoint(x + (i + 1) * w / teeth, y + h);
                    p.addPoint(x + i * w / teeth + w / (2 * teeth), y);
            }
            g2.setPaint(new GradientPaint(x, y + h, new Color(120, 130, 145), x, y, new Color(238, 244, 255)));
            g2.fill(p);
            g2.setColor(new Color(90, 100, 115));
            g2.draw(p);
        }
    }

    private void paintMover(Graphics2D g2, MoverView m) {
        int x = (int) m.x, y = (int) m.y, w = (int) m.w, h = (int) m.h;
        if ("FALLING_BLOCK".equals(m.kind)) {
            g2.setPaint(new GradientPaint(x, y, new Color(150, 64, 60), x, y + h, new Color(86, 28, 30)));
            g2.fillRect(x, y, w, h);
            g2.setColor(new Color(35, 12, 12)); g2.drawRect(x, y, w - 1, h - 1);
            g2.setColor(new Color(225, 185, 130));
            g2.fillOval(x + 4, y + 4, 4, 4); g2.fillOval(x + w - 8, y + 4, 4, 4);
            g2.fillOval(x + 4, y + h - 8, 4, 4); g2.fillOval(x + w - 8, y + h - 8, 4, 4);
        } else if ("FALLING_SPIKE".equals(m.kind)) {
            int cx = x + w / 2, cy = y + h / 2, r = Math.min(w, h) / 2 - 3;
            g2.setColor(new Color(225, 235, 245));
            for (int i = 0; i < 8; i++) {
                double a = i * Math.PI / 4;
                int sx = cx + (int) (Math.cos(a) * (r + 6)), sy = cy + (int) (Math.sin(a) * (r + 6));
                int bx1 = cx + (int) (Math.cos(a - 0.25) * r), by1 = cy + (int) (Math.sin(a - 0.25) * r);
                int bx2 = cx + (int) (Math.cos(a + 0.25) * r), by2 = cy + (int) (Math.sin(a + 0.25) * r);
                g2.fillPolygon(new int[]{sx, bx1, bx2}, new int[]{sy, by1, by2}, 3);
            }
            g2.setPaint(new RadialGradientPaint(cx - 3f, cy - 3f, r + 2,
                    new float[]{0f, 1f}, new Color[]{new Color(120, 120, 135), new Color(40, 40, 52)}));
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
            g2.setColor(new Color(20, 20, 28)); g2.drawOval(cx - r, cy - r, r * 2, r * 2);
        } else {
            g2.setPaint(new GradientPaint(x, y, new Color(126, 136, 168), x, y + h, new Color(72, 82, 112)));
            g2.fillRoundRect(x, y, w, h, 8, 8);
            g2.setColor(new Color(38, 44, 64)); g2.drawRoundRect(x, y, w - 1, h - 1, 8, 8);
            g2.setColor(new Color(225, 230, 245));
            int cy = y + h / 2;
            g2.fillPolygon(new int[]{x + 9, x + 17, x + 17}, new int[]{cy, cy - 4, cy + 4}, 3);
            g2.fillPolygon(new int[]{x + w - 9, x + w - 17, x + w - 17}, new int[]{cy, cy - 4, cy + 4}, 3);
            g2.setColor(new Color(255, 255, 255, 50)); g2.fillRect(x + 3, y + 2, w - 6, 2);
        }
    }

    private void paintDoor(Graphics2D g2, DoorView d) {
        int x = (int) d.x, y = (int) d.y, w = (int) d.w, h = (int) d.h;
        long t = System.currentTimeMillis();
        float pulse = (float) (0.25 + 0.12 * Math.sin(t / 320.0));
        Composite oc = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));
        RadialGradientPaint glow = new RadialGradientPaint(x + w / 2f, y + h / 2f, w,
                new float[]{0f, 1f}, new Color[]{new Color(120, 230, 160), new Color(120, 230, 160, 0)});
        g2.setPaint(glow);
        g2.fillRect(x - w, y - h / 2, w * 3, h * 2);
        g2.setComposite(oc);
        g2.setColor(new Color(54, 38, 30));
        g2.fillRoundRect(x - 5, y - 6, w + 10, h + 6, 14, 14);
        g2.setPaint(new GradientPaint(x, y, new Color(122, 84, 52), x, y + h, new Color(80, 52, 30)));
        g2.fillRoundRect(x, y, w, h, 9, 9);
        g2.setColor(new Color(45, 28, 16));
        g2.drawRoundRect(x, y, w - 1, h - 1, 9, 9);
        g2.drawLine(x + w / 2, y + 5, x + w / 2, y + h - 5);
        g2.setColor(new Color(60, 38, 22, 140));
        g2.drawLine(x + 4, y + h / 3, x + w - 4, y + h / 3);
        g2.drawLine(x + 4, y + 2 * h / 3, x + w - 4, y + 2 * h / 3);
        g2.setColor(new Color(150, 245, 180));
        g2.fillOval(x + w / 2 - 4, y + h / 2 - 6, 8, 8);
        g2.fillRect(x + w / 2 - 2, y + h / 2 - 1, 4, 9);
    }

    private void paintImp(Graphics2D g2, double dx, double dy, double dw, double dh, int facing, boolean glow) {
        int x = (int) dx, y = (int) dy, w = (int) dw, h = (int) dh;
        long t = System.currentTimeMillis();
        if (glow) {
            // A subtle flat contact-shadow under the feet (not a halo around the body).
            Composite oc = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.30f));
            g2.setColor(new Color(0, 0, 0));
            g2.fillOval(x - 1, y + h - 4, w + 2, 8);
            g2.setComposite(oc);
        }
        g2.setColor(new Color(150, 30, 40));
        int tailBaseX = facing >= 0 ? x + 3 : x + w - 3, tailDir = facing >= 0 ? -1 : 1;
        Path2D.Double tail = new Path2D.Double();
        tail.moveTo(tailBaseX, y + h - 6);
        tail.quadTo(tailBaseX + tailDir * 12, y + h - 2, tailBaseX + tailDir * 9, y + h - 16);
        g2.setStroke(new BasicStroke(3f));
        g2.draw(tail);
        g2.fillPolygon(new int[]{tailBaseX + tailDir * 9, tailBaseX + tailDir * 14, tailBaseX + tailDir * 6},
                       new int[]{y + h - 20, y + h - 16, y + h - 14}, 3);
        g2.setStroke(new BasicStroke(1f));
        g2.setPaint(new GradientPaint(x, y, new Color(232, 76, 72), x, y + h, new Color(156, 30, 42)));
        g2.fillRoundRect(x, y + 6, w, h - 6, 11, 11);
        g2.setColor(new Color(92, 16, 22));
        g2.drawRoundRect(x, y + 6, w - 1, h - 7, 11, 11);
        g2.setColor(new Color(255, 255, 255, 55));
        g2.fillRoundRect(x + 4, y + 9, w - 8, 3, 4, 4);
        g2.setColor(new Color(247, 233, 214));
        g2.fillPolygon(new int[]{x + 4, x + 10, x + 1}, new int[]{y + 8, y + 8, y - 3}, 3);
        g2.fillPolygon(new int[]{x + w - 4, x + w - 10, x + w - 1}, new int[]{y + 8, y + 8, y - 3}, 3);
        boolean blink = (t % 3200) < 140;
        int eyeY = y + 13, pupil = facing >= 0 ? 2 : -2;
        if (blink) {
            g2.setColor(new Color(60, 10, 12));
            g2.drawLine(x + 5, eyeY + 4, x + 13, eyeY + 4);
            g2.drawLine(x + w - 13, eyeY + 4, x + w - 5, eyeY + 4);
        } else {
            g2.setColor(Color.WHITE);
            g2.fillOval(x + 5, eyeY, 8, 8); g2.fillOval(x + w - 13, eyeY, 8, 8);
            g2.setColor(new Color(20, 8, 8));
            g2.fillOval(x + 7 + pupil, eyeY + 2, 4, 4); g2.fillOval(x + w - 11 + pupil, eyeY + 2, 4, 4);
        }
        g2.setColor(new Color(60, 10, 12));
        g2.drawArc(x + w / 2 - 6, y + h - 16, 12, 7, 200, 140);
    }

    private void paintFloatingText(Graphics2D g2, FloatingText ft, long now) {
        long age = now - ft.startedAtMs;
        if (age >= FloatingText.DURATION_MS) return;
        float k = age / (float) FloatingText.DURATION_MS;
        Composite oc = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, 1f - k)));
        g2.setFont(new Font("SansSerif", Font.BOLD, 20));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(ft.text), x = (int) ft.x - tw / 2, y = (int) (ft.y - k * 40);
        g2.setColor(new Color(0, 0, 0, 200)); g2.drawString(ft.text, x + 2, y + 2);
        g2.setColor(colorFor(ft.colorKind)); g2.drawString(ft.text, x, y);
        g2.setComposite(oc);
    }

    private void paintBanner(Graphics2D g2, String title, String subtitle) {
        long t = System.currentTimeMillis();
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, getWidth(), getHeight());
        Font titleFont = new Font("SansSerif", Font.BOLD, 44), subFont = new Font("SansSerif", Font.PLAIN, 17);
        g2.setFont(titleFont); FontMetrics tm = g2.getFontMetrics();
        int tw = tm.stringWidth(title);
        g2.setFont(subFont); FontMetrics sm = g2.getFontMetrics();
        int sw = sm.stringWidth(subtitle);
        int boxW = Math.max(tw, sw) + 110, boxH = tm.getHeight() + sm.getHeight() + 70;
        int boxX = (getWidth() - boxW) / 2, boxY = (getHeight() - boxH) / 2;
        g2.setPaint(new GradientPaint(boxX, boxY, new Color(78, 22, 32, 245), boxX, boxY + boxH, new Color(26, 8, 14, 245)));
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 20, 20);
        g2.setColor(new Color(230, 120, 120)); g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(boxX, boxY, boxW - 1, boxH - 1, 20, 20); g2.setStroke(new BasicStroke(1f));
        int titleY = boxY + 30 + tm.getAscent();
        g2.setFont(titleFont);
        g2.setColor(new Color(0, 0, 0, 200)); g2.drawString(title, (getWidth() - tw) / 2 + 2, titleY + 2);
        g2.setColor(new Color(255, 96, 92)); g2.drawString(title, (getWidth() - tw) / 2, titleY);
        float a = (float) (0.7 + 0.3 * Math.sin(t / 350.0));
        Composite oc = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.4f, a)));
        g2.setFont(subFont); g2.setColor(new Color(240, 224, 224));
        g2.drawString(subtitle, (getWidth() - sw) / 2, titleY + 22 + sm.getAscent());
        g2.setComposite(oc);
    }

    private static Color colorFor(String kind) {
        if (kind == null) return Color.WHITE;
        switch (kind) {
            case "RED":   return new Color(255, 96, 92);
            case "GREEN": return new Color(120, 220, 120);
            case "GOLD":  return new Color(245, 215, 85);
            default:       return Color.WHITE;
        }
    }

    /** A clickable menu button (geometry + the router action it triggers). */
    public static final class Button {
        public final int x, y, w, h;
        public final String label;
        public final String route;
        public final int param;
        public final boolean enabled;
        Button(int x, int y, int w, int h, String label, String route, int param, boolean enabled) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.label = label; this.route = route; this.param = param; this.enabled = enabled;
        }
        boolean contains(int px, int py) { return px >= x && px <= x + w && py >= y && py <= y + h; }
    }

    private static final class TileView  { final double x, y, w, h; final String kind; TileView(double x,double y,double w,double h,String k){this.x=x;this.y=y;this.w=w;this.h=h;this.kind=k;} }
    private static final class SpikeView { final double x, y, w, h; final String dir; SpikeView(double x,double y,double w,double h,String d){this.x=x;this.y=y;this.w=w;this.h=h;this.dir=d;} }
    private static final class MoverView { double x, y; final double w, h; final String kind; MoverView(double x,double y,double w,double h,String k){this.x=x;this.y=y;this.w=w;this.h=h;this.kind=k;} }
    private static final class DoorView  { double x, y; final double w, h; final boolean real; DoorView(double x,double y,double w,double h,boolean r){this.x=x;this.y=y;this.w=w;this.h=h;this.real=r;} }
    private static final class FloatingText {
        static final long DURATION_MS = 1000;
        final String text; final double x, y; final String colorKind; final long startedAtMs, expiresAtMs;
        FloatingText(String t, double x, double y, String c, long start) { this.text=t; this.x=x; this.y=y; this.colorKind=c; this.startedAtMs=start; this.expiresAtMs=start+DURATION_MS; }
    }
}
