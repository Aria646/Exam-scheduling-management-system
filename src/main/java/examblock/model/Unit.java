package examblock.model;

import examblock.view.components.Verbose;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Objects;

/**
 * An object describing a single-semester Year 12 Unit of a Year 12 Subject.
 * These are typically Unit 3 or Unit 4 for the Year 12 units, but may be different.
 * [1] Implement the code according to the homework requirements
 * [2] Code according to Javadoc
 * [3][4] Comment in Javadoc style
 */
public class Unit implements StreamManager, ManageableListItem {

    /**
     * The parent subject of this unit.
     */
    private Subject subject;
    /**
     * The unit identifier of this unit.
     */
    private Character unitId;
    /**
     * The title of this unit.
     */
    private String title;
    /**
     * The description of this unit.
     */
    private String description;

    /**
     * Constructs a new {@link Subject} {@code Unit} object.
     * Consists of a parent {@link Subject},
     * the applicable {@code unitId} (typically '3' or '4' for Year 12),
     * as a single character (i.e. '0' to '9' or 'A' to 'Z');
     * a unit {@code title} that may be multiple (optionally capitalised) words,
     * including numbers (in words or digits) and/or Roman numerals (I,IV, etc.),
     * each separated by a SINGLE space, with NO leading or trailing spaces and
     * no trailing full stop (.), but other internal punctuation may be present -
     * ({@code title}s supplied with multiple spaces or leading or trailing spaces
     * must be rectified); AND a {@code description}, in whole English sentences,
     * each beginning with a capital letter and finishing with a full stop.
     *
     * @param subject    the parent subject of this unit.
     * @param unitId     the single character unit identifier of this unit.
     * @param title      the string title of this unit,
     *                   consisting of one or more capitalised words
     *                   separated by one or more spaces or other punctuation.
     * @param description the string description of this unit, in whole sentences,
     *                   each beginning with a capital and finishing with a full stop,
     *                   with words separated by one or more spaces or other punctuation.
     * @param registry   the global object registry
     */
    public Unit(Subject subject, Character unitId, String title, String description,
                Registry registry) {
        this.subject = subject;
        this.unitId = unitId;
        this.title = sanitiseTitle(title);
        this.description = sanitiseDescription(description);
        registry.add(this, Unit.class);
    }

    /**
     * Constructs a Unit by reading a description from a text stream
     *
     * @param br       BufferedReader opened and ready to read from
     * @param registry the global object registry, needed to resolve textual Subject names
     * @param nthItem  the index number of this serialized object
     * @throws IOException      on any read failure
     * @throws RuntimeException on any logic related issues
     */
    public Unit(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        streamIn(br, registry, nthItem);
        registry.add(this, Unit.class);
    }

