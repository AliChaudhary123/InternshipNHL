package nhl;

import java.util.*;

/**
 * Provides utility methods to generate a defensively strong lineup against a specific opponent player.
 * Includes logic to evaluate players based on defensive metrics, possession stats, and offensive contributions.
 */
public class LineupGenerator {

    /**
     * Generates the best defensive lineup against a target player based on an opponent team.
     * This is a convenience method that does not consider the full league context.
     *
     * @param opponentTeam       The team from which to select the defensive lineup.
     * @param targetPlayerName   The name of the opposing player to defend against.
     * @return A list of up to 5 players selected for optimal defensive coverage.
     */
    public static List<Player> getBestDefensiveLineup(Team opponentTeam, String targetPlayerName) {
        return getBestDefensiveLineup(opponentTeam, targetPlayerName, null);
    }

    /**
     * Generates the best defensive lineup against a target player, optionally using the entire league data
     * to compute threat boosts and matchup adjustments.
     *
     * @param opponentTeam       The team from which to select players.
     * @param targetPlayerName   The name of the opposing player to defend against.
     * @param allTeams           Optional: the full list of teams for identifying the target player's stats.
     * @return A list of players forming the defensive lineup (2 D, 3 F).
     */
    public static List<Player> getBestDefensiveLineup(Team opponentTeam, String targetPlayerName, List<Team> allTeams) {
        Player targetPlayer = null;

        // Try to find the target player in the full team list
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

        // Determine threat boost based on offensive performance of the target
        double threatBoost = 0.0;
        if (targetPlayer != null) {
            double offensiveThreat = targetPlayer.getHighDangerxGoals() + targetPlayer.getGoals();
            threatBoost = Math.min(offensiveThreat / 5.0, 1.0);
        }

        List<Player> sorted = new ArrayList<>(opponentTeam.getRoster());
        List<Player> lineup = new ArrayList<>();

        // Exclude goalies and players with fewer than 50 games played
        sorted.removeIf(p -> p.getPosition().equalsIgnoreCase("G") || p.getGamesPlayed() < 50);

        // Sort players based on composite score
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

        // Select 2 defensemen and 1 of each forward position (L, C, R)
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

        // If any forward positions are still missing, fill them
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

        // Output the lineup for verification
        System.out.println("\n--- Defensive Lineup against " + targetPlayerName + " ---");
        for (Player p : lineup) {
            System.out.println(p.getName() + " - " + p.getPosition());
        }

        return lineup;
    }

    /**
     * Calculates a composite score for a player based on defensive and offensive contributions,
     * adjusted by the threat level of the opposing target player.
     *
     * @param p             The player being evaluated.
     * @param target        The opponent player this player would defend against (can be null).
     * @param defWeight     The weighting for defensive score in the final composite.
     * @param offWeight     The weighting for offensive score in the final composite.
     * @param threatBoost   A scaling factor representing the offensive threat of the target.
     * @return A numeric score representing the player’s all-around contribution value.
     */
    public static double getPlayerCompositeScore(Player p, Player target, double defWeight, double offWeight, double threatBoost) {
        double minutes = p.getIceTime() > 0 ? p.getIceTime() : 1; // prevent division by zero
        double xGA_per60 = (p.getExpectedGoalsAgainst() / minutes) * 60.0;

        // Defensive components
        double baseDefScore =
            -1.5 * xGA_per60 +
            0.04 * p.getHits() +
            0.05 * p.getBlockedShots();

        // Possession effectiveness
        double possessionScore = 2.0 * (p.getTakeaways() - 0.5 * p.getGiveaways());

        // Adjust for target player threat
        double defScore = baseDefScore + possessionScore;
        double matchupMultiplier = 1.0 + 0.25 * threatBoost;
        double matchupDefScore = defScore * matchupMultiplier;

        // Offensive scoring components
        double offScore =
            0.15 * p.getGoals() +
            0.10 * p.getPoints() +
            0.08 * p.getHighDangerxGoals() +
            0.10 * p.getReboundGoals();

        // Final weighted composite
        double composite = defWeight * matchupDefScore + offWeight * offScore;

        // Debug output
        System.out.printf(
            "%s - xGA/60: %.2f, Def: %.2f, Off: %.2f, Matchup: %.2f, Composite: %.2f, Takeaways: %d, Giveaways: %d%n",
            p.getName(), xGA_per60, defScore, offScore, matchupMultiplier, composite,
            p.getTakeaways(), p.getGiveaways()
        );

        return composite;
    }
}