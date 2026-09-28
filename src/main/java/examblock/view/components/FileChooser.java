package examblock.view.components;

import examblock.model.Utilities;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * A save dialog that prompts for overwrite, as well as ensuring newVersion
 * is greater than oldVersion
 * [1] Implement the code according to the homework requirements
 * [2] Code according to Javadoc
 * [3][4] Comment in Javadoc style
 */
public class FileChooser {

    /**
     * The parent frame for dialogs.
     */
    private static Component parent = null;
    /**
     * The name of the Exam Block, not the filename.
     */
    private final String title;
    /**
     * The existing version of the Exam Block being saved.
     */
    private final double oldVersion;
    /**
     * The suggested version of the Exam Block
     */
    private double suggestedVersion;

    /**
     * Create a new FileChooser
     *
     * @param title           the name of the Exam Block, not the filename.
     * @param oldVersion      the existing version of the Exam Block being saved.
     * @param suggestedVersion this value is placed into the textbox as a default
     */
    public FileChooser(String title, double oldVersion, double suggestedVersion) {
        this.title = title;
        this.oldVersion = oldVersion;
        this.suggestedVersion = suggestedVersion;
    }

    /**
     * Create a new FileChooser
     *
     * @param title      the name of the Exam Block, not the filename.
     * @param oldVersion the existing version of the Exam Block being saved.
     */
    public FileChooser(String title, double oldVersion) {
        this(title, oldVersion, oldVersion + 0.1);
    }

    /**
     * Create a new FileChooser with empty title and version, used when those values aren't needed.
     */
    public FileChooser() {
        this("", 0.0, 1.0);
    }

    /**
     * set the frame for dialogs
     *
     * @param frame where we should be centered
     */
    public static void setParent(Component frame) {
        parent = frame;
    }

    /**
     * getter
     *
     * @return new Title
     */
    public String title() {
        return title;
    }

    /**
     * getter
     *
     * @return new Version
     */
    public double version() {
        return suggestedVersion;
    }

    /**
     * If you want to set an acceptable version number that the user can just accept, call this
     *
     * @param suggestedVersion the version the user can accept as-is.
     */
    public void suggestedVersion(double suggestedVersion) {
        this.suggestedVersion = suggestedVersion;
    }

    /**
     * Display the load/open dialog, and return the selected filename.
     *
     * @param hint     initial suggested filename, or null (or empty) for none
     * @param fileType Select this file filter by default.
     * @return the selected existing file as a File, or null if none chosen.
     */
    public File open(String hint, Utilities.FileType fileType) {
        JFileChooser chooser = new JFileChooser();
        if (hint != null && !hint.isEmpty()) {
            chooser.setSelectedFile(new File(hint));
        }
        chooser.setFileFilter(new FileNameExtensionFilter(fileType.name()
                + " files", fileType.getExtension()));
        int result = chooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    /**
     * Save file dialog with overwrite confirmation and version check (accessory panel).
     *
     * @param hint     initial suggested filename, or null (or empty) for none
     * @param fileType Select this file filter by default.
     * @return the selected new filename, or an empty string if none chosen.
     */
    public String save(String hint, Utilities.FileType fileType) {
        JFileChooser chooser = new JFileChooser();
        if (hint != null && !hint.isEmpty()) {
            chooser.setSelectedFile(new File(hint));
        }
        chooser.setFileFilter(new FileNameExtensionFilter(fileType.name()
                + " files", fileType.getExtension()));

        // Add accessory panel for version input if title and oldVersion are meaningful
        JPanel accessory = null;
        JTextField versionField = null;
        if (title != null && !title.isEmpty() && oldVersion > 0) {
            accessory = new JPanel(new GridLayout(2, 2, 5, 5));
            accessory.add(new JLabel("Title:"));
            JLabel titleLabel = new JLabel(title);
            accessory.add(titleLabel);
            accessory.add(new JLabel("Version (must be > " + oldVersion + "):"));
            versionField = new JTextField(String.format("%.1f", suggestedVersion),
                    10);
            accessory.add(versionField);
            chooser.setAccessory(accessory);
        }

        int result = chooser.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return "";
        }

        File file = chooser.getSelectedFile();
        String path = file.getPath();
        if (!path.endsWith("." + fileType.getExtension())) {
            path += "." + fileType.getExtension();
            file = new File(path);
        }

        // Overwrite confirmation
        if (file.exists()) {
            int overwrite = DialogUtils.askQuestion("File '"
                    + file.getName() + "' already exists. Overwrite?");
            if (overwrite != JOptionPane.YES_OPTION) {
                return "";
            }
        }

        // Version validation if accessory present
        if (versionField != null) {
            try {
                double enteredVersion = Double.parseDouble(versionField.getText().trim());
                if (enteredVersion <= oldVersion) {
                    DialogUtils.showMessage("Version must be greater than " + oldVersion);
                    return "";
                }
                this.suggestedVersion = enteredVersion;
            } catch (NumberFormatException e) {
                DialogUtils.showMessage("Invalid version number entered.");
                return "";
            }
        }

        return path;
    }
}