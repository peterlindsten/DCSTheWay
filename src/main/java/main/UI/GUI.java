package main.UI;

import com.formdev.flatlaf.FlatDarkLaf;
import main.DCSconnection.PortListenerThread;
import main.Utils.CoordinateUtils;
import main.Waypoints.WaypointManager;
import main.models.F15EOptions;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.InputStream;
import java.math.BigDecimal;

public class GUI {
    static private JFrame frame;
    static private Crosshair crosshair;
    static private JTextArea textArea;
    static private JButton beginSelectionButton;
    static private JButton selectPointButton;
    static private JButton transferToDCSButton;
    static private JButton clearPointsButton;
    static private JTextArea debug;


    public static void show() {
        FlatDarkLaf.setup();
        crosshair = new Crosshair();
        JPanel gui = new JPanel(new GridLayout(0, 1, 10, 10));
        gui.setBorder(new EmptyBorder(10, 10, 10, 10));
        beginSelectionButton = new JButton("Start selecting on map");
        textArea = new JTextArea(1, 10);
        transferToDCSButton = new JButton("Begin transfer to DCS");
        selectPointButton = new JButton("Select point");
        clearPointsButton = new JButton("Discard points");
        debug = new JTextArea(3, 10);


        beginSelectionButton.addActionListener(e -> emptySelectionState());
        transferToDCSButton.addActionListener(e -> {
            WaypointManager.transfer();
            transferredState();
        });

        selectPointButton.addActionListener(e -> {
            if (WaypointManager.saveWaypointSuccessful()) {
                refreshWaypointCount();
                populatedSelectionState();
                debug.setText("Lat: " + CoordinateUtils.decimalToDMS(new BigDecimal(PortListenerThread.getLatitude())) +
                        "\nLong: " + CoordinateUtils.decimalToDMS(new BigDecimal(PortListenerThread.getLongitude())) +
                        "\nElev: " + PortListenerThread.getElevation());
                debug.updateUI();
            } else {
                error("No connection to DCS detected.");
                standbyState();
            }
        });

        clearPointsButton.addActionListener(e -> {
            standbyState();
            WaypointManager.clearWaypoints();
            refreshWaypointCount();
        });

        gui.add(beginSelectionButton);
        gui.add(textArea);
        gui.add(selectPointButton);
        gui.add(clearPointsButton);
        gui.add(transferToDCSButton);
        //gui.add(debug);

        standbyState();
        frame = new JFrame("The Way");
        frame.add(gui);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        frame.setFocusableWindowState(false);
        frame.pack();
        frame.setResizable(false);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(0, dim.height - frame.getHeight());
        loadIcon();
        frame.setVisible(true);
    }

    public static void warning(String text) {
        JDialog dialog = new JDialog(frame, "Heads up!", true);
        JButton continueButton = new JButton("Continue");
        continueButton.addActionListener(e -> dialog.dispose());
        JButton[] option = {continueButton};
        JOptionPane optionPane = new JOptionPane(text, JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION, null, option, option[0]);

        Point framePoint = frame.getLocation();
        dialog.setLocation((int) framePoint.getX() + frame.getWidth(), (int) framePoint.getY());
        dialog.setContentPane(optionPane);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.setFocusableWindowState(false);
        dialog.pack();
        dialog.setVisible(true);
    }

    public static void error(String text) {
        JDialog dialog = new JDialog(frame, "Error", true);
        JButton continueButton = new JButton("OK");
        continueButton.addActionListener(e -> dialog.dispose());
        JButton[] option = {continueButton};
        JOptionPane optionPane = new JOptionPane(text, JOptionPane.ERROR_MESSAGE, JOptionPane.YES_NO_OPTION, null, option, option[0]);
        Point framePoint = frame.getLocation();
        dialog.setLocation((int) framePoint.getX() + frame.getWidth(), (int) framePoint.getY());
        dialog.setContentPane(optionPane);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.setFocusableWindowState(false);
        dialog.pack();
        dialog.setVisible(true);
    }

    public static String choice(String question, String option1, String option2) {
        JDialog dialog = new JDialog(frame, "Choose an option", true);
        Object[] options = {option1, option2};
        JOptionPane optionPane = new JOptionPane(question, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null, options, options[0]);

        Point framePoint = frame.getLocation();
        dialog.setLocation((int) framePoint.getX() + frame.getWidth(), (int) framePoint.getY());
        dialog.setContentPane(optionPane);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setResizable(false);
        dialog.setFocusableWindowState(false);

        optionPane.addPropertyChangeListener(
                e -> {
                    String prop = e.getPropertyName();
                    if (dialog.isVisible()
                            && (e.getSource() == optionPane)
                            && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                        dialog.setVisible(false);
                    }
                });

        dialog.pack();
        dialog.setVisible(true);
        return (String) optionPane.getValue();
    }

