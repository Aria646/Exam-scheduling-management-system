package examblock.view;

import examblock.given.SessionHandler;
import examblock.model.*;
import examblock.view.components.DialogUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * the V in MVC model. Drives and owns the UI
 * [1] Implement the code according to the homework requirements
 * [2] Code according to Javadoc
 * [3][4] Comment in Javadoc style
 */
public class ExamBlockView implements ModelObserver {
    /**
     * registry
     */
    private final Registry registry;
    /**
     * session node map
     */
    private final Map<DefaultMutableTreeNode, Session> sessionNodeMap = new HashMap<>();
    /**
     * venue node map
     */
    private final Map<DefaultMutableTreeNode, Venue> venueNodeMap = new HashMap<>();
    /**
     * exam node map
     */
    private final Map<DefaultMutableTreeNode, Exam> examNodeMap = new HashMap<>();
    /**
     * exam node map
     */
    private final Map<Integer, Exam> examIndexMap = new HashMap<>();
    /**
     * model
     */
    private ExamBlockModel model;
    /**
     * frame
     */
    private JFrame frame;
    /**
     * tabbed pane
     */
    private JTabbedPane tabbedPane;
    /**
     * exam table
     */
    private JTable examTable;
    /**
     * exam table model
     */
    private DefaultTableModel examTableModel;
    /**
     * session tree
     */
    private JTree sessionTree;
    /**
     * session tree model
     */
    private DefaultMutableTreeNode sessionRoot;
    /**
     * exam table
     */
    private DefaultMutableTreeNode venueRoot;
    /**
     * finalise button
     */
    private JButton finaliseButton;
    /**
     * add button
     */
    private JButton addButton;
    /**
     * clear button
     */
    private JButton clearButton;

    /**
     * Constructor
     *
     * @param registry registry
     */
    public ExamBlockView(Registry registry) {
        this.registry = registry;
        createUi();
    }

    private void createUi() {
        frame = new JFrame("Exam Block");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setJMenuBar(createMenuBar());

        tabbedPane = new JTabbedPane();
        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.add(createTopPanel(), BorderLayout.NORTH);
        frame.add(createBottomPanel(), BorderLayout.SOUTH);

        // Initialize tree
        sessionRoot = new DefaultMutableTreeNode("Sessions");
        venueRoot = new DefaultMutableTreeNode("Venues");
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Exam Sessions");
        root.add(sessionRoot);
        root.add(venueRoot);
        sessionTree = new JTree(root);
        JScrollPane treeScroll = new JScrollPane(sessionTree);
        treeScroll.setPreferredSize(new Dimension(250, 400));
        frame.add(treeScroll, BorderLayout.WEST);

        // Exam table
        examTableModel = new DefaultTableModel(new String[]{"Internal", "Exam", "Date", "Time"}, 0);
        examTable = new JTable(examTableModel);
        JScrollPane tableScroll = new JScrollPane(examTable);
        tabbedPane.addTab("Exams", tableScroll);

        frame.pack();
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
    }

    private JMenuBar createMenuBar() {
        JMenu fileMenu = new JMenu("File");
        JMenuItem loadItem = new JMenuItem("Load...");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem saveAsItem = new JMenuItem("Save As");
        JMenuItem exitItem = new JMenuItem("Exit");
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu viewMenu = new JMenu("View");
        JMenuItem deskAllocItem = new JMenuItem("Desk Allocations...");
        JMenuItem finaliseReportItem = new JMenuItem("Finalise Reports...");
        viewMenu.add(deskAllocItem);
        viewMenu.add(finaliseReportItem);
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        return menuBar;
    }

    /**
     * Get Add button
     *
     * @return add button
     */
    public JButton getAddButton() {
        return addButton;
    }

    /**
     * Get clear button
     *
     * @return clear button
     */
    public JButton getClearButton() {
        return clearButton;
    }

    private void saveAs() {
        String filename = model.getFilename();
        boolean saved = model.saveToFile(model.getRegistry(), null,
                model.getTitle(), model.getVersion());
        if (saved) {
            model.setFilename(filename);
        }
    }

