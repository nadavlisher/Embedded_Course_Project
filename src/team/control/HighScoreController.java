package team.control;

import db.ExcelDB;
import db.ExcelTable;
import team.model.HighScore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Arcade high-score table, persisted through the instructor's Excel
 * infrastructure ({@link db.ExcelDB} / {@link db.ExcelTable}).
 *
 * The table {@code HighScores} has columns [seq, name, level]. {@code seq} is a
 * unique increasing arrival number used as the primary key and as the
 * tie-breaker: ranking is by level reached (high to low), and for equal levels
 * the smaller seq (who arrived first) ranks above.
 *
 * All Excel access is defensive: if the spreadsheet cannot be created or
 * written, the game keeps running (scores just are not persisted).
 */
public class HighScoreController {

    private static final String TABLE = "HighScores";
    private static final String[] HEADINGS = { "seq", "name", "level" };
    private static final int MAX_NAME = 12;

    private ExcelTable table;
    private int nextSeq = 1;
    private int lastSeq = -1;
    private final StringBuilder nameBuf = new StringBuilder();

    public HighScoreController() {
        try {
            new File("db_tables").mkdirs();
            ExcelDB db = ExcelDB.getInstance();
            // ExcelTable stores the file at "db_tables\<name>.xlsx" - mirror that here.
            File file = new File("db_tables" + File.separator + TABLE + ".xlsx");
            File altFile = new File("db_tables\\" + TABLE + ".xlsx");
            if (file.exists() || altFile.exists()) {
                table = db.createTableFromExcel(TABLE);
                for (HighScore hs : all()) nextSeq = Math.max(nextSeq, hs.getSeq() + 1);
            } else {
                table = db.createNewTable(TABLE, HEADINGS);
            }
        } catch (Exception e) {
            System.out.println("[HighScore] Excel unavailable, scores will not persist: " + e.getMessage());
            table = null;
        }
    }

    // ---- name-entry buffer ----
    public void beginEntry()        { nameBuf.setLength(0); }
    public void typeChar(char c)    { if (nameBuf.length() < MAX_NAME && (Character.isLetterOrDigit(c) || c == ' ')) nameBuf.append(c); }
    public void backspace()         { if (nameBuf.length() > 0) nameBuf.deleteCharAt(nameBuf.length() - 1); }
    public String currentName()     { return nameBuf.toString(); }

    /** Record a finished game; returns the new entry's seq (or -1 on failure). */
    public int add(String name, int level) {
        int seq = nextSeq++;
        if (table != null) {
            try {
                table.insertRow(new String[]{ String.valueOf(seq), name, String.valueOf(level) });
                ExcelDB.getInstance().commit();
            } catch (Exception e) {
                System.out.println("[HighScore] could not save: " + e.getMessage());
            }
        }
        lastSeq = seq;
        return seq;
    }

    public int lastSeq() { return lastSeq; }

    public List<HighScore> all() {
        List<HighScore> out = new ArrayList<>();
        if (table == null) return out;
        for (String[] row : table.getTableAsMatrix()) {
            if (row == null || row.length < 3 || row[0] == null) continue;
            try {
                out.add(new HighScore(Integer.parseInt(row[0].trim()), row[1], Integer.parseInt(row[2].trim())));
            } catch (Exception ignored) { }
        }
        return out;
    }

    /** Sorted best-first: level desc, then arrival (seq) asc. */
    public List<HighScore> top(int n) {
        List<HighScore> list = all();
        list.sort((a, b) -> a.getLevel() != b.getLevel()
                ? Integer.compare(b.getLevel(), a.getLevel())
                : Integer.compare(a.getSeq(), b.getSeq()));
        return list.size() <= n ? list : new ArrayList<>(list.subList(0, n));
    }

    /** Rows for the UI: {rank, name, level, seq}. */
    public String[][] topRows(int n) {
        List<HighScore> t = top(n);
        String[][] rows = new String[t.size()][4];
        for (int i = 0; i < t.size(); i++) {
            HighScore h = t.get(i);
            rows[i] = new String[]{ String.valueOf(i + 1), h.getName(), String.valueOf(h.getLevel()), String.valueOf(h.getSeq()) };
        }
        return rows;
    }
}
