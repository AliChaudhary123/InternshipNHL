package nhl;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Main {

    private static final String[] COLUMN_NAMES = {
        "Name", "Position", "xGA", "Hits", "Takeaways",
        "Blocked Shots", "Giveaways", "oZone Starts",
        "dZone Starts", "High Danger xGoals", "Rebound Goals"
    };

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("NHL Defensive Lineup Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);  // Center window

        // Main container panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        frame.setContentPane(mainPanel);

        // Load all teams once
        List<Team> allTeams = DataLoader.loadTeamsFromCSV("data/skaters.csv");

        // Extract unique team names sorted alphabetically
        List<String> teamNamesList = allTeams.stream()
                                             .map(Team::getName)
                                             .distinct()
                                             .sorted()
                                             .collect(Collectors.toList());

        JComboBox<String> teamSelector = new JComboBox<>(teamNamesList.toArray(new String[0]));
        teamSelector.setPreferredSize(new Dimension(180, 25));

        // Input panel with GridBagLayout
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Input Parameters",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 16),
            new Color(0, 70, 130))
        );

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Target Player label and text field
        JLabel targetLabel = new JLabel("Target Player:");
        targetLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        inputPanel.add(targetLabel, gbc);

        JTextField targetPlayerField = new JTextField();
        targetPlayerField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        inputPanel.add(targetPlayerField, gbc);

        // Team label and combo box
        JLabel teamLabel = new JLabel("Defending Team:");
        teamLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        inputPanel.add(teamLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        inputPanel.add(teamSelector, gbc);

        // Generate lineup button
        JButton generateButton = new JButton("Generate Lineup");
        generateButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        generateButton.setBackground(new Color(0, 102, 204));
        generateButton.setForeground(Color.WHITE);
        generateButton.setFocusPainted(false);
        generateButton.setPreferredSize(new Dimension(180, 35));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(generateButton, gbc);

        // View heatmap button
        JButton heatmapButton = new JButton("View Heatmap");
        heatmapButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        heatmapButton.setBackground(new Color(0, 153, 0));
        heatmapButton.setForeground(Color.WHITE);
        heatmapButton.setFocusPainted(false);
        heatmapButton.setPreferredSize(new Dimension(180, 35));
        gbc.gridy = 3;
        inputPanel.add(heatmapButton, gbc);

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // Table for displaying lineup results
        DefaultTableModel tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // prevent cell editing
            }
        };
        JTable outputTable = new JTable(tableModel);
        outputTable.setFillsViewportHeight(true);
        outputTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        outputTable.setRowHeight(24);

        JScrollPane scrollPane = new JScrollPane(outputTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Lineup Results",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 16),
            new Color(0, 70, 130))
        );
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Action listener for generate button
        generateButton.addActionListener(e -> {
            String targetPlayer = targetPlayerField.getText().trim();
            String selectedTeam = (String) teamSelector.getSelectedItem();

            if (targetPlayer.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter the name of the offensive player you want to stop.",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (selectedTeam == null || selectedTeam.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please select a defending team.",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Clear any previous results
            tableModel.setRowCount(0);

            // Find the defending team object
            Team defendingTeam = allTeams.stream()
                                         .filter(t -> t.getName().equalsIgnoreCase(selectedTeam))
                                         .findFirst()
                                         .orElse(null);

            if (defendingTeam == null) {
                JOptionPane.showMessageDialog(frame, "Selected defending team not found.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Debug: print inputs
            System.out.println("Generating lineup from team: " + defendingTeam.getName() + " to stop player: " + targetPlayer);

            // Generate the lineup from the selected team to stop the target player
            List<Player> defensiveLineup = LineupGenerator.getBestDefensiveLineup(defendingTeam, targetPlayer, allTeams);

            if (defensiveLineup.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No defensive lineup could be generated to stop " + targetPlayer + ".",
                        "No Data", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Fill table with defensive lineup players
            for (Player p : defensiveLineup) {
                tableModel.addRow(new Object[]{
                    p.getName(),
                    p.getPosition(),
                    String.format("%.2f", p.getExpectedGoalsAgainst()),
                    p.getHits(),
                    p.getTakeaways(),
                    p.getBlockedShots(),
                    p.getGiveaways(),
                    p.getOZoneStarts(),
                    p.getDZoneStarts(),
                    String.format("%.2f", p.getHighDangerxGoals()),
                    p.getReboundGoals()
                });
            }
        });

        // Action listener for heatmap button
        heatmapButton.addActionListener(e -> {
            List<ShotData> sampleShots = new ArrayList<>();
            sampleShots.add(new ShotData(200, 100));
            sampleShots.add(new ShotData(220, 130));
            sampleShots.add(new ShotData(180, 90));
            sampleShots.add(new ShotData(300, 170));
            sampleShots.add(new ShotData(350, 200));

            JFrame heatmapFrame = new JFrame("Heatmap Visualization");
            heatmapFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            heatmapFrame.add(new RinkPanel(sampleShots));
            heatmapFrame.pack();
            heatmapFrame.setLocationRelativeTo(null);
            heatmapFrame.setVisible(true);
        });

        frame.setVisible(true);
    }
}