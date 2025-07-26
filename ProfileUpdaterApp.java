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

        // Create student table if not exists
        createStudentTableIfNotExists();

        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            parent.setContentPane(mainFrame.getContentPane());
            parent.revalidate();
            parent.repaint();
        });
    }

    private static void createStudentTableIfNotExists() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/nexclub", "root", "admin")) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS student (" +
                "nID VARCHAR(30), " +
                "fullName VARCHAR(100) NOT NULL, " +
                "phone VARCHAR(15) NOT NULL, " +
                "dob DATE, " +
                "gender VARCHAR(10) NOT NULL, " +
                "college VARCHAR(100) NOT NULL, " +
                "university VARCHAR(100) NOT NULL, " +
                "degree VARCHAR(100) NOT NULL, " +
                "branch VARCHAR(100) NOT NULL, " +
                "yearOfStudy VARCHAR(15) NOT NULL, " +
                "PRIMARY KEY (nID), " +
                "UNIQUE KEY (nID)" +
                ")";
            Statement stmt = conn.createStatement();
            stmt.execute(createTableSQL);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        field.setMaximumSize(new Dimension(400, 45));
        field.setPreferredSize(new Dimension(400, 45));
        field.setMinimumSize(new Dimension(400, 45));

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
        field.setMaximumSize(new Dimension(400, 45));
        field.setPreferredSize(new Dimension(400, 45));
        field.setMinimumSize(new Dimension(400, 45));
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
        label.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        label.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}

class BasicDetailsPanel extends AbstractFormPanel {

    public BasicDetailsPanel(MainFrame frame) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Create greeting panel
        JPanel greetingPanel = new JPanel();
        greetingPanel.setLayout(new BoxLayout(greetingPanel, BoxLayout.Y_AXIS));
        greetingPanel.setBackground(Color.WHITE);
        greetingPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 0));

        JLabel greeting = new JLabel("Hello, You!");
        greeting.setFont(new Font("Microsoft JhengHei", Font.BOLD, 40));
        JLabel instruction = new JLabel("Let's update the student profile");
        instruction.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));

        greetingPanel.add(greeting);
        greetingPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        greetingPanel.add(instruction);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);
        Box contentBox = Box.createVerticalBox();

        JLabel sectionTitle = new JLabel("Basic Details");
        sectionTitle.setFont(sectionFont);
        sectionTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 12, 0));
        sectionTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField fullNameField = createTextField();
        JTextField phoneField = createTextField();
        JTextField dobField = createTextField();
        JComboBox<String> genderComboBox = createComboBox(
            new String[] {"-- Select Gender --", "Male", "Female", "Other"}, 400, 45);

        // Add document listener for phone number validation
        phoneField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validatePhone();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validatePhone();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validatePhone();
            }

            private void validatePhone() {
                String phone = phoneField.getText().trim();
                if (!phone.matches("\\d{0,10}")) {
                    phoneField.setText(phone.replaceAll("[^\\d]", ""));
                }
            }
        });

        JLabel validationLabel = createValidationLabel();
        validationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        validationLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton nextButton = createFlatButton("Next");
        nextButton.addActionListener(e -> {
            String fullName = fullNameField.getText().trim();
            String phone = phoneField.getText().trim();
            String dob = dobField.getText().trim();
            String gender = (String) genderComboBox.getSelectedItem();

            // Validate all fields
            if (fullName.isEmpty() || phone.isEmpty() || dob.isEmpty() || gender.equals("-- Select Gender --")) {
                validationLabel.setText("Please complete all fields.");
                validationLabel.setForeground(Color.RED);
                return;
            }

            // Validate phone number (exactly 10 digits)
            if (!phone.matches("\\d{10}")) {
                validationLabel.setText("Phone number must be 10 digits.");
                validationLabel.setForeground(Color.RED);
                return;
            }

            // Validate date format (YYYY-MM-DD)
            if (!dob.matches("\\d{4}-\\d{2}-\\d{2}")) {
                validationLabel.setText("Please enter date in YYYY-MM-DD format.");
                validationLabel.setForeground(Color.RED);
                return;
            }

            // Store basic details
            MainFrame.basicDetails[0] = fullName;
            MainFrame.basicDetails[1] = phone;
            MainFrame.basicDetails[2] = dob;
            MainFrame.basicDetails[3] = gender;

            frame.showCard("AcademicInfo");
        });

        Dimension spacing = new Dimension(0, 16);

        contentBox.add(sectionTitle);
        contentBox.add(Box.createRigidArea(spacing));
        contentBox.add(createLabeledField("Full Name", fullNameField));
        contentBox.add(Box.createRigidArea(spacing));
        contentBox.add(createLabeledField("Phone Number", phoneField));
        contentBox.add(Box.createRigidArea(spacing));
        contentBox.add(createLabeledField("Date of Birth (YYYY-MM-DD)", dobField));
        contentBox.add(Box.createRigidArea(spacing));
        contentBox.add(createLabeledField("Gender", genderComboBox));
        contentBox.add(Box.createRigidArea(spacing));
        contentBox.add(validationLabel);

        centerPanel.add(contentBox, new GridBagConstraints());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 40, 20));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(nextButton);

        // Add greeting panel to the north
        add(greetingPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}

