package nhl;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Set;
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
        frame.setLocationRelativeTo(null); // Center on screen

        // Main panel with padding and border layout
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        frame.setContentPane(mainPanel);

        // Load teams from CSV once
        List<Team> allTeams = DataLoader.loadTeamsFromCSV("data/skaters.csv");
        Set<String> teamNamesSet = allTeams.stream()
                                           .map(Team::getName)
                                           .collect(Collectors.toSet());
        String[] teamNames = teamNamesSet.toArray(new String[0]);
        JComboBox<String> teamSelector = new JComboBox<>(teamNames);
        teamSelector.setPreferredSize(new Dimension(180, 25));

        // Input panel with GridBagLayout for neat form style
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Input Parameters",
            TitledBorder.CENTER, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 16), new Color(0, 70, 130))
        );

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Target player label + text field
        JLabel targetLabel = new JLabel("Target Player:");
        targetLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(targetLabel, gbc);

        JTextField targetPlayerField = new JTextField();
        targetPlayerField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        inputPanel.add(targetPlayerField, gbc);

        // Team selector label + combo box
        JLabel teamLabel = new JLabel("Team:");
        teamLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        inputPanel.add(teamLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        inputPanel.add(teamSelector, gbc);

        // Generate button
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

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // Table for output with scroll pane
        DefaultTableModel tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            // Make cells non-editable
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable outputTable = new JTable(tableModel);
        outputTable.setFillsViewportHeight(true);
        outputTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        outputTable.setRowHeight(24);


        JScrollPane scrollPane = new JScrollPane(outputTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Lineup Results",
            TitledBorder.CENTER, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 16), new Color(0, 70, 130))
        );
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Action listener for the button
        generateButton.addActionListener(e -> {
            String targetPlayer = targetPlayerField.getText().trim();
            String selectedTeam = (String) teamSelector.getSelectedItem();

            if (targetPlayer.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter a player's name.",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Clear previous results
            tableModel.setRowCount(0);

            // Find the selected team
            Team team = allTeams.stream()
                                .filter(t -> t.getName().equalsIgnoreCase(selectedTeam))
                                .findFirst()
                                .orElse(null);

            if (team == null) {
                JOptionPane.showMessageDialog(frame, "Selected team not found.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Get lineup
            List<Player> lineup = LineupGenerator.getBestDefensiveLineup(team, targetPlayer);

            if (lineup.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No lineup could be generated.",
                        "No Data", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Populate table with lineup data
            for (Player p : lineup) {
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

        frame.setVisible(true);
    }
}