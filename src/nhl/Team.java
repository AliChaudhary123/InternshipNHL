package nhl;

import java.util.List;

/**
 * Represents an NHL team containing a name and a list of player objects as its roster.
 */
public class Team {
    private String name;
    private List<Player> roster;

    /**
     * Constructs a new {@code Team} instance with the specified team name and player roster.
     *
     * @param name   The name of the team (e.g., "Calgary Flames").
     * @param roster A list of {@link Player} objects representing the team’s roster.
     */
    public Team(String name, List<Player> roster) {
        this.name = name;
        this.roster = roster;
    }

    /**
     * Gets the name of the team.
     *
     * @return The team name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the roster of players on the team.
     *
     * @return A list of {@link Player} objects representing the roster.
     */
    public List<Player> getRoster() {
        return roster;
    }
}