package examblock.model;

import examblock.view.components.Verbose;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * An object describing a single Year 12 Student.
 */
public class Student implements StreamManager, ManageableListItem {

    /**
     * The list of Subjects for this Student.
     */
    private final SubjectList subjects;
    /**
     * The list of Exams for this Student.
     */
    private final ExamList exams;
    /**
     * The student's 10-digit Learner Un
     */
    private Long lui;
    /**
     * the student's given names
     */
    private String givenNames;
    /**
     * the student's family name
     */
    private String familyName;
    /**
     * the student's date of birth
     */
    private LocalDate dob;
    /**
     * the student's house colour
     */
    private String house;
    /**
     * whether the student requires AARA adjustments
     */
    private Boolean aara;

    /**
     * Constructs a new Student object with no AARA requirements by default.
     *
     * @param lui        the student's 10-digit Learner Unique Identifier (LUI). The LUI
     *                   must be unique to each student throughout the entire cohort.
     * @param givenNames the initial given names for the student, which must be a
     *                   single string with one or more names. Names must contain
     *                   only alphabetic characters, hyphens, or apostrophes; and
     *                   multiple names must be separated by one or more spaces.
     *                   Any leading and trailing spaces are ignored.
     * @param familyName the initial family name for the student, which must be a
     *                   single string with one or more names. Names must contain
     *                   only alphabetic characters, hyphens, or apostrophes; and
     *                   multiple names must be separated by one or more spaces.
     *                   Any leading and trailing spaces are ignored.
     * @param day        the integer day of the date of birth for the student, which must
     *                   be a valid day for the month and year provided.
     * @param month      the integer month of the date of birth for the student, which
     *                   must be between 1 - 12 inclusive.
     * @param year       the 4-digit integer year of the date of birth for the student,
     *                   which must be between 1965 and 2015.
     * @param house      the initial house colour for the student, which must be one of:
     *                   Blue, Green, Red, White, or Yellow.
     * @param registry   the global registry, needed to resolve textual object names
     */
    public Student(Long lui, String givenNames, String familyName, int day,
                   int month, int year, String house, Registry registry) {
        this(lui, givenNames, familyName, day, month, year, house, false, registry);
    }

    /**
     * Constructs a new Student object with AARA requirements.
     * Overloaded constructor for a new Student requiring access arrangements
     * and reasonable adjustments.
     *
     * @param lui        the student's 10-digit Learner Unique Identifier (LUI). The LUI
     *                   must be unique to each student throughout the entire cohort.
     * @param givenNames the initial given names for the student, which must be a
     *                   single string with one or more names. Names must contain
     *                   only alphabetic characters, hyphens, or apostrophes; and
     *                   multiple names must be separated by one or more spaces.
     *                   Any leading and trailing spaces are ignored.
     * @param familyName the initial family name for the student, which must be a
     *                   single string with one or more names. Names must contain
     *                   only alphabetic characters, hyphens, or apostrophes; and
     *                   multiple names must be separated by one or more spaces.
     *                   Any leading and trailing spaces are ignored.
     * @param day        the integer day of the date of birth for the student, which must
     *                   be a valid day for the month and year provided.
     * @param month      the integer month of the date of birth for the student, which
     *                   must be between 1 - 12 inclusive.
     * @param year       the 4-digit integer year of the date of birth for the student,
     *                   which must be between 1965 and 2015.
     * @param house      the initial house colour for the student, which must be one of:
     *                   Blue, Green, Red, White, or Yellow.
     * @param aara       the initial aara setting for the student, true or false:
     *                   true requires AARA adjustments, false does not.
     * @param registry   the global registry, needed to resolve textual object names
     */
    public Student(Long lui, String givenNames, String familyName, int day, int month,
                   int year, String house, Boolean aara, Registry registry) {
        this.lui = lui;
        this.givenNames = sanitiseName(givenNames);
        this.familyName = sanitiseName(familyName);
        this.dob = LocalDate.of(year, month, day);
        this.house = house;
        this.aara = aara;
        this.subjects = new SubjectList(registry);
        this.exams = new ExamList(registry);
        registry.add(this, Student.class);
    }

    /**
     * Constructs an Exam by reading a description from a text stream
     *
     * @param br       BufferedReader opened and ready to read from
     * @param registry the global object registry, needed to resolve textual Subject names
     * @param nthItem  the index number of this serialized object
     * @throws IOException      on any read failure
     * @throws RuntimeException on any logic related issues
     */
    public Student(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        this.subjects = new SubjectList(registry);
        this.exams = new ExamList(registry);
        streamIn(br, registry, nthItem);

        registry.add(this, Student.class);
    }

    /**
     * Return a string from the input string complying with the following rules
     * <ul>
     *      <li>a single string with one or more names.</li>
     *      <li>names must contain only alphabetic characters, hyphens, or apostrophes</li>
     *      <li>multiple names must be separated by one or more spaces.</li>
     *      <li>any leading and trailing spaces are ignored</li>
     * </ul>
     *
     * @param text the string to sanitise
     * @return the sanitised string
     */
    public String sanitiseName(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim().replaceAll("\\s+", " ");
        // Remove characters that are not letters, hyphen, apostrophe, or space
        trimmed = trimmed.replaceAll("[^a-zA-Z\\-\\' ]", "");
        return trimmed;
    }