class AcademicInfoPanel extends AbstractFormPanel {
    public AcademicInfoPanel(MainFrame frame) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);
        Box contentBox = Box.createVerticalBox();

        JPanel greetingPanel = new JPanel();
        greetingPanel.setLayout(new BoxLayout(greetingPanel, BoxLayout.Y_AXIS));
        greetingPanel.setBackground(Color.WHITE);
        greetingPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 0));

        JLabel greeting = new JLabel("Hello, You!");
        greeting.setFont(new Font("Microsoft JhengHei", Font.BOLD, 40));
        JLabel instruction = new JLabel("Let's update the student profile");
        instruction.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));

        greetingPanel.add(greeting);
        greetingPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        greetingPanel.add(instruction);
        
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

        // University ComboBox
        JComboBox<String> universityComboBox = createComboBox(
            new String[] {"-- Select University --", "Anna University", "VTU", "JNTU", "KTU","Delhi University"}, 400, 45);

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
            new String[] {"-- Select Degree --", "B.Tech", "M.Tech", "MBA", "BBA", "B.Sc", "M.Sc", "PhD"}, 400, 45);

        JComboBox<String> branchComboBox = createComboBox(
            new String[] {"-- Select Branch --", "Computer Science", "Mechanical Engineering", "Civil Engineering", "Electrical Engineering", "Electronics", "Chemical"}, 400, 45);

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
            String selectedUniversity = (String) universityComboBox.getSelectedItem();
            String selectedDegree = (String) degreeComboBox.getSelectedItem();
            String selectedBranch = (String) branchComboBox.getSelectedItem();
            String year = yearOfStudyField.getText().trim();

            // Validation checks
            if (fullName.isEmpty() || phone.isEmpty() || dob.isEmpty() || gender.isEmpty() ||
                selectedCollege.equals("-- Select College --") ||
                selectedUniversity.equals("-- Select University --") ||
                selectedDegree.equals("-- Select Degree --") ||
                selectedBranch.equals("-- Select Branch --") ||
                year.isEmpty()) {
                
                validationLabel.setText("Please complete all fields.");
                validationLabel.setForeground(Color.RED);
                return;
            }

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/nexclub", "root", "admin")) {
                // Get nID from login table
                String nID = null;
                PreparedStatement getNIDStmt = conn.prepareStatement(
                    "SELECT nID FROM login ORDER BY login_time DESC LIMIT 1");
                ResultSet nIDRs = getNIDStmt.executeQuery();
                if (nIDRs.next()) {
                    nID = nIDRs.getString("nID");
                }

                if (nID == null) {
                    validationLabel.setText("No user logged in.");
                    validationLabel.setForeground(Color.RED);
                    return;
                }

                // Check if profile exists
                PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM student WHERE nID = ?");
                checkStmt.setString(1, nID);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);

                if (count > 0) {
                    // Update existing profile
                    PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE student SET fullName=?, phone=?, dob=?, gender=?, college=?, university=?, degree=?, branch=?, yearOfStudy=? WHERE nID=?");
                    
                    updateStmt.setString(1, fullName);
                    updateStmt.setString(2, phone);
                    updateStmt.setString(3, dob);
                    updateStmt.setString(4, gender);
                    updateStmt.setString(5, selectedCollege);
                    updateStmt.setString(6, selectedUniversity);
                    updateStmt.setString(7, selectedDegree);
                    updateStmt.setString(8, selectedBranch);
                    updateStmt.setString(9, year);
                    updateStmt.setString(10, nID);
                    updateStmt.executeUpdate();

                    validationLabel.setText("Profile successfully updated.");
                    validationLabel.setForeground(new Color(0, 153, 0));
                } else {
                    // Insert new profile
                    PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO student(nID, fullName, phone, dob, gender, college, university, degree, branch, yearOfStudy) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    
                    insertStmt.setString(1, nID);
                    insertStmt.setString(2, fullName);
                    insertStmt.setString(3, phone);
                    insertStmt.setString(4, dob);
                    insertStmt.setString(5, gender);
                    insertStmt.setString(6, selectedCollege);
                    insertStmt.setString(7, selectedUniversity);
                    insertStmt.setString(8, selectedDegree);
                    insertStmt.setString(9, selectedBranch);
                    insertStmt.setString(10, year);
                    insertStmt.executeUpdate();

                    validationLabel.setText("Profile successfully submitted.");
                    validationLabel.setForeground(new Color(0, 153, 0));
                }

                // Add 2-second delay before switching to WelcomeUI
                Timer timer = new Timer(2000, evt -> {
                    JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(this);
                    WelcomeUI.show(parentFrame);
                });
                timer.setRepeats(false); // Only execute once
                timer.start();
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
        contentBox.add(createLabeledField("University", universityComboBox));
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
         
        add(greetingPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}