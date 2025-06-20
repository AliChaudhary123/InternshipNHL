package nhl;

public class Player {
    private String name;
    private String position; // F, D, G
    private double expectedGoalsAgainst;
    private int hits;
    private int takeaways;
    private int goals;
    private int points;
    private int blockedShots;
    private int shotAttemptsAgainst;
    private int dZoneStarts;
    private int giveaways;
    private int oZoneStarts;
    private int nZoneStarts;
    private double iceTime; // in minutes
    private int shifts;
    private int timeOnBench; // in seconds
    private int penalties;
    private int penaltyMinutes;
    private double highDangerxGoals;
    private int reboundGoals;
    private int gamesPlayed;

    // ✅ New field
    private double takeawayEfficiencyScore;

    // Constructor
    public Player(String name, String position, double expectedGoalsAgainst, int hits, int takeaways,
                  int goals, int points, int blockedShots, int shotAttemptsAgainst, int dZoneStarts,
                  int giveaways, int oZoneStarts, int nZoneStarts, double iceTime, int shifts,
                  int timeOnBench, int penalties, int penaltyMinutes, double highDangerxGoals,
                  int reboundGoals, int gamesPlayed) {
        this.name = name;
        this.position = position;
        this.expectedGoalsAgainst = expectedGoalsAgainst;
        this.hits = hits;
        this.takeaways = takeaways;
        this.goals = goals;
        this.points = points;
        this.blockedShots = blockedShots;
        this.shotAttemptsAgainst = shotAttemptsAgainst;
        this.dZoneStarts = dZoneStarts;
        this.giveaways = giveaways;
        this.oZoneStarts = oZoneStarts;
        this.nZoneStarts = nZoneStarts;
        this.iceTime = iceTime;
        this.shifts = shifts;
        this.timeOnBench = timeOnBench;
        this.penalties = penalties;
        this.penaltyMinutes = penaltyMinutes;
        this.highDangerxGoals = highDangerxGoals;
        this.reboundGoals = reboundGoals;
        this.gamesPlayed = gamesPlayed;
    }

    // Getters
    public String getName() { return name; }
    public String getPosition() { return position; }
    public double getExpectedGoalsAgainst() { return expectedGoalsAgainst; }
    public int getHits() { return hits; }
    public int getTakeaways() { return takeaways; }
    public int getGoals() { return goals; }
    public int getPoints() { return points; }
    public int getBlockedShots() { return blockedShots; }
    public int getShotAttemptsAgainst() { return shotAttemptsAgainst; }
    public int getDZoneStarts() { return dZoneStarts; }
    public int getGiveaways() { return giveaways; }
    public int getOZoneStarts() { return oZoneStarts; }
    public int getNZoneStarts() { return nZoneStarts; }
    public double getIceTime() { return iceTime; }
    public int getShifts() { return shifts; }
    public int getTimeOnBench() { return timeOnBench; }
    public int getPenalties() { return penalties; }
    public int getPenaltyMinutes() { return penaltyMinutes; }
    public double getHighDangerxGoals() { return highDangerxGoals; }
    public int getReboundGoals() { return reboundGoals; }
    public int getGamesPlayed() { return gamesPlayed; }

    // ✅ Takeaway efficiency score
    public double getTakeawayEfficiencyScore() { return takeawayEfficiencyScore; }
    public void setTakeawayEfficiencyScore(double score) { this.takeawayEfficiencyScore = score; }
}