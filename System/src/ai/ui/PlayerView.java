package ai.ui;

public class PlayerView {
    public int x;
    public int y;
    public int width;
    public int height;
    public double vx;
    public double vy;
    public boolean onGround;

    public PlayerView(int x, int y, int width, int height, double vx, double vy, boolean onGround) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.vx = vx;
        this.vy = vy;
        this.onGround = onGround;
    }
}