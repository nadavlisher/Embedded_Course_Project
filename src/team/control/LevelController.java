package team.control;

import team.model.Arena;
import team.model.Door;
import team.model.GameState;
import team.model.GameStatus;
import team.model.HitBounds;
import team.model.IdGenerator;
import team.model.Level;
import team.model.Mover;
import team.model.MoverKind;
import team.model.Player;
import team.model.Spike;
import team.model.SpikeDir;
import team.model.Tile;
import team.model.TileKind;
import team.model.Trap;
import team.model.TrapKind;
import shared.ui_ports.LevelDevilUiPort;

/**
 * Builds and runs the three levels (classic troll progression):
 *   1. First Steps  - run + jump, with one mild disappearing-floor troll.
 *   2. Trust Issues - disappearing floors and pop-up spikes.
 *   3. Devil's Final - a moving platform over a spike pit, a fake/moving door
 *                      and falling blocks.
 *
 * Each level is authored on a 48 px grid (20 x 12 cells). The whole layout is
 * rebuilt from scratch on every load, so dying and respawning fully re-arms
 * the traps - exactly like the Breakout LevelController rebuilt its brick grid.
 */
public class LevelController {

    public static final int TOTAL_LEVELS = 6;
    public static final int TILE = 48;
    private static final int GROUND_ROW = 11; // y = 528..576

    private static final String[] NAMES = { "First Steps", "Trust Issues", "Devil's Final",
                                            "Stairway to Hell", "Hall of Lies", "Grand Finale" };

    private final GameState gameState;
    private final Arena arena;
    private final IdGenerator ids;
    private final PlayerController playerController;
    private final AudioController audio;

    public LevelController(GameState gameState, Arena arena, IdGenerator ids,
                           PlayerController playerController, AudioController audio) {
        this.gameState = gameState;
        this.arena = arena;
        this.ids = ids;
        this.playerController = playerController;
        this.audio = audio;
    }

    private LevelDevilUiPort ui() {
        return LevelDevilUiPort.getInstance();
    }

    public void loadLevel(int number, boolean announce) {
        int n = Math.max(1, Math.min(TOTAL_LEVELS, number));
        gameState.setLevelNumber(n);
        gameState.setCurrentLevel(new Level(n, NAMES[n - 1]));

        arena.clearDynamic();
        ui().clearLevel();

        switch (n) {
            case 1: buildLevel1(); break;
            case 2: buildLevel2(); break;
            case 3: buildLevel3(); break;
            case 4: buildLevel4(); break;
            case 5: buildLevel5(); break;
            case 6: buildLevel6(); break;
        }

        // Place the player at the spawn point and clear momentum/intents.
        Player p = arena.getPlayer();
        p.setPosition(arena.getSpawnX(), arena.getSpawnY());
        p.setVx(0);
        p.setVy(0);
        p.setOnGround(false);
        p.setFacing(1);
        playerController.resetIntents();

        drawWorld();
        ui().updateLevel(n);
        if (announce) {
            ui().showLevelStart(n, NAMES[n - 1]);
        }
        ui().updateStatusText("Reach the door    ←→ / A D move    SPACE jump");
    }

    public void respawn() {
        loadLevel(gameState.getLevelNumber(), false);
    }

    public void advanceToNextLevel() {
        int current = gameState.getLevelNumber();
        int next = current + 1;
        if (next > TOTAL_LEVELS) {
            // The backend turns VICTORY into the name-entry / high-score flow.
            gameState.changeStateTo(GameStatus.VICTORY);
            audio.playOnVictory();
            return;
        }
        audio.playOnLevelComplete();
        gameState.changeStateTo(GameStatus.LEVEL_TRANSITION);
        ui().showLevelComplete(current);
        loadLevel(next, false);
        gameState.changeStateTo(GameStatus.PLAYING);
    }

    private void drawWorld() {
        for (Tile t : arena.tiles())
            ui().drawTile(t.getId(), t.getX(), t.getY(), t.getWidth(), t.getHeight(), t.getKind().name());
        for (Spike s : arena.spikes())
            ui().drawSpike(s.getId(), s.getX(), s.getY(), s.getWidth(), s.getHeight(), s.getDir().name());
        for (Mover m : arena.movers())
            ui().drawMover(m.getId(), m.getX(), m.getY(), m.getWidth(), m.getHeight(), m.getKind().name());
        for (Door d : arena.doors())
            ui().drawDoor(d.getId(), d.getX(), d.getY(), d.getWidth(), d.getHeight(), d.isReal());
        Player p = arena.getPlayer();
        ui().setPlayer(p.getX(), p.getY(), p.getWidth(), p.getHeight(), p.getFacing());
    }

