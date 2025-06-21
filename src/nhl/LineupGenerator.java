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
            threatBoost = Math.min(offensiveThreat / 5.0, 1.0);
        }

        List<Player> sorted = new ArrayList<>(opponentTeam.getRoster());
        List<Player> lineup = new ArrayList<>();

        // Filter out goalies and players with fewer than 50 games
        sorted.removeIf(p -> p.getPosition().equalsIgnoreCase("G") || p.getGamesPlayed() < 50);

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

        // Debug output
        System.out.println("\n--- Defensive Lineup against " + targetPlayerName + " ---");
        for (Player p : lineup) {
            System.out.println(p.getName() + " - " + p.getPosition());
        }

        return lineup;
    }

    public static double getPlayerCompositeScore(Player p, Player target, double defWeight, double offWeight, double threatBoost) {
        double minutes = p.getIceTime() > 0 ? p.getIceTime() : 1; // prevent division by zero
        double xGA_per60 = (p.getExpectedGoalsAgainst() / minutes) * 60.0;

        // Base defensive score: xGA/60, hits, blocked shots
        double baseDefScore =
            -1.5 * xGA_per60 +
            0.04 * p.getHits() +
            0.05 * p.getBlockedShots();

        // Possession score (takeaways vs giveaways)
        double possessionScore = 2.0 * (p.getTakeaways() - 0.5 * p.getGiveaways());

        // Combined defensive score
        double defScore = baseDefScore + possessionScore;

        // Threat scaling
        double matchupMultiplier = 1.0 + 0.25 * threatBoost;
        double matchupDefScore = defScore * matchupMultiplier;

        // Offensive contribution
        double offScore =
            0.15 * p.getGoals() +
            0.10 * p.getPoints() +
            0.08 * p.getHighDangerxGoals() +
            0.10 * p.getReboundGoals();

        // Final composite score
        double composite = defWeight * matchupDefScore + offWeight * offScore;

        // Debug print
        System.out.printf(
            "%s - xGA/60: %.2f, Def: %.2f, Off: %.2f, Matchup: %.2f, Composite: %.2f, Takeaways: %d, Giveaways: %d%n",
            p.getName(), xGA_per60, defScore, offScore, matchupMultiplier, composite,
            p.getTakeaways(), p.getGiveaways()
        );

        return composite;
    }
}