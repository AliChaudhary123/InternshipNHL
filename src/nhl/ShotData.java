package nhl;

public class ShotData {
    private double x;
    private double y;
    private String shooter;
    private double xGoal;

    public ShotData(double x, double y, String shooter, double xGoal) {
        this.x = x;
        this.y = y;
        this.shooter = shooter;
        this.xGoal = xGoal;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public String getShooter() { return shooter; }
    public double getXGoal() { return xGoal; }
}