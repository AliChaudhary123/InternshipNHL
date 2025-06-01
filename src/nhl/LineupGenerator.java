package nhl;

import java.util.*;

public class LineupGenerator {

    public static List<Player> getBestDefensiveLineup(Team team, String targetPlayer) {
        List<Player> defenseLineup = new ArrayList<>();

        // Copy roster to avoid modifying original list
        List<Player> sorted = new ArrayList<>(team.getRoster());

        // Sort players by defensive effectiveness:
        // Lower xGA is better, higher hits and takeaways are better
        sorted.sort((a, b) -> {
            double aScore = a.getExpectedGoalsAgainst() - 0.01 * a.getHits() - 0.02 * a.getTakeaways();
            double bScore = b.getExpectedGoalsAgainst() - 0.01 * b.getHits() - 0.02 * b.getTakeaways();
            return Double.compare(aScore, bScore);
        });

        // Pick top 6 skaters (non-goalies)
        for (Player p : sorted) {
            if (!p.getPosition().equalsIgnoreCase("G")) {
                defenseLineup.add(p);
            }
            if (defenseLineup.size() == 6) {
                break;
            }
        }

        return defenseLineup;
    }
}