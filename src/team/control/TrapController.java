package leveldevil.control;

import leveldevil.model.Arena;
import leveldevil.model.Door;
import leveldevil.model.HitBounds;
import leveldevil.model.Mover;
import leveldevil.model.MoverKind;
import leveldevil.model.Player;
import leveldevil.model.Spike;
import leveldevil.model.SpikeDir;
import leveldevil.model.Tile;
import leveldevil.model.Trap;
import leveldevil.model.IdGenerator;
import shared.ui_ports.LevelDevilUiPort;

import java.util.ArrayList;
import java.util.List;

/**
 * The troll engine. Two responsibilities:
 *   1. updateMovers(dt): integrate every dynamic block - oscillating platforms
 *      (which carry a rider) and falling blocks (gravity + landing), and slide
 *      any door that a MOVE_DOOR trap set in motion.
 *   2. springTriggers(dt): when the player enters a trap's trigger region, arm
 *      its fuse and - once the fuse elapses - apply the effect exactly once.
 */
public class TrapController {

    private static final double BLOCK_GRAVITY  = 1500; // px/s^2
    private static final double BLOCK_MAX_FALL = 1100; // px/s

    private final Arena arena;
    private final IdGenerator ids;
    private final AudioController audio;

    public TrapController(Arena arena, IdGenerator ids, AudioController audio) {
        this.arena = arena;
        this.ids = ids;
        this.audio = audio;
    }

    private LevelDevilUiPort ui() {
        return LevelDevilUiPort.getInstance();
    }

    public void updateMovers(double dt) {
        Player p = arena.getPlayer();
        List<Integer> toRemove = new ArrayList<>();

        for (Mover m : new ArrayList<>(arena.movers())) {
            switch (m.getKind()) {
                case PLATFORM_H: {
                    double oldX = m.getX();
                    boolean riding = isRiding(p, m);
                    double nx = oldX + m.getDir() * m.getSpeed() * dt;
                    if (nx <= m.getRangeMin()) { nx = m.getRangeMin(); m.setDir(1); }
                    if (nx >= m.getRangeMax()) { nx = m.getRangeMax(); m.setDir(-1); }
                    m.setX(nx);
                    if (riding && p != null) p.setX(p.getX() + (nx - oldX));
                    ui().updateMover(m.getId(), m.getX(), m.getY());
                    break;
                }
                case PLATFORM_V: {
                    double oldY = m.getY();
                    boolean riding = isRiding(p, m);
                    double ny = oldY + m.getDir() * m.getSpeed() * dt;
                    if (ny <= m.getRangeMin()) { ny = m.getRangeMin(); m.setDir(1); }
                    if (ny >= m.getRangeMax()) { ny = m.getRangeMax(); m.setDir(-1); }
                    m.setY(ny);
                    if (riding && p != null) p.setY(p.getY() + (ny - oldY));
                    ui().updateMover(m.getId(), m.getX(), m.getY());
                    break;
                }
                case FALLING_BLOCK:
                case FALLING_SPIKE: {
                    if (m.isLanded()) break;
                    double vy = Math.min(BLOCK_MAX_FALL, m.getVy() + BLOCK_GRAVITY * dt);
                    m.setVy(vy);
                    m.setY(m.getY() + vy * dt);
                    double landY = landingYFor(m);
                    if (!Double.isNaN(landY) && m.getY() >= landY) {
                        m.setY(landY);
                        m.setVy(0);
                        m.setLanded(true);
                        ui().updateMover(m.getId(), m.getX(), m.getY());
                    } else if (m.getY() > arena.getHeight() + 80) {
                        toRemove.add(m.getId());
                    } else {
                        ui().updateMover(m.getId(), m.getX(), m.getY());
                    }
                    break;
                }
            }
        }
        for (int id : toRemove) { arena.removeMover(id); ui().removeMover(id); }

        // Slide any door that a MOVE_DOOR trap put in motion.
        for (Door d : arena.doors()) {
            if (d.isMoving()) {
                d.stepSlide(dt);
                ui().updateDoor(d.getId(), d.getX(), d.getY());
            }
        }
    }

