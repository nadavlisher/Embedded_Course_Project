package team.model;

/**
 * The end point of the level.
 * If the player collides with the door, the level is completed.
 */
public class Door extends GameObject {

    public Door(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
}
