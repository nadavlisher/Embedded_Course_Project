package team.model;

public class LevelProgress {
    private static final int TOTAL_LEVELS = 6;

    private final boolean[] completed = new boolean[TOTAL_LEVELS + 1];
    private int highestUnlockedLevel = 1;

    public boolean isUnlocked(int levelNumber) {
        return isValidLevel(levelNumber) && levelNumber <= highestUnlockedLevel;
    }

    public boolean isCompleted(int levelNumber) {
        return isValidLevel(levelNumber) && completed[levelNumber];
    }

    public void markCompleted(int levelNumber) {
        if (!isValidLevel(levelNumber)) {
            return;
        }

        completed[levelNumber] = true;
        if (levelNumber == highestUnlockedLevel && highestUnlockedLevel < TOTAL_LEVELS) {
            highestUnlockedLevel++;
        }
    }

    public LevelState getLevelState(int levelNumber) {
        if (isCompleted(levelNumber)) {
            return LevelState.COMPLETED;
        }

        if (isUnlocked(levelNumber)) {
            return LevelState.OPEN;
        }

        return LevelState.LOCKED;
    }

    public LevelState[] getLevelStates() {
        LevelState[] states = new LevelState[TOTAL_LEVELS];
        for (int i = 1; i <= TOTAL_LEVELS; i++) {
            states[i - 1] = getLevelState(i);
        }
        return states;
    }

    public int getHighestUnlockedLevel() {
        return highestUnlockedLevel;
    }

    public int getTotalLevels() {
        return TOTAL_LEVELS;
    }

    private boolean isValidLevel(int levelNumber) {
        return levelNumber >= 1 && levelNumber <= TOTAL_LEVELS;
    }
}
