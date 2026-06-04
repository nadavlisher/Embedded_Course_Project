package ai.ui;

/**
 * Lightweight view data object for drawing a spike.
 * The UI receives this from LevelDevilUiPortImpl and draws it in DrawingPanel.
 */
public class SpikeView {
    public final int x;
    public final int y;
    public final int width;
    public final int height;
    public final boolean visible;
    public final boolean dangerous;

    public SpikeView(int x, int y, int width, int height, boolean visible, boolean dangerous) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.visible = visible;
        this.dangerous = dangerous;
    }
}
