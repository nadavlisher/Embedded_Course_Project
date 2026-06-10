package team.model;

/**
 * High-level state machine of the game.
 *
 *   MAIN_MENU      <-> LEVEL_SELECT / HIGH_SCORES (menu navigation)
 *   MAIN_MENU / LEVEL_SELECT --(start)--> PLAYING
 *   PLAYING        --(reach real door, lvl<6)--> LEVEL_TRANSITION --> PLAYING
 *   PLAYING        --(reach real door, lvl=6)--> VICTORY --> ENTER_NAME
 *   PLAYING        --(die, lives>0)--> PLAYING (respawn)
 *   PLAYING        --(die, lives==0)--> GAME_OVER --> ENTER_NAME
 *   ENTER_NAME     --(submit)--> HIGH_SCORES --> MAIN_MENU
 *
 * MAIN_MENU is the entry state on application start.
 */
public enum GameStatus {
    MAIN_MENU,
    LEVEL_SELECT,
    PLAYING,
    LEVEL_TRANSITION,
    GAME_OVER,
    VICTORY,
    ENTER_NAME,
    HIGH_SCORES
}
