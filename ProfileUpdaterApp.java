import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Vector;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ProfileUpdaterApp {
    public static void show(JFrame parent) {
    try {
        UIManager.setLookAndFeel(new FlatLightLaf());
    } catch (Exception ex) {
        System.err.println("Failed to initialize FlatLaf");
    }

    SwingUtilities.invokeLater(() -> {
        MainFrame mainFrame = new MainFrame();
        parent.setContentPane(mainFrame.getContentPane()); // ✅ switch view
        parent.revalidate();
        parent.repaint();
    });
}
}

class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;

    public static String[] basicDetails = new String[4];

    public MainFrame() {
        setTitle("Profile Updater");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        BasicDetailsPanel basicDetailsPanel = new BasicDetailsPanel(this);
        AcademicInfoPanel academicInfoPanel = new AcademicInfoPanel(this);

        cardPanel.add(basicDetailsPanel, "BasicDetails");
        cardPanel.add(academicInfoPanel, "AcademicInfo");

        add(cardPanel);
        cardLayout.show(cardPanel, "BasicDetails");
    }

    public void showCard(String cardName) {
        cardLayout.show(cardPanel, cardName);
    }
}

abstract class AbstractFormPanel extends JPanel {
    protected Font labelFont = new Font("Microsoft JhengHei", Font.PLAIN, 15);
    protected Font sectionFont = new Font("Microsoft JhengHei", Font.BOLD, 21);
    protected Font fieldFont = new Font("Microsoft JhengHei", Font.PLAIN, 15);

    protected JPanel createLabeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel(labelText);
        label.setFont(labelFont);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(300, 45));
        field.setPreferredSize(new Dimension(300, 45));
        field.setMinimumSize(new Dimension(300, 45));

        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(field);
        return panel;
    }

    protected JTextField createTextField() {
        JTextField field = new JTextField();
        styleField(field);
        return field;
    }

    protected void styleField(JTextField field) {
        field.setHorizontalAlignment(JTextField.LEFT);
        field.setFont(fieldFont);
        field.setMaximumSize(new Dimension(300, 45));
        field.setPreferredSize(new Dimension(300, 45));
        field.setMinimumSize(new Dimension(300, 45));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    protected JComboBox<String> createComboBox(String[] items, int width, int height) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(fieldFont);
        comboBox.setMaximumSize(new Dimension(width, height));
        comboBox.setPreferredSize(new Dimension(width, height));
        comboBox.setMinimumSize(new Dimension(width, height));
        comboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        return comboBox;
    }

    protected JButton createFlatButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Microsoft JhengHei", Font.BOLD, 15));
        button.setForeground(Color.BLACK);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    protected JLabel createValidationLabel() {
        JLabel label = new JLabel("");
        label.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}

class AcademicInfoPanel extends AbstractFormPanel {
    public AcademicInfoPanel(MainFrame frame) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);
        Box contentBox = Box.createVerticalBox();

        JLabel sectionTitle = new JLabel("Academic Details");
        sectionTitle.setFont(sectionFont);
        sectionTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 12, 0));
        sectionTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // College ComboBox with default item
        JComboBox<String> collegeComboBox = new JComboBox<>();
        collegeComboBox.addItem("-- Select College --");
        collegeComboBox.setFont(fieldFont);
        collegeComboBox.setMaximumSize(new Dimension(400, 50));
        collegeComboBox.setPreferredSize(new Dimension(400, 50));
        collegeComboBox.setMinimumSize(new Dimension(400, 50));
        collegeComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/nexclub", "root", "admin")) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name FROM colleges");
            while (rs.next()) {
                collegeComboBox.addItem(rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Degree and Branch ComboBoxes with default selections
        JComboBox<String> degreeComboBox = createComboBox(
            new String[] {"-- Select Degree --", "B.Tech", "M.Tech", "MBA", "BBA", "B.Sc", "M.Sc", "PhD"}, 300, 45);

        JComboBox<String> branchComboBox = createComboBox(
            new String[] {"-- Select Branch --", "Computer Science", "Mechanical Engineering", "Civil Engineering", "Electrical Engineering", "Electronics", "Chemical"}, 300, 45);

        JTextField yearOfStudyField = createTextField();

        JLabel validationLabel = createValidationLabel();
        validationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        validationLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton submitButton = createFlatButton("Submit");
        submitButton.addActionListener(e -> {
            String fullName = MainFrame.basicDetails[0];
            String phone = MainFrame.basicDetails[1];
            String dob = MainFrame.basicDetails[2];
            String gender = MainFrame.basicDetails[3];

            String selectedCollege = (String) collegeComboBox.getSelectedItem();
            String selectedDegree = (String) degreeComboBox.getSelectedItem();
            String selectedBranch = (String) branchComboBox.getSelectedItem();
            String year = yearOfStudyField.getText().trim();

            if (fullName.isEmpty() || phone.isEmpty() || dob.isEmpty() || gender.isEmpty()) {
                validationLabel.setText("Basic details are incomplete.");
                validationLabel.setForeground(Color.RED);
                return;
            }

            if (selectedCollege.equals("-- Select College --") ||
                selectedDegree.equals("-- Select Degree --") ||
                selectedBranch.equals("-- Select Branch --") ||
                year.isEmpty()) {

                validationLabel.setText("Please complete all academic fields.");
                validationLabel.setForeground(Color.RED);
                return;
            }

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/nexclub", "root", "admin")) {
                PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM Student WHERE fullName = ?");
                checkStmt.setString(1, fullName);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);

                if (count > 0) {
                    validationLabel.setText("Profile already exists.");
                    validationLabel.setForeground(Color.RED);
                } else {
                    PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO Student(fullName, phone, dob, gender, college, degree, branch, yearOfStudy) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                    insertStmt.setString(1, fullName);
                    insertStmt.setString(2, phone);
                    insertStmt.setString(3, dob);
                    insertStmt.setString(4, gender);
                    insertStmt.setString(5, selectedCollege);
                    insertStmt.setString(6, selectedDegree);
                    insertStmt.setString(7, selectedBranch);
                    insertStmt.setString(8, year);
                    insertStmt.executeUpdate();

                    validationLabel.setText("Profile successfully submitted.");
                    validationLabel.setForeground(new Color(0, 153, 0));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                validationLabel.setText("Database error occurred.");
                validationLabel.setForeground(Color.RED);
            }
        });

        JButton backButton = createFlatButton("Back");
        backButton.addActionListener(e -> frame.showCard("BasicDetails"));

        Dimension spacing = new Dimension(0, 16);

        contentBox.add(sectionTitle);
        contentBox.add(Box.createRigidArea(spacing));
        contentBox.add(createLabeledField("College", collegeComboBox));
        contentBox.add(Box.createRigidArea(spacing));
        contentBox.add(createLabeledField("Degree", degreeComboBox));
        contentBox.add(Box.createRigidArea(spacing));
        contentBox.add(createLabeledField("Branch", branchComboBox));
        contentBox.add(Box.createRigidArea(spacing));
        contentBox.add(createLabeledField("Year of Study", yearOfStudyField));
        contentBox.add(Box.createRigidArea(spacing));
        contentBox.add(validationLabel);

        centerPanel.add(contentBox, new GridBagConstraints());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 40, 20));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(backButton);
        buttonPanel.add(submitButton);

        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}


