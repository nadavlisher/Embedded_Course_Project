package team.model;

/** The direction a spike points (i.e. which surface it sticks out of). */
public enum SpikeDir {
    UP,    // sits on a floor, points up
    DOWN,  // hangs from a ceiling, points down
    LEFT,  // sticks out of a right-hand wall, points left
    RIGHT  // sticks out of a left-hand wall, points right
}
