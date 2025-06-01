package nhl;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("NHL Defensive Lineup Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Load teams from CSV
        List<Team> allTeams = DataLoader.loadTeamsFromCSV("data/skaters.csv");
        Set<String> teamNamesSet = allTeams.stream()
                                           .map(Team::getName)
                                           .collect(Collectors.toSet());

        String[] teamNames = teamNamesSet.toArray(new String[0]);
        JComboBox<String> teamSelector = new JComboBox<>(teamNames);

        // Top input panel
        JPanel inputPanel = new JPanel(new FlowLayout());

        JTextField targetPlayerField = new JTextField(20);
        JButton generateButton = new JButton("Generate Lineup");

        inputPanel.add(new JLabel("Target Player:"));
        inputPanel.add(targetPlayerField);
        inputPanel.add(new JLabel("Team:"));
        inputPanel.add(teamSelector);
        inputPanel.add(generateButton);

        // Output area
        JTextArea outputArea = new JTextArea(20, 80);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        generateButton.addActionListener(e -> {
            String targetPlayer = targetPlayerField.getText().trim();
            String selectedTeam = (String) teamSelector.getSelectedItem();

            if (targetPlayer.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter a player's name.");
                return;
            }

            outputArea.setText("");

            for (Team team : allTeams) {
                if (team.getName().equalsIgnoreCase(selectedTeam)) {
                    List<Player> lineup = LineupGenerator.getBestDefensiveLineup(team, targetPlayer);
                    outputArea.append("Team: " + team.getName() + "\n\n");
                    for (Player p : lineup) {
                        outputArea.append("  - " + p.getName()
                                + " | Pos: " + p.getPosition()
                                + " | xGA: " + p.getExpectedGoalsAgainst()
                                + " | Hits: " + p.getHits()
                                + " | Takeaways: " + p.getTakeaways()
                                + " | Blocked Shots: " + p.getBlockedShots()
                                + " | Giveaways: " + p.getGiveaways()
                                + " | oZone Starts: " + p.getOZoneStarts()
                                + " | dZone Starts: " + p.getDZoneStarts()
                                + " | High Danger xGoals: " + p.getHighDangerxGoals()
                                + " | Rebound Goals: " + p.getReboundGoals()
                                + "\n");
                    }
                    break;
                }
            }
        });

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }
}
