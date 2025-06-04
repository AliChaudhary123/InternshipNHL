package nhl;

import java.util.*;

public class LineupGenerator {

    public static List<Player> getBestDefensiveLineup(Team team, String targetPlayer) {
        List<Player> defenseLineup = new ArrayList<>();
        List<Player> sorted = new ArrayList<>(team.getRoster());

        // Sort by defensive effectiveness (low xGA, high hits, takeaways)
        sorted.sort((a, b) -> {
            double aScore = a.getExpectedGoalsAgainst() - 0.01 * a.getHits() - 0.02 * a.getTakeaways();
            double bScore = b.getExpectedGoalsAgainst() - 0.01 * b.getHits() - 0.02 * b.getTakeaways();
            return Double.compare(aScore, bScore);
        });

        Map<String, Player> selectedForwards = new HashMap<>();
        int defensemenSelected = 0;

        for (Player p : sorted) {
            if (p.getPosition().equalsIgnoreCase("G")) continue;

            String pos = p.getPosition().toUpperCase().trim();

            if (pos.equals("D")) {
                // Pick two defensemen
                if (defensemenSelected < 2) {
                    defenseLineup.add(p);
                    defensemenSelected++;
                }
            } else if ((pos.equals("L") || pos.equals("C") || pos.equals("R")) && !selectedForwards.containsKey(pos)) {
                selectedForwards.put(pos, p);
            }

            // Stop once we have 2 defensemen and 3 forwards (L,C,R)
            if (defensemenSelected == 2 && selectedForwards.size() == 3) {
                break;
            }
        }

        // Add forwards in order: LW, C, RW
        for (String pos : Arrays.asList("L", "C", "R")) {
            if (selectedForwards.containsKey(pos)) {
                defenseLineup.add(selectedForwards.get(pos));
            }
        }

        return defenseLineup;
    }
}