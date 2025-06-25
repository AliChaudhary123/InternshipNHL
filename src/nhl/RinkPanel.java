package nhl;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RinkPanel extends JPanel {

    private List<ShotData> shots;
    private double maxXG = 0; // Used for the legend

    public RinkPanel(List<ShotData> shots) {
        this.shots = shots;
        setPreferredSize(new Dimension(1000, 425));
        setBackground(Color.WHITE);
    }

    public void setShots(List<ShotData> shots) {
        this.shots = shots;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        drawRink(g2);
        drawHeatmap(g2);
        drawLegend(g2);
    }

    private void drawRink(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();

        double scaleX = width / 200.0;
        double scaleY = height / 85.0;

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, width, height, (int)(scaleX * 28), (int)(scaleY * 28));
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(0, 0, width - 1, height - 1, (int)(scaleX * 28), (int)(scaleY * 28));

        g2.setColor(Color.RED);
        int centerX = (int)(100 * scaleX);
        g2.fillRect(centerX - 2, 0, 4, height);

        g2.setColor(Color.BLUE);
        int blue1 = (int)(75 * scaleX);
        int blue2 = (int)(125 * scaleX);
        g2.fillRect(blue1 - 2, 0, 4, height);
        g2.fillRect(blue2 - 2, 0, 4, height);

        g2.setColor(Color.RED);
        int goal1 = (int)(11 * scaleX);
        int goal2 = (int)(189 * scaleX);
        g2.fillRect(goal1 - 1, 0, 2, height);
        g2.fillRect(goal2 - 1, 0, 2, height);

        g2.setColor(new Color(173, 216, 230));
        int creaseRadius = (int)(6 * scaleX);
        g2.fillArc(goal1 - creaseRadius, height / 2 - creaseRadius, creaseRadius * 2, creaseRadius * 2, 270, 180);
        g2.fillArc(goal2 - creaseRadius, height / 2 - creaseRadius, creaseRadius * 2, creaseRadius * 2, 90, 180);

        int trapWidth = (int)(40 * scaleX);
        int trapBottom = (int)(28 * scaleY);
        int trapTop = (int)(8 * scaleY);

        Polygon trapezoidLeft = new Polygon();
        trapezoidLeft.addPoint(goal1, height / 2 - trapTop);
        trapezoidLeft.addPoint(goal1 - trapWidth, height / 2 - trapBottom);
        trapezoidLeft.addPoint(goal1 - trapWidth, height / 2 + trapBottom);
        trapezoidLeft.addPoint(goal1, height / 2 + trapTop);
        g2.setColor(Color.WHITE);
        g2.fillPolygon(trapezoidLeft);
        g2.setColor(Color.RED);
        g2.drawPolygon(trapezoidLeft);

        Polygon trapezoidRight = new Polygon();
        trapezoidRight.addPoint(goal2, height / 2 - trapTop);
        trapezoidRight.addPoint(goal2 + trapWidth, height / 2 - trapBottom);
        trapezoidRight.addPoint(goal2 + trapWidth, height / 2 + trapBottom);
        trapezoidRight.addPoint(goal2, height / 2 + trapTop);
        g2.setColor(Color.WHITE);
        g2.fillPolygon(trapezoidRight);
        g2.setColor(Color.RED);
        g2.drawPolygon(trapezoidRight);

        g2.setColor(Color.RED);
        int circleR = (int)(15 * scaleX);
        int[][] centers = {
            {(int)(31 * scaleX), (int)(22 * scaleY)},
            {(int)(31 * scaleX), (int)(63 * scaleY)},
            {(int)(169 * scaleX), (int)(22 * scaleY)},
            {(int)(169 * scaleX), (int)(63 * scaleY)},
            {(int)(100 * scaleX), (int)(42.5 * scaleY)}
        };

        for (int[] center : centers) {
            g2.drawOval(center[0] - circleR, center[1] - circleR, 2 * circleR, 2 * circleR);
            g2.fillOval(center[0] - 2, center[1] - 2, 4, 4);
        }
    }

    private void drawHeatmap(Graphics2D g2) {
        if (shots == null || shots.isEmpty()) {
            maxXG = 0;
            return;
        }

        double scaleX = getWidth() / 200.0;
        double scaleY = getHeight() / 85.0;

        int cellSize = 10;
        int gridWidth = getWidth() / cellSize;
        int gridHeight = getHeight() / cellSize;

        double[][] xgSum = new double[gridWidth][gridHeight];
        maxXG = 0;

        for (ShotData shot : shots) {
            double normalizedX = shot.getX() + 100;
            double normalizedY = 42.5 - shot.getY(); // flip Y

            int pixelX = (int)(normalizedX * scaleX);
            int pixelY = (int)(normalizedY * scaleY);
            int cellX = pixelX / cellSize;
            int cellY = pixelY / cellSize;

            if (cellX >= 0 && cellX < gridWidth && cellY >= 0 && cellY < gridHeight) {
                xgSum[cellX][cellY] += shot.getXGoal();
                if (xgSum[cellX][cellY] > maxXG) {
                    maxXG = xgSum[cellX][cellY];
                }
            }
        }

        if (maxXG == 0) return;

        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                double total = xgSum[i][j];
                if (total > 0) {
                    float intensity = (float)(total / maxXG);
                    Color heatColor = interpolateColor(
                        new Color(255, 255, 0, 40),   // Yellow (low)
                        new Color(255, 0, 0, 180),    // Red (high)
                        intensity
                    );

                    g2.setColor(heatColor);
                    int cx = i * cellSize + cellSize / 2;
                    int cy = j * cellSize + cellSize / 2;
                    int radius = (int)(cellSize * (0.7 + intensity));
                    g2.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
                }
            }
        }
    }

    private void drawLegend(Graphics2D g2) {
        if (maxXG == 0) return;

        int legendWidth = 20;
        int legendHeight = 150;
        int x = getWidth() - legendWidth - 15;
        int y = 30;

        // Red at top, yellow at bottom
        GradientPaint gp = new GradientPaint(
            x, y, new Color(255, 0, 0, 180),        // Red = High
            x, y + legendHeight, new Color(255, 255, 0, 40) // Yellow = Low
        );
        g2.setPaint(gp);
        g2.fillRect(x, y, legendWidth, legendHeight);

        g2.setColor(Color.BLACK);
        g2.drawRect(x, y, legendWidth, legendHeight);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString("xG Intensity", x - 60, y - 5);
        g2.drawString(String.format("%.2f", maxXG), x - 45, y + 15);             // Top = red
        g2.drawString("0", x - 10, y + legendHeight + 15);                       // Bottom = yellow
    }

    private static Color interpolateColor(Color start, Color end, float fraction) {
        float r = start.getRed() + fraction * (end.getRed() - start.getRed());
        float g = start.getGreen() + fraction * (end.getGreen() - start.getGreen());
        float b = start.getBlue() + fraction * (end.getBlue() - start.getBlue());
        float a = start.getAlpha() + fraction * (end.getAlpha() - start.getAlpha());
        return new Color((int) r, (int) g, (int) b, (int) a);
    }
}