package test.nhl;

import nhl.Team;
import nhl.Player;
import org.junit.*;
import static org.junit.Assert.*;

import java.util.*;

public class TeamTest {

    private Player samplePlayer;

    @Before
    public void setUp() {
        samplePlayer = new Player(
            "Sample Player", "C", 1.0, 1.0,
            10, 5, 2, 3, 4, 8, 1, 0, 2, 3,
            60.0, 20, 600, 1, 2, 0.5, 1, 10
        );
    }

    @Test
    public void testGetTeamName() {
        Team team = new Team("Flames", Arrays.asList(samplePlayer));
        assertEquals("Flames", team.getName());
    }

    @Test
    public void testGetRosterSize() {
        Team team = new Team("Flames", Arrays.asList(samplePlayer));
        assertEquals(1, team.getRoster().size());
    }

    @Test
    public void testGetRosterPlayerName() {
        Team team = new Team("Flames", Arrays.asList(samplePlayer));
        assertEquals("Sample Player", team.getRoster().get(0).getName());
    }

    @Test
    public void testEmptyRoster() {
        Team team = new Team("EmptyTeam", new ArrayList<>());
        assertTrue(team.getRoster().isEmpty());
    }

    @Test
    public void testMultiplePlayersInRoster() {
        Player p2 = new Player("B", "D", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        Team team = new Team("Combo", Arrays.asList(samplePlayer, p2));
        assertEquals(2, team.getRoster().size());
    }

    @Test
    public void testGetNameEdgeCaseEmptyString() {
        Team team = new Team("", Arrays.asList(samplePlayer));
        assertEquals("", team.getName());
    }

    @Test
    public void testGetNameWhitespace() {
        Team team = new Team(" ", Arrays.asList(samplePlayer));
        assertEquals(" ", team.getName());
    }

    @Test
    public void testNullNameAllowed() {
        Team team = new Team(null, Arrays.asList(samplePlayer));
        assertNull(team.getName());
    }

    @Test
    public void testNullRosterAllowed() {
        Team team = new Team("GhostTeam", null);
        assertNull(team.getRoster());
    }

    @Test
    public void testLargeRosterSize() {
        List<Player> bigRoster = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            bigRoster.add(new Player("P" + i, "F", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, 0, 0));
        }
        Team team = new Team("BigTeam", bigRoster);
        assertEquals(1000, team.getRoster().size());
    }
}