    public void springTriggers(double dt) {
        Player p = arena.getPlayer();
        if (p == null) return;
        HitBounds pb = p.getBounds();
        for (Trap tr : arena.traps()) {
            if (tr.isSprung()) continue;
            if (!tr.isArmed() && pb.intersects(tr.getTrigger())) {
                tr.arm();
            }
            if (tr.isArmed() && !tr.isSprung()) {
                tr.tickFuse(dt);
                if (tr.isReadyToFire()) fire(tr, p);
            }
        }
    }

    private void fire(Trap tr, Player p) {
        switch (tr.getKind()) {
            case DISAPPEARING_FLOOR:
                for (int tileId : tr.getTileIds()) {
                    arena.removeTile(tileId);
                    ui().removeTile(tileId);
                }
                break;
            case POPUP_SPIKE:
                for (double[] s : tr.getSpikeSpecs()) {
                    SpikeDir dir = SpikeDir.values()[(int) s[4]];
                    Spike spike = new Spike(ids.next(), s[0], s[1], s[2], s[3], dir);
                    arena.addSpike(spike);
                    ui().drawSpike(spike.getId(), s[0], s[1], s[2], s[3], dir.name());
                }
                break;
            case FALLING_BLOCK: {
                double[] b = tr.getBlockSpec();
                if (b != null) {
                    Mover m = new Mover(ids.next(), MoverKind.FALLING_BLOCK, b[0], b[1], b[2], b[3]);
                    arena.addMover(m);
                    ui().drawMover(m.getId(), b[0], b[1], b[2], b[3], MoverKind.FALLING_BLOCK.name());
                }
                break;
            }
            case FALLING_SPIKE: {
                double[] b = tr.getBlockSpec();
                if (b != null) {
                    Mover m = new Mover(ids.next(), MoverKind.FALLING_SPIKE, b[0], b[1], b[2], b[3]);
                    arena.addMover(m);
                    ui().drawMover(m.getId(), b[0], b[1], b[2], b[3], MoverKind.FALLING_SPIKE.name());
                }
                break;
            }
            case MOVE_DOOR: {
                Door d = arena.getDoor(tr.getDoorId());
                if (d != null) d.slideTo(tr.getDoorTargetX(), tr.getDoorTargetY(), 320);
                break;
            }
        }
        tr.markSprung();
        audio.playOnTrap();
        if (tr.getTaunt() != null) {
            ui().showFloatingText(tr.getTaunt(), p.getBounds().cx(), p.getY() - 12, "RED");
        }
    }

    /** True if the player is standing on top of this platform (so it carries them). */
    private boolean isRiding(Player p, Mover m) {
        if (p == null || m.getKind() == MoverKind.FALLING_BLOCK) return false;
        HitBounds pb = p.getBounds();
        boolean xOverlap = pb.right() > m.getX() + 2 && pb.left() < m.getX() + m.getWidth() - 2;
        boolean onTop = Math.abs(pb.bottom() - m.getY()) <= 4.0;
        return xOverlap && onTop;
    }

    /** Y at which a falling block should rest on the nearest tile beneath it (NaN if none). */
    private double landingYFor(Mover m) {
        HitBounds mb = m.getBounds();
        double bestTop = Double.NaN;
        for (Tile t : arena.tiles()) {
            HitBounds tb = t.getBounds();
            boolean xOverlap = mb.right() > tb.left() + 2 && mb.left() < tb.right() - 2;
            if (xOverlap && tb.top() >= mb.top()) {
                if (Double.isNaN(bestTop) || tb.top() < bestTop) bestTop = tb.top();
            }
        }
        return Double.isNaN(bestTop) ? Double.NaN : bestTop - m.getHeight();
    }
}
