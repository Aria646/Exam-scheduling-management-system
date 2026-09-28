package examblock.model;

import org.junit.Before;
import org.junit.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import static org.junit.Assert.*;
/**
 * [6] implement SessionTest in JUnit
 */
public class SessionTest {

    private Registry registry;
    private Venue venue;
    private Session session;
    private Subject subject;
    private Exam exam;

    @Before
    public void setUp() {
        registry = new Registry();
        RoomList rooms = new RoomList(registry);
        rooms.add(new Room("R1", registry));
        venue = new Venue("V1", 1, rooms, 3, 5, 15, false, registry);
        session = new Session(venue, 1, LocalDate.of(2025, 3, 10), LocalTime.of(12, 30), registry);
        subject = new Subject("Maths", "Study of maths", registry);
        exam = new Exam(subject, Exam.ExamType.INTERNAL, 10, 3, 2025, 12, 30, registry);
    }

    @Test
    public void testConstructor() {
        assertEquals(venue, session.getVenue());
        assertEquals(1, session.getSessionNumber());
        assertEquals(LocalDate.of(2025, 3, 10), session.getDate());
        assertEquals(LocalTime.of(12, 30), session.getTime());
        assertEquals(15, session.getTotalDesks());
    }

    @Test
    public void testScheduleAndRemoveExam() {
        session.scheduleExam(exam);
        assertTrue(session.getExams().contains(exam));
        session.removeExam(exam);
        assertFalse(session.getExams().contains(exam));
    }

    @Test
    public void testGetDesk() {
        Desk d = session.getDesk(0, 0);
        assertNotNull(d);
        assertEquals(1, d.deskNumber());
    }

    @Test
    public void testToString() {
        assertTrue(session.toString().contains("Session 1 at V1"));
    }

    @Test
    public void testEqualsAndHashCode() {
        Registry tempRegistry = new Registry();
        Session other = new Session(venue, 1, LocalDate.of(2025, 3, 10), LocalTime.of(12, 30), tempRegistry);
        assertEquals(session, other);
        assertEquals(session.hashCode(), other.hashCode());
    }

    @Test
    public void testCountStudentsBeforeAllocation() {
        session.scheduleExam(exam);
        assertEquals(0, session.countStudents()); // no students allocated yet
    }

    @Test
    public void testAllocateStudents() {
        session.scheduleExam(exam);
        StudentList cohort = new StudentList(registry);
        Student s = new Student(12345L, "John", "Doe", 1, 1, 2000, "Blue", false, registry);
        s.addSubject(subject);
        cohort.add(s);
        session.allocateStudents(new ExamList(registry), cohort);
        assertTrue(session.countStudents() >= 1);
        // check desk 0,0 contains the student
        assertEquals("Doe", session.getDesk(0, 0).deskFamilyName());
    }
}