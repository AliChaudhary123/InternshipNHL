package nhl;

/**
 * Represents a shot attempt in a hockey game, including location, shooter, and expected goal value.
 */
public class ShotData {
    private double x;
    private double y;
    private String shooter;
    private double xGoal;

    /**
     * Constructs a new {@code ShotData} instance with coordinates, shooter name, and xG value.
     *
     * @param x       The x-coordinate of the shot on the rink (e.g., 0–200 scale).
     * @param y       The y-coordinate of the shot on the rink (e.g., 0–85 scale).
     * @param shooter The name of the player who took the shot.
     * @param xGoal   The expected goals value (xG) for the shot (range 0.0 to 1.0).
     */
    public ShotData(double x, double y, String shooter, double xGoal) {
        this.x = x;
        this.y = y;
        this.shooter = shooter;
        this.xGoal = xGoal;
    }

    /**
     * Gets the x-coordinate of the shot.
     *
     * @return the x-coordinate.
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of the shot.
     *
     * @return the y-coordinate.
     */
    public double getY() {
        return y;
    }

    /**
     * Gets the name of the shooter.
     *
     * @return the shooter's name.
     */
    public String getShooter() {
        return shooter;
    }

    /**
     * Gets the expected goals (xG) value of the shot.
     *
     * @return the xG value.
     */
    public double getXGoal() {
        return xGoal;
    }
}