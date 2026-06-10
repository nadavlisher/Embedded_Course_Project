package ai.ui;

import base.Params;
import shared.MainRouter;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;

public class DrawingPanel extends JPanel {
    private final MainRouter mainRouter;
    private UiPortImpl gameUiPort;

    public DrawingPanel(MainRouter mainRouter) {
        this.mainRouter = mainRouter;
        setupKeyboard();
    }

    public void setGameUiPort(UiPortImpl gameUiPort) {
        this.gameUiPort = gameUiPort;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        try {
            if (gameUiPort == null) {
                return;
            }

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            scaleToPanel(g2);

            drawBackground(g2);
            drawDoor(g2);
            drawSpike(g2);
            drawPlayer(g2);
            drawHud(g2);
            drawWinText(g2);
        } finally {
            g2.dispose();
        }
    }

    private void scaleToPanel(Graphics2D g2) {
        double scaleX = getWidth() / (double) gameUiPort.getWorldWidth();
        double scaleY = getHeight() / (double) gameUiPort.getWorldHeight();
        g2.scale(scaleX, scaleY);
    }

    private void drawBackground(Graphics2D g2) {
        int w = gameUiPort.getWorldWidth();
        int h = gameUiPort.getWorldHeight();
        int floorY = gameUiPort.getFloorY();

        g2.setColor(new Color(25, 28, 40));
        g2.fillRect(0, 0, w, h);

        g2.setColor(new Color(45, 50, 70));
        g2.fillRect(0, floorY, w, h - floorY);

        g2.setColor(new Color(80, 85, 110));
        g2.drawLine(0, floorY, w, floorY);
    }

    private void drawDoor(Graphics2D g2) {
        Rectangle door = gameUiPort.getDoor();

        if (door == null) {
            return;
        }

        g2.setColor(Color.WHITE);
        g2.fillRect(door.x, door.y, door.width, door.height);

        g2.setColor(Color.BLACK);
        g2.setStroke(new java.awt.BasicStroke(2));
        g2.drawRect(door.x, door.y, door.width, door.height);
    }

    private void drawSpike(Graphics2D g2) {
        SpikeView spike = gameUiPort.getSpike();

        if (spike == null || !spike.visible) {
            return;
        }

        int[] xs = {
            spike.x,
            spike.x + spike.width / 2,
            spike.x + spike.width
        };
        int[] ys = {
            spike.y + spike.height,
            spike.y,
            spike.y + spike.height
        };

        g2.setColor(spike.dangerous ? new Color(230, 60, 60) : new Color(180, 180, 180));
        g2.fillPolygon(xs, ys, 3);

        g2.setColor(Color.WHITE);
        g2.drawPolygon(xs, ys, 3);
    }

    private void drawPlayer(Graphics2D g2) {
        Rectangle player = gameUiPort.getPlayer();

        if (player == null) {
            return;
        }

        g2.setColor(new Color(80, 170, 255));
        g2.fillRoundRect(player.x, player.y, player.width, player.height, 10, 10);

        g2.setColor(Color.WHITE);
        g2.fillOval(player.x + 8, player.y + 10, 6, 6);
        g2.fillOval(player.x + player.width - 14, player.y + 10, 6, 6);
    }

    private void drawHud(Graphics2D g2) {
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(Color.WHITE);
        g2.drawString("Level: " + gameUiPort.getLevelNumber(), 24, 34);
        g2.drawString("Attempts: " + gameUiPort.getAttemptCount(), 24, 58);
    }

    private void drawWinText(Graphics2D g2) {
        if (!gameUiPort.isWon()) {
            return;
        }

        g2.setFont(new Font("Arial", Font.BOLD, 36));
        g2.setColor(Color.WHITE);
        g2.drawString("LEVEL COMPLETE", 280, 170);
    }

    private void setupKeyboard() {
        setFocusable(true);

        bindBooleanKey("pressed LEFT", "/game/player/left", true);
        bindBooleanKey("released LEFT", "/game/player/left", false);
        bindBooleanKey("pressed A", "/game/player/left", true);
        bindBooleanKey("released A", "/game/player/left", false);

        bindBooleanKey("pressed RIGHT", "/game/player/right", true);
        bindBooleanKey("released RIGHT", "/game/player/right", false);
        bindBooleanKey("pressed D", "/game/player/right", true);
        bindBooleanKey("released D", "/game/player/right", false);

        bindSimpleKey("pressed SPACE", "/game/player/jump");
        bindSimpleKey("pressed W", "/game/player/jump");
        bindSimpleKey("pressed UP", "/game/player/jump");
    }

    private void bindBooleanKey(String keyStrokeText, String route, boolean pressed) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyStrokeText), keyStrokeText);

        getActionMap().put(keyStrokeText, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainRouter.route(route, Params.of(pressed));
            }
        });
    }

    private void bindSimpleKey(String keyStrokeText, String route) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyStrokeText), keyStrokeText);

        getActionMap().put(keyStrokeText, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainRouter.route(route, Params.of());
            }
        });
    }
}