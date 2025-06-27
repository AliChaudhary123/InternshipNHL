package nhl;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * A custom JPanel that visualizes an NHL rink with a heatmap overlay representing
 * shot expected goals (xG) intensity.
 */
public class RinkPanel extends JPanel {

    /** List of shots to be displayed as a heatmap */
    private List<ShotData> shots;

    /** Maximum xG value found in current shots, used for color intensity scaling */
    private double maxXG = 0;

    /**
     * Constructs a RinkPanel with an initial list of shots.
     *
     * @param shots List of shot data to visualize on the rink
     */
    public RinkPanel(List<ShotData> shots) {
        this.shots = shots;
        setPreferredSize(new Dimension(1000, 425)); // Set preferred size of the panel
        setBackground(Color.WHITE);                  // Background color of the rink
    }

    /**
     * Updates the shot data for the heatmap and repaints the component.
     *
     * @param shots New list of shots to display
     */
    public void setShots(List<ShotData> shots) {
        this.shots = shots;
        repaint();
    }

    /**
     * Paints the rink, heatmap overlay, and legend.
     *
     * @param g The Graphics context to use for painting
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        drawRink(g2);       // Draw rink lines and shapes
        drawHeatmap(g2);    // Overlay heatmap of shot xG
        drawLegend(g2);     // Draw color legend for xG intensity
    }

    /**
     * Draws the NHL rink with all relevant lines, zones, and markings.
     *
     * @param g2 Graphics2D context used for drawing
     */
    private void drawRink(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Scale factors to map rink coordinates to panel size
        double scaleX = width / 200.0;
        double scaleY = height / 85.0;

        // Draw rink background and outline with rounded corners
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, width, height, (int)(scaleX * 28), (int)(scaleY * 28));
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(0, 0, width - 1, height - 1, (int)(scaleX * 28), (int)(scaleY * 28));

        // Center red line
        g2.setColor(Color.RED);
        int centerX = (int)(100 * scaleX);
        g2.fillRect(centerX - 2, 0, 4, height);

        // Blue lines
        g2.setColor(Color.BLUE);
        int blue1 = (int)(75 * scaleX);
        int blue2 = (int)(125 * scaleX);
        g2.fillRect(blue1 - 2, 0, 4, height);
        g2.fillRect(blue2 - 2, 0, 4, height);

        // Goal lines
        g2.setColor(Color.RED);
        int goal1 = (int)(11 * scaleX);
        int goal2 = (int)(189 * scaleX);
        g2.fillRect(goal1 - 1, 0, 2, height);
        g2.fillRect(goal2 - 1, 0, 2, height);

        // Crease areas in light blue
        g2.setColor(new Color(173, 216, 230));
        int creaseRadius = (int)(6 * scaleX);
        g2.fillArc(goal1 - creaseRadius, height / 2 - creaseRadius, creaseRadius * 2, creaseRadius * 2, 270, 180);
        g2.fillArc(goal2 - creaseRadius, height / 2 - creaseRadius, creaseRadius * 2, creaseRadius * 2, 90, 180);

        // Trapezoid areas behind the goals
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

        // Faceoff circles and dots
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

    /**
     * Draws the heatmap circles on the rink indicating shot intensity (xG) at locations.
     *
     * @param g2 Graphics2D context used for drawing
     */
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

        // Accumulate xG values in grid cells
        double[][] xgSum = new double[gridWidth][gridHeight];
        maxXG = 0;

        for (ShotData shot : shots) {
            // Normalize coordinates to rink reference frame
            double normalizedX = shot.getX() + 100;
            double normalizedY = 42.5 - shot.getY(); // flip Y coordinate for display

            int pixelX = (int)(normalizedX * scaleX);
            int pixelY = (int)(normalizedY * scaleY);
            int cellX = pixelX / cellSize;
            int cellY = pixelY / cellSize;

            // Accumulate xG in cell if valid
            if (cellX >= 0 && cellX < gridWidth && cellY >= 0 && cellY < gridHeight) {
                xgSum[cellX][cellY] += shot.getXGoal();
                if (xgSum[cellX][cellY] > maxXG) {
                    maxXG = xgSum[cellX][cellY];
                }
            }
        }

        if (maxXG == 0) return; // nothing to draw

        // Draw circles with intensity proportional to xG values
        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                double total = xgSum[i][j];
                if (total > 0) {
                    float intensity = (float)(total / maxXG);
                    Color heatColor = interpolateColor(
                        new Color(255, 255, 0, 40),   // Yellow, low intensity
                        new Color(255, 0, 0, 180),    // Red, high intensity
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

    /**
     * Draws the legend on the right side showing the color gradient from low to high xG intensity.
     *
     * @param g2 Graphics2D context used for drawing
     */
    private void drawLegend(Graphics2D g2) {
        if (maxXG == 0) return;

        int legendWidth = 20;
        int legendHeight = 150;
        int x = getWidth() - legendWidth - 15;
        int y = 30;

        // Create a vertical gradient from red (high) to yellow (low)
        GradientPaint gp = new GradientPaint(
            x, y, new Color(255, 0, 0, 180),         // Red = High intensity
            x, y + legendHeight, new Color(255, 255, 0, 40) // Yellow = Low intensity
        );
        g2.setPaint(gp);
        g2.fillRect(x, y, legendWidth, legendHeight);

        // Draw border around legend
        g2.setColor(Color.BLACK);
        g2.drawRect(x, y, legendWidth, legendHeight);

        // Draw labels for legend
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString("xG Intensity", x - 60, y - 5);
        g2.drawString(String.format("%.2f", maxXG), x - 45, y + 15);             // Top = red
        g2.drawString("0", x - 10, y + legendHeight + 15);                       // Bottom = yellow
    }

    /**
     * Interpolates between two colors based on a fraction.
     *
     * @param start    The start color (fraction=0)
     * @param end      The end color (fraction=1)
     * @param fraction Value between 0 and 1 indicating interpolation amount
     * @return Interpolated color
     */
    private static Color interpolateColor(Color start, Color end, float fraction) {
        float r = start.getRed() + fraction * (end.getRed() - start.getRed());
        float g = start.getGreen() + fraction * (end.getGreen() - start.getGreen());
        float b = start.getBlue() + fraction * (end.getBlue() - start.getBlue());
        float a = start.getAlpha() + fraction * (end.getAlpha() - start.getAlpha());
        return new Color((int) r, (int) g, (int) b, (int) a);
    }
}