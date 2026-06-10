package team.model;

/** The four troll mechanics that betray the player. */
public enum TrapKind {
    DISAPPEARING_FLOOR, // named tiles vanish a beat after the player steps near
    POPUP_SPIKE,        // spikes shoot out of a surface
    FALLING_BLOCK,      // a block drops from above
    FALLING_SPIKE,      // an always-lethal spiked block drops from above
    MOVE_DOOR           // the exit slides away to a new spot
}
