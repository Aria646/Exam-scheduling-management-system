package examblock.controller;

import examblock.given.SessionHandler;
import examblock.model.*;
import examblock.view.ExamBlockView;
import examblock.view.components.DialogUtils;
import examblock.view.components.FileChooser;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Main controller to coordinate between model and view
 * [1] The controller is the glue between the model and the view.
 * [2] Code according to Javadoc
 * [3][4] Comment in Javadoc style
 */
public class ExamBlockController {

    /**
     * The model
     */
    private ExamBlockModel model;
    /**
     * The view
     */
    private ExamBlockView view;

    /**
     * The C in MVC. The controller holds all the pieces, and controls the app
     * The controller needs to do the following tasks<br>
     * <ul>
     *     <li>Construct the model</li>
     *     <li>Construct the view</li>
     *     <li>Register the view as an observer of the model</li>
     *     <li>Add all the listeners to the view</li>
     *     <li>Causes the view item to (re-)display itself</li>
     *     <li>Construct and install the menu items. There needs to be two top level menus,
     *     <ul>
     *         <li>File
     *         <ul>
     *             <li>Load...</li>
     *             <li>Separator</li>
     *             <li>Save</li>
     *             <li>Save As</li>
     *             <li>Separator</li>
     *             <li>Exit</li>
     *         </ul>
     *         </li>
     *         <li>View
     *         <ul>
     *             <li>Desk Allocations...</li>
     *             <li>Finalise Reports...</li>
     *         </ul>
     *         </li>
     *     </ul>
     *     </li>
     *     <li>Display the view</li>
     * </ul>
     */
    public ExamBlockController() {
        // 1. Construct the model
        model = new ExamBlockModel();

        // 2. Construct the view
        view = new ExamBlockView(model.getRegistry());

        // 3. Register the view as an observer of the model
        model.addObserver(view);

        // 4. Add all the listeners to the view
        addMenuListeners();
        addButtonListeners();

        // 5. Connect view to model (for later updates)
        view.setModel(model);

        // 6. Display the view
        SwingUtilities.invokeLater(() -> {
            // Set parent for dialogs (ensures proper centering)
            DialogUtils.setParent(view.getFrame());
            FileChooser.setParent(view.getFrame());
            view.display();
        });
    }

    private void addMenuListeners() {
        JMenuBar menuBar = view.getFrame().getJMenuBar();
        if (menuBar == null) {
            return;
        }
        JMenu fileMenu = menuBar.getMenu(0);
        JMenu viewMenu = menuBar.getMenu(1);
        if (fileMenu != null) {
            for (int i = 0; i < fileMenu.getItemCount(); i++) {
                JMenuItem item = fileMenu.getItem(i);
                if (item != null) {
                    switch (item.getText()) {
                        case "Load...":
                            item.addActionListener(e -> model.loadFromFile());
                            break;
                        case "Save":
                            item.addActionListener(e -> saveFile());
                            break;
                        case "Save As":
                            item.addActionListener(e -> saveAs());
                            break;
                        case "Exit":
                            item.addActionListener(e -> System.exit(0));
                            break;
                    }
                }
            }
        }
        if (viewMenu != null) {
            for (int i = 0; i < viewMenu.getItemCount(); i++) {
                JMenuItem item = viewMenu.getItem(i);
                if (item != null) {
                    switch (item.getText()) {
                        case "Desk Allocations...":
                            item.addActionListener(e -> showDeskAllocations());
                            break;
                        case "Finalise Reports...":
                            item.addActionListener(e -> showFinaliseReport());
                            break;
                    }
                }
            }
        }
    }

    private void addButtonListeners() {
        view.addFinaliseButtonListener(e -> handleFinalise());
        view.addAddButtonListener(e -> handleAdd());
        view.addClearButtonListener(e -> handleClear());
    }

    private void saveFile() {
        if (model.getFilename() == null) {
            saveAs();
        } else {
            model.saveToFile(model.getRegistry(), model.getFilename(),
                    model.getTitle(), model.getVersion() + 0.1);
        }
    }

    private void saveAs() {
        model.saveToFile(model.getRegistry(), null, model.getTitle(), model.getVersion());
    }

    private void showDeskAllocations() {
        StringBuilder sb = new StringBuilder();
        model.getVenues().writeAllocations(sb, model.getSessions());
        examblock.view.components.DialogUtils.showTextViewer(sb.toString(),
                "Desk Allocations",
                examblock.view.components.DialogUtils.ViewerOptions.SCROLL,
                Utilities.FileType.TXT);
    }

    private void showFinaliseReport() {
        SessionHandler.printEverything(model);
    }

    private void handleAdd() {
        DefaultMutableTreeNode selectedNode = view.getSelectedTreeNode();
        if (selectedNode == null) {
            DialogUtils.showMessage("Please select an exam node in the tree to add "
                    + "a desk allocation.");
            return;
        }
        // If selected node is an exam node, we could add a desk (unspecified in spec)
        // Instead, spec says Add button is for adding new items (e.g., new Exam).
        // Since no clear requirement, we show a dialog to add a new exam.
        String subjectTitle = DialogUtils.getUserInput("Enter subject title:", "Add Exam", "");
        if (subjectTitle == null || subjectTitle.isEmpty()) {
            return;
        }
        Subject subject = model.getSubjects().find(subjectTitle);
        if (subject == null) {
            DialogUtils.showMessage("Subject not found.");
            return;
        }
        String dayStr = DialogUtils.getUserInput("Day (1-31):", "Add Exam", "10");
        String monthStr = DialogUtils.getUserInput("Month (1-12):", "Add Exam", "3");
        String yearStr = DialogUtils.getUserInput("Year (>=2025):", "Add Exam", "2025");
        String hourStr = DialogUtils.getUserInput("Hour (7-17):", "Add Exam", "9");
        String minuteStr = DialogUtils.getUserInput("Minute (0-59):", "Add Exam", "0");
        try {
            int day = Integer.parseInt(dayStr);
            int month = Integer.parseInt(monthStr);
            int year = Integer.parseInt(yearStr);
            int hour = Integer.parseInt(hourStr);
            int minute = Integer.parseInt(minuteStr);
            Exam exam = new Exam(subject, Exam.ExamType.INTERNAL, day, month, year, hour,
                    minute, model.getRegistry());
            model.getExams().add(exam);
            model.notifyObservers("exams");
            DialogUtils.showMessage("Exam added successfully.");
        } catch (NumberFormatException ex) {
            DialogUtils.showMessage("Invalid number format.");
        }
    }

    private void handleClear() {
        view.removeAllSelections();
    }

    private void handleFinalise() {
        if (view.hasUnfinalisedSessions()) {
            int choice = DialogUtils.askQuestion(
                    "Some sessions are un-finalised. Do you want to finalise them now?");
            if (choice == JOptionPane.YES_OPTION) {
                SessionHandler.finaliseExamBlock(model);
                view.updateTree(model.getSessions(), model.getVenues());
            }
        } else {
            DialogUtils.showMessage("All sessions are already finalised.");
        }
    }
}