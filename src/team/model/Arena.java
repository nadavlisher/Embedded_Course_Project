package team.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The world container: holds the player and every
 * level object. Controllers read and mutate the world through this single
 * object; the UI never sees it.
 *
 * A level can contain more than one door: exactly one (or more) is the real
 * exit and any others are fake decoys used by the troll mechanics.
 */
public class Arena {

    private final int width, height;

    private Player player;
    private double spawnX, spawnY;

    private final Map<Integer, Tile>  tiles  = new LinkedHashMap<>();
    private final Map<Integer, Spike> spikes = new LinkedHashMap<>();
    private final Map<Integer, Mover> movers = new LinkedHashMap<>();
    private final Map<Integer, Door>  doors  = new LinkedHashMap<>();
    private final List<Trap>          traps  = new ArrayList<>();

    public Arena(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth()  { return width; }
    public int getHeight() { return height; }

    public Player getPlayer()       { return player; }
    public void setPlayer(Player p) { this.player = p; }

    public double getSpawnX()       { return spawnX; }
    public double getSpawnY()       { return spawnY; }
    public void setSpawn(double x, double y) { this.spawnX = x; this.spawnY = y; }

    public void addTile(Tile t)     { tiles.put(t.getId(), t); }
    public void removeTile(int id)  { tiles.remove(id); }
    public Tile getTile(int id)     { return tiles.get(id); }
    public Collection<Tile> tiles() { return tiles.values(); }

    public void addSpike(Spike s)   { spikes.put(s.getId(), s); }
    public void removeSpike(int id) { spikes.remove(id); }
    public Collection<Spike> spikes(){ return spikes.values(); }

    public void addMover(Mover m)   { movers.put(m.getId(), m); }
    public void removeMover(int id) { movers.remove(id); }
    public Collection<Mover> movers(){ return movers.values(); }

    public void addDoor(Door d)     { doors.put(d.getId(), d); }
    public Door getDoor(int id)     { return doors.get(id); }
    public Collection<Door> doors() { return doors.values(); }

    public void addTrap(Trap t)     { traps.add(t); }
    public List<Trap> traps()       { return traps; }

    /** Wipe everything except arena dimensions, ready for a fresh level load. */
    public void clearDynamic() {
        tiles.clear();
        spikes.clear();
        movers.clear();
        doors.clear();
        traps.clear();
    }
}
