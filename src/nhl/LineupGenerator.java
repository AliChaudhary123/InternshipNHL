package nhl;

import java.util.*;

public class LineupGenerator {

    public static List<Player> getBestDefensiveLineup(Team team, String targetPlayer) {
        List<Player> defenseLineup = new ArrayList<>();
        List<Player> sorted = new ArrayList<>(team.getRoster());

        // Find the target player object (case-insensitive match)
        Player target = null;
        for (Player p : sorted) {
            if (p.getName().equalsIgnoreCase(targetPlayer)) {
                target = p;
                break;
            }
        }

        if (target == null) {
            // If target player not found, just generate lineup normally
            return generateLineupWithoutTarget(sorted);
        }

        // Always add the target player first
        defenseLineup.add(target);

        // Remove target from sorted list so we don't add twice
        sorted.remove(target);

        // Sort remaining players by defensive effectiveness (low xGA, high hits, takeaways)
        sorted.sort((a, b) -> {
            double aScore = a.getExpectedGoalsAgainst() - 0.01 * a.getHits() - 0.02 * a.getTakeaways();
            double bScore = b.getExpectedGoalsAgainst() - 0.01 * b.getHits() - 0.02 * b.getTakeaways();
            return Double.compare(aScore, bScore);
        });

        // Track how many defensemen and forwards have been added, excluding the target player
        int defensemenSelected = target.getPosition().equalsIgnoreCase("D") ? 1 : 0;
        Set<String> selectedForwards = new HashSet<>();
        if (target.getPosition().equalsIgnoreCase("L") || target.getPosition().equalsIgnoreCase("C") || target.getPosition().equalsIgnoreCase("R")) {
            selectedForwards.add(target.getPosition().toUpperCase().trim());
        }

        // Fill rest of lineup until we have 2 defensemen and 3 forwards total (including target)
        for (Player p : sorted) {
            if (defenseLineup.size() >= 5) break;  // Full lineup

            String pos = p.getPosition().toUpperCase().trim();
            if (pos.equals("G")) continue; // Skip goalies

            if (pos.equals("D") && defensemenSelected < 2) {
                defenseLineup.add(p);
                defensemenSelected++;
            } else if ((pos.equals("L") || pos.equals("C") || pos.equals("R")) && !selectedForwards.contains(pos)) {
                defenseLineup.add(p);
                selectedForwards.add(pos);
            }
        }

        // In rare cases, if lineup is still not full (less than 5), try to fill forwards first in order L, C, R
        List<String> forwardPositions = Arrays.asList("L", "C", "R");
        for (String pos : forwardPositions) {
            if (defenseLineup.size() >= 5) break;
            if (!selectedForwards.contains(pos)) {
                // Find first forward with this position from sorted list
                for (Player p : sorted) {
                    if (p.getPosition().equalsIgnoreCase(pos) && !defenseLineup.contains(p)) {
                        defenseLineup.add(p);
                        selectedForwards.add(pos);
                        break;
                    }
                }
            }
        }

        return defenseLineup;
    }

    // Helper method: original lineup generator without a target player
    private static List<Player> generateLineupWithoutTarget(List<Player> sorted) {
        List<Player> lineup = new ArrayList<>();
        Map<String, Player> selectedForwards = new HashMap<>();
        int defensemenSelected = 0;

        for (Player p : sorted) {
            if (p.getPosition().equalsIgnoreCase("G")) continue;

            String pos = p.getPosition().toUpperCase().trim();

            if (pos.equals("D")) {
                if (defensemenSelected < 2) {
                    lineup.add(p);
                    defensemenSelected++;
                }
            } else if ((pos.equals("L") || pos.equals("C") || pos.equals("R")) && !selectedForwards.containsKey(pos)) {
                selectedForwards.put(pos, p);
            }

            if (defensemenSelected == 2 && selectedForwards.size() == 3) {
                break;
            }
        }

        // Add forwards in order L, C, R
        for (String pos : Arrays.asList("L", "C", "R")) {
            if (selectedForwards.containsKey(pos)) {
                lineup.add(selectedForwards.get(pos));
            }
        }
        return lineup;
    }
}