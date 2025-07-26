import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstitutionProfileUpdater {
    private static JFrame mainAppFrame;

    public static void show(JFrame parent) {
        mainAppFrame = parent;
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        SwingUtilities.invokeLater(() -> {
            InstitutionFrame institutionFrame = new InstitutionFrame();
            parent.setContentPane(institutionFrame.getContentPane());
            parent.revalidate();
            parent.repaint();
        });
    }

    public static JFrame getMainAppFrame() {
        return mainAppFrame;
    }
}

class InstitutionFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    public static String[] institutionDetails = new String[3];
    public static String[] adminDetails = new String[3];

    public InstitutionFrame() {
        setTitle("Institution Profile Updater");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(new InstitutionDetailsPanel(this), "InstitutionDetails");
        cardPanel.add(new AdminDetailsPanel(this), "AdminDetails");

        add(cardPanel);
        cardLayout.show(cardPanel, "InstitutionDetails");
    }

    public void showCard(String cardName) {
        cardLayout.show(cardPanel, cardName);
    }
}

abstract class BaseDetailsPanel extends JPanel {
    protected final Font greetingFont = new Font("Microsoft JhengHei", Font.BOLD, 26);
    protected final Font labelFont = new Font("Microsoft JhengHei", Font.PLAIN, 15);
    protected final Font sectionFont = new Font("Microsoft JhengHei", Font.BOLD, 21);
    protected final Font fieldFont = new Font("Microsoft JhengHei", Font.PLAIN, 15);
    protected final Dimension newDimensions = new Dimension(300, 45);

    protected JPanel createLabeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(10, 4));
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel(labelText);
        label.setFont(labelFont);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        field.setFont(fieldFont);
        field.setPreferredSize(newDimensions);
        field.setMaximumSize(newDimensions);

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    protected JTextField createTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(newDimensions);
        return field;
    }

    protected JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setPreferredSize(newDimensions);
        return combo;
    }

    protected JComboBox<String> createCollegeComboBox() {
        JComboBox<String> combo = new JComboBox<>();
        combo.setPreferredSize(newDimensions);
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/nexclub", "root", "admin")) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT name FROM Colleges");
            List<String> colleges = new ArrayList<>();
            colleges.add("Select College");
            while (rs.next()) {
                colleges.add(rs.getString("name"));
            }
            combo.setModel(new DefaultComboBoxModel<>(colleges.toArray(new String[0])));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return combo;
    }

    protected JButton createFlatButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Microsoft JhengHei", Font.BOLD, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }
}

class InstitutionDetailsPanel extends BaseDetailsPanel {
    public InstitutionDetailsPanel(InstitutionFrame frame) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JTextField collegeNameField = createTextField();
        JPopupMenu suggestionPopup = new JPopupMenu();

        JComboBox<String> type = createComboBox(new String[]{"Select Institution Type", "Public University", "Private College", "Institute", "School"});
        JTextField contact = createTextField();
        JLabel validationLabel = new JLabel(" ");
        validationLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        validationLabel.setForeground(Color.RED);
        validationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        load(collegeNameField, type, contact);

        JPanel greetingPanel = new JPanel();
        greetingPanel.setLayout(new BoxLayout(greetingPanel, BoxLayout.Y_AXIS));
        greetingPanel.setBackground(Color.WHITE);
        greetingPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 0));

        JLabel greeting = new JLabel("Hello,You!");
        greeting.setFont(new Font("Microsoft JhengHei", Font.BOLD, 35));
        JLabel instruction = new JLabel("Let's update the profile");
        instruction.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));

        greetingPanel.add(greeting);
        greetingPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        greetingPanel.add(instruction);

        Box box = Box.createVerticalBox();
        JLabel title = new JLabel("Institution Details");
        title.setFont(sectionFont);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(title);
        box.add(Box.createRigidArea(new Dimension(0, 16)));
        box.add(createLabeledField("College/University Name", collegeNameField));
        box.add(Box.createRigidArea(new Dimension(0, 16)));
        box.add(createLabeledField("Institution Type", type));
        box.add(Box.createRigidArea(new Dimension(0, 16)));
        box.add(createLabeledField("Contact Number", contact));
        box.add(Box.createRigidArea(new Dimension(0, 30)));
        box.add(validationLabel);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(Color.WHITE);
        center.add(box);

        JButton next = createFlatButton("Next");
        next.addActionListener(e -> {
            String collegeName = collegeNameField.getText().trim();
            String contactNum = contact.getText().trim();

            if (collegeName.isEmpty() ||
                type.getSelectedItem().toString().equals("Select Institution Type") ||
                contactNum.isEmpty()) {
                validationLabel.setText("Please fill all institution details.");
                validationLabel.setForeground(Color.RED);
                return;
            }

            if (!contactNum.matches("\\d{10}")) {
                validationLabel.setText("Contact number must be exactly 10 digits.");
                validationLabel.setForeground(Color.RED);
                return;
            }

            validationLabel.setText(" ");
            InstitutionFrame.institutionDetails[0] = collegeName;
            InstitutionFrame.institutionDetails[1] = (String) type.getSelectedItem();
            InstitutionFrame.institutionDetails[2] = contactNum;
            frame.showCard("AdminDetails");
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setBackground(Color.WHITE);
        south.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 20));
        south.add(next);

        add(greetingPanel, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        // Live search logic
        collegeNameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void searchColleges(String input) {
                SwingUtilities.invokeLater(() -> {
                    suggestionPopup.setVisible(false);
                    suggestionPopup.removeAll();

                    if (input.isEmpty()) return;

                    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/nexclub", "root", "admin")) {
                        PreparedStatement stmt = conn.prepareStatement(
                            "SELECT name FROM Colleges WHERE LOWER(name) LIKE ?");
                        stmt.setString(1, "%" + input.toLowerCase() + "%");

                        ResultSet rs = stmt.executeQuery();
                        while (rs.next()) {
                            String college = rs.getString("name");
                            JMenuItem item = new JMenuItem(college);
                            item.setFont(fieldFont);
                            item.setBackground(Color.WHITE);
                            item.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                            item.addActionListener(ev -> {
                                collegeNameField.setText(college);
                                suggestionPopup.setVisible(false);
                            });
                            suggestionPopup.add(item);
                        }

                        if (suggestionPopup.getComponentCount() > 0) {
                            suggestionPopup.show(collegeNameField, 0, collegeNameField.getHeight());
                            suggestionPopup.setFocusable(false);
                        }

                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                searchColleges(collegeNameField.getText().trim());
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                searchColleges(collegeNameField.getText().trim());
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                searchColleges(collegeNameField.getText().trim());
            }
        });
    }

    private void load(JTextField nameField, JComboBox<String> type, JTextField contact) {
        nameField.setText("");
        type.setSelectedIndex(0);
        contact.setText("");
    }
}