    /**
     * Used to write data to the disk.<br>
     * <br>
     * The format of the text written to the stream must be matched exactly by streamIn, so it
     * is very important to format the output as described.<br>
     * <br>
     * 1. LIAM ALEXANDER SMITH<br>
     * LUI: 9999365663, Family Name: Smith, Given Name(s): Liam Alexander,
     * Date of Birth: 2007-12-08, House: Blue, AARA: false<br>
     * Subjects: Essential English, Essential Mathematics, Ancient History,
     * Industrial Technology Skills, Trade Course, Another Trade Course<br>
     *
     * @param bw      writer, already opened. Your data should be written at the
     *                current file position
     * @param nthItem a number representing this item's position in the stream.
     *                Used for sanity checks
     * @throws IOException on any stream related issues
     */
    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        // Header line: index. FULL_NAME
        bw.write(nthItem + ". " + fullName() + "\n");
        // Detail line
        bw.write("LUI: " + lui + ", Family Name: " + familyName
                + ", Given Name(s): " + givenNames + ", Date of Birth: "
                + dob + ", House: " + house + ", AARA: " + aara + "\n");
        // Subjects line
        StringBuilder subjBuilder = new StringBuilder("Subjects: ");
        for (Subject s : subjects.all()) {
            subjBuilder.append(s.getTitle()).append(", ");
        }
        if (subjects.size() > 0) {
            subjBuilder.setLength(subjBuilder.length() - 2);
        }
        bw.write(subjBuilder.toString() + "\n");
    }

    /**
     * Used to read data from the disk. IOExceptions and RuntimeExceptions must be allowed
     * to propagate out to the calling method, which co-ordinates the streaming. Any other
     * exceptions should be converted to RuntimeExceptions and rethrown.<br>
     * <br>
     * For the format of the text in the input stream, refer to the {@code streamOut} documentation.
     *
     * @param br       reader, already opened. Y
     * @param registry the global object registry
     * @param nthItem  a number representing this item's position in the stream. Used for
     *                 sanity checks
     * @throws IOException      on any stream related issues
     * @throws RuntimeException on any logic related issues
     */
    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        // 1. LIAM ALEXANDER SMITH
        String heading = Utilities.getLine(br);
        if (heading == null) {
            throw new RuntimeException("EOF reading Student #" + nthItem);
        }
        String[] headingParts = heading.split("\\. ");
        int idx = Utilities.toInt(headingParts[0], "Invalid index for Student");
        if (idx != nthItem) {
            throw new RuntimeException("Student index out of sync");
        }

        String fullNameStr = headingParts[1]; // e.g., "LIAM ALEXANDER SMITH"
        // The format is "GIVEN_NAMES FAMILY_NAME" where family name is the last word
        String[] nameWords = fullNameStr.split(" ");
        this.familyName = sanitiseName(nameWords[nameWords.length - 1]);
        StringBuilder givenBuilder = new StringBuilder();
        for (int i = 0; i < nameWords.length - 1; i++) {
            givenBuilder.append(nameWords[i]).append(" ");
        }
        this.givenNames = sanitiseName(givenBuilder.toString().trim());

        String line = Utilities.getLine(br);
        if (line == null) {
            throw new RuntimeException("EOF reading Student details #" + nthItem);
        }
        String[] pairs = line.split(", ");
        for (String pair : pairs) {
            String[] kv = Utilities.keyValuePair(pair);
            if (kv == null) {
                continue;
            }
            switch (kv[0]) {
                case "LUI":
                    this.lui = Long.parseLong(kv[1]);
                    break;
                case "Family Name":
                    this.familyName = sanitiseName(kv[1]);
                    break;
                case "Given Name(s)":
                    this.givenNames = sanitiseName(kv[1]);
                    break;
                case "Date of Birth":
                    this.dob = LocalDate.parse(kv[1]);
                    break;
                case "House":
                    this.house = kv[1];
                    break;
                case "AARA":
                    this.aara = Boolean.parseBoolean(kv[1]);
                    break;
            }
        }

        // Subjects: Essential English, Essential Mathematics, ...
        String subjLine = Utilities.getLine(br);
        if (subjLine == null || !subjLine.startsWith("Subjects:")) {
            // No subjects line, may be empty
            return;
        }
        String subjectsStr = subjLine.substring(9).trim();
        if (!subjectsStr.isEmpty()) {
            String[] titles = subjectsStr.split(", ");
            for (String title : titles) {
                Subject subj = registry.find(title, Subject.class);
                if (subj != null) {
                    addSubject(subj);
                }
            }
        }

        if (Verbose.isVerbose()) {
            System.out.println("Loaded Student: " + fullName());
        }
    }

    /**
     * Creates and returns a string representation of this student's detailed state.
     *
     * @return the string representation of this student's detailed state.
     */
    @Override
    public String getFullDetail() {
        return fullName() + "\nLUI: " + lui + ", Family Name: " + familyName
                + ", Given Name(s): " + givenNames + ", Date of Birth: " + dob
                + ", House: " + house + ", AARA: " + aara + "\nSubjects: "
                + subjects;
    }

    /**
     * return an Object[] containing class values suitable for use in the view model
     *
     * @return an Object[] containing class values suitable for use in the view model
     */
    @Override
    public Object[] toTableRow() {
        return new Object[]{lui, fullName(), house,
            aara ? "Yes" : "No"};
    }

    /**
     * Return a unique string identifying us
     *
     * @return a unique string identifying us
     */
    @Override
    public String getId() {
        return String.valueOf(lui);
    }

    /**
     * Change the LUI of the student.
     *
     * @param lui the student's 10-digit Learner Unique Identifier (LUI). The LUI
     *            must be unique to each student throughout the entire cohort.
     */
    public void changeLui(Long lui) {
        this.lui = lui;
    }

    /**
     * Sets the given names of the student.
     *
     * @param givenNames the new given names for the student, which must be a
     *                   single string with one or more names. Names must contain
     *                   only alphabetic characters, hyphens, or apostrophes; and
     *                   multiple names must be separated by one or more spaces.
     *                   Any leading and trailing spaces are ignored.
     */
    public void setGiven(String givenNames) {
        this.givenNames = sanitiseName(givenNames);
    }

    /**
     * Sets the family name of the student.
     *
     * @param familyName the new family name for the student, which must be a
     *                   single string with one or more names. Names must contain
     *                   only alphabetic characters, hyphens, or apostrophes; and
     *                   multiple names must be separated by one or more spaces.
     *                   Any leading and trailing spaces are ignored.
     */
    public void setFamily(String familyName) {
        this.familyName = sanitiseName(familyName);
    }

    /**
     * Returns the LUI of the student.
     *
     * @return the LUI of the student.
     */
    public Long getLui() {
        return lui;
    }

    /**
     * Returns the given names of the student.
     *
     * @return the given names of the student.
     */
    public String givenNames() {
        return givenNames;
    }

    /**
     * Returns the first name of the student.
     *
     * @return the first name of the student.
     */
    public String firstName() {
        return givenNames.split(" ")[0];
    }

    /**
     * Returns the family name of the student.
     *
     * @return the family name of the student.
     */
    public String familyName() {
        return familyName;
    }

    /**
     * Returns the short name of the student.
     *
     * @return the short name of the student.
     */
    public String shortName() {
        return firstName() + " " + familyName;
    }

    /**
     * Returns the full name of the student.
     *
     * @return the full name of the student.
     */
    public String fullName() {
        return givenNames + " " + familyName;
    }

    /**
     * Returns the date of birth of the student.
     *
     * @return the date of birth of the student.
     */
    public LocalDate getDob() {
        return dob;
    }

    /**
     * Returns the house of the student.
     *
     * @return the house of the student.
     */
    public String getHouse() {
        return house;
    }

    /**
     * Returns the AARA status of the student.
     *
     * @return the AARA status of the student.
     */
    public Boolean isAara() {
        return aara;
    }

    /**
     * Returns the subjects of the student.
     *
     * @return the subjects of the student.
     */
    public SubjectList getSubjects() {
        return subjects;
    }

    /**
     * Returns the exams of the student.
     *
     * @return the exams of the student.
     */
    public ExamList getExams() {
        return exams;
    }

    /**
     * Adds a subject to this student.
     *
     * @param subject the Subject being added to this student.
     */
    public void addSubject(Subject subject) {
        subjects.add(subject);
    }

    /**
     * Adds a unit to this student.
     *
     * @param unit the Unit being added to this student.
     */
    public void addUnit(Unit unit) {
        if (unit != null) {
            addSubject(unit.getSubject());
        }
    }

    /**
     * Adds an exam to this student.
     *
     * @param exam the Exam being added to this student.
     */
    public void addExam(Exam exam) {
        exams.add(exam);
    }

    /**
     * Removes a subject from this student.
     *
     * @param subject the Subject being removed from this student.
     */
    public void removeSubject(Subject subject) {
        subjects.remove(subject);
    }

    /**
     * Creates and returns a string representation of this student's basic state.
     *
     * @return the string representation of this student's basic state.
     */
    @Override
    public String toString() {
        return fullName();
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
        Student student = (Student) o;
        return Objects.equals(lui, student.lui)
                && Objects.equals(givenNames, student.givenNames)
                && Objects.equals(familyName, student.familyName)
                && Objects.equals(dob, student.dob)
                && Objects.equals(house, student.house)
                && Objects.equals(aara, student.aara);
    }

    /**
     * return the hash value of this object
     *
     * @return the hash value of this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(lui, givenNames, familyName, dob, house, aara);
    }
}