package examblock.model;

import examblock.view.components.DialogUtils;
import examblock.view.components.FileChooser;
import examblock.view.components.Verbose;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The M in the MVC model - this is the source of truth for all the data in the app
 */
public class ExamBlockModel {

    /**
     * The lists of subject
     */
    private final SubjectList subjects;
    /**
     * The lists of unit
     */
    private final UnitList units;
    /**
     * The lists of student
     */
    private final StudentList students;
    /**
     * The lists of exam
     */
    private final ExamList exams;
    /**
     * The lists of room
     */
    private final RoomList rooms;
    /**
     * The lists of venue
     */
    private final VenueList venues;
    /**
     * The lists of session
     */
    private final SessionList sessions;
    /**
     * list of model observers
     */
    private final List<ModelObserver> observers;
    /**
     * The registry
     */
    private Registry registry;
    /**
     * title of model
     */
    private String title;
    /**
     * version of model
     */
    private double version;
    /**
     * filename of model
     */
    private String filename;

    /**
     * Constructor
     */
    public ExamBlockModel() {
        this.registry = new Registry();
        this.subjects = new SubjectList(registry);
        this.units = new UnitList(registry);
        this.students = new StudentList(registry);
        this.exams = new ExamList(registry);
        this.rooms = new RoomList(registry);
        this.venues = new VenueList(registry);
        this.sessions = new SessionList(registry);
        this.title = "Exam Block";
        this.version = 1.0;
        this.filename = null;
        this.observers = new ArrayList<>();
    }

    /**
     * Add an observer
     *
     * @param observer the new observer to be called on update
     */
    public void addObserver(ModelObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Notify observers that data has changed
     *
     * @param property a string naming the property
     */
    public void notifyObservers(String property) {
        for (ModelObserver o : observers) {
            o.modelChanged(property);
        }
    }

    /**
     * Get the subject list
     * @return the subject list
     */
    public SubjectList getSubjects() {
        return subjects;
    }

    /**
     * get the list of units
     * @return the list of units
     */
    public UnitList getUnits() {
        return units;
    }

    /**
     * Get the list of students
     * @return the list of students
     */
    public StudentList getStudents() {
        return students;
    }

    /**
     * Get the list of exams
     * @return the list of exams
     */
    public ExamList getExams() {
        return exams;
    }

    /**
     * Get the list of rooms
     * @return the list of rooms
     */
    public RoomList getRooms() {
        return rooms;
    }

    /**
     * Get the list of venues
     * @return the list of venues
     */
    public VenueList getVenues() {
        return venues;
    }

    /**
     * Get the list of sessions
     * @return the list of sessions
     */
    public SessionList getSessions() {
        return sessions;
    }

    /**
     * Get the registry
     * @return the registry
     */
    public Registry getRegistry() {
        return registry;
    }

    /**
     * Get the title
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the title
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
        notifyObservers("title");
    }

    /**
     * Get the version
     * @return the version
     */
    public double getVersion() {
        return version;
    }

    /**
     * Set the version
     * @param version the version
     */
    public void setVersion(double version) {
        if (version <= this.version) {
            throw new IllegalArgumentException("New version must be greater than current version");
        }
        this.version = version;
        notifyObservers("version");
    }

    /**
     * Get the filename
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Set the filename
     * @param filename the filename
     */
    public void setFilename(String filename) {
        this.filename = filename;
        notifyObservers("filename");
    }

    /**
     * Save to file
     * @param registry the registry
     * @param filename the filename
     * @param title the title
     * @param version the version
     * @return true if successful
     */
    public boolean saveToFile(Registry registry, String filename, String title, double version) {
        if (filename == null || filename.isEmpty()) {
            FileChooser chooser = new FileChooser(title, this.version);
            String chosen = chooser.save(null, Utilities.FileType.EBD);
            if (chosen == null || chosen.isEmpty()) {
                return false;
            }
            filename = chosen;
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            bw.write("Title: " + title + "\n");
            bw.write("Version: " + version + "\n");
            bw.write("[Begin]\n");

            // Write subjects
            subjects.streamOut(bw, 0);

            // Write units
            units.streamOut(bw, 0);

            // Write students
            students.streamOut(bw, 0);

            // Write exams
            //ExamList exams = new ExamList(registry);
            exams.streamOut(bw, 0);

            // Write rooms
            rooms.streamOut(bw, 0);

            // Write venues
            venues.streamOut(bw, 0);

            // Write sessions
            sessions.streamOut(bw, 0);

            bw.write("[End]\n");
            setFilename(filename);
            setTitle(title);
            setVersion(version);
            if (Verbose.isVerbose()) {
                System.out.println("Saved to " + filename);
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
            DialogUtils.showMessage("Failed to save file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Load a registry from disk (prompts user for file).
     */
    public void loadFromFile() {
        FileChooser chooser = new FileChooser();
        java.io.File file = chooser.open(null, Utilities.FileType.EBD);
        if (file == null) {
            return;
        }
        loadFromFile(registry, file.getAbsolutePath());
    }

    /**
     * Load a registry from disk
     * @param registry the registry
     * @param filename the filename
     */
    public void loadFromFile(Registry registry, String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            // Read Title line
            String titleLine = Utilities.getLine(br);
            if (titleLine == null || !titleLine.startsWith("Title: ")) {
                throw new RuntimeException("Invalid file format: missing Title");
            }
            this.title = titleLine.substring(7).trim();

            // Read Version line
            String versionLine = Utilities.getLine(br);
            if (versionLine == null || !versionLine.startsWith("Version: ")) {
                throw new RuntimeException("Invalid file format: missing Version");
            }
            this.version = Double.parseDouble(versionLine.substring(9).trim());

            // Read [Begin]
            String beginLine = Utilities.getLine(br);
            if (beginLine == null || !beginLine.equals("[Begin]")) {
                throw new RuntimeException("Invalid file format: missing [Begin]");
            }

            // Clear existing registry data
            registry.clear();

            // Load subjects
            subjects.streamIn(br, registry, 0);

            // Load units
            units.streamIn(br, registry, 0);

            // Load students
            students.streamIn(br, registry, 0);

            // Load exams
            exams.streamIn(br, registry, 0);

            // Load rooms
            rooms.streamIn(br, registry, 0);

            // Load venues
            venues.streamIn(br, registry, 0);

            // Load sessions
            sessions.streamIn(br, registry, 0);

            // Read [End]
            String endLine = Utilities.getLine(br);
            if (endLine == null || !endLine.equals("[End]")) {
                throw new RuntimeException("Invalid file format: missing [End]");
            }

            setFilename(filename);
            notifyObservers("loaded");
            if (Verbose.isVerbose()) {
                System.out.println("Loaded from " + filename);
            }
        } catch (IOException | RuntimeException e) {
            System.err.println("Fatal error loading file: " + e.getMessage());
            DialogUtils.showMessage("Failed to load file: " + e.getMessage());
            System.exit(1); // terminate app as per spec
        }
    }
}