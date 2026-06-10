package team.model;

/**
 * Hands out unique, monotonically increasing integer ids for every visual
 * object (tiles, spikes, movers, door). The same id is used by the model and
 * by the UI views so the UI port can address a single object.
 */
public class IdGenerator {

    private int counter = 0;

    public int next() {
        return ++counter;
    }
}
