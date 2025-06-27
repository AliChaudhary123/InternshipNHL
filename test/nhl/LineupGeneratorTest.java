package test.nhl;

import nhl.*;
import org.junit.*;
import java.util.*;
import static org.junit.Assert.*;

public class LineupGeneratorTest {

    private Player createPlayer(String name, String pos, int gp, double xga, double iceTime,
                                int hits, int takeaways, int giveaways, int blocked, int goals, int points,
                                double highDanger, int reboundGoals) {
        return new Player(name, pos, xga, 0.0, hits, takeaways, goals, points,
                blocked, 0, 0, giveaways, 0, 0, iceTime, 0, 0, 0, 0, highDanger, reboundGoals, gp);
    }

    @Test
    public void testLineupExcludesGoalies() {
        Player g = createPlayer("Goalie", "G", 82, 1.0, 1200, 0, 0, 0, 0, 0, 0, 0.0, 0);
        Team team = new Team("GoalieTeam", Arrays.asList(g));
        List<Player> result = LineupGenerator.getBestDefensiveLineup(team, "Someone");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testLineupExcludesLowGamesPlayed() {
        Player f = createPlayer("Forward", "C", 40, 1.0, 1200, 1, 1, 1, 1, 1, 1, 1.0, 1);
        Team team = new Team("ShortSeason", Arrays.asList(f));
        List<Player> result = LineupGenerator.getBestDefensiveLineup(team, "Target");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testLineupIncludesAllPositions() {
        Player l = createPlayer("L", "L", 82, 1, 1200, 1, 2, 1, 3, 1, 2, 0.5, 1);
        Player c = createPlayer("C", "C", 82, 1, 1200, 2, 1, 0, 2, 1, 2, 0.4, 1);
        Player r = createPlayer("R", "R", 82, 1, 1200, 2, 3, 2, 2, 3, 3, 0.3, 0);
        Player d1 = createPlayer("D1", "D", 82, 0.8, 1300, 5, 4, 1, 4, 0, 1, 0.2, 0);
        Player d2 = createPlayer("D2", "D", 82, 0.9, 1250, 6, 3, 2, 3, 0, 1, 0.1, 0);
        Team team = new Team("FullTeam", Arrays.asList(l, c, r, d1, d2));

        List<Player> lineup = LineupGenerator.getBestDefensiveLineup(team, "Enemy");
        assertEquals(5, lineup.size());

        Set<String> positions = new HashSet<>();
        for (Player p : lineup) positions.add(p.getPosition().toUpperCase());
        assertTrue(positions.containsAll(Arrays.asList("L", "C", "R", "D")));
    }

    @Test
    public void testFallbackForMissingPositions() {
        Player c = createPlayer("OnlyCenter", "C", 82, 1, 1200, 1, 1, 1, 1, 1, 1, 0.5, 0);
        Player d1 = createPlayer("D1", "D", 82, 1, 1200, 1, 1, 1, 1, 1, 1, 0.5, 0);
        Player d2 = createPlayer("D2", "D", 82, 1, 1200, 1, 1, 1, 1, 1, 1, 0.5, 0);
        Team team = new Team("Partial", Arrays.asList(c, d1, d2));
        List<Player> lineup = LineupGenerator.getBestDefensiveLineup(team, "Target");
        assertTrue(lineup.contains(c));
        assertEquals(3, lineup.size()); // only 1 forward + 2 defensemen
    }

    @Test
    public void testCompositeScoreBasic() {
        Player p = createPlayer("Comp", "C", 82, 1.0, 60, 5, 4, 2, 3, 2, 5, 1.0, 1);
        double score = LineupGenerator.getPlayerCompositeScore(p, null, 0.7, 0.3, 0.0);
        assertTrue(score > 0); // Not testing exact value, just validity
    }

    @Test
    public void testCompositeScoreWithThreatBoost() {
        Player p = createPlayer("ToughGuy", "D", 82, 1.0, 60, 5, 4, 2, 3, 2, 5, 1.0, 1);
        Player threat = createPlayer("Star", "C", 82, 0, 1000, 0, 0, 0, 0, 20, 40, 10.0, 3);
        double boosted = LineupGenerator.getPlayerCompositeScore(p, threat, 0.7, 0.3, 1.0);
        double normal = LineupGenerator.getPlayerCompositeScore(p, threat, 0.7, 0.3, 0.0);
        assertTrue(boosted > normal);
    }

    @Test
    public void testCompositeScoreHandlesZeroIceTime() {
        Player p = createPlayer("Zero", "C", 82, 1.0, 0.0, 1, 1, 0, 1, 0, 0, 0.0, 0);
        double score = LineupGenerator.getPlayerCompositeScore(p, null, 0.7, 0.3, 0.0);
        assertFalse(Double.isInfinite(score));
        assertFalse(Double.isNaN(score));
    }

    @Test
    public void testBestLineupAgainstTargetWithThreat() {
        Player enemy = createPlayer("Enemy", "C", 82, 1.0, 1000, 0, 0, 0, 0, 10, 20, 5.0, 2);
        Player d1 = createPlayer("D1", "D", 82, 0.5, 1200, 5, 5, 1, 5, 0, 1, 0.5, 0);
        Player d2 = createPlayer("D2", "D", 82, 0.6, 1100, 6, 4, 2, 6, 0, 1, 0.4, 0);
        Player c = createPlayer("Center", "C", 82, 0.9, 1000, 2, 2, 2, 2, 1, 2, 0.3, 0);
        Team team = new Team("Opposing", Arrays.asList(enemy, d1, d2, c));

        List<Team> allTeams = Collections.singletonList(new Team("TargetTeam", Arrays.asList(enemy)));
        List<Player> result = LineupGenerator.getBestDefensiveLineup(team, "Enemy", allTeams);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testLineupAgainstUnknownTargetStillWorks() {
        Player p = createPlayer("Someone", "C", 82, 1, 1200, 1, 1, 1, 1, 1, 1, 1, 1);
        Team team = new Team("NoTarget", Arrays.asList(p));
        List<Player> result = LineupGenerator.getBestDefensiveLineup(team, "Nonexistent", null);
        assertEquals(1, result.size());
    }
}