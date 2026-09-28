package examblock.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A collection object for holding and managing {@link Session}s.
 * [1] Implement the code according to the homework requirements
 * [2] Code according to Javadoc
 * [3][4] Comment in Javadoc style
 */
public class SessionList extends ListManager<Session> {

    /**
     * Constructor
     *
     * @param registry The registry to use for this list
     */
    public SessionList(Registry registry) {
        super(Session::new, registry, Session.class);
    }

    /**
     * Get the session number for a given venue, day and start time
     *
     * @param venue venue
     * @param day   day
     * @param start start time
     * @return session number
     */
    public int getSessionNumber(Venue venue, LocalDate day, LocalTime start) {
        for (Session s : all()) {
            if (s.getVenue().equals(venue) && s.getDate().equals(day)
                    && s.getTime().equals(start)) {
                return s.getSessionNumber();
            }
        }
        return 0;
    }

    /**
     * Get the session for a given venue and exam
     * @param venue  venue
     * @param sessionNumber session number
     * @return  session
     * @throws IllegalStateException if no session for venue and exam found
     */
    public Session getSession(Venue venue, int sessionNumber) throws IllegalStateException {
        for (Session s : all()) {
            if (s.getVenue().equals(venue) && s.getSessionNumber() == sessionNumber) {
                return s;
            }
        }
        throw new IllegalStateException("No session for venue " + venue.venueId()
                + " number " + sessionNumber);
    }

    /**
     * Get the session for a given venue, exam
     * @param venue  venue
     * @param exam  exam
     * @return  session
     * @throws IllegalStateException if no session for venue and exam found
     */
    public Session getSession(Venue venue, Exam exam) throws IllegalStateException {
        for (Session s : all()) {
            if (s.getVenue().equals(venue) && s.getExams().contains(exam)) {
                return s;
            }
        }
        throw new IllegalStateException("No session for venue " + venue.venueId()
                + " containing exam " + exam.getShortTitle());
    }

    private int nextSessionNumber(Venue venue) {
        int max = 0;
        for (Session s : all()) {
            if (s.getVenue().equals(venue) && s.getSessionNumber() > max) {
                max = s.getSessionNumber();
            }
        }
        return max + 1;
    }

    /**
     * Get the total number of students who will be taking an exam in
     * @param venue venue
     * @param exam  exam
     * @param numberStudents number of students
     * @return total number of students
     */
    public int getSessionNewTotal(Venue venue, Exam exam, int numberStudents) {
        LocalDate day = exam.getDate();
        LocalTime start = exam.getTime();
        int sessionNum = getSessionNumber(venue, day, start);
        Session session;
        if (sessionNum == 0) {
            System.out.println("There is currently no exam session in that venue at that time.");
            System.out.println("Creating a session...");
            sessionNum = nextSessionNumber(venue);
            session = new Session(venue, sessionNum, day, start, getRegistry());
            add(session);
        } else {
            session = getSession(venue, sessionNum);
        }
        int existingTotal = session.countStudents();
        int newTotal = existingTotal + numberStudents;
        if (existingTotal > 0) {
            System.out.println("There are already " + existingTotal
                    + " students who will be taking an exam in that venue; along with the "
                    + numberStudents + " students for this exam.");
        }
        System.out.println("That's a total of " + newTotal + " students.");
        return newTotal;
    }

    /**
     * Get the total number of students who will be taking an exam in
     * @param venue  venue
     * @param exam  exam
     * @return total number of students
     */
    public int getExistingSessionTotal(Venue venue, Exam exam) {
        int sessionNum = getSessionNumber(venue, exam.getDate(), exam.getTime());
        if (sessionNum == 0) {
            return 0;
        }
        Session session = getSession(venue, sessionNum);
        return session.countStudents();
    }

    /**
     * Schedule an exam
     * @param venue  venue
     * @param exam  exam
     */
    public void scheduleExam(Venue venue, Exam exam) {
        int sessionNum = getSessionNumber(venue, exam.getDate(), exam.getTime());
        Session session;
        if (sessionNum == 0) {
            sessionNum = nextSessionNumber(venue);
            session = new Session(venue, sessionNum, exam.getDate(), exam.getTime(), getRegistry());
            add(session);
        } else {
            session = getSession(venue, sessionNum);
        }
        session.scheduleExam(exam);
        System.out.println(exam.getSubject().getTitle() + " exam added to " + venue.venueId());
    }

    /**
     * Remove an exam
     * @param venue  venue
     * @param exam  exam
     */
    public void removeExam(Venue venue, Exam exam) {
        int sessionNum = getSessionNumber(venue, exam.getDate(), exam.getTime());
        if (sessionNum == 0) {
            return;
        }
        Session session = getSession(venue, sessionNum);
        session.removeExam(exam);
        System.out.println(exam.getSubject().getTitle() + " exam removed from " + venue.venueId());
        if (session.getExams().isEmpty()) {
            remove(session);
        }
    }

    /**
     * Get all sessions for a venue
     * @param venue  venue
     * @return  sessions
     */
    public List<Session> forVenue(Venue venue) {
        List<Session> result = new ArrayList<>();
        for (Session s : all()) {
            if (s.getVenue().equals(venue)) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Get all sessions
     * @return  sessions
     */
    public String getFullDetail() {
        StringBuilder sb = new StringBuilder();
        for (Session s : all()) {
            sb.append(s.getFullDetail()).append("\n");
        }
        return sb.toString();
    }
}