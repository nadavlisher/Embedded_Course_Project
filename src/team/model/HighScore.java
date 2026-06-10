package team.model;

/**
 * One entry in the arcade high-score table.
 *
 * {@code seq} is a unique, monotonically increasing arrival number: it both
 * identifies the row (primary key in the Excel table) and breaks ties between
 * players who reached the same level - the smaller seq arrived first and ranks
 * higher.
 */
public class HighScore {

    private final int seq;
    private final String name;
    private final int level;

    public HighScore(int seq, String name, int level) {
        this.seq = seq;
        this.name = name;
        this.level = level;
    }

    public int getSeq()    { return seq; }
    public String getName(){ return name; }
    public int getLevel()  { return level; }
}