    // ============================== LEVEL 1 ==============================
    // Teach run + jump (a 2-wide spike pit) then one mild, telegraphed
    // disappearing-floor troll right before the exit.
    private void buildLevel1() {
        setSpawnOnGround(1);

        floor(GROUND_ROW, 0, 6, TileKind.SOLID);     // start ground
        spikeUp(7); spikeUp(8);                       // jump this 2-wide spike pit
        floor(GROUND_ROW, 9, 13, TileKind.SOLID);
        int b1 = tile(14, GROUND_ROW, TileKind.BRITTLE);
        int b2 = tile(15, GROUND_ROW, TileKind.BRITTLE);
        floor(GROUND_ROW, 16, 19, TileKind.SOLID);

        doorOnGround(18, true);

        // Mild troll: as you near the cracked tiles they crumble, leaving a gap.
        Trap t = new Trap(ids.next(), TrapKind.DISAPPEARING_FLOOR,
                triggerBand(12, 12), 0.12, "Gotcha!");
        t.setTileIds(new int[]{ b1, b2 });
        arena.addTrap(t);
    }

    // ============================== LEVEL 2 ==============================
    // The floor cannot be trusted: pop-up spikes (jump them) and disappearing
    // floors (they open as a gap just ahead). Everything is spaced so each
    // troll has a safe landing - hard, but fair once you have learned it.
    private void buildLevel2() {
        setSpawnOnGround(1);

        floor(GROUND_ROW, 0, 4, TileKind.SOLID);      // start
        floor(GROUND_ROW, 5, 6, TileKind.SOLID);      // pop-up spikes sit on top here
        floor(GROUND_ROW, 7, 10, TileKind.SOLID);
        int d1 = tile(11, GROUND_ROW, TileKind.BRITTLE);
        int d2 = tile(12, GROUND_ROW, TileKind.BRITTLE);
        floor(GROUND_ROW, 13, 15, TileKind.SOLID);
        floor(GROUND_ROW, 16, 17, TileKind.SOLID);    // pop-up spikes sit on top here
        floor(GROUND_ROW, 18, 19, TileKind.SOLID);

        doorOnGround(19, true);

        // Pop-up spikes on c5..c6 (one row above the floor so they hit you).
        Trap s1 = new Trap(ids.next(), TrapKind.POPUP_SPIKE, triggerBand(3, 4), 0.22, "Surprise!");
        s1.addSpikeSpec(5 * TILE, (GROUND_ROW - 1) * TILE, TILE, TILE, SpikeDir.UP);
        arena.addTrap(s1);

        // Disappearing floor c11..c12: short fuse + earlier trigger so it opens
        // as a gap ahead of you rather than vanishing under your feet.
        Trap f1 = new Trap(ids.next(), TrapKind.DISAPPEARING_FLOOR, triggerBand(9, 9), 0.12, "Bye floor!");
        f1.setTileIds(new int[]{ d1, d2 });
        arena.addTrap(f1);

        // Pop-up spikes on c16..c17.
        Trap s2 = new Trap(ids.next(), TrapKind.POPUP_SPIKE, triggerBand(14, 15), 0.22, "Again!");
        s2.addSpikeSpec(16 * TILE, (GROUND_ROW - 1) * TILE, TILE, TILE, SpikeDir.UP);
        arena.addTrap(s2);
    }