    public static String multiChoice(String question, String[] choices) {
        JDialog dialog = new JDialog(frame, "Choose an option", true);
        JOptionPane optionPane = new JOptionPane(question, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null, choices, choices[0]);

        Point framePoint = frame.getLocation();
        dialog.setLocation((int) framePoint.getX() + frame.getWidth(), (int) framePoint.getY());
        dialog.setContentPane(optionPane);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setResizable(false);
        dialog.setFocusableWindowState(false);

        optionPane.addPropertyChangeListener(
                e -> {
                    String prop = e.getPropertyName();
                    if (dialog.isVisible()
                            && (e.getSource() == optionPane)
                            && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                        dialog.setVisible(false);
                    }
                });

        dialog.pack();
        dialog.setVisible(true);
        return (String) optionPane.getValue();
    }

    public static F15EOptions f15eDialog() {
        JDialog dialog = new JDialog(frame, "Choose options", true);
        Object[] options = {"Pilot", "WSO"};
        JOptionPane optionPane = new JOptionPane("Seat?\nWarning: Cannot overwrite current SP", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null, options, options[0]);

        JPanel dialogPanel = new JPanel(new GridLayout(1, 0, 0, 10));
        JPanel dialogTable = new JPanel(new GridLayout(0, 2, 10, 10));
        dialogTable.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        JComboBox<String> seriesBox = new JComboBox<>(new String[]{"A", "B", "C"});
        seriesBox.setSelectedIndex(1);
        seriesBox.setEditable(false);
        JComboBox<String> firstBox = new JComboBox<>(new String[]{"1", "2", "3", "4", "5"});
        firstBox.setSelectedIndex(0);
        firstBox.setEditable(true);
        dialogTable.add(new JLabel("SP Series"));
        dialogTable.add(seriesBox);
        dialogTable.add(new JLabel("First SP"));
        dialogTable.add(firstBox);
        dialogPanel.add(optionPane);
        dialogPanel.add(dialogTable);

        Point framePoint = frame.getLocation();
        dialog.setLocation((int) framePoint.getX() + frame.getWidth(), (int) framePoint.getY());
        dialog.setContentPane(dialogPanel);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setResizable(false);
        dialog.setFocusableWindowState(false);
        optionPane.addPropertyChangeListener(
                e -> {
                    String prop = e.getPropertyName();
                    if (dialog.isVisible()
                            && (e.getSource() == optionPane)
                            && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                        dialog.setVisible(false);
                    }
                });

        dialog.pack();
        dialog.setVisible(true);

        return new F15EOptions("Pilot".equals(optionPane.getValue()),
                (String) seriesBox.getSelectedObjects()[0],
                Integer.parseInt((String) firstBox.getEditor().getItem()));
    }

    private static void standbyState() {
        crosshair.hide();
        beginSelectionButton.setEnabled(true);
        clearPointsButton.setEnabled(false);
        transferToDCSButton.setEnabled(false);
        selectPointButton.setEnabled(false);
    }

    private static void emptySelectionState() {
        crosshair.show();
        beginSelectionButton.setEnabled(false);
        selectPointButton.setEnabled(true);
    }

    private static void populatedSelectionState() {
        clearPointsButton.setEnabled(true);
        transferToDCSButton.setEnabled(true);
    }

    private static void transferredState() {
        crosshair.hide();
        selectPointButton.setEnabled(false);
        clearPointsButton.setEnabled(true);
        beginSelectionButton.setEnabled(false);
    }

    private static void refreshWaypointCount() {
        int count = WaypointManager.getSelectedWaypointsCount();
        if (count > 0) {
            if (count == 1) {
                textArea.setText("1 waypoint selected");
            } else {
                textArea.setText(count + " waypoints selected");
            }
        } else {
            textArea.setText("No waypoints selected");
        }
    }

    private static void loadIcon() {
        try {
            try (InputStream in = GUI.class.getClassLoader()
                    .getResourceAsStream("TheWayIcon40.png")) {
                if (in != null) {
                    byte[] bytes = in.readAllBytes();
                    ImageIcon icon = new ImageIcon(bytes);
                    frame.setIconImage(icon.getImage());
                }
            }
        } catch (Exception ignored) {
            // No icon
        }
    }
}
