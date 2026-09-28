package examblock.model;

/**
 * A collection object for holding and managing {@link Student}s.
 */
public class StudentList extends ListManager<Student> {

    /**
     * constructor
     *
     * @param registry registry
     */
    public StudentList(Registry registry) {
        super(Student::new, registry, Student.class);
    }

    /**
     * Get the {@link Student} with a matching {@code LUI}.
     *
     * @param lui - the {@code LUI} of the {@link Student} to be found.
     * @return {@link Student} with a matching {@code LUI}, if it exists.
     * @throws IllegalStateException - throw an IllegalStateException if it can't
     *                               find a matching student as that indicates there
     *                               is a misalignment of the executing state and the
     *                               complete list of possible items.
     */
    public Student byLui(Long lui) throws IllegalStateException {
        for (Student student : all()) {
            if (student.getLui().equals(lui)) {
                return student;
            }
        }
        throw new IllegalStateException("No student with LUI " + lui + " exists.");
    }

    /**
     * Counts the number of either non-AARA or AARA students taking a particular {@link Subject}.
     *
     * @param subject the subject to be found.
     * @param aara    true to count AARA students or false to count non-AARA students.
     * @return The number of either non-AARA or AARA students taking a particular subject.
     */
    public int countStudents(Subject subject, boolean aara) {
        int count = 0;
        for (Student student : all()) {
            if (student.isAara() == aara && student.getSubjects().find(subject.getId()) != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns detailed string representations of the contents of this student list.
     *
     * @return detailed string representations of the contents of this student list.
     */
    public String getFullDetail() {
        StringBuilder sb = new StringBuilder();
        int counter = 1;
        for (Student student : all()) {
            sb.append(counter).append(". ").append(student.getFullDetail()).append("\n");
            counter++;
        }
        return sb.toString();
    }

    /**
     * Returns a minimal string representation of the contents of this student list.
     *
     * @return a minimal string representation of the contents of this student list.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int counter = 1;
        for (Student student : all()) {
            sb.append(counter).append(". ").append(student.toString()).append("\n");
            counter++;
        }
        return sb.toString();
    }
}