    // ============================== LEVEL 3 ==============================
    // A moving platform across a spike pit, a fake door that slides away, and
    // falling blocks on the home stretch.
    private void buildLevel3() {
        setSpawnOnGround(1);

        floor(GROUND_ROW, 0, 3, TileKind.SOLID);     // start ground
        for (int c = 4; c <= 12; c++) spikeUp(c);    // spike pit
        floor(GROUND_ROW, 13, 19, TileKind.SOLID);   // landing + home stretch

        // Moving platform over the pit (row 9, 2 tiles wide).
        Mover plat = new Mover(ids.next(), MoverKind.PLATFORM_H, 4 * TILE, 9 * TILE, 2 * TILE, 24);
        plat.configurePatrol(4 * TILE, 11 * TILE, 115, 1);
        arena.addMover(plat);

        // Fake door right where you land - it slides away when you approach.
        Door fake = new Door(ids.next(), 13 * TILE + 4, GROUND_ROW * TILE - 64, 40, 64, false);
        arena.addDoor(fake);
        Trap moveDoor = new Trap(ids.next(), TrapKind.MOVE_DOOR, triggerBand(12, 13), 0.05, "Not this one!");
        moveDoor.setDoorTarget(fake.getId(), 10 * TILE, 6 * TILE); // slide up over the pit
        arena.addTrap(moveDoor);

        // Real exit far right.
        doorOnGround(19, true);

        // Falling blocks on the home stretch (dodge by keeping moving).
        Trap fb1 = new Trap(ids.next(), TrapKind.FALLING_BLOCK, triggerBand(14, 14), 0.05, "Heads up!");
        fb1.setBlockSpec(15 * TILE, 2 * TILE, TILE, TILE);
        arena.addTrap(fb1);

        Trap fb2 = new Trap(ids.next(), TrapKind.FALLING_BLOCK, triggerBand(16, 16), 0.05, "And again!");
        fb2.setBlockSpec(17 * TILE, 2 * TILE, TILE, TILE);
        arena.addTrap(fb2);
    }

    // ============================== LEVEL 4 ==============================
    // Stairway to Hell: climb a staircase, then a flat high corridor with a
    // spiked ceiling (never jump here!) and a crumbling bridge over a pit.
    private void buildLevel4() {
        setSpawnOnGround(1);
        floor(GROUND_ROW, 0, 2, TileKind.SOLID);          // start ground

        // staircase up to the high path (3-wide steps, +1 row each)
        floor(10, 3, 5, TileKind.SOLID);
        floor(9,  6, 8, TileKind.SOLID);
        floor(8,  9, 11, TileKind.SOLID);

        // flat high corridor with a spiked ceiling above it
        floor(8, 11, 15, TileKind.SOLID);
        ceilingSpikes(6, 13, 14);

        // crumbling bridge over a spike pit
        for (int c = 16; c <= 17; c++) spikeUp(c);         // pit floor
        crumbleBridge(8, 16, 17, 0.35);

        floor(8, 18, 19, TileKind.SOLID);                  // landing
        doorOnTopRow(19, 8, true);
    }

    // ============================== LEVEL 5 ==============================
    // Hall of Lies: decoy doors everywhere (one even runs away), pop-up spikes,
    // a spike from the sky, and a wall-spiked pit. The real exit is the last door.
    private void buildLevel5() {
        setSpawnOnGround(1);
        floor(GROUND_ROW, 0, 12, TileKind.SOLID);

        doorOnGround(3, false);                            // decoy #1
        doorOnGround(7, false);                            // decoy #2
        Door runaway = doorOnGround(11, false);            // decoy #3 - slides away

        // pop-up spike #1 on c5
        Trap s1 = new Trap(ids.next(), TrapKind.POPUP_SPIKE, triggerBand(3, 4), 0.22, "Surprise!");
        s1.addSpikeSpec(5 * TILE, (GROUND_ROW - 1) * TILE, TILE, TILE, SpikeDir.UP);
        arena.addTrap(s1);

        // spike from the sky around c10
        fallingSpike(8, 8, 0.05, 10, 1, "Liar!");

        // the decoy at c11 slides up and away when you approach
        Trap mv = new Trap(ids.next(), TrapKind.MOVE_DOOR, triggerBand(9, 10), 0.05, "Two-faced!");
        mv.setDoorTarget(runaway.getId(), 9 * TILE, 6 * TILE);
        arena.addTrap(mv);

        // spike pit on c13..c14 (jump it)
        for (int c = 13; c <= 14; c++) spikeUp(c);

        floor(GROUND_ROW, 15, 19, TileKind.SOLID);

        // pop-up spike #2 on c17
        Trap s2 = new Trap(ids.next(), TrapKind.POPUP_SPIKE, triggerBand(15, 16), 0.22, "Told you!");
        s2.addSpikeSpec(17 * TILE, (GROUND_ROW - 1) * TILE, TILE, TILE, SpikeDir.UP);
        arena.addTrap(s2);

        doorOnGround(19, true);                            // the real exit
    }

