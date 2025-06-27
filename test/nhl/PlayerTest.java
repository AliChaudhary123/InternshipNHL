package test.nhl;

import nhl.Player;
import org.junit.*;
import static org.junit.Assert.*;

public class PlayerTest {

    private Player player;

    @Before
    public void setUp() {
        player = new Player(
            "Connor McTest", "C", 1.5, 0.75,
            100, 25, 20, 50, 10, 200, 30, 5, 40, 30,
            1234.5, 400, 3600, 10, 20, 2.5, 3, 82
        );
    }

    @Test
    public void testGetName() {
        assertEquals("Connor McTest", player.getName());
    }

    @Test
    public void testGetPosition() {
        assertEquals("C", player.getPosition());
    }

    @Test
    public void testExpectedGoalsAgainst() {
        assertEquals(1.5, player.getExpectedGoalsAgainst(), 0.001);
    }

    @Test
    public void testOnIceXGAper60() {
        assertEquals(0.75, player.getOnIceExpectedGoalsAgainstPer60(), 0.001);
    }

    @Test
    public void testTakeawayEfficiencyScoreSetAndGet() {
        player.setTakeawayEfficiencyScore(1.234);
        assertEquals(1.234, player.getTakeawayEfficiencyScore(), 0.001);
    }

    @Test
    public void testIceTimeBoundary() {
        assertEquals(1234.5, player.getIceTime(), 0.001);
    }

    @Test
    public void testZeroPenaltiesEdgeCase() {
        assertEquals(10, player.getPenalties()); // not zero here, just checking field
    }

    @Test
    public void testNegativeGiveaways() {
        Player p = new Player("Neg", "D", 0, 0, 0, 0, 0, 0, 0, 0, 0, -5, 0, 0, 0, 0, 0, 0, 0, 0.0, 0, 0);
        assertEquals(-5, p.getGiveaways());
    }

    @Test
    public void testMaxShifts() {
        Player p = new Player("Max", "W", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Integer.MAX_VALUE, 0, 0, 0, 0.0, 0, 0);
        assertEquals(Integer.MAX_VALUE, p.getShifts());
    }

    @Test
    public void testZeroHighDangerXGoals() {
        Player p = new Player("ZeroXG", "G", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, 0, 0);
        assertEquals(0.0, p.getHighDangerxGoals(), 0.001);
    }
}