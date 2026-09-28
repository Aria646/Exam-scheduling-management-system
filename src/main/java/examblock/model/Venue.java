package examblock.model;

import examblock.view.components.Verbose;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an exam venue, consisting of one or more {@link Room}s.
 * [1] Implement the code according to the homework requirements
 * [2] Code according to Javadoc
 * [3][4] Comment in Javadoc style
 */
public class Venue implements StreamManager, ManageableListItem {

    /**
     * The identifier for the venue (e.g. "E101" or "L1+)
     */
    private String id;
    /**
     * the number of rooms used in the venue; must be one of 1, 2
     */
    private int roomCount;
    /**
     * the list of rooms used in the venue
     */
    private List<Room> rooms;
    /**
     * the number of rows and columns of Desks
     */
    private int rows;
    /**
     * the number of columns of Desks
     */
    private int columns;
    /**
     * the total number of Desks in the venue
     */
    private int totalDesks;
    /**
     * whether the venue is to be used for AARA
     */
    private boolean aara;

    /**
     * Constructs a new {@code Venue} object, consisting of one or more {@link Room}s.
     *
     * @param id         a String identifier for the venue (e.g. "E101" or "L1+L2").
     * @param roomCount  the number of rooms used in the venue; must be one of 1, 2, or 3.
     * @param rooms      the list of room objects - there must be at least one room.
     * @param rows       the number of rows of Desks, rows run across the room, counted
     *                   front to back.
     * @param columns    the number of columns of Desks, columns run front to back,
     *                   counted left to right.
     * @param totalDesks the total available Desks (may be less than rows x columns).
     * @param aara       the venue is to be used for AARA exam sessions.
     * @param registry   the global object registry, needed to resolve textual Subject names
     */
    public Venue(String id, int roomCount, RoomList rooms, int rows, int columns, int totalDesks,
                 boolean aara, Registry registry) {
        if (roomCount < 1 || roomCount > 3) {
            throw new IllegalArgumentException("roomCount must be 1, 2, or 3");
        }
        this.id = id;
        this.roomCount = roomCount;
        this.rooms = new ArrayList<>(rooms.all());
        if (this.rooms.size() != roomCount) {
            throw new IllegalArgumentException("Number of rooms does not match roomCount");
        }
        this.rows = rows;
        this.columns = columns;
        this.totalDesks = totalDesks;
        this.aara = aara;
        registry.add(this, Venue.class);
    }

    /**
     * Constructs a Venue by reading a description from a text stream
     *
     * @param br       BufferedReader opened and ready to read from
     * @param registry the global object registry, needed to resolve textual Subject names
     * @param nthItem  the index number of this serialized object
     * @throws IOException      on any read failure
     * @throws RuntimeException on any logic related issues
     */
    public Venue(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        streamIn(br, registry, nthItem);
        registry.add(this, Venue.class);
    }

