package leveldevil.control;

import leveldevil.model.Arena;
import leveldevil.model.Door;
import leveldevil.model.HitBounds;
import leveldevil.model.Mover;
import leveldevil.model.Player;
import leveldevil.model.Spike;
import leveldevil.model.Tile;

import java.util.ArrayList;
import java.util.List;

/**
 * Integrates the player's position and resolves it against the world using
 * classic axis-separated AABB resolution (move X and resolve, then move Y and
 * resolve). Also reports the two events the backend cares about: the player
 * died (spike, crush, or fell out of the world) or reached a real exit door.
 *
 * Spikes and airborne falling blocks are hazards, never solids; static tiles,
 * platforms and landed blocks are solids.
 */
public class CollisionController {

    public enum Event { NONE, DEATH, DOOR }

    private final Arena arena;

    public CollisionController(Arena arena) {
        this.arena = arena;
    }

    public Event integrate(double dt) {
        Player p = arena.getPlayer();
        if (p == null) return Event.NONE;

        List<HitBounds> solids = solids();

        // --- Horizontal move + resolve ---
        p.setX(p.getX() + p.getVx() * dt);
        for (HitBounds s : solids) {
            if (p.getBounds().intersects(s)) {
                if (p.getVx() > 0)      p.setX(s.left() - p.getWidth());
                else if (p.getVx() < 0) p.setX(s.right());
                p.setVx(0);
            }
        }

        // --- Vertical move + resolve ---
        p.setOnGround(false);
        p.setY(p.getY() + p.getVy() * dt);
        for (HitBounds s : solids) {
            if (p.getBounds().intersects(s)) {
                if (p.getVy() > 0) {          // falling: land on top
                    p.setY(s.top() - p.getHeight());
                    p.setVy(0);
                    p.setOnGround(true);
                } else if (p.getVy() < 0) {   // rising: bonk head
                    p.setY(s.bottom());
                    p.setVy(0);
                }
            }
        }

        if (hitsHazard()) return Event.DEATH;
        if (p.getY() > arena.getHeight() + 40) return Event.DEATH; // fell off the bottom

        for (Door d : arena.doors()) {
            if (d.isReal() && p.getBounds().intersects(d.getBounds())) return Event.DOOR;
        }
        return Event.NONE;
    }

    /** True if the player is currently touching a spike or an airborne block. */
    public boolean hitsHazard() {
        Player p = arena.getPlayer();
        if (p == null) return false;
        HitBounds pb = p.getBounds();
        for (Spike s : arena.spikes()) {
            if (pb.intersects(s.getBounds())) return true;
        }
        for (Mover m : arena.movers()) {
            if (m.isLethalNow() && pb.intersects(m.getBounds())) return true;
        }
        return false;
    }

    private List<HitBounds> solids() {
        List<HitBounds> out = new ArrayList<>();
        for (Tile t : arena.tiles())  out.add(t.getBounds());
        for (Mover m : arena.movers()) if (m.isSolid()) out.add(m.getBounds());
        return out;
    }
}
