package examblock.model;

import examblock.view.components.Verbose;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * An object describing a single {@link Exam} {@code Session}.
 * [1] Implement the code according to the homework requirements
 * [2] Code according to Javadoc
 * [3][4] Comment in Javadoc style
 */
public class Session implements StreamManager, ManageableListItem {

    /**
     * The registry
     */
    private final Registry registry;
    /**
     * The venue
     */
    private Venue venue;
    /**
     * The session number
     */
    private int sessionNumber;
    /**
     * The day
     */
    private LocalDate day;
    /**
     * The start time
     */
    private LocalTime start;
    /**
     * The exams
     */
    private List<Exam> exams;
    /**
     * The desks
     */
    private Desk[][] desks;
    /**
     * The cached student count
     */
    private int cachedStudentCount;

    /**
     * Constructs a Session.
     *
     * @param venue         venue
     * @param sessionNumber session number
     * @param day           day
     * @param start         start time
     * @param registry      registry
     */
    public Session(Venue venue, int sessionNumber, LocalDate day,
                   LocalTime start, Registry registry) {
        this.registry = registry;
        this.venue = venue;
        this.sessionNumber = sessionNumber;
        this.day = day;
        this.start = start;
        this.exams = new ArrayList<>();
        int rows = venue.getRows();
        int cols = venue.getColumns();
        this.desks = new Desk[rows][cols];
        int deskNum = 1;
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                desks[r][c] = new Desk(deskNum++);
            }
        }
        this.cachedStudentCount = -1;
        registry.add(this, Session.class);
    }

    /**
     * Constructs a Session.
     *
     * @param br       reader, already opened.
     * @param registry the global object registry
     * @param nthItem  a number representing this item's position in the stream. Used
     * @throws IOException      on any stream related issues
     * @throws RuntimeException on any logic related issues
     */
    public Session(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        this.registry = registry;
        streamIn(br, registry, nthItem);
        registry.add(this, Session.class);
    }

    /**
     * Writes data to the disk.
     *
     * @param bw      writer, already opened. Your data should be written at the current
     *                file position
     * @param nthItem a number representing this item's position in the stream. Used for sanity
     *                checks
     * @throws IOException on any stream related issues
     */
    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        bw.write(nthItem + ". Venue: " + venue.venueId() + ", Session Number: " + sessionNumber
                + ", Day: " + day + ", Start: " + start + ", Student Count: " + countStudents()
                + ", Exams: " + exams.size() + System.lineSeparator());
        for (Exam exam : exams) {
            bw.write(exam.getShortTitle() + System.lineSeparator());
            List<Desk> desksForExam = new ArrayList<>();
            for (int c = 0; c < desks[0].length; c++) {
                for (int r = 0; r < desks.length; r++) {
                    Desk d = desks[r][c];
                    if (d.deskExam().equals(exam.getShortTitle()) && d.deskLui() != 0) {
                        desksForExam.add(d);
                    }
                }
            }
            bw.write("[Desks: " + desksForExam.size() + "]" + System.lineSeparator());
            for (Desk d : desksForExam) {
                d.streamOut(bw);
            }
        }
    }

    /**
     * Reads data from the disk.
     * @param br       reader, already opened
     * @param registry the global object registry
     * @param nthItem  a number representing this item's position in the stream. Used for sanity
     *                 checks
     * @throws IOException      on any stream related issues
     * @throws RuntimeException on any logic related issues
     */
    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        String header = Utilities.getLine(br);
        if (header == null) {
            throw new RuntimeException("EOF reading Session #" + nthItem);
        }
        String[] parts = header.split("\\. ");
        int idx = Utilities.toInt(parts[0], "Invalid index for Session");
        if (idx != nthItem) {
            throw new RuntimeException("Session index out of sync");
        }
        String[] pairs = parts[1].split(", ");
        Venue v = null;
        int sessNum = 0;
        int examsNumber = 0;
        LocalDate d = null;
        LocalTime t = null;
        for (String pair : pairs) {
            String[] kv = Utilities.keyValuePair(pair);
            if (kv == null) {
                continue;
            }
            switch (kv[0]) {
                case "Venue" -> v = registry.get(kv[1], Venue.class);
                case "Session Number" ->
                    sessNum = Utilities.toInt(kv[1], "Invalid session number");
                case "Day" ->
                    d = LocalDate.parse(kv[1]);
                case "Start" ->
                    t = LocalTime.parse(kv[1]);
                case "Exams" ->
                    examsNumber = Utilities.toInt(kv[1], "Invalid session number");
            }
        }
        if (v == null) {
            throw new RuntimeException("Venue not found for Session #" + nthItem);
        }
        this.venue = v;
        this.sessionNumber = sessNum;
        this.day = d;
        this.start = t;
        this.exams = new ArrayList<>();
        int rows = venue.getRows();
        int cols = venue.getColumns();
        this.desks = new Desk[rows][cols];
        int deskNum = 1;
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                desks[r][c] = new Desk(deskNum++);
            }
        }
        String line;
        while (examsNumber-- > 0 && (line = Utilities.getLine(br, true)) != null
                && !line.matches("^\\d+\\..*")) {
            String examTitle = Utilities.getLine(br);
            if (examTitle == null) {
                break;
            }
            Exam exam = registry.get(examTitle, Exam.class);
            exams.add(exam);
            String desksHeader = Utilities.getLine(br); // e.g., "[Desks: 6]"
            if (desksHeader == null) {
                break;
            }
            int numDesks = Utilities.toInt(desksHeader.substring(1, desksHeader.length() - 1)
                    .split(": ")[1], "Invalid desks count");
            for (int i = 0; i < numDesks; i++) {
                String deskLine = Utilities.getLine(br);
                if (deskLine == null) {
                    break;
                }
                String[] parts2 = deskLine.split(", ");
                int deskNumber = Utilities.toInt(parts2[0].split(": ")[1], "Invalid desk number");
                long lui = Long.parseLong(parts2[1].split(": ")[1]);
                String namePart = deskLine.substring(deskLine.indexOf("Name: ") + 6);
                String[] nameSplit = namePart.split(", ");
                String family = nameSplit[0];
                String givenInit = nameSplit[1];
                Student student = registry.find(String.valueOf(lui), Student.class);
                if (student != null) {
                    for (int r = 0; r < desks.length; r++) {
                        for (int c = 0; c < desks[0].length; c++) {
                            if (desks[r][c].deskNumber() == deskNumber) {
                                desks[r][c].setStudent(student);
                                desks[r][c].setExam(exam);
                                desks[r][c].setGivenAndInit(givenInit);
                                break;
                            }
                        }
                    }
                }
            }
        }
        cachedStudentCount = -1;
        if (Verbose.isVerbose()) {
            System.out.println("Loaded Session: Venue "
                    + venue.venueId() + ", session " + sessionNumber);
        }
    }

    /**
     * Gets the venue for this session.
     * @return the venue for this session
     */
    public Venue getVenue() {
        return venue;
    }

    /**
     * Gets the session number.
     * @return the session number
     */
    public int getSessionNumber() {
        return sessionNumber;
    }

    /**
     * Gets the date for this session.
     * @return the date for this session
     */
    public LocalDate getDate() {
        return day;
    }

    /**
     * Gets the time for this session.
     * @return the time for this session
     */
    public LocalTime getTime() {
        return start;
    }

    /**
     * Gets the exams for this session.
     * @return the exams for this session
     */
    public List<Exam> getExams() {
        return Collections.unmodifiableList(exams);
    }

    /**
     * Gets the total number of desks in this session.
     * @return the total number of desks in this session
     */
    public int getTotalDesks() {
        return desks.length * desks[0].length;
    }

    /**
     * Gets a desk.
     * @param row row number
     * @param column column number
     * @return  desk
     */
    public Desk getDesk(int row, int column) {
        return desks[row][column];
    }

    /**
     * Gets the total number of students in this session.
     * @return the total number of students in this session
     */
    public int countStudents() {
        if (cachedStudentCount < 0) {
            recomputeStudentCount();
        }
        return cachedStudentCount;
    }

    /**
     * Recomputes the total number of students in this session.
     */
    private void recomputeStudentCount() {
        if (exams.isEmpty()) {
            cachedStudentCount = 0;
            return;
        }
        List<Student> allStudents = registry.getAll(Student.class);
        int total = 0;
        for (Exam exam : exams) {
            for (Student s : allStudents) {
                if (s.isAara() == venue.isAara()
                        && s.getSubjects().find(exam.getSubject().getId()) != null) {
                    total++;
                }
            }
        }
        cachedStudentCount = total;
    }

    /**
     * Schedules an exam for this session.
     * @param exam exam to schedule
     */
    public void scheduleExam(Exam exam) {
        if (!exams.contains(exam)) {
            exams.add(exam);
            cachedStudentCount = -1;
        }
    }

    /**
     * Removes an exam from this session.
     * @param exam exam to remove
     */
    public void removeExam(Exam exam) {
        exams.remove(exam);
        for (int r = 0; r < desks.length; r++) {
            for (int c = 0; c < desks[0].length; c++) {
                if (desks[r][c].deskExam().equals(exam.getShortTitle())) {
                    desks[r][c].setStudent(null);
                    desks[r][c].setExam(null);
                    desks[r][c].setGivenAndInit("");
                }
            }
        }
        cachedStudentCount = -1;
    }

    /**
     * Allocates students to desks.
     * @param exams exams
     * @param cohort cohort
     */
    public void allocateStudents(ExamList exams, StudentList cohort) {
        // Collect all students who take any of the exams in this session and match AARA
        Set<Student> eligibleSet = new LinkedHashSet<>();
        for (Exam exam : this.exams) {
            for (Student s : cohort.all()) {
                if (s.isAara() == venue.isAara()
                        && s.getSubjects().find(exam.getSubject().getId()) != null) {
                    eligibleSet.add(s);
                }
            }
        }
        List<Student> ordered = new ArrayList<>(eligibleSet);
        ordered.sort(Comparator.comparing(Student::familyName).thenComparing(Student::givenNames));

        // Clear all desks first
        for (int r = 0; r < desks.length; r++) {
            for (int c = 0; c < desks[0].length; c++) {
                desks[r][c].setStudent(null);
                desks[r][c].setExam(null);
                desks[r][c].setGivenAndInit("");
            }
        }

        // Column‑by‑column assignment
        int rows = desks.length;
        int cols = desks[0].length;
        int idx = 0;
        for (int c = 0; c < cols && idx < ordered.size(); c++) {
            for (int r = 0; r < rows && idx < ordered.size(); r++) {
                Student s = ordered.get(idx);
                desks[r][c].setStudent(s);
                // Assign an exam (use the first exam the student is taking)
                for (Exam exam : this.exams) {
                    if (s.getSubjects().find(exam.getSubject().getId()) != null) {
                        desks[r][c].setExam(exam);
                        break;
                    }
                }
                idx++;
            }
        }
        cachedStudentCount = -1; // force recompute
    }

    /**
     * prints the desks
     */
    public void printDesks() {
        StringBuilder sb = new StringBuilder();
        printDesks(sb);
        System.out.print(sb.toString());
    }

    /**
     * prints the desks
     * @param sb string builder
     */
    public void printDesks(StringBuilder sb) {
        String title = "Venue " + venue.venueId();
        for (int i = 0; i < (20 * desks[0].length - title.length()) / 2; i++) {
            sb.append(" ");
        }
        sb.append(title).append(System.lineSeparator());
        int rows = desks.length;
        int cols = desks[0].length;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Desk d = desks[r][c];
                if (d.deskLui() == 0) {
                    sb.append(String.format("%-20s", ""));
                } else {
                    sb.append(String.format("%-20s", "Desk " + d.deskNumber() + ":"));
                }
            }
            sb.append(System.lineSeparator());
            for (int c = 0; c < cols; c++) {
                Desk d = desks[r][c];
                if (d.deskLui() != 0) {
                    sb.append(String.format("%-20s", d.deskFamilyName()));
                } else {
                    sb.append(String.format("%-20s", "empty"));
                }
            }
            sb.append(System.lineSeparator());
            for (int c = 0; c < cols; c++) {
                Desk d = desks[r][c];
                if (d.deskLui() != 0) {
                    sb.append(String.format("%-20s", d.deskGivenAndInit()));
                } else {
                    sb.append(String.format("%-20s", ""));
                }
            }
            sb.append(System.lineSeparator());
            sb.append(System.lineSeparator());
        }
    }

    /**
     * Returns a string representation of this session.
     * @return a string representation of this session.
     */
    @Override
    public String getFullDetail() {
        return "Venue: " + venue.venueId() + ", Session Number: " + sessionNumber
                + ", Day: " + day + ", Start: " + start + ", Student Count: " + countStudents();
    }

    /**
     * Returns the id of this session.
     * @return the id of this session.
     */
    @Override
    public String getId() {
        return venue.venueId() + "-" + sessionNumber;
    }

    /**
     * Returns a string representation of this session.
     * @return a string representation of this session.
     */
    @Override
    public String toString() {
        return "Session " + sessionNumber + " at " + venue.venueId() + " on " + day + " " + start;
    }

    /**
     * Checks if this session is equal to another session.
     * @param o the other session
     * @return true if this session is equal to the other session, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Session session = (Session) o;
        return sessionNumber == session.sessionNumber && venue.equals(session.venue);
    }

    /**
     * Returns a hash code value for this session.
     * @return a hash code value for this session.
     */
    @Override
    public int hashCode() {
        return Objects.hash(venue, sessionNumber);
    }
}