    /**
     * Used to write data to the disk.
     *
     * @param bw      writer, already opened.
     * @param nthItem a number representing this item's position in the stream.
     * @throws IOException on any stream related issues
     */
    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        bw.write(nthItem + ". " + id + " (" + totalDesks + " desks)" + System.lineSeparator());
        StringBuilder sb = new StringBuilder("Room Count: " + roomCount + ", Rooms: ");
        for (Room r : rooms) {
            sb.append(r.roomId()).append(" ");
        }
        sb.append(", Rows: ").append(rows).append(", Columns: ").append(columns)
                .append(", Desks: ").append(totalDesks).append(", AARA: ").append(aara);
        bw.write(sb.toString() + System.lineSeparator());
    }

    /**
     * Used to read data from the disk.
     *
     * @param br       reader, already opened.
     * @param registry the global object registry
     * @param nthItem  a number representing this item's position in the stream
     * @throws RuntimeException on any logic related issues
     */
    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem)
            throws RuntimeException {
        // First line: "7. W1+W2 (15 desks)"
        String header = Utilities.getLine(br);
        if (header == null) {
            throw new RuntimeException("EOF reading Venue #" + nthItem);
        }
        String[] parts = header.split("\\. ");
        int idx = Utilities.toInt(parts[0], "Invalid index in Venue");
        if (idx != nthItem) {
            throw new RuntimeException("Venue index out of sync");
        }
        // Extract id and totalDesks from first line
        String idAndDesks = parts[1];
        int leftParen = idAndDesks.indexOf('(');
        if (leftParen == -1) {
            throw new RuntimeException("Missing '(' in Venue header");
        }
        this.id = idAndDesks.substring(0, leftParen).trim();
        String second = Utilities.getLine(br);
        if (second == null) {
            throw new RuntimeException("EOF reading Venue details #" + nthItem);
        }
        String[] pairs = second.split(", ");
        int roomCountTemp = 0;
        List<String> roomIds = new ArrayList<>();
        int rowsTemp = 0;
        int colsTemp = 0;
        int desksTemp = 0;
        boolean aaraTemp = false;
        for (String pair : pairs) {
            String[] kv = Utilities.keyValuePair(pair);
            if (kv == null) {
                continue;
            }
            switch (kv[0]) {
                case "Room Count":
                    roomCountTemp = Utilities.toInt(kv[1], "Invalid room count");
                    break;
                case "Rooms":
                    String[] ids = kv[1].split(" ");
                    for (String rid : ids) {
                        if (!rid.isEmpty()) {
                            roomIds.add(rid);
                        }
                    }
                    break;
                case "Rows":
                    rowsTemp = Utilities.toInt(kv[1], "Invalid rows");
                    break;
                case "Columns":
                    colsTemp = Utilities.toInt(kv[1], "Invalid columns");
                    break;
                case "Desks":
                    desksTemp = Utilities.toInt(kv[1], "Invalid desks");
                    break;
                case "AARA":
                    aaraTemp = Boolean.parseBoolean(kv[1]);
                    break;
            }
        }
        // Validate consistency
        if (roomCountTemp != roomIds.size()) {
            throw new RuntimeException("Room count mismatch in Venue #" + nthItem);
        }
        String desksPart = idAndDesks.substring(leftParen + 1).split("\\s+")[0].trim();
        int desksFromHeader = Utilities.toInt(desksPart, "Invalid desks number in header");
        if (desksFromHeader != desksTemp) {
            throw new RuntimeException("Desk count mismatch between header and details");
        }
        this.roomCount = roomCountTemp;
        this.rooms = new ArrayList<>();
        for (String rid : roomIds) {
            Room r = registry.find(rid, Room.class);
            if (r == null) {
                throw new RuntimeException("Room not found: " + rid);
            }
            this.rooms.add(r);
        }
        this.rows = rowsTemp;
        this.columns = colsTemp;
        this.totalDesks = desksTemp;
        this.aara = aaraTemp;

        if (Verbose.isVerbose()) {
            System.out.println("Loaded Venue: " + id);
        }
    }

    /**
     * Get the venue id.
     *
     * @return the venue id.
     */
    public String venueId() {
        return id;
    }

    /**
     * Get the rooms in this venue.
     *
     * @return the rooms in this venue.
     */
    public List<Room> getRooms() {
        return new ArrayList<>(rooms);
    }

    /**
     * Get the number of rows in this venue.
     *
     * @return the number of rows in this venue.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Get the number of columns in this venue.
     *
     * @return the number of columns in this venue.
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Get the total number of desks in this venue.
     *
     * @return the total number of desks in this venue.
     */
    public int deskCount() {
        return totalDesks;
    }

    /**
     * Get whether this venue is an AARA venue or not.
     *
     * @return true if this venue is an AARA venue.
     */
    public boolean isAara() {
        return aara;
    }

    /**
     * Check if the venue type is AARA or not. Print the appropriate message if the type
     * doesn't match.
     *
     * @param aara the venue is to be used for AARA exam sessions.
     * @return True if this venue is the same AARA type as the parameter.
     */
    public boolean checkVenueType(boolean aara) {
        if (this.aara == aara) {
            System.out.println(aara ? "This is an AARA venue." : "This is NOT an AARA venue.");
            return true;
        }
        return false;
    }

    /**
     * Checks if numberStudents will fit in this venue.
     *
     * @param numberStudents the number of students to test
     * @return True if numberStudents will fit in this venue.
     */
    public boolean willFit(int numberStudents) {
        if (numberStudents <= totalDesks) {
            return true;
        }
        System.out.println("This venue only has " + totalDesks + " desks, "
                + numberStudents + " students will not fit in this venue!");
        return false;
    }

    @Override
    public String getFullDetail() {
        return id + "\nRoom Count: " + roomCount + ", Rooms: " + rooms
                + ", Rows: " + rows + ", Columns: " + columns
                + ", Desks: " + totalDesks + ", AARA: " + aara;
    }

    /**
     * Get the venue details as a table row.
     *
     * @return the venue details as a table row.
     */
    @Override
    public Object[] toTableRow() {
        return new Object[]{id, roomCount, rows, columns, totalDesks, aara ? "Yes" : "No"};
    }

    /**
     * Get the venue id.
     *
     * @return the venue id.
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Get the venue id.
     *
     * @return the venue id.
     */
    @Override
    public String toString() {
        return id;
    }

    /**
     * Check if this venue is equal to another venue.
     *
     * @param o the other venue
     * @return true if this venue is equal to the other venue.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Venue venue = (Venue) o;
        return id.equals(venue.id);
    }

    /**
     * Get the hash code of this venue.
     *
     * @return the hash code of this venue.
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}