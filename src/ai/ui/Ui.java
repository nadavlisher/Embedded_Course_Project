package ai.ui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;

import base.Params;
import shared.MainRouter;
import shared.ui_ports.LevelDevilUiPort;

/**
 * Top-level UI for the Level Devil game.
 *
 * The UI never calls the backend directly.
 * It forwards user input - keyboard AND mouse - to the MainRouter and renders
 * whatever the UI port pushes down. Mouse clicks on menu buttons are hit-tested
 * in GamePanel and routed exactly like keys.
 *
 * Keyboard:
 *   ← → / A D     move           Space / Up / W   jump (also Start from menu)
 *   R             restart        M / Esc          main menu
 *   L             level map      H                high scores
 *   S             toggle sound   N                next level (debug)
 *   1..6          pick a level on the map
 *   typed chars / Backspace / Enter   name entry on the score screen
 */
public class Ui {

    private static final int LEFT = -1;
    private static final int RIGHT = +1;

    private MainRouter mainRouter;
    private LevelDevilUiPortImpl uiInstance;
    private GamePanel gamePanel;
    private HudPanel hudPanel;
    private DialogManager dialogManager;
    private SoundPlayer soundPlayer;
    private JFrame frame;

    private final Set<Integer> heldDirs = new HashSet<>();
    private boolean jumpHeld = false;

    public void setUiPorts() {
        gamePanel = new GamePanel();
        hudPanel = new HudPanel();
        soundPlayer = new SoundPlayer();
        dialogManager = new DialogManager(gamePanel);
        uiInstance = new LevelDevilUiPortImpl(gamePanel, hudPanel, dialogManager, soundPlayer);
        LevelDevilUiPort.setInstance(uiInstance);
    }

    public void start(MainRouter mainRouter) {
        this.mainRouter = mainRouter;
        SwingUtilities.invokeLater(this::createAndShowWindow);
    }

    private void route(String path)            { mainRouter.route(path, Params.of()); }
    private void route(String path, Object p)  { mainRouter.route(path, Params.of(p)); }

    private void createAndShowWindow() {
        frame = new JFrame("Level Devil");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(hudPanel, BorderLayout.NORTH);
        frame.add(gamePanel, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        dialogManager.install();

        gamePanel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                GamePanel.Button b = gamePanel.hitTest(e.getX(), e.getY());
                if (b != null) mainRouter.route(b.route, Params.of(b.param));
                gamePanel.requestFocusInWindow();
            }
        });

        gamePanel.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                // While entering a high-score name, typed characters are the name
                // (handled in keyTyped). Only Enter/Backspace/Esc act here; every
                // other global shortcut (L/H/S/M/R/N, digits, move/jump) is
                // suppressed so the first letter no longer exits the screen.
                if (gamePanel.isNameEntry()) {
                    if (code == KeyEvent.VK_ENTER)           route("/leveldevil/name/submit");
                    else if (code == KeyEvent.VK_BACK_SPACE) route("/leveldevil/name/backspace");
                    else if (code == KeyEvent.VK_ESCAPE)     route("/leveldevil/menu/show");
                    return;
                }
                if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
                    if (heldDirs.add(LEFT)) routeMove();
                } else if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
                    if (heldDirs.add(RIGHT)) routeMove();
                } else if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                    if (!jumpHeld) { jumpHeld = true; route("/leveldevil/player/jump"); }
                } else if (code == KeyEvent.VK_ENTER) {
                    route("/leveldevil/name/submit");
                } else if (code == KeyEvent.VK_BACK_SPACE) {
                    route("/leveldevil/name/backspace");
                } else if (code == KeyEvent.VK_R) {
                    route("/leveldevil/game/restart");
                } else if (code == KeyEvent.VK_M || code == KeyEvent.VK_ESCAPE) {
                    route("/leveldevil/menu/show");
                } else if (code == KeyEvent.VK_L) {
                    route("/leveldevil/menu/levels");
                } else if (code == KeyEvent.VK_H) {
                    route("/leveldevil/menu/scores");
                } else if (code == KeyEvent.VK_S) {
                    route("/leveldevil/audio/toggleMute");
                } else if (code == KeyEvent.VK_N) {
                    route("/leveldevil/level/next");
                } else if (code >= KeyEvent.VK_1 && code <= KeyEvent.VK_6) {
                    route("/leveldevil/level/select", (code - KeyEvent.VK_1 + 1));
                }
            }

            @Override public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
                    heldDirs.remove(LEFT); routeMove();
                } else if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
                    heldDirs.remove(RIGHT); routeMove();
                } else if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                    jumpHeld = false;
                }
            }

            @Override public void keyTyped(KeyEvent e) {
                // Feed typed characters to the score-name entry (the backend
                // ignores them unless it is in the name-entry state).
                char c = e.getKeyChar();
                if (c != KeyEvent.CHAR_UNDEFINED && !Character.isISOControl(c)) {
                    route("/leveldevil/name/type", String.valueOf(c));
                }
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) { gamePanel.requestFocusInWindow(); }
        });

        frame.setVisible(true);
        gamePanel.requestFocusInWindow();
        route("/leveldevil/start");
    }

    /** Send the net horizontal intent (-1 / 0 / +1) from the held-keys set. */
    private void routeMove() {
        int dir = (heldDirs.contains(RIGHT) ? 1 : 0) + (heldDirs.contains(LEFT) ? -1 : 0);
        route("/leveldevil/player/move", dir);
    }
}
