package nhl;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RinkPanel extends JPanel {

    private List<ShotData> shots; // shot coordinates (x: 0-200, y: 0-85)

    public RinkPanel(List<ShotData> shots) {
        this.shots = shots;
        setPreferredSize(new Dimension(1000, 425)); // 200x85 ft rink scaled to 1000x425 px
        setBackground(Color.WHITE);
    }

    public void setShots(List<ShotData> shots) {
        this.shots = shots;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawRink((Graphics2D) g);
        drawHeatmap((Graphics2D) g);
    }

    /**
     * Draws a realistic NHL rink including:
     * - Rounded corners
     * - Center line, blue lines, goal lines
     * - Creases
     * - Faceoff circles
     * - Trapezoids behind the nets
     */
    private void drawRink(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();

        double scaleX = width / 200.0;
        double scaleY = height / 85.0;

        // Rink boundary with rounded corners
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, width, height, (int)(scaleX * 28), (int)(scaleY * 28));
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(0, 0, width - 1, height - 1, (int)(scaleX * 28), (int)(scaleY * 28));

        // Center red line
        g2.setColor(Color.RED);
        int centerX = (int)(100 * scaleX);
        g2.fillRect(centerX - 2, 0, 4, height);

        // Blue lines (between center and goals)
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

        // Goal creases
        g2.setColor(new Color(173, 216, 230)); // light blue
        int creaseRadius = (int)(6 * scaleX);
        g2.fillArc(goal1 - creaseRadius, height / 2 - creaseRadius, creaseRadius * 2, creaseRadius * 2, 270, 180);
        g2.fillArc(goal2 - creaseRadius, height / 2 - creaseRadius, creaseRadius * 2, creaseRadius * 2, 90, 180);

        // Trapezoids behind the nets (white fill, red outline)
        int trapWidth = (int)(40 * scaleX);
        int trapBottom = (int)(28 * scaleY);
        int trapTop = (int)(8 * scaleY);

        Polygon trapezoidLeft = new Polygon();
        trapezoidLeft.addPoint((int)(goal1), height / 2 - trapTop);
        trapezoidLeft.addPoint((int)(goal1 - trapWidth), height / 2 - trapBottom);
        trapezoidLeft.addPoint((int)(goal1 - trapWidth), height / 2 + trapBottom);
        trapezoidLeft.addPoint((int)(goal1), height / 2 + trapTop);
        g2.setColor(Color.WHITE);
        g2.fillPolygon(trapezoidLeft);
        g2.setColor(Color.RED);
        g2.drawPolygon(trapezoidLeft);

        Polygon trapezoidRight = new Polygon();
        trapezoidRight.addPoint((int)(goal2), height / 2 - trapTop);
        trapezoidRight.addPoint((int)(goal2 + trapWidth), height / 2 - trapBottom);
        trapezoidRight.addPoint((int)(goal2 + trapWidth), height / 2 + trapBottom);
        trapezoidRight.addPoint((int)(goal2), height / 2 + trapTop);
        g2.setColor(Color.WHITE);
        g2.fillPolygon(trapezoidRight);
        g2.setColor(Color.RED);
        g2.drawPolygon(trapezoidRight);

        // Faceoff circles (4 in zones, 1 at center)
        g2.setColor(Color.RED);
        int circleR = (int)(15 * scaleX);
        int[][] centers = {
            {(int)(31 * scaleX), (int)(22 * scaleY)},
            {(int)(31 * scaleX), (int)(63 * scaleY)},
            {(int)(169 * scaleX), (int)(22 * scaleY)},
            {(int)(169 * scaleX), (int)(63 * scaleY)},
            {(int)(100 * scaleX), (int)(42.5 * scaleY)} // Center
        };

        for (int[] center : centers) {
            g2.drawOval(center[0] - circleR, center[1] - circleR, 2 * circleR, 2 * circleR);
            g2.fillOval(center[0] - 2, center[1] - 2, 4, 4);
        }
    }

    /**
     * Draws heatmap showing shot intensity on the rink using grid cells with color intensity.
     */
    private void drawHeatmap(Graphics2D g2) {
        if (shots == null || shots.isEmpty()) return;

        double scaleX = getWidth() / 200.0;
        double scaleY = getHeight() / 85.0;

        int cellSize = 10;
        int gridWidth = (int) Math.ceil(getWidth() / cellSize);
        int gridHeight = (int) Math.ceil(getHeight() / cellSize);

        int[][] shotCounts = new int[gridWidth][gridHeight];

        // Count shots per grid cell
        for (ShotData shot : shots) {
            int cellX = (int)(shot.getX() * scaleX) / cellSize;
            int cellY = (int)(shot.getY() * scaleY) / cellSize;

            if (cellX >= 0 && cellX < gridWidth && cellY >= 0 && cellY < gridHeight) {
                shotCounts[cellX][cellY]++;
            }
        }

        // Find max count for normalization
        int maxCount = 1;
        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                if (shotCounts[i][j] > maxCount) {
                    maxCount = shotCounts[i][j];
                }
            }
        }

        // Draw heat circles per cell with interpolated color from faint yellow to bright red
        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                int count = shotCounts[i][j];
                if (count > 0) {
                    float intensity = (float) count / maxCount;
                    Color heatColor = interpolateColor(new Color(255, 255, 0, 40), new Color(255, 0, 0, 180), intensity);

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
     * Interpolates between two colors by fraction (0.0 - 1.0)
     */
    private static Color interpolateColor(Color start, Color end, float fraction) {
        float r = start.getRed() + fraction * (end.getRed() - start.getRed());
        float g = start.getGreen() + fraction * (end.getGreen() - start.getGreen());
        float b = start.getBlue() + fraction * (end.getBlue() - start.getBlue());
        float a = start.getAlpha() + fraction * (end.getAlpha() - start.getAlpha());
        return new Color((int) r, (int) g, (int) b, (int) a);
    }
}
  

