package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Represents an individual desk in an exam venue.
 * [1] Implement the code according to the homework requirements
 * [2] Code according to Javadoc
 * [3][4] Comment in Javadoc style
 */
public class Desk {
    /**
     * The desk number.
     */
    private final int deskNumber;
    /**
     * The student assigned to this desk.
     */
    private Student student;
    /**
     * The exam being taken at this desk.
     */
    private Exam exam;
    /**
     * The first given name and initial of the student assigned to this desk.
     */
    private String givenAndInit;

    /**
     * Constructs a desk.
     * Assigns the integer deskNumber as the numerical identifier and
     * assigns empty Strings to the names.
     *
     * @param deskNumber the non-zero positive integer desk number.
     */
    public Desk(int deskNumber) {
        this.deskNumber = deskNumber;
        this.student = null;
        this.exam = null;
        this.givenAndInit = "";
    }

    /**
     * Gets the number of this desk.
     *
     * @return The number of this desk.
     */
    public int deskNumber() {
        return deskNumber;
    }

    /**
     * Return the student allocated to this desk
     *
     * @return the Student, or an empty string if not allocated
     */
    public String deskStudent() {
        return student == null ? "" : student.fullName();
    }

    /**
     * Gets the LUI of the student assigned to this desk.
     *
     * @return The LUI of the student assigned to this desk.
     */
    public long deskLui() {
        return student == null ? 0L : student.getLui();
    }

    /**
     * Gets the family name of the student assigned to this desk.
     *
     * @return The family name of the student assigned to this desk.
     */
    public String deskFamilyName() {
        return student == null ? "" : student.familyName();
    }

    /**
     * Gets the first given name and initial of the student assigned to this desk.
     * Gets the first given name, a space, the initial of first middle name, if any,
     * with a full stop after the initial (if present) of the student assigned to this desk.
     *
     * @return The first given name and initial of the student assigned to this desk.
     */

    public String deskGivenAndInit() {
        if (student != null) {
            String firstName = student.firstName();
            String[] givenNames = student.givenNames().split(" ");
            if (givenNames.length > 1) {
                return firstName + " " + givenNames[1].charAt(0) + ".";
            }
            return firstName;
        }
        return givenAndInit;
    }


    /**
     * Allocate a student to this desk
     *
     * @param student student to assign
     */
    public void setStudent(Student student) {
        this.student = student;
        if (student != null) {
            this.givenAndInit = deskGivenAndInit();
        } else {
            this.givenAndInit = "";
        }
    }

    /**
     * Manually change the allocated student's displayed name
     *
     * @param givenAndInit new name
     */
    public void setGivenAndInit(String givenAndInit) {
        this.givenAndInit = givenAndInit;
    }

    /**
     * Allocate an exam for this desk
     *
     * @param exam exam to allocate
     */
    public void setExam(Exam exam) {
        this.exam = exam;
    }

    /**
     * Return the exam being taken at this desk
     *
     * @return the exam at this desk
     */
    public String deskExam() {
        return exam == null ? "" : exam.getShortTitle();
    }

    /**
     * Returns a string representation of this desk.
     * (Returns the desk number and any assigned student.)
     *
     * @return The string representation of this desk.
     */
    @Override
    public String toString() {
        if (student == null) {
            return "Desk " + deskNumber + ": empty";
        }
        return "Desk " + deskNumber + ": " + student.familyName() + ", " + student.firstName();
    }

    /**
     * Write a string representation of this desk to disk.
     *
     * @param bw stream to write to
     * @throws IOException on any IO related issues
     */
    public void streamOut(BufferedWriter bw) throws IOException {
        bw.write("Desk: " + deskNumber);
        if (student != null) {
            bw.write(", LUI: " + student.getLui());
            bw.write(", Name: " + student.familyName() + ", " + givenAndInit);
        } else {
            bw.write(", Unallocated");
        }
        bw.write(System.lineSeparator());
    }

    /**
     * Read itself from an input stream
     *
     * @param br       stream to read from
     * @param examName Use this as the exam name value (saves duplicating it for each desk)
     * @throws IOException on any IO related issues
     */
    public void streamIn(BufferedReader br, String examName) throws IOException {
        // This method is called by Session to read desk lines.
        // The format expected: "Desk: 1, LUI: 9999831170, Name: Ahmad, Tariq N."
        // For simplicity, we just consume the line; actual parsing is done in Session.
        // Desk does not store exam directly in streamIn, only via setExam after construction.
        String line = br.readLine();
        if (line == null) {
            return;
        }
        // Line is ignored here because deserialization happens at Session level.
        // The Session will parse and call setStudent/setExam on the desk.
    }
}