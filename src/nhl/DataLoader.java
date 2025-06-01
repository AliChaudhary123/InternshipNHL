package nhl;

import java.io.*;
import java.util.*;

public class DataLoader {

    public static List<Team> loadTeamsFromCSV(String filePath) {
        Map<String, List<Player>> teamMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            // Skip header
            line = br.readLine();
            if (line == null) {
                throw new IOException("CSV file is empty");
            }

            while ((line = br.readLine()) != null) {
                // Split all fields even if empty
                String[] fields = line.split(",", -1);

                // Skip invalid or incomplete rows
                if (fields.length < 138) continue;

                try {
                    String playerName = fields[2];     // name
                    String teamName = fields[3];       // team
                    String position = fields[4];       // position

                    double xGA = parseSafeDouble(fields[137]);
                    int hits = parseSafeInt(fields[49]);
                    int takeaways = parseSafeInt(fields[50]);
                    int goals = parseSafeInt(fields[33]);
                    int points = parseSafeInt(fields[32]);
                    int blockedShots = parseSafeInt(fields[36]);
                    int shotAttemptsAgainst = parseSafeInt(fields[134]);
                    int dZoneStarts = parseSafeInt(fields[62]);
                    int giveaways = parseSafeInt(fields[51]);
                    int oZoneStarts = parseSafeInt(fields[61]);
                    int nZoneStarts = parseSafeInt(fields[63]);
                    double iceTime = parseSafeDouble(fields[7]);
                    int shifts = parseSafeInt(fields[8]);
                    int timeOnBench = parseSafeInt(fields[94]);
                    int penalties = parseSafeInt(fields[46]);
                    int penaltyMinutes = parseSafeInt(fields[47]);
                    double highDangerxGoals = parseSafeDouble(fields[54]);
                    int reboundGoals = parseSafeInt(fields[34]);


                    Player player = new Player(playerName, position, xGA, hits, takeaways, goals, points, blockedShots, shotAttemptsAgainst, dZoneStarts, giveaways, oZoneStarts, nZoneStarts, iceTime, shifts, timeOnBench,
                    penalties, penaltyMinutes, highDangerxGoals, reboundGoals);
                    teamMap.computeIfAbsent(teamName, k -> new ArrayList<>()).add(player);

                } catch (NumberFormatException e) {
                    // Skip bad rows
                    continue;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Build list of Team objects
        List<Team> teams = new ArrayList<>();
        for (Map.Entry<String, List<Player>> entry : teamMap.entrySet()) {
            teams.add(new Team(entry.getKey(), entry.getValue()));
        }

        return teams;
    }

    // Safe integer parsing (defaults to 0)
    private static int parseSafeInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Safe double parsing (defaults to 0.0)
    private static double parseSafeDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}