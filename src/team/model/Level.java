package team.model;

/**
 * Lightweight per-level descriptor: just the number and a display name.
 *
 * The actual geometry (tiles, spikes, door, traps, spawn) is built directly
 * into the Arena by LevelController, so the model layer stays free of
 * "create object id N" logic - exactly as the Breakout LevelController did.
 */
public class Level {

    private final int number;
    private final String name;

    public Level(int number, String name) {
        this.number = number;
        this.name = name;
    }

    public int number()  { return number; }
    public String name()  { return name; }
}
