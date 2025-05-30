package nhl;

import java.io.*;
import java.util.*;

public class DataLoader {
    public static List<Team> loadTeamsFromCSV(String filePath) {
        Map<String, List<Player>> teamMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Read header line
            line = br.readLine();
            if (line == null) {
                throw new IOException("CSV file is empty");
            }

            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length < 10) continue; // Ensure there are enough fields

                String playerName = fields[0];
                String position = fields[1];
                String teamName = fields[2];
                double xGA = Double.parseDouble(fields[3]);
                int hits = Integer.parseInt(fields[4]);
                int takeaways = Integer.parseInt(fields[5]);
                int goals = Integer.parseInt(fields[6]);
                int points = Integer.parseInt(fields[7]);

                Player player = new Player(playerName, position, xGA, hits, takeaways, goals, points);

                teamMap.computeIfAbsent(teamName, k -> new ArrayList<>()).add(player);
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
}