class AdminDetailsPanel extends BaseDetailsPanel {
    public AdminDetailsPanel(InstitutionFrame frame) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JTextField admin = createTextField();
        JComboBox<String> desig = createComboBox(new String[]{"Select Designation", "Super Admin", "Faculty Coordinator", "Department Head", "Staff"});
        JComboBox<String> dept = createComboBox(new String[]{"Select Department", "Student Welfare", "Computer Science", "Administration", "All Departments"});
        JLabel validationLabel = new JLabel(" ");
        validationLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        validationLabel.setForeground(Color.RED);
        validationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        load(admin, desig, dept);

        Box box = Box.createVerticalBox();
        JLabel title = new JLabel("Admin Details");
        title.setFont(sectionFont);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(title);
        box.add(Box.createRigidArea(new Dimension(0, 16)));
        box.add(createLabeledField("Admin Name", admin));
        box.add(Box.createRigidArea(new Dimension(0, 16)));
        box.add(createLabeledField("Designation", desig));
        box.add(Box.createRigidArea(new Dimension(0, 16)));
        box.add(createLabeledField("Department", dept));
        box.add(Box.createRigidArea(new Dimension(0, 30)));
        box.add(validationLabel);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(Color.WHITE);
        center.add(box);

        JButton back = createFlatButton("Back");
        back.addActionListener(e -> frame.showCard("InstitutionDetails"));

        JButton save = createFlatButton("Save");
        save.addActionListener(e -> {
            String adminName = admin.getText().trim();
            if (adminName.isEmpty() ||
                desig.getSelectedItem().toString().equals("Select Designation") ||
                dept.getSelectedItem().toString().equals("Select Department")) {
                validationLabel.setText("Please fill all admin details.");
                validationLabel.setForeground(Color.RED);
                return;
            }

            if (!adminName.matches("[A-Za-z ]+")) {
                validationLabel.setText("Admin name must contain only alphabets and spaces.");
                validationLabel.setForeground(Color.RED);
                return;
            }

            InstitutionFrame.adminDetails[0] = adminName;
            InstitutionFrame.adminDetails[1] = (String) desig.getSelectedItem();
            InstitutionFrame.adminDetails[2] = (String) dept.getSelectedItem();

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/nexclub", "root", "admin")) {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO Institution(collegeName, institutionType, contactNumber, adminName, designation, department,nID) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE institutionType=?, contactNumber=?, adminName=?, designation=?, department=?");
                stmt.setString(1, InstitutionFrame.institutionDetails[0]);
                stmt.setString(2, InstitutionFrame.institutionDetails[1]);
                stmt.setString(3, InstitutionFrame.institutionDetails[2]);
                stmt.setString(4, adminName);
                stmt.setString(5, (String) desig.getSelectedItem());
                stmt.setString(6, (String) dept.getSelectedItem());
                stmt.setString(7, InstitutionFrame.institutionDetails[1]);
                stmt.setString(8, InstitutionFrame.institutionDetails[2]);
                stmt.setString(9, adminName);
                stmt.setString(10, (String) desig.getSelectedItem());
                stmt.setString(11, (String) dept.getSelectedItem());
                stmt.executeUpdate();

                PreparedStatement updateStudentStmt = conn.prepareStatement(
                    "UPDATE Student s JOIN login l ON s.nID IS NULL SET s.nID = l.nID WHERE l.login_time = (SELECT MAX(login_time) FROM login)");
                updateStudentStmt.executeUpdate();

                validationLabel.setText("Profile saved successfully!");
                validationLabel.setForeground(new Color(0, 153, 0));

                Timer timer = new Timer(2000, evt -> {
                    JFrame mainFrame = InstitutionProfileUpdater.getMainAppFrame();
                    if (mainFrame != null) {
                        WelcomeUI.show(mainFrame);
                    } else {
                        WelcomeUI.show(new JFrame());
                    }
                });
                timer.setRepeats(false);
                timer.start();

            } catch (SQLException ex) {
                ex.printStackTrace();
                validationLabel.setText("Error saving to database.");
                validationLabel.setForeground(Color.RED);
            }
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setBackground(Color.WHITE);
        south.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 20));
        south.add(back);
        south.add(save);

        add(Box.createVerticalStrut(30), BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    private void load(JTextField admin, JComboBox<String> desig, JComboBox<String> dept) {
        admin.setText("");
        desig.setSelectedIndex(0);
        dept.setSelectedIndex(0);
    }
}
