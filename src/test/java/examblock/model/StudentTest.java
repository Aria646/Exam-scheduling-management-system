package examblock.model;

import examblock.view.components.Verbose;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.time.LocalDate;

import static org.junit.Assert.*;
/**
 * [6] implement StudentTest in JUnit
 */
public class StudentTest {

    private Registry registry;
    private Student student;
    private Student aaraStudent;

    @Before
    public void setUp() {
        registry = new Registry();
        student = new Student(9999831170L, "Tariq  N.", "Ahmad",
                15, 3, 2007, "Blue", false, registry);
        aaraStudent = new Student(9999353258L, "Eliza  G.", "Black",
                22, 4, 2007, "Green", true, registry);
    }

    // sanitiseName tests
    @Test
    public void testSanitiseName_null() { assertEquals("", student.sanitiseName(null)); }
    @Test
    public void testSanitiseName_trimSpaces() { assertEquals("Tariq N", student.sanitiseName("  Tariq   N.  ")); }
    @Test
    public void testSanitiseName_removeInvalidChars() { assertEquals("Tariq N", student.sanitiseName("Tariq@ N.123")); }
    @Test
    public void testSanitiseName_allowHyphenApostrophe() { assertEquals("Jean-Luc O'Connor", student.sanitiseName("Jean-Luc O'Connor")); }

    // Constructor and getters
    @Test
    public void testConstructor_noAara() {
        assertEquals(Long.valueOf(9999831170L), student.getLui());
        assertEquals("Tariq N", student.givenNames());
        assertEquals("Ahmad", student.familyName());
        assertEquals(LocalDate.of(2007, 3, 15), student.getDob());
        assertEquals("Blue", student.getHouse());
        assertFalse(student.isAara());
    }
    @Test
    public void testConstructor_withAara() { assertTrue(aaraStudent.isAara()); }
    @Test
    public void testFirstName() { assertEquals("Tariq", student.firstName()); }
    @Test
    public void testShortName() { assertEquals("Tariq Ahmad", student.shortName()); }
    @Test
    public void testFullName() { assertEquals("Tariq N Ahmad", student.fullName()); }

    // Subject/Exam management
    @Test
    public void testAddSubject() {
        Subject maths = new Subject("Maths", "Study of maths", registry);
        student.addSubject(maths);
        assertNotNull(student.getSubjects().find(maths.getId()));
    }
    @Test
    public void testAddExam() {
        Subject maths = new Subject("Maths", "Study of maths", registry);
        Exam exam = new Exam(maths, Exam.ExamType.INTERNAL, 15, 3, 2025, 9, 0, registry);
        student.addExam(exam);
        assertNotNull(student.getExams().find(exam.getId()));
    }
    @Test
    public void testRemoveSubject() {
        Subject maths = new Subject("Maths", "Study of maths", registry);
        student.addSubject(maths);
        student.removeSubject(maths);
        assertNull(student.getSubjects().find("Maths"));
    }

    // ID and details
    @Test
    public void testGetId() { assertEquals("9999831170", student.getId()); }
    @Test
    public void testGetFullDetail() {
        String detail = student.getFullDetail();
        assertTrue(detail.contains("Tariq N") && detail.contains("Ahmad") && detail.contains("9999831170"));
    }
    @Test
    public void testToTableRow() {
        Object[] row = student.toTableRow();
        assertEquals(4, row.length);
        assertEquals(9999831170L, row[0]);
        assertEquals("Tariq N Ahmad", row[1]);
        assertEquals("Blue", row[2]);
        assertEquals("No", row[3]);
    }

    // Setters
    @Test
    public void testChangeLui() {
        student.changeLui(1234567890L);
        assertEquals(Long.valueOf(1234567890L), student.getLui());
    }
    @Test
    public void testSetGiven() {
        student.setGiven("  John  Paul  ");
        assertEquals("John Paul", student.givenNames());
    }
    @Test
    public void testSetFamily() {
        student.setFamily("  Smith-Jones  ");
        assertEquals("Smith-Jones", student.familyName());
    }

    // equals/hashCode
    @Test
    public void testEquals() {
        Registry temporaryRegistry = new Registry();
        Student same = new Student(9999831170L, "Tariq N", "Ahmad", 15, 3, 2007, "Blue", false, temporaryRegistry);
        assertEquals(student, same);
        assertNotEquals(student, aaraStudent);
    }
    @Test
    public void testHashCode() {
        Registry temporaryRegistry = new Registry();
        Student same = new Student(9999831170L, "Tariq N", "Ahmad", 15, 3, 2007, "Blue", false, temporaryRegistry);
        assertEquals(student.hashCode(), same.hashCode());
    }

    // toString
    @Test
    public void testToString() { assertEquals("Tariq N Ahmad", student.toString()); }

    // Streaming
    @Test
    public void testStreamOut_streamIn() throws IOException {
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        student.streamOut(bw, 1);
        bw.flush();
        String output = sw.toString();

        // Add a dummy subject to registry for loading
        Subject maths = new Subject("Mathematics", "Study of maths", registry);
        student.addSubject(maths);

        Registry newReg = new Registry();
        // Need to register subjects used in streaming
        newReg.add(maths, Subject.class);
        BufferedReader br = new BufferedReader(new StringReader(output));
        Verbose.setVerbose(false);
        Student loaded = new Student(br, newReg, 1);

        assertEquals(student.getLui(), loaded.getLui());
        assertEquals(student.givenNames(), loaded.givenNames());
        assertEquals(student.familyName(), loaded.familyName());
        assertEquals(student.getDob(), loaded.getDob());
        assertEquals(student.getHouse(), loaded.getHouse());
        assertEquals(student.isAara(), loaded.isAara());
        // Subjects should be loaded as well
        assertEquals(1, student.getSubjects().size());
    }
}