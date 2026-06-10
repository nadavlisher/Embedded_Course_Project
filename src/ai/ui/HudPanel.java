package ai.ui;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

/**
 * Heads-Up Display: lives (as little hearts), level, deaths, a sound indicator
 * and a status line. Shows only the values the abstract UI port pushes to it
 * It never computes them.
 */
public class HudPanel extends JPanel {

    private int lives = 9;
    private int level = 1;
    private int deaths = 0;
    private boolean muted = false;
    private String status = "Press SPACE to start";

    public HudPanel() {
        setPreferredSize(new Dimension(960, 42));
        setBackground(new Color(22, 9, 13));
    }

    public void setLives(int n)        { this.lives = n; repaint(); }
    public void setLevel(int n)        { this.level = n; repaint(); }
    public void setDeaths(int n)       { this.deaths = n; repaint(); }
    public void setMuted(boolean m)    { this.muted = m; repaint(); }
    public void setStatusText(String t){ this.status = (t == null ? "" : t); repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int cy = getHeight() / 2, x = 14;
        int shown = Math.min(lives, 9);
        for (int i = 0; i < shown; i++) { drawHeart(g2, x + 7, cy, 7); x += 18; }
        if (lives > 9) {
            g2.setColor(new Color(255, 120, 120));
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString("x" + lives, x, cy + 5); x += 34;
        }
        x += 12;

        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.setColor(new Color(255, 196, 110));
        g2.drawString("LEVEL " + level + " / 6", x, cy + 5); x += 110;

        drawSkull(g2, x + 7, cy, 8); x += 20;
        g2.setColor(new Color(205, 205, 220));
        g2.drawString("" + deaths, x, cy + 5); x += 38;

        g2.setColor(new Color(150, 150, 165));
        g2.drawLine(x, 8, x, getHeight() - 8); x += 14;

        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g2.setColor(new Color(235, 230, 235));
        g2.drawString(status, x, cy + 5);

        // sound indicator at the far right
        g2.setColor(muted ? new Color(150, 90, 90) : new Color(120, 210, 130));
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        String snd = muted ? "SOUND OFF" : "SOUND ON";
        g2.drawString(snd, getWidth() - 96, cy + 5);
    }

    private void drawHeart(Graphics2D g2, int cx, int cy, int s) {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(cx, cy + s * 0.85);
        p.curveTo(cx + s * 1.4, cy - s * 0.1, cx + s * 0.55, cy - s * 1.2, cx, cy - s * 0.35);
        p.curveTo(cx - s * 0.55, cy - s * 1.2, cx - s * 1.4, cy - s * 0.1, cx, cy + s * 0.85);
        p.closePath();
        g2.setColor(new Color(225, 60, 70)); g2.fill(p);
        g2.setColor(new Color(255, 150, 150)); g2.setStroke(new BasicStroke(1f)); g2.draw(p);
    }

    private void drawSkull(Graphics2D g2, int cx, int cy, int s) {
        g2.setColor(new Color(220, 220, 230));
        g2.fillRoundRect(cx - s, cy - s, s * 2, (int) (s * 1.7), s, s);
        g2.fillRect(cx - s / 2, cy + s / 2, s, s / 2);
        g2.setColor(new Color(30, 20, 24));
        g2.fillOval(cx - s + 2, cy - s / 2, s - 1, s - 1);
        g2.fillOval(cx + 1, cy - s / 2, s - 1, s - 1);
    }
}
