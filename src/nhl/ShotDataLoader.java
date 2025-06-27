package nhl;

import java.io.*;
import java.util.*;

/**
 * Utility class for loading shot data from a CSV file and filtering it by shooter.
 */
public class ShotDataLoader {

    /**
     * Loads shot data for a specific player from a CSV file.
     *
     * @param filePath   The path to the CSV file containing shot data.
     * @param playerName The name of the player whose shots should be loaded.
     * @return A list of {@link ShotData} objects corresponding to the specified player.
     */
    public static List<ShotData> loadShotsForPlayer(String filePath, String playerName) {
        List<ShotData> shots = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String headerLine = br.readLine(); // Read header line
            if (headerLine == null) return shots; // Return empty list if file is empty

            // Map column names to their indices
            String[] headers = headerLine.split(",");
            Map<String, Integer> columnMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                columnMap.put(headers[i].trim(), i);
            }

            // Get relevant column indices
            int shooterIndex = columnMap.getOrDefault("shooterName", -1);
            int xIndex = columnMap.getOrDefault("xCordAdjusted", -1);
            int yIndex = columnMap.getOrDefault("yCordAdjusted", -1);
            int xgIndex = columnMap.getOrDefault("xGoal", -1);

            // Ensure required columns are present
            if (shooterIndex == -1 || xIndex == -1 || yIndex == -1 || xgIndex == -1) {
                System.err.println("Required columns not found in CSV.");
                return shots;
            }

            // Read each data row
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",", -1); // -1 to preserve empty strings

                // Skip if data row is too short
                if (tokens.length <= Math.max(Math.max(shooterIndex, xIndex), yIndex)) continue;

                // Match shooter
                String shooter = tokens[shooterIndex].trim();
                if (!shooter.equalsIgnoreCase(playerName.trim())) continue;

                // Parse coordinates and xG value
                double x = parseSafe(tokens[xIndex]);
                double y = parseSafe(tokens[yIndex]);
                double xg = parseSafe(tokens[xgIndex]);

                // Add valid shot to the list
                shots.add(new ShotData(x, y, shooter, xg));
            }

        } catch (IOException e) {
            e.printStackTrace(); // Print error if file read fails
        }

        return shots;
    }

    /**
     * Safely parses a string into a double.
     * Returns 0.0 if parsing fails.
     *
     * @param val The string to parse.
     * @return The parsed double value, or 0.0 if invalid.
     */
    private static double parseSafe(String val) {
        try {
            return Double.parseDouble(val.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}