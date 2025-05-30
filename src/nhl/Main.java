package nhl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("NHL Defensive Lineup Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Top input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());

        JTextField targetPlayerField = new JTextField(20);
        JButton generateButton = new JButton("Generate Lineups");

        inputPanel.add(new JLabel("Target Player:"));
        inputPanel.add(targetPlayerField);
        inputPanel.add(generateButton);

        // Output area
        JTextArea outputArea = new JTextArea(20, 60);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Load teams
        List<Team> allTeams = DataLoader.loadTeamsFromCSV("data/skaters.csv");

        generateButton.addActionListener(e -> {
            String targetPlayer = targetPlayerField.getText().trim();
            if (targetPlayer.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter a player's name.");
                return;
            }

            outputArea.setText("");

            for (Team team : allTeams) {
                List<Player> lineup = LineupGenerator.getBestDefensiveLineup(team, targetPlayer);
                outputArea.append("Team: " + team.getName() + "\n");
                for (Player p : lineup) {
                    outputArea.append("  - " + p.getName() + " | " + p.getPosition()
                            + " | xGA: " + p.getExpectedGoalsAgainst()
                            + " | Hits: " + p.getHits()
                            + " | Takeaways: " + p.getTakeaways() + "\n");
                }
                outputArea.append("\n");
            }
        });

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }
}