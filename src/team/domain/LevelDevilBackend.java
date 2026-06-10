package leveldevil.domain;

import leveldevil.control.AudioController;
import leveldevil.control.CollisionController;
import leveldevil.control.GameLifecycleController;
import leveldevil.control.HighScoreController;
import leveldevil.control.LevelController;
import leveldevil.control.PlayerController;
import leveldevil.control.TrapController;
import leveldevil.model.Arena;
import leveldevil.model.GameState;
import leveldevil.model.GameStatus;
import leveldevil.model.IdGenerator;
import leveldevil.model.Player;
import shared.ui_ports.LevelDevilUiPort;

/**
 * Backend facade for the Level Devil game.
 *
 * Owns the whole model and every controller; all router calls (keyboard AND
 * mouse) land here and are delegated. The UI is touched only through the
 * abstract {@link LevelDevilUiPort}.
 *
 * Beyond the core platformer it drives the menu screens: the level-progression
 * map (replay any solved level, never skip ahead), the arcade high-score table
 * (Excel-persisted, name entry at game end, ranked by level reached then
 * arrival order), a sound mute toggle, and full mouse navigation of the menus.
 */
public class LevelDevilBackend {

    public static final int ARENA_WIDTH  = 960;
    public static final int ARENA_HEIGHT = 576;

    private static final double PLAYER_WIDTH  = 30;
    private static final double PLAYER_HEIGHT = 42;
    private static final double TICK_DURATION_S = 30.0 / 1000.0;
    private static final int PHYSICS_SUBSTEPS = 5;
    private static final int HIGH_SCORE_ROWS = 10;

    private final GameState   gameState;
    private final Arena       arena;
    private final IdGenerator ids;

    private final AudioController         audio;
    private final PlayerController        playerController;
    private final CollisionController     collisionController;
    private final TrapController          trapController;
    private final LevelController         levelController;
    private final GameLifecycleController lifecycleController;
    private final HighScoreController     highScores;

    public LevelDevilBackend() {
        this.gameState = new GameState();
        this.arena     = new Arena(ARENA_WIDTH, ARENA_HEIGHT);
        this.ids       = new IdGenerator();
        this.arena.setPlayer(new Player(0, 0, PLAYER_WIDTH, PLAYER_HEIGHT));
        this.gameState.setArena(arena);

        this.audio               = new AudioController();
        this.playerController    = new PlayerController(arena, audio);
        this.collisionController = new CollisionController(arena);
        this.trapController      = new TrapController(arena, ids, audio);
        this.levelController     = new LevelController(gameState, arena, ids, playerController, audio);
        this.lifecycleController = new GameLifecycleController(gameState, arena, audio);
        this.lifecycleController.setLevelController(levelController);
        this.highScores          = new HighScoreController();
    }

    private LevelDevilUiPort ui() {
        return LevelDevilUiPort.getInstance();
    }

    // ===================== startup / menus =====================

    public void start() {
        ui().log("LevelDevilBackend.start()");
        ui().drawArena(ARENA_WIDTH, ARENA_HEIGHT);
        showMainMenu();
    }

    public void showMainMenu() {
        arena.clearDynamic();
        ui().clearLevel();
        ui().clearBanner();
        gameState.setLives(GameLifecycleController.INITIAL_LIVES);
        gameState.setDeaths(0);
        gameState.setLevelNumber(1);
        ui().updateLives(gameState.getLives());
        ui().updateDeaths(gameState.getDeaths());
        ui().updateLevel(gameState.getLevelNumber());
        gameState.changeStateTo(GameStatus.MAIN_MENU);
        audio.playOnMenu();
        ui().setMuted(gameState.isMuted());
        ui().showMainMenu();
        ui().updateStatusText("SPACE / click Start     L levels     H scores     S sound");
    }

    public void openLevelSelect() {
        gameState.changeStateTo(GameStatus.LEVEL_SELECT);
        ui().showLevelSelect(LevelController.TOTAL_LEVELS, gameState.getSolvedUpTo());
        ui().updateStatusText("Pick an unlocked level     M = back");
    }

    public void openHighScores() {
        gameState.changeStateTo(GameStatus.HIGH_SCORES);
        ui().showHighScores(highScores.topRows(HIGH_SCORE_ROWS), -1);
        ui().updateStatusText("M = back to menu");
    }

    public void toggleMute() {
        boolean muted = !gameState.isMuted();
        gameState.setMuted(muted);
        ui().setMuted(muted);
    }

