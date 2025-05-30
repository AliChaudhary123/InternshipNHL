package nhl;

import java.util.List;

public class Team {
    private String name;
    private List<Player> roster;

    public Team(String name, List<Player> roster) {
        this.name = name;
        this.roster = roster;
    }

    public String getName() {
        return name;
    }

    public List<Player> getRoster() {
        return roster;
    }
}