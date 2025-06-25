package nhl;

import java.io.*;
import java.util.*;

public class ShotDataLoader {
    public static List<ShotData> loadShotsForPlayer(String filePath, String playerName) {
        List<ShotData> shots = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String headerLine = br.readLine(); // Read header
            if (headerLine == null) return shots;

            String[] headers = headerLine.split(",");
            Map<String, Integer> columnMap = new HashMap<>();

            for (int i = 0; i < headers.length; i++) {
                columnMap.put(headers[i].trim(), i);
            }

            // Safely get indices
            int shooterIndex = columnMap.getOrDefault("shooterName", -1);
            int xIndex = columnMap.getOrDefault("xCordAdjusted", -1);
            int yIndex = columnMap.getOrDefault("yCordAdjusted", -1);
            int xgIndex = columnMap.getOrDefault("xGoal", -1);

            if (shooterIndex == -1 || xIndex == -1 || yIndex == -1 || xgIndex == -1) {
                System.err.println("Required columns not found in CSV.");
                return shots;
            }

            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",", -1); // -1 preserves empty strings

                if (tokens.length <= Math.max(Math.max(shooterIndex, xIndex), yIndex)) continue;

                String shooter = tokens[shooterIndex].trim();
                if (!shooter.equalsIgnoreCase(playerName.trim())) continue;

                double x = parseSafe(tokens[xIndex]);
                double y = parseSafe(tokens[yIndex]);
                double xg = parseSafe(tokens[xgIndex]);

                shots.add(new ShotData(x, y, shooter, xg));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return shots;
    }

    private static double parseSafe(String val) {
        try {
            return Double.parseDouble(val.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}