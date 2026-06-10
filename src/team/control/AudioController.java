package team.control;

import shared.ui_ports.LevelDevilUiPort;

/**
 * Decides *when* to play *which* sound, but never plays it directly - playback
 * goes through the UI port (implemented by SoundPlayer on the UI side). This
 * controller only maps semantic game events to semantic sound names.
 */
public class AudioController {

    private LevelDevilUiPort ui() {
        return LevelDevilUiPort.getInstance();
    }

    public void playOnJump()          { ui().playSound("JUMP"); }
    public void playOnTrap()          { ui().playSound("TRAP"); }
    public void playOnDeath()         { ui().playSound("DEATH"); }
    public void playOnDoor()          { ui().playSound("DOOR"); }
    public void playOnLevelComplete() { ui().playSound("LEVEL_COMPLETE"); }
    public void playOnVictory()       { ui().playSound("VICTORY"); }
    public void playOnGameOver()      { ui().playSound("GAME_OVER"); }
    public void playOnMenu()          { ui().playSound("MENU"); }
}
