package nhl;

import nhl.ShotData;
import nhl.ShotDataLoader;

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
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        frame.setContentPane(mainPanel);

        List<Team> allTeams = DataLoader.loadTeamsFromCSV("data/skaters.csv");

        List<String> teamNamesList = allTeams.stream()
                .map(Team::getName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        JComboBox<String> teamSelector = new JComboBox<>(teamNamesList.toArray(new String[0]));
        teamSelector.setPreferredSize(new Dimension(180, 25));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Input Parameters",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                new Color(0, 70, 130)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

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

        JLabel teamLabel = new JLabel("Defending Team:");
        teamLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        inputPanel.add(teamLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        inputPanel.add(teamSelector, gbc);

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

        JButton heatmapButton = new JButton("View Heatmap");
        heatmapButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        heatmapButton.setBackground(new Color(0, 153, 0));
        heatmapButton.setForeground(Color.WHITE);
        heatmapButton.setFocusPainted(false);
        heatmapButton.setPreferredSize(new Dimension(180, 35));
        gbc.gridy = 3;
        inputPanel.add(heatmapButton, gbc);

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        DefaultTableModel tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
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
                BorderFactory.createEtchedBorder(),
                "Lineup Results",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                new Color(0, 70, 130))
        );
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JLabel summaryLabel = new JLabel("Summary: ");
        summaryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        summaryLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        mainPanel.add(summaryLabel, BorderLayout.SOUTH);

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

            tableModel.setRowCount(0);

            Team defendingTeam = allTeams.stream()
                    .filter(t -> t.getName().equalsIgnoreCase(selectedTeam))
                    .findFirst()
                    .orElse(null);

            if (defendingTeam == null) {
                JOptionPane.showMessageDialog(frame, "Selected defending team not found.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<Player> defensiveLineup = LineupGenerator.getBestDefensiveLineup(defendingTeam, targetPlayer, allTeams);

            if (defensiveLineup.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No defensive lineup could be generated to stop " + targetPlayer + ".",
                        "No Data", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

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

            double totalXGA = 0.0;
            int totalTakeaways = 0;
            int totalGiveaways = 0;
            double totalScore = 0.0;
            Player target = DataLoader.findPlayerByName(targetPlayer, allTeams);
            double threatBoost = (target != null)
                    ? Math.min((target.getHighDangerxGoals() + target.getGoals()) / 5.0, 1.0)
                    : 0.0;

            for (Player p : defensiveLineup) {
                totalXGA += p.getExpectedGoalsAgainst();
                totalTakeaways += p.getTakeaways();
                totalGiveaways += p.getGiveaways();
                totalScore += LineupGenerator.getPlayerCompositeScore(p, target, 0.7, 0.3, threatBoost);
            }

            int count = defensiveLineup.size();
            String summaryText = String.format(
                    "<html><b>Summary:</b> Avg xGA: %.2f | Total Takeaways: %d | Total Giveaways: %d | Avg Score: %.2f</html>",
                    totalXGA / count, totalTakeaways, totalGiveaways, totalScore / count
            );
            summaryLabel.setText(summaryText);
        });

        heatmapButton.addActionListener(e -> {
            try {
                String targetPlayer = targetPlayerField.getText().trim();
                List<ShotData> shotList = ShotDataLoader.loadShotsForPlayer("data/shots.csv", targetPlayer);
               

                if (shotList == null || shotList.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "No shot data found or loaded.",
                            "Data Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                JFrame heatmapFrame = new JFrame("xGoal Heatmap vs Target Player");
                heatmapFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                RinkPanel rinkPanel = new RinkPanel(shotList);
                heatmapFrame.add(rinkPanel);

                heatmapFrame.pack();
                heatmapFrame.setLocationRelativeTo(frame);
                heatmapFrame.setVisible(true);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error loading heatmap data: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }
}