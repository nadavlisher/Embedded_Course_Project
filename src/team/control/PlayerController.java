package leveldevil.control;

import leveldevil.model.Arena;
import leveldevil.model.Player;

/**
 * Owns the player's intent and per-step kinematics: horizontal movement,
 * gravity and jumping. Position integration and collision are done by
 * {@link CollisionController}; this controller only writes the velocity.
 *
 * Two small "game feel" helpers make the precise platforming of a troll game
 * fair rather than frustrating:
 *   - coyote time: you may still jump for a few ms after walking off a ledge;
 *   - jump buffering: a jump pressed just before landing is remembered.
 */
public class PlayerController {

    public static final double GRAVITY      = 2000;  // px/s^2
    public static final double MOVE_SPEED    = 240;   // px/s
    public static final double JUMP_VELOCITY = -720;  // px/s (negative = up)
    public static final double MAX_FALL      = 980;   // px/s

    private static final double COYOTE_TIME  = 0.08;  // s
    private static final double JUMP_BUFFER  = 0.10;  // s

    private final Arena arena;
    private final AudioController audio;

    private int moveDir = 0;       // -1 / 0 / +1
    private double coyote = 0;
    private double jumpBuffer = 0;

    public PlayerController(Arena arena, AudioController audio) {
        this.arena = arena;
        this.audio = audio;
    }

    public void setMoveDir(int dir) { this.moveDir = Integer.signum(dir); }
    public int  getMoveDir()        { return moveDir; }

    public void requestJump()       { this.jumpBuffer = JUMP_BUFFER; }

    public void resetIntents() {
        moveDir = 0;
        jumpBuffer = 0;
        coyote = 0;
    }

    /** Set velocity from intent + gravity and apply a jump if one is allowed. */
    public void prePhysics(double dt) {
        Player p = arena.getPlayer();
        if (p == null) return;

        p.setVx(moveDir * MOVE_SPEED);

        if (p.isOnGround()) coyote = COYOTE_TIME;
        else                coyote = Math.max(0, coyote - dt);

        jumpBuffer = Math.max(0, jumpBuffer - dt);
        if (jumpBuffer > 0 && coyote > 0) {
            p.setVy(JUMP_VELOCITY);
            p.setOnGround(false);
            coyote = 0;
            jumpBuffer = 0;
            audio.playOnJump();
        }

        double vy = p.getVy() + GRAVITY * dt;
        if (vy > MAX_FALL) vy = MAX_FALL;
        p.setVy(vy);
    }
}
