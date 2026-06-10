package ai.ui;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Tiny synthesizer that plays a distinct short tone per semantic sound name
 * (JUMP, TRAP, DEATH, DOOR, LEVEL_COMPLETE, VICTORY, GAME_OVER, MENU). Each
 * event gets its own little PCM buffer, played on a background daemon thread.
 *
 * Audio failures are swallowed on purpose - sound is cosmetic and must never
 * crash the game (e.g. on a headless machine).
 */
public class SoundPlayer {

    private static final float SAMPLE_RATE = 22050f;
    private static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);

    private final Map<String, byte[]> cache = new HashMap<>();
    private final ExecutorService pool;
    private boolean enabled = true;

    public SoundPlayer() {
        ThreadFactory tf = r -> { Thread t = new Thread(r, "LevelDevilSound"); t.setDaemon(true); return t; };
        pool = Executors.newSingleThreadExecutor(tf);
        precache();
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public void play(String soundName) {
        if (!enabled) return;
        byte[] data = cache.getOrDefault(soundName, cache.get("DEFAULT"));
        if (data == null) return;
        final byte[] payload = data;
        pool.submit(() -> writeToLine(payload));
    }

    private void writeToLine(byte[] data) {
        SourceDataLine line = null;
        try {
            line = AudioSystem.getSourceDataLine(FORMAT);
            line.open(FORMAT, Math.max(4096, data.length));
            line.start();
            line.write(data, 0, data.length);
            line.drain();
        } catch (LineUnavailableException | IllegalArgumentException ignored) {
        } catch (Throwable t) {
        } finally {
            if (line != null) { try { line.stop(); line.close(); } catch (Throwable ignored) {} }
        }
    }

    private void precache() {
        cache.put("JUMP",           synthSweep(520, 780, 95, 0.40));
        cache.put("TRAP",           synthSquare(170, 150, 0.40));
        cache.put("DEATH",          synthSweep(420, 90, 360, 0.55));
        cache.put("DOOR",           synthArpeggio(new int[]{660, 880, 1175}, 80, 0.45));
        cache.put("LEVEL_COMPLETE", synthArpeggio(new int[]{523, 659, 784, 1046}, 110, 0.55));
        cache.put("VICTORY",        synthArpeggio(new int[]{523, 659, 784, 1046, 1318, 1568}, 120, 0.6));
        cache.put("GAME_OVER",      synthChordSequence(new int[][]{{220,330},{196,294},{165,247},{147,196}}, 230, 0.55));
        cache.put("MENU",           synthSine(440, 120, 0.30));
        cache.put("DEFAULT",        synthSine(440, 60, 0.30));
    }

    private interface SampleSrc { double at(int t, int totalSamples); }

    private static byte[] synthSine(double hz, int durMs, double amp) {
        return synth((t, n) -> Math.sin(2 * Math.PI * hz * t / SAMPLE_RATE), durMs, amp);
    }
    private static byte[] synthSquare(double hz, int durMs, double amp) {
        return synth((t, n) -> Math.signum(Math.sin(2 * Math.PI * hz * t / SAMPLE_RATE)), durMs, amp);
    }
    private static byte[] synthSweep(double startHz, double endHz, int durMs, double amp) {
        return synth((t, n) -> {
            double k = (double) t / n;
            double f = startHz * (1 - k) + endHz * k;
            return Math.sin(2 * Math.PI * f * t / SAMPLE_RATE);
        }, durMs, amp);
    }
    private static byte[] synthArpeggio(int[] notes, int perNoteMs, double amp) {
        int total = (int) (SAMPLE_RATE * notes.length * perNoteMs / 1000.0);
        byte[] buf = new byte[total * 2];
        int idx = 0, perNote = total / notes.length;
        for (int note : notes) {
            for (int t = 0; t < perNote; t++) {
                double v = Math.sin(2 * Math.PI * note * t / SAMPLE_RATE) * envelope(t, perNote);
                idx = writeSample(buf, idx, v * amp);
            }
        }
        return buf;
    }
    private static byte[] synthChordSequence(int[][] chords, int perChordMs, double amp) {
        int total = (int) (SAMPLE_RATE * chords.length * perChordMs / 1000.0);
        byte[] buf = new byte[total * 2];
        int idx = 0, perChord = total / chords.length;
        for (int[] freqs : chords) {
            for (int t = 0; t < perChord; t++) {
                double v = 0;
                for (int f : freqs) v += Math.sin(2 * Math.PI * f * t / SAMPLE_RATE);
                v = v / freqs.length * envelope(t, perChord);
                idx = writeSample(buf, idx, v * amp);
            }
        }
        return buf;
    }
    private static byte[] synth(SampleSrc src, int durMs, double amp) {
        int total = (int) (SAMPLE_RATE * durMs / 1000.0);
        byte[] buf = new byte[total * 2];
        int idx = 0;
        for (int t = 0; t < total; t++) idx = writeSample(buf, idx, src.at(t, total) * envelope(t, total) * amp);
        return buf;
    }
    private static int writeSample(byte[] buf, int idx, double v) {
        short s = (short) (Math.max(-1, Math.min(1, v)) * Short.MAX_VALUE);
        buf[idx++] = (byte) (s & 0xff);
        buf[idx++] = (byte) ((s >> 8) & 0xff);
        return idx;
    }
    private static double envelope(int t, int total) {
        int attack = Math.min(total / 10, 200);
        int release = total / 4;
        if (t < attack) return t / (double) attack;
        if (t > total - release) return (total - t) / (double) release;
        return 1.0;
    }
}
