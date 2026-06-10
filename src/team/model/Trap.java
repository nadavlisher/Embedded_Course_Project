package team.model;

import base.IdentifiedObject;
import java.util.ArrayList;
import java.util.List;

/**
 * A position-triggered troll event.
 *
 * When the player's body enters {@link #getTrigger()} the trap arms its fuse;
 * after {@code fuse} seconds {@link TrapController} applies the effect described
 * by the payload fields and marks the trap sprung (one-shot). A fresh set of
 * traps is rebuilt every time a level loads, so dying and respawning re-arms
 * everything.
 */
public class Trap extends IdentifiedObject {

    private final TrapKind kind;
    private final HitBounds trigger;
    private final double fuse;       // seconds between trigger and effect
    private final String taunt;      // floating text shown when it fires (may be null)

    private boolean armed = false;   // trigger entered, counting down
    private boolean sprung = false;  // effect already applied
    private double fuseRemaining = 0;

    // Payloads (only the ones relevant to the kind are populated):
    private int[] tileIds = new int[0];                 // DISAPPEARING_FLOOR
    private final List<double[]> spikeSpecs = new ArrayList<>(); // POPUP_SPIKE: {x,y,w,h,dirOrdinal}
    private double[] blockSpec;                          // FALLING_BLOCK: {x,y,w,h}
    private int    doorId = -1;                          // MOVE_DOOR: which door to slide
    private double doorTargetX, doorTargetY;             // MOVE_DOOR

    public Trap(int id, TrapKind kind, HitBounds trigger, double fuse, String taunt) {
        super(id);
        this.kind = kind;
        this.trigger = trigger;
        this.fuse = fuse;
        this.taunt = taunt;
    }

    public TrapKind getKind()    { return kind; }
    public HitBounds getTrigger(){ return trigger; }
    public double getFuse()      { return fuse; }
    public String getTaunt()     { return taunt; }

    public boolean isArmed()  { return armed; }
    public boolean isSprung() { return sprung; }
    public double getFuseRemaining() { return fuseRemaining; }

    public void arm()                 { this.armed = true; this.fuseRemaining = fuse; }
    public void tickFuse(double dt)   { if (armed && !sprung) fuseRemaining -= dt; }
    public boolean isReadyToFire()    { return armed && !sprung && fuseRemaining <= 0; }
    public void markSprung()          { this.sprung = true; this.armed = false; }

    public int[] getTileIds()             { return tileIds; }
    public void setTileIds(int[] ids)     { this.tileIds = ids; }

    public List<double[]> getSpikeSpecs() { return spikeSpecs; }
    public void addSpikeSpec(double x, double y, double w, double h, SpikeDir dir) {
        spikeSpecs.add(new double[]{ x, y, w, h, dir.ordinal() });
    }

    public double[] getBlockSpec()        { return blockSpec; }
    public void setBlockSpec(double x, double y, double w, double h) {
        this.blockSpec = new double[]{ x, y, w, h };
    }

    public int getDoorId()         { return doorId; }
    public double getDoorTargetX() { return doorTargetX; }
    public double getDoorTargetY() { return doorTargetY; }
    public void setDoorTarget(int doorId, double x, double y) {
        this.doorId = doorId; this.doorTargetX = x; this.doorTargetY = y;
    }
}
