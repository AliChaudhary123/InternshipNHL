package nhl;

import java.io.*;
import java.util.*;

/**
 * Utility class responsible for loading player and team data from a CSV file,
 * normalizing stats, and computing metrics such as takeaway efficiency.
 */
public class DataLoader {

    /**
     * Loads player data from a CSV file and organizes them into teams.
     * Filters for 5-on-5 play only and calculates additional metrics like on-ice xGA/60
     * and takeaway efficiency.
     *
     * @param filePath Path to the CSV file.
     * @return A list of {@link Team} objects, each containing its player roster.
     */
    public static List<Team> loadTeamsFromCSV(String filePath) {
        Map<String, List<Player>> teamMap = new HashMap<>();
        Set<String> addedPlayers = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Skip header
            if (line == null) throw new IOException("CSV file is empty");

            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",", -1);
                if (fields.length < 138) continue;
                if (!fields[5].trim().equals("5on5")) continue;

                try {
                    String playerName = fields[2].trim();
                    String teamName = fields[3].trim();
                    String position = fields[4].trim();
                    String key = teamName + "-" + playerName;

                    if (addedPlayers.contains(key)) continue;
                    addedPlayers.add(key);

                    double onIceXGA = parseSafeDouble(fields[106]);
                    double iceTime = parseSafeDouble(fields[7]) / 60.0;
                    double onIceXGA60 = iceTime > 0 ? onIceXGA / iceTime : 0;

                    Player player = new Player(
                        playerName,
                        position,
                        parseSafeDouble(fields[134]),
                        onIceXGA60,
                        (int) parseSafeDouble(fields[46]),
                        (int) parseSafeDouble(fields[47]),
                        (int) parseSafeDouble(fields[34]),
                        (int) parseSafeDouble(fields[33]),
                        (int) parseSafeDouble(fields[83]),
                        (int) parseSafeDouble(fields[122]),
                        (int) parseSafeDouble(fields[70]),
                        (int) parseSafeDouble(fields[48]),
                        (int) parseSafeDouble(fields[69]),
                        (int) parseSafeDouble(fields[71]),
                        iceTime,
                        (int) parseSafeDouble(fields[8]),
                        (int) parseSafeDouble(fields[79]),
                        (int) parseSafeDouble(fields[43]),
                        (int) parseSafeDouble(fields[44]),
                        parseSafeDouble(fields[54]),
                        (int) parseSafeDouble(fields[36]),
                        (int) parseSafeDouble(fields[6])
                    );

                    teamMap.computeIfAbsent(teamName, k -> new ArrayList<>()).add(player);

                } catch (Exception e) {
                    System.err.println("Error parsing player data: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading CSV: " + e.getMessage());
        }

        return finalizeTeams(teamMap);
    }

    /**
     * Finalizes team data by normalizing takeaways and giveaways and
     * computing a takeaway efficiency score for each player.
     *
     * @param teamMap A map of team names to lists of their players.
     * @return A list of {@link Team} objects with normalized player data.
     */
    private static List<Team> finalizeTeams(Map<String, List<Player>> teamMap) {
        int maxTakeaways = Integer.MIN_VALUE;
        int minTakeaways = Integer.MAX_VALUE;
        int maxGiveaways = Integer.MIN_VALUE;
        int minGiveaways = Integer.MAX_VALUE;

        for (List<Player> roster : teamMap.values()) {
            for (Player p : roster) {
                maxTakeaways = Math.max(maxTakeaways, p.getTakeaways());
                minTakeaways = Math.min(minTakeaways, p.getTakeaways());
                maxGiveaways = Math.max(maxGiveaways, p.getGiveaways());
                minGiveaways = Math.min(minGiveaways, p.getGiveaways());
            }
        }

        for (List<Player> roster : teamMap.values()) {
            for (Player p : roster) {
                double normTake = (maxTakeaways - minTakeaways) == 0 ? 0 :
                        (p.getTakeaways() - minTakeaways) / (double)(maxTakeaways - minTakeaways);
                double normGive = (maxGiveaways - minGiveaways) == 0 ? 0 :
                        (p.getGiveaways() - minGiveaways) / (double)(maxGiveaways - minGiveaways);

                double efficiencyScore = (2.0 * normTake) - (0.5 * normGive);
                p.setTakeawayEfficiencyScore(efficiencyScore);
            }
        }

        System.out.println("Top 5 Takeaway Efficiency Players:");
        teamMap.values().stream()
            .flatMap(List::stream)
            .sorted(Comparator.comparingDouble(Player::getTakeawayEfficiencyScore).reversed())
            .limit(5)
            .forEach(p -> System.out.printf("- %s: %.3f%n", p.getName(), p.getTakeawayEfficiencyScore()));

        List<Team> teams = new ArrayList<>();
        for (Map.Entry<String, List<Player>> entry : teamMap.entrySet()) {
            teams.add(new Team(entry.getKey(), entry.getValue()));
        }
        return teams;
    }

    /**
     * Finds a player by name from the provided list of teams.
     *
     * @param name  The name of the player to search for.
     * @param teams The list of all teams.
     * @return The matching {@link Player}, or null if not found.
     */
    public static Player findPlayerByName(String name, List<Team> teams) {
        for (Team team : teams) {
            for (Player player : team.getRoster()) {
                if (player.getName().equalsIgnoreCase(name)) {
                    return player;
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the name of the team that a given player belongs to.
     *
     * @param name  The player's name.
     * @param teams The list of all teams.
     * @return The name of the player's team, or "Unknown Team" if not found.
     */
    public static String getTeamNameForPlayer(String name, List<Team> teams) {
        for (Team team : teams) {
            for (Player player : team.getRoster()) {
                if (player.getName().equalsIgnoreCase(name)) {
                    return team.getName();
                }
            }
        }
        return "Unknown Team";
    }

    /**
     * Collects the names of all players across all teams.
     *
     * @param teams The list of teams to extract player names from.
     * @return A list of all player names.
     */
    public static List<String> getAllPlayerNames(List<Team> teams) {
        List<String> names = new ArrayList<>();
        for (Team team : teams) {
            for (Player player : team.getRoster()) {
                names.add(player.getName());
            }
        }
        return names;
    }

    /**
     * Parses a string to a double safely.
     * Returns 0.0 if parsing fails.
     *
     * @param s The string to parse.
     * @return The parsed double value or 0.0 on error.
     */
    private static double parseSafeDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}