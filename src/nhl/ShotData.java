package nhl;

public class ShotData {
    private int x; // horizontal coordinate
    private int y; // vertical coordinate

    public ShotData(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}