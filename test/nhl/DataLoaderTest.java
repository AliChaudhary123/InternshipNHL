package test.nhl;


import org.junit.*;

import nhl.DataLoader;
import nhl.Player;
import nhl.Team;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;

public class DataLoaderTest {

    private File tempCsv;

    @After
    public void tearDown() {
        if (tempCsv != null && tempCsv.exists()) {
            tempCsv.delete();
        }
    }

    private File createTempCSV(String content) throws IOException {
        File file = File.createTempFile("test_players", ".csv");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(content);
        }
        return file;
    }

    @Test
    public void testLoadTeams_validSinglePlayer() throws IOException {
        String csv = "Header1,...,Strength,Name,Team,Position,...\n" +
                     "0,0,Player One,TestTeam,C,5on5," + "0,".repeat(105) +
                     "1," + "0,".repeat(27) + "2," + "0,".repeat(8) + "3," + "0";
        tempCsv = createTempCSV(csv);
        List<Team> teams = DataLoader.loadTeamsFromCSV(tempCsv.getAbsolutePath());
        assertEquals(1, teams.size());
    }

    @Test
    public void testLoadTeams_skipsNon5on5() throws IOException {
        String csv = "Header\n" +
                     "0,0,Player One,TestTeam,C,PowerPlay," + "0,".repeat(137);
        tempCsv = createTempCSV(csv);
        List<Team> teams = DataLoader.loadTeamsFromCSV(tempCsv.getAbsolutePath());
        assertTrue(teams.isEmpty());
    }

    @Test
    public void testLoadTeams_handlesMissingFieldsGracefully() throws IOException {
        String csv = "Header\n" +
                     "0,0,Player One,TestTeam,C,5on5"; // too short
        tempCsv = createTempCSV(csv);
        List<Team> teams = DataLoader.loadTeamsFromCSV(tempCsv.getAbsolutePath());
        assertTrue(teams.isEmpty());
    }

    @Test
    public void testLoadTeams_duplicatePlayerSkipped() throws IOException {
        String csv = "Header\n" +
                     "0,0,Player One,TeamA,C,5on5," + "0,".repeat(137) +
                     "\n0,0,Player One,TeamA,C,5on5," + "0,".repeat(137);
        tempCsv = createTempCSV(csv);
        List<Team> teams = DataLoader.loadTeamsFromCSV(tempCsv.getAbsolutePath());
        assertEquals(1, teams.get(0).getRoster().size());
    }

    @Test
    public void testFindPlayerByName_found() throws IOException {
        List<Team> teams = new ArrayList<>();
        Player p = new Player("Test", "C", 1.0, 2.0, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13.5, 14, 15, 16, 17, 18.0, 19, 20);
        teams.add(new Team("TeamX", Arrays.asList(p)));
        Player found = DataLoader.findPlayerByName("test", teams);
        assertEquals("Test", found.getName());
    }

    @Test
    public void testFindPlayerByName_notFound() {
        List<Team> teams = new ArrayList<>();
        teams.add(new Team("X", new ArrayList<>()));
        Player found = DataLoader.findPlayerByName("Ghost", teams);
        assertNull(found);
    }

    @Test
    public void testGetTeamNameForPlayer_found() {
        List<Team> teams = new ArrayList<>();
        Player p = new Player("Zorro", "RW", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        teams.add(new Team("Sharks", Arrays.asList(p)));
        String team = DataLoader.getTeamNameForPlayer("Zorro", teams);
        assertEquals("Sharks", team);
    }

    @Test
    public void testGetTeamNameForPlayer_notFound() {
        List<Team> teams = new ArrayList<>();
        teams.add(new Team("Sharks", new ArrayList<>()));
        String team = DataLoader.getTeamNameForPlayer("Unknown", teams);
        assertEquals("Unknown Team", team);
    }

    @Test
    public void testGetAllPlayerNames_emptyList() {
        List<Team> teams = new ArrayList<>();
        teams.add(new Team("None", new ArrayList<>()));
        List<String> names = DataLoader.getAllPlayerNames(teams);
        assertTrue(names.isEmpty());
    }

    @Test
    public void testGetAllPlayerNames_multipleTeams() {
        Player p1 = new Player("A", "C", 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0, 0);
        Player p2 = new Player("B", "L", 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0, 0);
        List<Team> teams = Arrays.asList(new Team("One", Arrays.asList(p1)), new Team("Two", Arrays.asList(p2)));
        List<String> names = DataLoader.getAllPlayerNames(teams);
        assertEquals(Arrays.asList("A", "B"), names);
    }
}