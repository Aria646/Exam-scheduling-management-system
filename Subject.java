package examblock.model;

import examblock.view.components.Verbose;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Objects;

/**
 * An object describing a single Year 12 Subject.
 * [1] Implement the code according to the homework requirements
 * [2] Code according to Javadoc
 * [3][4] Comment in Javadoc style
 */
public class Subject implements StreamManager, ManageableListItem {

    /**
     * The title of this subject
     */
    private String title;
    /**
     * The description of this subject
     */
    private String description;

    /**
     * Constructs a new Year 12 Subject object.
     * Consists of a {@code title} that may be multiple capitalised words,
     * including numbers (in words or digits) and/or Roman numerals (I,IV, etc.),
     * each separated by a SINGLE space, with NO leading or trailing spaces and
     * no trailing full stop (.), but other internal punctuation may be present -
     * ({@code title}s supplied with multiple spaces or leading or trailing spaces
     * must be rectified); AND a {@code description}, in whole English sentences,
     * each beginning with a capital letter and finishing with a full stop.
     *
     * @param title       the string title of this subject, formatted as described above.
     * @param description the string description of this subject, in whole sentences,
     *                    each beginning with a capital and finishing with a full stop,
     *                    with words separated by one or more spaces or other punctuation.
     * @param registry    the global object registry
     */
    public Subject(String title, String description, Registry registry) {
        this.title = sanitiseTitle(title);
        this.description = sanitiseDescription(description);
        registry.add(this, Subject.class);
    }

    /**
     * Constructs a Subject by reading a description from a text stream
     *
     * @param br       BufferedReader opened and ready to read from
     * @param registry the global object registry
     * @param nthItem  the index number of this serialized object
     * @throws IOException      on any read failure
     * @throws RuntimeException on any logic related issues
     */
    public Subject(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        streamIn(br, registry, nthItem);
        registry.add(this, Subject.class);
    }

    /**
     * Used to write data to the disk.<br>
     * <br>
     * The format of the text written to the stream must be matched exactly by streamIn, so it
     * is very important to format the output as described.<br>
     * <br>
     * 1. ACCOUNTING<br>
     * Accounting<br>
     * "The study of the management of financial resources of the public sector, businesses,
     * and individuals."<br>
     *
     * @param bw      writer, already opened. Your data should be written at the current
     *                file position
     * @param nthItem a number representing this item's position in the stream. Used for sanity
     *                checks
     * @throws IOException on any stream related issues
     */
    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        bw.write(nthItem + ". " + title.toUpperCase() + "\n");
        bw.write(title + "\n");
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
     * @param nthItem  a number representing this item's position in the stream. Used for sanity
     *                 checks
     * @throws IOException      on any stream related issues.
     * @throws RuntimeException on any logic related issues
     */
    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {

        // 1. ACCOUNTING
        String heading = Utilities.getLine(br);
        if (heading == null) {
            throw new RuntimeException("EOF reading Subject #" + nthItem);
        }
        String[] bits = heading.split("\\. ");
        int index = Utilities.toInt(bits[0], "Number format exception parsing Subject "
                + nthItem + " header");
        if (index != nthItem) {
            throw new RuntimeException("Subject index out of sync!");
        }
        // bits[1] is the uppercase title, ignore it

        // second line: actual title
        String titleLine = Utilities.getLine(br);
        if (titleLine == null) {
            throw new RuntimeException("EOF reading Subject title #" + nthItem);
        }
        this.title = sanitiseTitle(titleLine);

        // third line: description in quotes
        String descLine = Utilities.getLine(br);
        if (descLine == null) {
            throw new RuntimeException("EOF reading Subject description #" + nthItem);
        }
        // remove surrounding quotes
        if (descLine.startsWith("\"") && descLine.endsWith("\"")) {
            descLine = descLine.substring(1, descLine.length() - 1);
        }
        this.description = sanitiseDescription(descLine);

        if (Verbose.isVerbose()) {
            System.out.println("Loaded Subject: " + this.title);
        }
    }

    /**
     * Returns a detailed string representation of this subject.
     * Returns the {@code title} in all uppercase, then on a new line,
     * the entire text {@code description} inside double quotes.
     *
     * @return a string representation of this subject.
     */
    @Override
    public String getFullDetail() {
        return title.toUpperCase() + "\n\"" + description + "\"";
    }

    /**
     * return an Object[] containing class values suitable for use in the view model
     *
     * @return an Object[] containing class values suitable for use in the view model
     */
    @Override
    public Object[] toTableRow() {
        return new Object[]{title};
    }

    /**
     * Return a unique string identifying us
     *
     * @return a unique string identifying us
     */
    @Override
    public String getId() {
        return title;
    }

    /**
     * Return a string from the input string, following these rules :-
     * <ul>
     *      <li>there may be multiple capitalised words,</li>
     *      <li>including numbers (in words or digits) and/or Roman numerals (I,IV, etc.),</li>
     *      <li>each separated by a SINGLE space,</li>
     *      <li>with NO leading or trailing spaces, and</li>
     *      <li>no trailing full stop (.), but other internal punctuation may be present</li>
     * </ul>
     *
     * @param text the string to sanitise
     * @return the sanitised string
     */
    public String sanitiseTitle(String text) {
        if (text == null) {
            return "";
        }
        // trim leading/trailing spaces, replace multiple spaces with single space
        String trimmed = text.trim().replaceAll("\\s+", " ");
        // remove trailing full stop if present
        while (trimmed.endsWith(".")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        String re = "";
        String[] splits = trimmed.split("\\s+");
        for (int i = 0; i < splits.length; i++) {
            re += Character.toUpperCase(splits[i].charAt(0))
                    + splits[i].substring(1).toLowerCase() + " ";
        }
        return re.substring(0, re.length() - 1);
    }

    /**
     * Return
     * <ul>
     *      <li>the string description of this subject, in whole sentences,</li>
     *      <li>each beginning with a capital and finishing with a full stop,</li>
     *      <li>with words separated by one or more spaces or other punctuation.</li>
     * </ul>
     *
     * @param text the string to sanitise
     * @return the sanitised string
     */
    public String sanitiseDescription(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        // ensure first character is uppercase
        if (trimmed.length() > 0) {
            trimmed = Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
        }
        // ensure ends with a full stop
        if (!trimmed.endsWith(".")) {
            trimmed = trimmed + ".";
        }
        return trimmed;
    }

    /**
     * Gets the {@code title} of this subject.
     *
     * @return the String title of this subject.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the text {@code description} of this subject.
     *
     * @return the String description of this subject.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns a brief string representation of this subject.
     * Returns the subject {@code title} in all uppercase.
     *
     * @return the subject title as a String in all uppercase and a newline.
     */
    @Override
    public String toString() {
        return title.toUpperCase();
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
        Subject other = (Subject) o;
        return Objects.equals(title, other.title)
                && Objects.equals(description, other.description);
    }

    /**
     * return the hash value of this object
     *
     * @return the hash value of this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(title, description);
    }
}