    private void showDeskAllocations() {
        if (model == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        model.getVenues().writeAllocations(sb, model.getSessions());
        examblock.view.components.DialogUtils.showTextViewer(sb.toString(), "Desk Allocations",
                examblock.view.components.DialogUtils.ViewerOptions.SCROLL,
                Utilities.FileType.TXT);
    }

    private void showFinaliseReport() {
        if (model == null) {
            return;
        }
        examblock.given.SessionHandler.printEverything(model);
    }

    /**
     * create the top panel
     *
     * @return the panel object
     */
    public JPanel createTopPanel() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Exam Block"));
        return top;
    }

    /**
     * create the bottom panel
     *
     * @return the panel
     */
    public JPanel createBottomPanel() {
        finaliseButton = new JButton("Finalise");
        addButton = new JButton("Add");
        clearButton = new JButton("Clear");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(finaliseButton);
        bottom.add(addButton);
        bottom.add(clearButton);
        return bottom;
    }

    /**
     * Show the window after construction
     */
    public void display() {
        frame.setVisible(true);
    }

    /**
     * remove old content and load new content
     *
     * @param exams new list of exams
     */
    public void updateExamTable(ExamList exams) {
        examTableModel.setRowCount(0);
        examIndexMap.clear();
        int idx = 0;
        for (Exam e : exams.all()) {
            examTableModel.addRow(e.toTableRow());
            examIndexMap.put(idx, e);
            idx++;
        }
    }

    /**
     * update the session tree
     *
     * @param sessions sessions
     * @param venues   venues
     */
    public void updateTree(SessionList sessions, VenueList venues) {
        sessionRoot.removeAllChildren();
        venueRoot.removeAllChildren();
        sessionNodeMap.clear();
        venueNodeMap.clear();
        examNodeMap.clear();

        // Group sessions by venue
        Map<Venue, java.util.List<Session>> sessionsByVenue = new HashMap<>();
        for (Session s : sessions.all()) {
            Venue v = s.getVenue();
            sessionsByVenue.computeIfAbsent(v, k -> new java.util.ArrayList<>()).add(s);
        }
        for (Map.Entry<Venue, java.util.List<Session>> entry : sessionsByVenue.entrySet()) {
            DefaultMutableTreeNode venueNode = new DefaultMutableTreeNode(entry.getKey().venueId());
            venueNodeMap.put(venueNode, entry.getKey());
            for (Session s : entry.getValue()) {
                DefaultMutableTreeNode sessionNode = new DefaultMutableTreeNode("Session "
                        + s.getSessionNumber());
                sessionNodeMap.put(sessionNode, s);
                for (Exam e : s.getExams()) {
                    DefaultMutableTreeNode examNode = new DefaultMutableTreeNode(e.getShortTitle());
                    examNodeMap.put(examNode, e);
                    sessionNode.add(examNode);
                }
                venueNode.add(sessionNode);
            }
            sessionRoot.add(venueNode);
        }

        for (Venue v : venues.all()) {
            DefaultMutableTreeNode venueNode = new DefaultMutableTreeNode(v.venueId());
            venueNodeMap.put(venueNode, v);
            venueRoot.add(venueNode);
        }

        ((DefaultTreeModel) sessionTree.getModel()).reload();
    }

    /**
     * new data for the Subjects page of the tabbed view
     *
     * @param subjects new SubjectList
     */
    public void updateSubjectPage(SubjectList subjects) {
        JTable table = new JTable();
        DefaultTableModel model = new DefaultTableModel(new String[]{"Title"}, 0);
        for (Subject s : subjects.all()) {
            model.addRow(new Object[]{s.getTitle()});
        }
        table.setModel(model);
        replaceTab("Subjects", table);
    }

    /**
     * new data for this page of the tabbed view
     *
     * @param exams new List of data
     */
    public void updateExamPage(ExamList exams) {
        updateExamTable(exams);
    }

    /**
     * new data for this page of the tabbed view
     *
     * @param units new List of data
     */
    public void updateUnitPage(UnitList units) {
        DefaultTableModel model = new DefaultTableModel(new String[]{"Subject",
            "Unit", "Title", "Description"}, 0);
        for (Unit u : units.all()) {
            model.addRow(u.toTableRow());
        }
        JTable table = new JTable(model);
        replaceTab("Units", table);
    }