    private String sanitiseTitle(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim().replaceAll("\\s+", " ");
        if (trimmed.endsWith(".")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private String sanitiseDescription(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        if (trimmed.length() > 0) {
            trimmed = Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
        }
        if (!trimmed.endsWith(".")) {
            trimmed = trimmed + ".";
        }
        return trimmed;
    }

    /**
     * Used to write data to the disk.<br>
     * <br>
     * The format of the text written to the stream must be matched exactly by streamIn, so it
     * is very important to format the output as described.<br>
     * <br>
     * 3. ANCIENT HISTORY<br>
     * Ancient History, Unit 3: Reconstructing the Ancient World<br>
     * "Investigate significant historical periods through an analysis of relevant archaeological
     * and written sources."<br>
     *
     * @param bw      writer, already opened. Your data should be written at the current file
     *                position
     * @param nthItem a number representing this item's position in the stream. Used for
     *                sanity checks
     * @throws IOException on any stream related issues
     */
    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        bw.write(nthItem + ". " + subject.getTitle().toUpperCase() + "\n");
        bw.write(subject.getTitle() + ", Unit " + unitId + ": " + title + "\n");
        bw.write("\"" + description + "\"\n");
    }

    /**
     * Used to read data from the disk. IOExceptions and RuntimeExceptions must be allowed
     * to propagate out to the calling method, which co-ordinates the streaming. Any other
     * exceptions should be converted to RuntimeExceptions and rethrown.<br>
     * <br>
     * For the format of the text in the input stream, refer to the {@code streamOut} documentation.
     *
     * @param br       reader, already opened.
     * @param registry the global object registry
     * @param nthItem  a number representing this item's position in the stream.
     *                 Used for sanity checks
     * @throws IOException      on any stream related issues
     * @throws RuntimeException on any logic related issues
     */
    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        // Line 1: "3. ANCIENT HISTORY"
        String heading = Utilities.getLine(br);
        if (heading == null) {
            throw new RuntimeException("EOF reading Unit #" + nthItem);
        }
        String[] headingParts = heading.split("\\. ");
        int idx = Utilities.toInt(headingParts[0], "Invalid index for Unit");
        if (idx != nthItem) {
            throw new RuntimeException("Unit index out of sync");
        }
        String subjectTitle = headingParts[1]; // e.g., "ANCIENT HISTORY"


        // Line 2: "Ancient History, Unit 3: Reconstructing the Ancient World"
        String secondLine = Utilities.getLine(br);
        if (secondLine == null) {
            throw new RuntimeException("EOF reading Unit second line #" + nthItem);
        }
        // Split: "Ancient History" , " Unit 3: Reconstructing the Ancient World"
        int commaIndex = secondLine.indexOf(',');
        if (commaIndex == -1) {
            throw new RuntimeException("Missing comma in Unit second line");
        }
        String subjTitle = secondLine.substring(0, commaIndex).trim();
        this.subject = registry.get(subjTitle, Subject.class);
        String rest = secondLine.substring(commaIndex + 1).trim();
        if (!subjTitle.equalsIgnoreCase(subject.getTitle())) {
            throw new RuntimeException("Subject title mismatch in Unit #" + nthItem);
        }
        // rest format: "Unit 3: Title"
        int colonIndex = rest.indexOf(':');
        if (colonIndex == -1) {
            throw new RuntimeException("Missing colon in Unit second line");
        }
        String unitPart = rest.substring(0, colonIndex).trim(); // "Unit 3"
        String[] unitWords = unitPart.split(" ");
        if (unitWords.length < 2) {
            throw new RuntimeException("Invalid unit identifier");
        }
        this.unitId = unitWords[1].charAt(0);
        this.title = sanitiseTitle(rest.substring(colonIndex + 1).trim());

        // Line 3: "\"description\""
        String thirdLine = Utilities.getLine(br);
        if (thirdLine == null) {
            throw new RuntimeException("EOF reading Unit description #" + nthItem);
        }
        if (thirdLine.startsWith("\"") && thirdLine.endsWith("\"")) {
            this.description = sanitiseDescription(thirdLine.substring(1, thirdLine.length() - 1));
        } else {
            throw new RuntimeException("Unit description not enclosed in quotes");
        }

        if (Verbose.isVerbose()) {
            System.out.println("Loaded Unit: " + subject.getTitle()
                    + " Unit " + unitId + ": " + title);
        }
    }

    /**
     * Returns a detailed string representation of this unit
     *
     * @return a detailed string representation of this unit.
     */
    @Override
    public String getFullDetail() {
        return subject.getTitle() + ", Unit " + unitId + ": " + title + "\n\"" + description + "\"";
    }

    /**
     * return an Object[] containing class values suitable for use in the view model
     *
     * @return an Object[] containing class values suitable for use in the view model
     */
    @Override
    public Object[] toTableRow() {
        return new Object[]{subject.getTitle(), unitId, title, description};
    }

    /**
     * Return a unique string identifying us
     *
     * @return a unique string identifying us
     */
    @Override
    public String getId() {
        return subject.getTitle() + "-" + unitId;
    }

    /**
     * Gets the parent {@link Subject} of this unit.
     *
     * @return the reference to the unit's parent subject.
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     * Returns the identifier of this unit.
     *
     * @return the identifier of this unit.
     */
    public Character id() {
        return unitId;
    }

    /**
     * Gets the text {@code description} of the unit.
     *
     * @return the string {@code description} of the unit.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns a brief string representation of this unit.
     *
     * @return the unitID and title of this unit.
     */
    @Override
    public String toString() {
        return "Unit " + unitId + ": " + title;
    }

    /**
     * class specific equals method
     *
     * @param o the other object
     * @return true if they match, field for field, otherwise false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Unit unit = (Unit) o;
        return Objects.equals(subject, unit.subject)
                && Objects.equals(unitId, unit.unitId)
                && Objects.equals(title, unit.title)
                && Objects.equals(description, unit.description);
    }

    /**
     * return the hash value of this object
     *
     * @return the hash value of this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(subject, unitId, title, description);
    }
}