package leveldevil.control;

import leveldevil.model.Arena;
import leveldevil.model.GameState;
import leveldevil.model.GameStatus;
import shared.ui_ports.LevelDevilUiPort;

/**
 * Owns lives/deaths and the death -> respawn -> game-over flow. Level loading
 * and the level-complete / victory flow live in {@link LevelController}; both
 * share the single {@link GameState}. The end-of-game name-entry / high-score
 * flow is orchestrated by the backend after these methods set GAME_OVER/VICTORY.
 */
public class GameLifecycleController {

    public static final int INITIAL_LIVES = 9;

    private final GameState gameState;
    private final AudioController audio;
    private LevelController levelController; // wired after construction (mutual reference)

    public GameLifecycleController(GameState gameState, Arena arena, AudioController audio) {
        this.gameState = gameState;
        this.audio = audio;
    }

    public void setLevelController(LevelController levelController) {
        this.levelController = levelController;
    }

    private LevelDevilUiPort ui() {
        return LevelDevilUiPort.getInstance();
    }

    public void startGame() {
        startGameAt(1);
    }

    /** Start a fresh game beginning at level {@code n} (used by the level-select map). */
    public void startGameAt(int n) {
        gameState.setLives(INITIAL_LIVES);
        gameState.setDeaths(0);
        gameState.setLevelNumber(n);
        gameState.changeStateTo(GameStatus.PLAYING);
        ui().updateLives(INITIAL_LIVES);
        ui().updateDeaths(0);
        levelController.loadLevel(n, true);
    }

    /** The player touched a hazard or fell out of the world. */
    public void onPlayerDied() {
        int deaths = gameState.addDeath();
        ui().updateDeaths(deaths);
        audio.playOnDeath();
        ui().flashDeath();

        int lives = gameState.decreaseLife();
        ui().updateLives(lives);

        if (lives <= 0) {
            // The backend converts GAME_OVER into the name-entry / high-score flow.
            gameState.changeStateTo(GameStatus.GAME_OVER);
            audio.playOnGameOver();
        } else {
            levelController.respawn();
        }
    }

    /** The player reached a real exit door. */
    public void onLevelCompleted() {
        audio.playOnDoor();
        levelController.advanceToNextLevel();
    }
}
