package nhl;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RinkPanel extends JPanel {

    private List<ShotData> shots; // contains shot coordinates (x, y)

    public RinkPanel(List<ShotData> shots) {
        this.shots = shots;
        setPreferredSize(new Dimension(800, 400)); // standard NHL rink size ratio
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawRink(g);
        drawHeatmap(g);
    }

    private void drawRink(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.BLUE);
        g2.drawRect(50, 50, 700, 300); // rink outline

        // Add blue lines, red lines, goal creases etc. as needed
        g2.setColor(Color.RED);
        g2.drawLine(400, 50, 400, 350); // center line
    }

    private void drawHeatmap(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        if (shots == null) return;

        for (ShotData shot : shots) {
            int x = shot.getX();
            int y = shot.getY();

            // Convert to rink coordinates (scale down or shift as needed)
            int drawX = 50 + (int) (x * 7); // example scale factor
            int drawY = 50 + (int) (y * 3);

            // Draw a translucent red circle
            g2.setColor(new Color(255, 0, 0, 100)); // red with alpha
            g2.fillOval(drawX - 5, drawY - 5, 10, 10);
        }
    }
}