package examblock.model;

/**
 * A collection object for holding and managing {@link Venue}s.
 * [1] Implement the code according to the homework requirements
 * [2] Code according to Javadoc
 * [3][4] Comment in Javadoc style
 */
public class VenueList extends ListManager<Venue> {

    /**
     * Constructs a new VenueList object.
     * @param registry the registry object to be used for managing the venues.
     */
    public VenueList(Registry registry) {
        super(Venue::new, registry, Venue.class);
    }

    /**
     * Adds a Venue to this list of Venues.
     *
     * @param venue - the venue object being added to this list.
     */
    public void addVenue(Venue venue) {
        add(venue);
    }

    /**
     * Removes a given Venue from the VenueList.
     *
     * @param venue the venue to remove from this list.
     */
    public void removeVenue(Venue venue) {
        remove(venue);
    }

    /**
     * Get the first Venue with a matching id.
     *
     * @param id the identifier of the Venue to be found.
     * @return first Venue with a matching id, if it exists.
     * @throws IllegalStateException if no matching venue found.
     */
    public Venue getVenue(String id) throws IllegalStateException {
        for (Venue v : all()) {
            if (v.venueId().equals(id)) {
                return v;
            }
        }
        throw new IllegalStateException("No venue with id " + id);
    }

    /**
     * Allocates Students to Desks for every Session in every Venue.
     *
     * @param sessions the current set of exam sessions allocated to items.
     * @param exams    the current set of Year 12 Exams.
     * @param cohort   all the Year 12 students.
     */
    public void allocateStudents(SessionList sessions, ExamList exams, StudentList cohort) {
        for (Session session : sessions.all()) {
            session.allocateStudents(exams, cohort);
        }
    }

    /**
     * Print the allocations of Students to Desks for every Session in every Venue.
     *
     * @param sessions the current set of exam sessions allocated to items.
     */
    public void printAllocations(SessionList sessions) {
        for (Session session : sessions.all()) {
            session.printDesks();
        }
    }

    /**
     * Write the allocations of Students to Desks for every Session in every Venue
     * to a StringBuilder.
     *
     * @param sb       the StringBuilder to write to
     * @param sessions the current set of exam sessions allocated to items.
     */
    public void writeAllocations(StringBuilder sb, SessionList sessions) {
        for (Session session : sessions.all()) {
            session.printDesks(sb);
        }
    }

    /**
     * Returns detailed string representations of the contents of this venue list.
     *
     * @return detailed string representations of the contents of this venue list.
     */
    public String getFullDetail() {
        StringBuilder sb = new StringBuilder();
        for (Venue v : all()) {
            sb.append(v.getFullDetail()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Returns a brief string representation of the contents of this venue list.
     *
     * @return a brief string representation of the contents of this venue list.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (Venue v : all()) {
            sb.append(i++).append(". ").append(v.toString()).append("\n");
        }
        return sb.toString();
    }
}