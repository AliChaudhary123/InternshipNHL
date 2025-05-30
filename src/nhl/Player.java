package nhl;

public class Player {
    private String name;
    private String position; // F, D, G
    private double expectedGoalsAgainst;
    private int hits;
    private int takeaways;
    private int goals;
    private int points;

    public Player(String name, String position, double xGA, int hits, int takeaways, int goals, int points) {
        this.name = name;
        this.position = position;
        this.expectedGoalsAgainst = xGA;
        this.hits = hits;
        this.takeaways = takeaways;
        this.goals = goals;
        this.points = points;
    }

    public String getName() { return name; }
    public String getPosition() { return position; }
    public double getExpectedGoalsAgainst() { return expectedGoalsAgainst; }
    public int getHits() { return hits; }
    public int getTakeaways() { return takeaways; }
    public int getGoals() { return goals; }
    public int getPoints() { return points; }
}