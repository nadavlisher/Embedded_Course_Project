package team.model;

/**
 * Global state of the running game.
 *
 * One instance per session. All controllers read/update lives, deaths, level,
 * unlocked-progress and game-status through this single object so the rules stay
 * consistent even when several controllers reach the same state in one tick.
 */
public class GameState {

    private GameStatus status = GameStatus.MAIN_MENU;
    private int lives;
    private int deaths;
    private int levelNumber;
    private int solvedUpTo = 0;   // highest level the player has completed (progression map)
    private int levelReached = 1; // level reached when the last game ended (for the score table)
    private boolean muted = false;
    private Arena arena;
    private Level currentLevel;

    public GameStatus getStatus()  { return status; }
    public int getLives()          { return lives; }
    public int getDeaths()         { return deaths; }
    public int getLevelNumber()    { return levelNumber; }
    public int getSolvedUpTo()     { return solvedUpTo; }
    public int getLevelReached()   { return levelReached; }
    public boolean isMuted()       { return muted; }
    public Arena getArena()        { return arena; }
    public Level getCurrentLevel() { return currentLevel; }

    public void setLives(int lives)          { this.lives = lives; }
    public void setDeaths(int deaths)        { this.deaths = deaths; }
    public void setLevelNumber(int n)        { this.levelNumber = n; }
    public void setSolvedUpTo(int n)         { this.solvedUpTo = n; }
    public void setLevelReached(int n)       { this.levelReached = n; }
    public void setMuted(boolean m)          { this.muted = m; }
    public void setArena(Arena arena)        { this.arena = arena; }
    public void setCurrentLevel(Level level) { this.currentLevel = level; }

    public void changeStateTo(GameStatus newStatus) {
        this.status = newStatus;
    }

    public int decreaseLife() {
        lives = Math.max(0, lives - 1);
        return lives;
    }

    public int addDeath() {
        return ++deaths;
    }
}