    /**
     * new data for this page of the tabbed view
     *
     * @param students new List of data
     */
    public void updateStudentPage(StudentList students) {
        DefaultTableModel model = new DefaultTableModel(new String[]{"LUI",
            "Name", "House", "AARA"}, 0);
        for (Student s : students.all()) {
            model.addRow(s.toTableRow());
        }
        JTable table = new JTable(model);
        replaceTab("Students", table);
    }

    /**
     * new data for this page of the tabbed view
     *
     * @param venues new List of data
     */
    public void updateVenuPage(VenueList venues) {
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID",
            "Rooms", "Rows", "Cols", "Desks", "AARA"}, 0);
        for (Venue v : venues.all()) {
            model.addRow(v.toTableRow());
        }
        JTable table = new JTable(model);
        replaceTab("Venues", table);
    }

    /**
     * new data for this page of the tabbed view
     *
     * @param rooms new List of data
     */
    public void updateRoomPage(RoomList rooms) {
        DefaultTableModel model = new DefaultTableModel(new String[]{"Room ID"}, 0);
        for (Room r : rooms.all()) {
            model.addRow(new Object[]{r.roomId()});
        }
        JTable table = new JTable(model);
        replaceTab("Rooms", table);
    }

    private void replaceTab(String title, JTable table) {
        int idx = tabbedPane.indexOfTab(title);
        JScrollPane scroll = new JScrollPane(table);
        if (idx != -1) {
            tabbedPane.setComponentAt(idx, scroll);
        } else {
            tabbedPane.addTab(title, scroll);
        }
    }

    /**
     * Add a listener for the Finalise button
     *
     * @param listener listener
     */
    public void addFinaliseButtonListener(ActionListener listener) {
        finaliseButton.addActionListener(listener);
    }

    /**
     * Add a listener for the Add button
     *
     * @param listener listener
     */
    public void addAddButtonListener(ActionListener listener) {
        addButton.addActionListener(listener);
    }

    /**
     * Add a listener for the Clear button
     *
     * @param listener listener
     */
    public void addClearButtonListener(ActionListener listener) {
        clearButton.addActionListener(listener);
    }

    /**
     * set the new title
     *
     * @param title title
     */
    public void setTitle(String title) {
        frame.setTitle("Exam Block - " + title);
    }

    /**
     * set the version to something new
     *
     * @param version new version
     */
    public void setVersion(double version) {
        // Not used directly, could show in status bar, but optional
    }

    /**
     * return the selected exam rows.
     *
     * @return the array of one object, or null is failure
     */
    public int[] getSelectedExamRows() {
        int[] rows = examTable.getSelectedRows();
        if (rows.length == 0) {
            return null;
        }
        return rows;
    }

    /**
     * get the node from the session tree
     *
     * @return the node
     */
    public DefaultMutableTreeNode getSelectedTreeNode() {
        return (DefaultMutableTreeNode) sessionTree.getLastSelectedPathComponent();
    }

    /**
     * clear the selection of any control, as well as some cached values
     */
    public void removeAllSelections() {
        examTable.clearSelection();
        sessionTree.clearSelection();
    }

    /**
     * Checks if there are any un-finalized sessions in the JTree.
     * A session is un-finalized if any Exam node has no Desk children.
     *
     * @return true if un-finalized sessions exist, false otherwise
     */
    public boolean hasUnfinalisedSessions() {
        for (DefaultMutableTreeNode sessionNode : sessionNodeMap.keySet()) {
            if (sessionNode.getChildCount() == 0) {
                continue;
            }
            for (int i = 0; i < sessionNode.getChildCount(); i++) {
                DefaultMutableTreeNode examNode = (DefaultMutableTreeNode) sessionNode
                        .getChildAt(i);
                if (examNode.getChildCount() == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * here we receive the notifications that model sent us
     *
     * @param property whatever it is
     */
    @Override
    public void modelChanged(String property) {
        switch (property) {
            case "exams":
                updateExamPage(model.getExams());
                break;
            case "subjects":
                updateSubjectPage(model.getSubjects());
                break;
            case "units":
                updateUnitPage(model.getUnits());
                break;
            case "students":
                updateStudentPage(model.getStudents());
                break;
            case "venues":
                updateVenuPage(model.getVenues());
                break;
            case "rooms":
                updateRoomPage(model.getRooms());
                break;
            case "sessions":
                updateTree(model.getSessions(), model.getVenues());
                break;
            case "loaded":
                updateExamPage(model.getExams());
                updateSubjectPage(model.getSubjects());
                updateUnitPage(model.getUnits());
                updateStudentPage(model.getStudents());
                updateVenuPage(model.getVenues());
                updateRoomPage(model.getRooms());
                updateTree(model.getSessions(), model.getVenues());
                break;
            case "finalised":
                updateExamPage(model.getExams());
                updateTree(model.getSessions(), model.getVenues());
                break;
        }
        setTitle(model.getTitle());
    }

    /**
     * model initialisation
     *
     * @param model reference to the model
     */
    public void setModel(ExamBlockModel model) {
        this.model = model;
        model.addObserver(this);
        // initial load of all data
        updateExamPage(model.getExams());
        updateSubjectPage(model.getSubjects());
        updateUnitPage(model.getUnits());
        updateStudentPage(model.getStudents());
        updateVenuPage(model.getVenues());
        updateRoomPage(model.getRooms());
        updateTree(model.getSessions(), model.getVenues());
        setTitle(model.getTitle());
    }

    /**
     * get the frame
     *
     * @return the frame
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * get the exam table
     *
     * @return the exam table
     */
    public JTable getExamTable() {
        return examTable;
    }

    /**
     * get the tree
     *
     * @return the tree
     */
    public JTree getTree() {
        return sessionTree;
    }

    /**
     * get the tabbed pane
     *
     * @return the tabbed pane
     */
    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    /**
     * get the finalise button
     *
     * @return the finalise button
     */
    public JButton getFinaliseButton() {
        return finaliseButton;
    }

    /**
     * get the session root
     *
     * @return the session root
     */
    public DefaultMutableTreeNode getSessionRoot() {
        return sessionRoot;
    }

    /**
     * get the venue root
     *
     * @return the venue root
     */
    public DefaultMutableTreeNode getVenueRoot() {
        return venueRoot;
    }

    /**
     * get the exam table model
     *
     * @return the exam table model
     */
    public DefaultTableModel getExamTableModel() {
        return examTableModel;
    }

    /**
     * add session to session node map
     *
     * @param sessionNode session node
     * @param session     session
     */
    public void addSessionToSessionNodeMap(DefaultMutableTreeNode sessionNode, Session session) {
        sessionNodeMap.put(sessionNode, session);
    }

    /**
     * get session from session node map
     *
     * @param sessionNode session node
     * @return session
     */
    public Session getSessionFromSessionNodeMap(DefaultMutableTreeNode sessionNode) {
        return sessionNodeMap.get(sessionNode);
    }

    /**
     * add venue to venue node map
     *
     * @param venueNode venue node
     * @param venue     venue
     */
    public void addVenueToVenueNodeMap(DefaultMutableTreeNode venueNode, Venue venue) {
        venueNodeMap.put(venueNode, venue);
    }

    /**
     * get venue from venue node map
     *
     * @param venueNode venue node
     * @return venue
     */
    public Venue getVenueFromVenueNodeMap(DefaultMutableTreeNode venueNode) {
        return venueNodeMap.get(venueNode);
    }

    /**
     * get exam from exam node map
     *
     * @param examNode exam node
     * @return exam
     */
    public Exam getExamFromExamNodeMap(DefaultMutableTreeNode examNode) {
        return examNodeMap.get(examNode);
    }

    /**
     * add exam to exam node map
     *
     * @param index index
     * @param exam  exam
     */
    public void addExamToExamMap(int index, Exam exam) {
        examIndexMap.put(index, exam);
    }

    /**
     * get exam from exam node map
     *
     * @param index index
     * @return exam
     */
    public Exam getExamFromExamMap(int index) {
        return examIndexMap.get(index);
    }

    /**
     * Document listener implementation
     */
    public abstract static class SimpleDocumentListener
            implements javax.swing.event.DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update(e);
        }

        /**
         * update by document event
         *
         * @param e document event
         */
        public abstract void update(DocumentEvent e);
    }
}