package nhl;

import java.util.*;

public class LineupGenerator {

    public static List<Player> getBestDefensiveLineup(Team opponentTeam, String targetPlayerName) {
        return getBestDefensiveLineup(opponentTeam, targetPlayerName, null);
    }

    public static List<Player> getBestDefensiveLineup(Team opponentTeam, String targetPlayerName, List<Team> allTeams) {
        Player targetPlayer = null;

        if (allTeams != null) {
            outerLoop:
            for (Team team : allTeams) {
                for (Player p : team.getRoster()) {
                    if (p.getName().equalsIgnoreCase(targetPlayerName)) {
                        targetPlayer = p;
                        break outerLoop;
                    }
                }
            }
        }

        double baseDefWeight = 0.7;
        double baseOffWeight = 0.3;

        double threatBoost = 0.0;
        if (targetPlayer != null) {
            double offensiveThreat = targetPlayer.getHighDangerxGoals() + targetPlayer.getGoals();
            threatBoost = Math.min(offensiveThreat / 5.0, 1.0); // Stronger scaling
        }

        List<Player> sorted = new ArrayList<>(opponentTeam.getRoster());
        List<Player> lineup = new ArrayList<>();

        // Filter out goalies and under-41 games
        sorted.removeIf(p -> p.getPosition().equalsIgnoreCase("G") || p.getGamesPlayed() < 41);

        final double DEF_WEIGHT = baseDefWeight;
        final double OFF_WEIGHT = baseOffWeight;
        final Player TARGET_FINAL = targetPlayer;
        final double THREAT_BOOST = threatBoost;

        sorted.sort((a, b) -> {
            double aScore = getPlayerCompositeScore(a, TARGET_FINAL, DEF_WEIGHT, OFF_WEIGHT, THREAT_BOOST);
            double bScore = getPlayerCompositeScore(b, TARGET_FINAL, DEF_WEIGHT, OFF_WEIGHT, THREAT_BOOST);

            return Double.compare(bScore, aScore);
        });

        int defensemenCount = 0;
        Set<String> forwardPositions = new HashSet<>();

        for (Player p : sorted) {
            String pos = p.getPosition().toUpperCase().trim();

            if (pos.equals("D") && defensemenCount < 2) {
                lineup.add(p);
                defensemenCount++;
            } else if ((pos.equals("L") || pos.equals("C") || pos.equals("R")) && !forwardPositions.contains(pos)) {
                lineup.add(p);
                forwardPositions.add(pos);
            }

            if (defensemenCount == 2 && forwardPositions.size() == 3) break;
        }

        // Fallback for missing positions
        for (String pos : Arrays.asList("L", "C", "R")) {
            if (lineup.size() >= 5) break;
            if (!forwardPositions.contains(pos)) {
                for (Player p : sorted) {
                    if (p.getPosition().equalsIgnoreCase(pos) && !lineup.contains(p)) {
                        lineup.add(p);
                        forwardPositions.add(pos);
                        break;
                    }
                }
            }
        }

        // DEBUG PRINT
        System.out.println("\n--- Defensive Lineup against " + targetPlayerName + " ---");
        for (Player p : lineup) {
            System.out.println(p.getName() + " - " + p.getPosition());
        }

        return lineup;
    }

    private static double getPlayerCompositeScore(Player p, Player target, double defWeight, double offWeight, double threatBoost) {
        double defScore =
                -p.getExpectedGoalsAgainst() * 2.0
                + 0.02 * p.getHits()
                + 0.04 * p.getTakeaways()
                - 0.015 * p.getGiveaways()
                + 0.03 * p.getBlockedShots();

        // Amplify based on matchup
        double matchupMultiplier = 1.0 + threatBoost * 1.5;  // Strong effect
        double matchupDefScore = defScore * matchupMultiplier;

        double offScore =
                0.06 * p.getGoals()
                + 0.04 * p.getPoints()
                + 0.05 * p.getHighDangerxGoals()
                + 0.04 * p.getReboundGoals();

        double composite = defWeight * matchupDefScore + offWeight * offScore;

        // DEBUG PRINT
        System.out.printf("%s - Def: %.2f, Off: %.2f, Matchup: %.2f, Composite: %.2f%n",
                          p.getName(), defScore, offScore, matchupMultiplier, composite);

        return composite;
    }
}