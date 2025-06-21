package nhl;

import java.io.*;
import java.util.*;

public class DataLoader {

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

                    Player player = new Player(
                        playerName,
                        position,
                        parseSafeDouble(fields[134]),  // xGA
                        (int) parseSafeDouble(fields[46]),  // hits
                        (int) parseSafeDouble(fields[47]),  // takeaways
                        (int) parseSafeDouble(fields[34]),  // goals
                        (int) parseSafeDouble(fields[33]),  // points
                        (int) parseSafeDouble(fields[83]),  // blocked shots
                        (int) parseSafeDouble(fields[122]), // shot attempts against
                        (int) parseSafeDouble(fields[70]),  // d-zone starts
                        (int) parseSafeDouble(fields[48]),  // giveaways
                        (int) parseSafeDouble(fields[69]),  // o-zone starts
                        (int) parseSafeDouble(fields[71]),  // n-zone starts
                        parseSafeDouble(fields[7]) / 60.0,  // ice time (minutes)
                        (int) parseSafeDouble(fields[8]),   // shifts
                        (int) parseSafeDouble(fields[79]),  // time on bench
                        (int) parseSafeDouble(fields[43]),  // penalties
                        (int) parseSafeDouble(fields[44]),  // penalty minutes
                        parseSafeDouble(fields[54]),        // high-danger xGoals
                        (int) parseSafeDouble(fields[36]),  // rebound goals
                        (int) parseSafeDouble(fields[6])    // games played
                    );

                    teamMap.computeIfAbsent(teamName, k -> new ArrayList<>()).add(player);

                } catch (Exception e) {
                    System.err.println("Error parsing player data: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV: " + e.getMessage());
        }

        // ✅ Normalize takeaway and giveaway values across all players
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

        // ✅ Compute efficiency score with weighted formula
        for (List<Player> roster : teamMap.values()) {
            for (Player p : roster) {
                double normTake = (maxTakeaways - minTakeaways) == 0 ? 0 :
                        (p.getTakeaways() - minTakeaways) / (double)(maxTakeaways - minTakeaways);
                double normGive = (maxGiveaways - minGiveaways) == 0 ? 0 :
                        (p.getGiveaways() - minGiveaways) / (double)(maxGiveaways - minGiveaways);

                // 🧠 Adjusted formula: prioritize takeaways and reduce giveaway penalty
                double efficiencyScore = (2.0 * normTake) - (0.5 * normGive);
                p.setTakeawayEfficiencyScore(efficiencyScore);
            }
        }

        // ✅ (Optional) Print top 5 players by takeaway efficiency
        System.out.println("Top 5 Takeaway Efficiency Players:");
        teamMap.values().stream()
            .flatMap(List::stream)
            .sorted(Comparator.comparingDouble(Player::getTakeawayEfficiencyScore).reversed())
            .limit(5)
            .forEach(p -> System.out.printf("- %s: %.3f%n", p.getName(), p.getTakeawayEfficiencyScore()));

        // Finalize team objects
        List<Team> teams = new ArrayList<>();
        for (Map.Entry<String, List<Player>> entry : teamMap.entrySet()) {
            teams.add(new Team(entry.getKey(), entry.getValue()));
        }

        return teams;
    }

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

    public static List<String> getAllPlayerNames(List<Team> teams) {
        List<String> names = new ArrayList<>();
        for (Team team : teams) {
            for (Player player : team.getRoster()) {
                names.add(player.getName());
            }
        }
        return names;
    }

    private static double parseSafeDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}