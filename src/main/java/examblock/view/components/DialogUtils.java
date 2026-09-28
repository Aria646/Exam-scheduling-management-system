package examblock.view.components;

import examblock.model.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

/**
 * Dialogs for easy use
 * [1] Implement the code according to the homework requirements
 * [2] Code according to Javadoc
 * [3][4] Comment in Javadoc style
 */
public class DialogUtils {

    /**
     * The parent component for dialogs
     */
    private static Component parent = null;

    /**
     * How to handle long lines of text in the TextViewer
     */
    public enum ViewerOptions {
        /** Scroll the window to show the text */
        SCROLL,
        /** Wrap the text in the window */
        WRAP
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
     * simple informational box
     *
     * @param message what to say
     */
    public static void showMessage(String message) {
        JOptionPane.showMessageDialog(parent, message,
                "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * prompt for a Yes/No/Cancel response from the user
     *
     * @param message text to display
     * @return return code (JOptionPane.YES_OPTION, NO_OPTION, CANCEL_OPTION)
     */
    public static int askQuestion(String message) {
        return JOptionPane.showConfirmDialog(parent, message,
                "Question", JOptionPane.YES_NO_CANCEL_OPTION);
    }

    /**
     * put up a dialog box with a JTextBox in it
     *
     * @param message      what to say
     * @param title        title for the popup window
     * @param initialValue if any
     * @return the entered text, or null if cancelled
     */
    public static String getUserInput(String message, String title, String initialValue) {
        return (String) JOptionPane.showInputDialog(parent, message, title,
                JOptionPane.QUESTION_MESSAGE, null, null, initialValue);
    }

    /**
     * Displays a text viewer dialog with a wrap text option.
     * Shows the provided text in a scrollable text area, with a View menu
     * to toggle line wrapping. The option parameter sets the initial wrap state.
     *
     * @param text     the text to display
     * @param title    the dialog title
     * @param option   how to handle long lines of text in the TextViewer
     * @param fileType the file type to save the text as (e.g., EDR or TXT)
     */
    public static void showTextViewer(String text, String title, ViewerOptions option,
                                      Utilities.FileType fileType) {
        JFrame viewerFrame = new JFrame(title);
        viewerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        viewerFrame.setSize(800, 600);

        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));


        // Set initial wrap state
        if (option == ViewerOptions.WRAP) {
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
        } else {
            textArea.setLineWrap(false);
        }
        // Create menubar with View menu
        JMenuBar menuBar = new JMenuBar();
        JMenu viewMenu = new JMenu("View");
        JCheckBoxMenuItem wrapItem = new JCheckBoxMenuItem("Wrap Text",
                option == ViewerOptions.WRAP);
        wrapItem.addActionListener((ActionEvent e) -> {
            JCheckBoxMenuItem source = (JCheckBoxMenuItem) e.getSource();
            textArea.setLineWrap(source.isSelected());
            textArea.setWrapStyleWord(source.isSelected());
        });
        viewMenu.add(wrapItem);
        menuBar.add(viewMenu);

        // Save button on toolbar (optional)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save As...");
        saveButton.addActionListener(e -> {
            FileChooser chooser = new FileChooser();
            String chosen = chooser.save(null, fileType);
            if (chosen != null && !chosen.isEmpty()) {
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(chosen))) {
                    bw.write(text);
                    JOptionPane.showMessageDialog(viewerFrame,
                            "Saved to " + chosen, "Save Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(viewerFrame,
                            "Error saving: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        bottomPanel.add(saveButton);
        JScrollPane scrollPane = new JScrollPane(textArea);
        viewerFrame.setJMenuBar(menuBar);
        viewerFrame.add(scrollPane, BorderLayout.CENTER);
        viewerFrame.add(bottomPanel, BorderLayout.SOUTH);
        viewerFrame.setLocationRelativeTo(parent);
        viewerFrame.setVisible(true);
    }
}