    // ===================== starting a game =====================

    public void startNewGame() {
        ui().clearBanner();
        ui().showPlaying();
        lifecycleController.startGame();
    }

    public void restart() {
        startNewGame();
    }

    /** From the level-select map: start at an unlocked level (solved levels + the next one). */
    public void selectLevel(int n) {
        if (gameState.getStatus() != GameStatus.LEVEL_SELECT) return;
        int maxUnlocked = Math.min(LevelController.TOTAL_LEVELS, gameState.getSolvedUpTo() + 1);
        if (n < 1 || n > maxUnlocked) return; // locked - ignore
        ui().clearBanner();
        ui().showPlaying();
        lifecycleController.startGameAt(n);
    }

    public void jumpToNextLevel() {
        if (gameState.getStatus() == GameStatus.PLAYING) {
            levelController.advanceToNextLevel();
            if (gameState.getStatus() == GameStatus.VICTORY) {
                gameState.setSolvedUpTo(Math.max(gameState.getSolvedUpTo(), LevelController.TOTAL_LEVELS));
                enterNameEntry(true);
            }
        }
    }

    // ===================== gameplay input =====================

    public void setMoveDir(int direction) {
        if (gameState.getStatus() != GameStatus.PLAYING) return;
        playerController.setMoveDir(direction);
    }

    public void jump() {
        switch (gameState.getStatus()) {
            case MAIN_MENU: startNewGame();                 return;
            case PLAYING:   playerController.requestJump();  return;
            default:        /* menus / name entry / scores: ignore */ return;
        }
    }

    public void tick() {
        if (gameState.getStatus() != GameStatus.PLAYING) return;

        double subDt = TICK_DURATION_S / PHYSICS_SUBSTEPS;
        for (int i = 0; i < PHYSICS_SUBSTEPS; i++) {
            trapController.updateMovers(subDt);
            playerController.prePhysics(subDt);
            CollisionController.Event ev = collisionController.integrate(subDt);
            if (ev == CollisionController.Event.DEATH) { handleDeath(); return; }
            if (ev == CollisionController.Event.DOOR)  { handleDoor();  return; }
        }

        trapController.springTriggers(TICK_DURATION_S);
        if (collisionController.hitsHazard()) { handleDeath(); return; }

        pushPlayerView();
    }

    private void handleDoor() {
        int completed = gameState.getLevelNumber();
        lifecycleController.onLevelCompleted();
        if (completed > gameState.getSolvedUpTo()) gameState.setSolvedUpTo(completed);
        if (gameState.getStatus() == GameStatus.VICTORY) enterNameEntry(true);
    }

    private void handleDeath() {
        lifecycleController.onPlayerDied();
        if (gameState.getStatus() == GameStatus.GAME_OVER) enterNameEntry(false);
    }

    // ===================== end of game: name entry + high scores =====================

    private void enterNameEntry(boolean won) {
        int levelReached = gameState.getLevelNumber();
        gameState.setLevelReached(levelReached);
        gameState.changeStateTo(GameStatus.ENTER_NAME);
        highScores.beginEntry();
        ui().showNameEntry(levelReached, won);
        ui().updateStatusText("Type your name, then ENTER");
    }

    public void typeNameChar(String s) {
        if (gameState.getStatus() != GameStatus.ENTER_NAME || s == null || s.isEmpty()) return;
        highScores.typeChar(s.charAt(0));
        ui().updateNameEntry(highScores.currentName());
    }

    public void nameBackspace() {
        if (gameState.getStatus() != GameStatus.ENTER_NAME) return;
        highScores.backspace();
        ui().updateNameEntry(highScores.currentName());
    }

    public void submitName() {
        if (gameState.getStatus() != GameStatus.ENTER_NAME) return;
        String name = highScores.currentName().trim();
        if (name.isEmpty()) name = "PLAYER";
        int seq = highScores.add(name, gameState.getLevelReached());
        gameState.changeStateTo(GameStatus.HIGH_SCORES);
        ui().showHighScores(highScores.topRows(HIGH_SCORE_ROWS), seq);
        ui().updateStatusText("M = menu     L = levels");
    }

    // ===================== misc =====================

    private void pushPlayerView() {
        Player p = arena.getPlayer();
        ui().setPlayer(p.getX(), p.getY(), p.getWidth(), p.getHeight(), p.getFacing());
    }

    public GameState gameState() { return gameState; }
    public Arena arena()         { return arena; }
}
