package examblock.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 * [6] implement VenueTest in JUnit
 */
public class VenueTest {
    private Registry registry;
    private RoomList roomList;
    private Venue venue;

    @Before
    public void setUp() {
        registry = new Registry();
        roomList = new RoomList(registry);
        Room r1 = new Room("S101", registry);
        Room r2 = new Room("S102", registry);
        roomList.add(r1);
        roomList.add(r2);
        venue = new Venue("W1+W2", 2, roomList, 3, 5, 15, true, registry);
    }

    @Test
    public void testGetters() {
        assertEquals("W1+W2", venue.venueId());
        assertEquals(2, venue.getRooms().size());
        assertEquals(3, venue.getRows());
        assertEquals(5, venue.getColumns());
        assertEquals(15, venue.deskCount());
        assertTrue(venue.isAara());
    }

    @Test
    public void testCheckVenueType() {
        assertTrue(venue.checkVenueType(true));
        assertFalse(venue.checkVenueType(false));
    }

    @Test
    public void testWillFit() {
        assertTrue(venue.willFit(10));
        assertTrue(venue.willFit(15));
        assertFalse(venue.willFit(20));
    }

    @Test
    public void testEqualsAndHashCode() {
        Registry tempRegistry = new Registry();
        Venue same = new Venue("W1+W2", 2, roomList, 3, 5, 15, true, tempRegistry);
        assertEquals(venue, same);
        assertEquals(venue.hashCode(), same.hashCode());
    }

    @Test
    public void testStreamOut() throws Exception {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.BufferedWriter bw = new java.io.BufferedWriter(sw);
        venue.streamOut(bw, 7);
        bw.flush();
        String output = sw.toString();
        assertTrue(output.contains("7. W1+W2 (15 desks)"));
        assertTrue(output.contains("Room Count: 2, Rooms: S101 S102"));
        assertTrue(output.contains("Rows: 3, Columns: 5, Desks: 15, AARA: true"));
    }

    @Test
    public void testStreamIn() throws Exception {
        registry.add(new Room("R1", registry), Room.class);
        String input = "3. V1 (10 desks)\n" +
                "Room Count: 1, Rooms: R1, Rows: 2, Columns: 5, Desks: 10, AARA: false\n";
        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.StringReader(input));
        Venue loaded = new Venue(br, registry, 3);
        assertEquals("V1", loaded.venueId());
        assertEquals(1, loaded.getRooms().size());
        assertEquals(2, loaded.getRows());
        assertEquals(5, loaded.getColumns());
        assertEquals(10, loaded.deskCount());
        assertFalse(loaded.isAara());
    }
}