package nhl;

import java.util.*;

public class LineupGenerator {

    public static List<Player> getBestDefensiveLineup(Team team, String targetPlayer) {
        List<Player> defenseLineup = new ArrayList<>();
        List<Player> sorted = new ArrayList<>(team.getRoster());

        // Sort players by best defensive stats to stop the player
        sorted.sort((a, b) -> {
            // Simple scoring: lower expectedGoalsAgainst preferred,
            // plus penalties for hits and takeaways (higher is better, so subtract)
            double aScore = a.getExpectedGoalsAgainst() - 0.01 * a.getHits() - 0.02 * a.getTakeaways();
            double bScore = b.getExpectedGoalsAgainst() - 0.01 * b.getHits() - 0.02 * b.getTakeaways();
            return Double.compare(aScore, bScore);
        });

        for (Player p : sorted) {
            if (defenseLineup.size() < 6 && !p.getPosition().equals("G")) { // pick max 6 skaters, no goalie
                defenseLineup.add(p);
            }
        }

        return defenseLineup;
    }
}