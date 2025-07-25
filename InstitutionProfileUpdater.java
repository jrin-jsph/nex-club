import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstitutionProfileUpdater {
       public static void show(JFrame parent) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        SwingUtilities.invokeLater(() -> {
            InstitutionFrame institutionFrame = new InstitutionFrame(); // or your custom JPanel
            parent.setContentPane(institutionFrame.getContentPane());
            parent.revalidate();
            parent.repaint();
        });
    }
    private static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/nexclub", "root", "admin")) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Institution (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "collegeName VARCHAR(100) NOT NULL, " +
                "institutionType VARCHAR(50) NOT NULL, " +
                "contactNumber VARCHAR(20) NOT NULL, " +
                "adminName VARCHAR(100) NOT NULL, " +
                "designation VARCHAR(50) NOT NULL, " +
                "department VARCHAR(50) NOT NULL, " +
                "UNIQUE(collegeName))");
                
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Colleges (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "UNIQUE(name))");
                
            // Insert some sample colleges if table is empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM Colleges");
            if (rs.next() && rs.getInt("count") == 0) {
                stmt.executeUpdate("INSERT INTO Colleges (name) VALUES ('Select College')");
                stmt.executeUpdate("INSERT INTO Colleges (name) VALUES ('Harvard University')");
                stmt.executeUpdate("INSERT INTO Colleges (name) VALUES ('Stanford University')");
                stmt.executeUpdate("INSERT INTO Colleges (name) VALUES ('MIT')");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            colleges.add("Select College"); // Add default option
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

        JComboBox<String> collegeName = createCollegeComboBox();
        JComboBox<String> type = createComboBox(new String[]{"Select Institution Type", "Public University", "Private College", "Institute", "School"});
        JTextField contact = createTextField();
        JLabel validationLabel = new JLabel(" ");
        validationLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        validationLabel.setForeground(Color.RED);
        validationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        load(collegeName, type, contact);

        // Greeting shown only in this frame
        JPanel greetingPanel = new JPanel();
        greetingPanel.setLayout(new BoxLayout(greetingPanel, BoxLayout.Y_AXIS));
        greetingPanel.setBackground(Color.WHITE);
        greetingPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 0));

        JLabel greeting = new JLabel("Hello,You!");
        greeting.setFont(greetingFont);
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
        box.add(createLabeledField("College/University Name", collegeName));
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
            if (collegeName.getSelectedItem() == null || 
                collegeName.getSelectedItem().toString().equals("Select College") || 
                type.getSelectedItem().toString().equals("Select Institution Type") || 
                contact.getText().isEmpty()) {
                validationLabel.setText("Please fill all institution details.");
                validationLabel.setForeground(Color.RED);
                return;
            }
            validationLabel.setText(" ");
            InstitutionFrame.institutionDetails[0] = collegeName.getSelectedItem().toString();
            InstitutionFrame.institutionDetails[1] = (String) type.getSelectedItem();
            InstitutionFrame.institutionDetails[2] = contact.getText();
            frame.showCard("AdminDetails");
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setBackground(Color.WHITE);
        south.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 20));
        south.add(next);

        add(greetingPanel, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    private void load(JComboBox<String> name, JComboBox<String> type, JTextField contact) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/nexclub", "root", "admin")) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM Institution LIMIT 1");
            if (rs.next()) {
                name.setSelectedItem(rs.getString("collegeName"));
                type.setSelectedItem(rs.getString("institutionType"));
                contact.setText(rs.getString("contactNumber"));
            } else {
                name.setSelectedIndex(0);
                type.setSelectedIndex(0);
            }
        } catch (SQLException ignored) {}
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
            if (admin.getText().isEmpty() || 
                desig.getSelectedItem().toString().equals("Select Designation") || 
                dept.getSelectedItem().toString().equals("Select Department")) {
                validationLabel.setText("Please fill all admin details.");
                validationLabel.setForeground(Color.RED);
                return;
            }
            
            InstitutionFrame.adminDetails[0] = admin.getText();
            InstitutionFrame.adminDetails[1] = (String) desig.getSelectedItem();
            InstitutionFrame.adminDetails[2] = (String) dept.getSelectedItem();

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/nexclub", "root", "admin")) {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO Institution(collegeName, institutionType, contactNumber, adminName, designation, department) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE institutionType=?, contactNumber=?, adminName=?, designation=?, department=?");
                stmt.setString(1, InstitutionFrame.institutionDetails[0]);
                stmt.setString(2, InstitutionFrame.institutionDetails[1]);
                stmt.setString(3, InstitutionFrame.institutionDetails[2]);
                stmt.setString(4, admin.getText());
                stmt.setString(5, (String) desig.getSelectedItem());
                stmt.setString(6, (String) dept.getSelectedItem());
                stmt.setString(7, InstitutionFrame.institutionDetails[1]);
                stmt.setString(8, InstitutionFrame.institutionDetails[2]);
                stmt.setString(9, admin.getText());
                stmt.setString(10, (String) desig.getSelectedItem());
                stmt.setString(11, (String) dept.getSelectedItem());
                stmt.executeUpdate();
                validationLabel.setText("Profile saved successfully!");
                validationLabel.setForeground(new Color(0, 153, 0));
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
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/nexclub", "root", "admin")) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM Institution LIMIT 1");
            if (rs.next()) {
                admin.setText(rs.getString("adminName"));
                desig.setSelectedItem(rs.getString("designation"));
                dept.setSelectedItem(rs.getString("department"));
            } else {
                desig.setSelectedIndex(0);
                dept.setSelectedIndex(0);
            }
        } catch (SQLException ignored) {}
    }
}