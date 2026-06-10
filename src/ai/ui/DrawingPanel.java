package ai.ui;

import base.Params;
import shared.MainRouter;
import team.control.GameScreen;
import team.model.LevelState;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DrawingPanel extends JPanel {
    private static final Color BACKGROUND = new Color(25, 28, 40);
    private static final Color FLOOR = new Color(45, 50, 70);
    private static final Color ACCENT = new Color(80, 170, 255);

    private final MainRouter mainRouter;
    private UiPortImpl gameUiPort;

    public DrawingPanel(MainRouter mainRouter) {
        this.mainRouter = mainRouter;
        setupKeyboard();
        setupMouse();
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

            GameScreen screen = gameUiPort.getScreen();
            if (screen == GameScreen.MAIN_MENU) {
                drawMainMenu(g2);
            } else if (screen == GameScreen.LEVEL_SELECT) {
                drawLevelSelect(g2);
            } else if (screen == GameScreen.VICTORY) {
                drawVictory(g2);
            } else {
                drawGameplay(g2);
            }
        } finally {
            g2.dispose();
        }
    }

    private void drawMainMenu(Graphics2D g2) {
        fillPanelBackground(g2);
        drawCenteredText(g2, "Level Devil Course", 0, 120, getWidth(), 52, new Font("Arial", Font.BOLD, 42), Color.WHITE);
        drawButton(g2, startButton(), "Start", true);
        drawButton(g2, levelSelectButton(), "Level Select", true);
        drawButton(g2, exitButton(), "Exit", true);
    }

    private void drawLevelSelect(Graphics2D g2) {
        fillPanelBackground(g2);
        drawCenteredText(g2, "Level Select", 0, 70, getWidth(), 44, new Font("Arial", Font.BOLD, 36), Color.WHITE);

        LevelState[] states = gameUiPort.getLevelStates();
        for (int i = 0; i < 6; i++) {
            LevelState state = i < states.length ? states[i] : LevelState.LOCKED;
            Rectangle bounds = levelButton(i);
            boolean enabled = state != LevelState.LOCKED;
            drawButton(g2, bounds, "Level " + (i + 1) + " - " + state.name(), enabled);
        }

        drawButton(g2, menuButton(), "Main Menu", true);
    }

    private void drawVictory(Graphics2D g2) {
        fillPanelBackground(g2);
        drawCenteredText(g2, "All Levels Complete", 0, 140, getWidth(), 52, new Font("Arial", Font.BOLD, 42), Color.WHITE);
        drawButton(g2, levelSelectButton(), "Level Select", true);
        drawButton(g2, exitButton(), "Exit", true);
    }

    private void drawGameplay(Graphics2D g2) {
        scaleToPanel(g2);

        drawBackground(g2);
        drawDisappearingFloors(g2);
        drawDoor(g2);
        drawSpikes(g2);
        drawPlayer(g2);
        drawHud(g2);
        drawWinText(g2);
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

        g2.setColor(BACKGROUND);
        g2.fillRect(0, 0, w, h);

        g2.setColor(FLOOR);
        g2.fillRect(0, floorY, w, h - floorY);

        g2.setColor(new Color(80, 85, 110));
        g2.drawLine(0, floorY, w, floorY);
    }

    private void drawDisappearingFloors(Graphics2D g2) {
        for (DisappearingFloorView floor : gameUiPort.getDisappearingFloors()) {
            if ("GONE".equals(floor.state)) {
                g2.setColor(BACKGROUND);
                g2.fillRect(floor.x, floor.y, floor.width, gameUiPort.getWorldHeight() - floor.y);
                continue;
            }

            g2.setColor("TRIGGERED".equals(floor.state) ? new Color(255, 195, 80) : new Color(95, 105, 135));
            g2.fillRect(floor.x, floor.y, floor.width, floor.height);
            g2.setColor(Color.WHITE);
            g2.drawRect(floor.x, floor.y, floor.width, floor.height);
        }
    }

    private void drawDoor(Graphics2D g2) {
        Rectangle door = gameUiPort.getDoor();

        if (door == null) {
            return;
        }

        g2.setColor(Color.WHITE);
        g2.fillRect(door.x, door.y, door.width, door.height);

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(door.x, door.y, door.width, door.height);
    }

    private void drawSpikes(Graphics2D g2) {
        for (SpikeView spike : gameUiPort.getSpikes()) {
            drawSpike(g2, spike);
        }
    }

    private void drawSpike(Graphics2D g2, SpikeView spike) {
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

        g2.setColor(ACCENT);
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

    private void setupMouse() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    private void handleClick(int x, int y) {
        GameScreen screen = gameUiPort.getScreen();

        if (screen == GameScreen.MAIN_MENU) {
            handleMainMenuClick(x, y);
        } else if (screen == GameScreen.LEVEL_SELECT) {
            handleLevelSelectClick(x, y);
        } else if (screen == GameScreen.VICTORY) {
            handleVictoryClick(x, y);
        }
    }

    private void handleMainMenuClick(int x, int y) {
        if (startButton().contains(x, y)) {
            mainRouter.route("/game/menu/start", Params.of());
        } else if (levelSelectButton().contains(x, y)) {
            mainRouter.route("/game/menu/level-select", Params.of());
        } else if (exitButton().contains(x, y)) {
            mainRouter.route("/game/menu/exit", Params.of());
        }
    }

    private void handleLevelSelectClick(int x, int y) {
        if (menuButton().contains(x, y)) {
            mainRouter.route("/game/menu", Params.of());
            return;
        }

        for (int i = 0; i < 6; i++) {
            if (levelButton(i).contains(x, y)) {
                mainRouter.route("/game/level/select", Params.of(i + 1));
                return;
            }
        }
    }

    private void handleVictoryClick(int x, int y) {
        if (levelSelectButton().contains(x, y)) {
            mainRouter.route("/game/menu/level-select", Params.of());
        } else if (exitButton().contains(x, y)) {
            mainRouter.route("/game/menu/exit", Params.of());
        }
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

    private void fillPanelBackground(Graphics2D g2) {
        g2.setColor(BACKGROUND);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawButton(Graphics2D g2, Rectangle bounds, String text, boolean enabled) {
        g2.setColor(enabled ? new Color(55, 70, 95) : new Color(55, 55, 60));
        g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g2.setColor(enabled ? Color.WHITE : new Color(150, 150, 150));
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        drawCenteredText(g2, text, bounds.x, bounds.y, bounds.width, bounds.height, new Font("Arial", Font.BOLD, 20), enabled ? Color.WHITE : new Color(150, 150, 150));
    }

    private void drawCenteredText(Graphics2D g2, String text, int x, int y, int width, int height, Font font, Color color) {
        g2.setFont(font);
        g2.setColor(color);
        FontMetrics metrics = g2.getFontMetrics();
        int textX = x + (width - metrics.stringWidth(text)) / 2;
        int textY = y + ((height - metrics.getHeight()) / 2) + metrics.getAscent();
        g2.drawString(text, textX, textY);
    }

    private Rectangle startButton() {
        return centeredButton(220);
    }

    private Rectangle levelSelectButton() {
        return centeredButton(300);
    }

    private Rectangle exitButton() {
        return centeredButton(380);
    }

    private Rectangle menuButton() {
        return new Rectangle(24, getHeight() - 78, 180, 48);
    }

    private Rectangle centeredButton(int y) {
        int width = 240;
        return new Rectangle((getWidth() - width) / 2, y, width, 54);
    }

    private Rectangle levelButton(int index) {
        int buttonWidth = 220;
        int buttonHeight = 58;
        int gap = 22;
        int col = index % 3;
        int row = index / 3;
        int totalWidth = buttonWidth * 3 + gap * 2;
        int startX = (getWidth() - totalWidth) / 2;
        int startY = 160;
        return new Rectangle(startX + col * (buttonWidth + gap), startY + row * 90, buttonWidth, buttonHeight);
    }
}
