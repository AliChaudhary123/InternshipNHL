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

            Set<String> addedPlayers = new HashSet<>();

            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",", -1);

                if (fields.length < 138) continue;

                String situation = fields[5].trim();
                if (!situation.equals("5on5")) continue;

                try {
                    String playerName = fields[2];
                    String teamName = fields[3];
                    String position = fields[4];
                    String uniqueKey = teamName + "-" + playerName;
                    if (addedPlayers.contains(uniqueKey)) continue;
                    addedPlayers.add(uniqueKey);

                    double xGA = parseSafeDouble(fields[134]);
                    int hits = (int) parseSafeDouble(fields[46]);
                    int takeaways = (int) parseSafeDouble(fields[47]);
                    int goals = (int) parseSafeDouble(fields[34]);
                    int points = (int) parseSafeDouble(fields[33]);
                    int blockedShots = (int) parseSafeDouble(fields[83]);
                    int shotAttemptsAgainst = (int) parseSafeDouble(fields[122]);
                    int dZoneStarts = (int) parseSafeDouble(fields[70]);
                    int giveaways = (int) parseSafeDouble(fields[48]);
                    int oZoneStarts = (int) parseSafeDouble(fields[69]);
                    int nZoneStarts = (int) parseSafeDouble(fields[71]);
                    double iceTime = parseSafeDouble(fields[7]) / 60.0;
                    int shifts = (int) parseSafeDouble(fields[8]);
                    int timeOnBench = (int) parseSafeDouble(fields[79]);
                    int penalties = (int) parseSafeDouble(fields[43]);
                    int penaltyMinutes = (int) parseSafeDouble(fields[44]);
                    double highDangerxGoals = parseSafeDouble(fields[54]);
                    int reboundGoals = (int) parseSafeDouble(fields[36]);

                    // Optional debug:
                    /*
                    System.out.println("Player: " + playerName);
                    System.out.println("Points raw: " + fields[33]);
                    System.out.println("Points parsed: " + points);
                    System.out.println("------------------------");
                    */

                    Player player = new Player(
                        playerName, position, xGA, hits, takeaways, goals, points,
                        blockedShots, shotAttemptsAgainst, dZoneStarts, giveaways,
                        oZoneStarts, nZoneStarts, iceTime, shifts, timeOnBench,
                        penalties, penaltyMinutes, highDangerxGoals, reboundGoals
                    );

                    teamMap.computeIfAbsent(teamName, k -> new ArrayList<>()).add(player);

                } catch (NumberFormatException e) {
                    System.err.println("Number format exception for player " + fields[2] + ": " + e.getMessage());
                    continue;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Team> teams = new ArrayList<>();
        for (Map.Entry<String, List<Player>> entry : teamMap.entrySet()) {
            teams.add(new Team(entry.getKey(), entry.getValue()));
        }

        return teams;
    }

    private static int parseSafeInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double parseSafeDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}