    // ============================== LEVEL 6 ==============================
    // Grand Finale: two moving platforms over spike pits, a spike from the sky,
    // and the real door runs away on the home stretch - you must chase it.
    private void buildLevel6() {
        setSpawnOnGround(1);
        floor(GROUND_ROW, 0, 3, TileKind.SOLID);

        for (int c = 4; c <= 8; c++) spikeUp(c);           // pit #1
        platformH(4, 7, 9, 2, 115);                        // ride across pit #1

        floor(GROUND_ROW, 9, 10, TileKind.SOLID);          // landing

        for (int c = 11; c <= 15; c++) spikeUp(c);         // pit #2
        platformH(11, 14, 9, 2, 120);                      // ride across pit #2

        floor(GROUND_ROW, 16, 19, TileKind.SOLID);         // home stretch

        // the real door starts at c17 and runs away to c19 when you get close
        Door real = doorOnGround(17, true);
        Trap run = new Trap(ids.next(), TrapKind.MOVE_DOOR, triggerBand(15, 16), 0.05, "Catch me!");
        double tx = 19 * TILE + (TILE - real.getWidth()) / 2.0;
        double ty = GROUND_ROW * TILE - real.getHeight();
        run.setDoorTarget(real.getId(), tx, ty);
        arena.addTrap(run);
    }

    // ============================== helpers ==============================
    private void setSpawnOnGround(int col) {
        double h = arena.getPlayer().getHeight();
        arena.setSpawn(col * TILE, GROUND_ROW * TILE - h);
    }

    private int tile(int col, int row, TileKind kind) {
        int id = ids.next();
        arena.addTile(new Tile(id, col * TILE, row * TILE, TILE, TILE, kind));
        return id;
    }

    private void floor(int row, int cFrom, int cTo, TileKind kind) {
        for (int c = cFrom; c <= cTo; c++) tile(c, row, kind);
    }

    private void spikeUp(int col) {
        arena.addSpike(new Spike(ids.next(), col * TILE, GROUND_ROW * TILE, TILE, TILE, SpikeDir.UP));
    }

    private Door doorOnGround(int col, boolean real) {
        return doorOnTopRow(col, GROUND_ROW, real);
    }

    private Door doorOnTopRow(int col, int standRow, boolean real) {
        double w = 40, h = 64;
        double x = col * TILE + (TILE - w) / 2.0;
        double y = standRow * TILE - h;
        Door d = new Door(ids.next(), x, y, w, h, real);
        arena.addDoor(d);
        return d;
    }

    private void spike(int col, int row, SpikeDir dir) {
        arena.addSpike(new Spike(ids.next(), col * TILE, row * TILE, TILE, TILE, dir));
    }

    private void ceilingSpikes(int row, int cFrom, int cTo) {
        for (int c = cFrom; c <= cTo; c++) spike(c, row, SpikeDir.DOWN);
    }

    /** Brittle tiles that each crumble a beat after you step on that column. */
    private void crumbleBridge(int row, int cFrom, int cTo, double fuse) {
        for (int c = cFrom; c <= cTo; c++) {
            int id = tile(c, row, TileKind.BRITTLE);
            Trap t = new Trap(ids.next(), TrapKind.DISAPPEARING_FLOOR, triggerBand(c, c), fuse, null);
            t.setTileIds(new int[]{ id });
            arena.addTrap(t);
        }
    }

    private Mover platformH(int cMin, int cMax, int row, int widthTiles, double speed) {
        Mover m = new Mover(ids.next(), MoverKind.PLATFORM_H, cMin * TILE, row * TILE, widthTiles * TILE, 24);
        m.configurePatrol(cMin * TILE, cMax * TILE, speed, 1);
        arena.addMover(m);
        return m;
    }

    private void fallingSpike(int trigLo, int trigHi, double fuse, int bCol, int bRow, String taunt) {
        Trap t = new Trap(ids.next(), TrapKind.FALLING_SPIKE, triggerBand(trigLo, trigHi), fuse, taunt);
        t.setBlockSpec(bCol * TILE, bRow * TILE, TILE, TILE);
        arena.addTrap(t);
    }

    /** A full-height trigger band spanning the given columns. */
    private HitBounds triggerBand(int cFrom, int cTo) {
        return new HitBounds(cFrom * TILE, 0, (cTo - cFrom + 1) * TILE, arena.getHeight());
    }
}
