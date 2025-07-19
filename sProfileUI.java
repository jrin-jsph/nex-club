import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class sProfileUI {
    public static void show(JFrame parent) {
        parent.setTitle("Student Profile");
        parent.getContentPane().removeAll();

        // Database setup
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/nexclub", "root", "admin");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS profile (
                    FullName VARCHAR(100),
                    Email VARCHAR(100),
                    Phone BIGINT,
                    DOB DATE,
                    Gender VARCHAR(10),
                    Nationality VARCHAR(50),
                    Address VARCHAR(255),
                    CollegeName VARCHAR(100),
                    University VARCHAR(100),
                    Degree VARCHAR(50),
                    Branch VARCHAR(100),
                    Year VARCHAR(10),
                    Roll VARCHAR(50),
                    Achievements TEXT
                )
            """);
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Main container with exact padding
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(40, 60, 40, 60));

        // Header section
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel heading = new JLabel("Hello, You!");
        heading.setFont(new Font("Inter", Font.BOLD, 36));
        heading.setForeground(new Color(45, 55, 72));
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subHeading = new JLabel("Let's update your profile");
        subHeading.setFont(new Font("Inter", Font.PLAIN, 24));
        subHeading.setForeground(new Color(113, 128, 150));
        subHeading.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(heading);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        headerPanel.add(subHeading);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        // Form container with two-column layout
        JPanel formContainer = new JPanel(new GridLayout(1, 2, 60, 0));
        formContainer.setBackground(Color.WHITE);

        // Field data
        String[] personalLabels = {"Full Name", "Email ID", "Phone Number", "Date of Birth", "Gender", "Nationality", "Address"};
        String[] academicLabels = {"College Name", "University", "Degree Program", "Branch / Department", "Year of Study", "Roll Number", "Academic Achievements"};

        JTextField[] personalFields = new JTextField[personalLabels.length];
        JTextField[] academicFields = new JTextField[academicLabels.length];
        JComboBox<String> genderBox = new JComboBox<>(new String[] {"Select", "Male", "Female", "Other"});
        JComboBox<String> yearBox = new JComboBox<>(new String[] {"Select", "1st", "2nd", "3rd", "4th"});

        // Create sections with exact styling
        JPanel personalPanel = createStyledSection("Basic Details", personalLabels, personalFields, genderBox, null);
        JPanel academicPanel = createStyledSection("Academic Info", academicLabels, academicFields, null, yearBox);

        formContainer.add(personalPanel);
        formContainer.add(academicPanel);

        // Footer with save button
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(new EmptyBorder(30, 0, 0, 0));

        JLabel statusLabel = new JLabel("");
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("Inter", Font.PLAIN, 14));

        JButton saveBtn = new JButton("Save");
        saveBtn.setFont(new Font("Inter", Font.BOLD, 16));
        saveBtn.setBackground(new Color(49, 130, 206));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Save button action with proper validation
        saveBtn.addActionListener(e -> {
            String fullName = personalFields[0].getText().trim();
            String email = personalFields[1].getText().trim();
            String phone = personalFields[2].getText().trim();
            String dob = personalFields[3].getText().trim();
            String gender = genderBox.getSelectedItem().toString();
            String nationality = personalFields[5].getText().trim();
            String address = personalFields[6].getText().trim();
            String college = academicFields[0].getText().trim();
            String university = academicFields[1].getText().trim();
            String degree = academicFields[2].getText().trim();
            String branch = academicFields[3].getText().trim();
            String year = yearBox.getSelectedItem().toString();
            String roll = academicFields[5].getText().trim();
            String achievements = academicFields[6].getText().trim();

            // Validation
            if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || dob.isEmpty() || 
                gender.equals("Select") || nationality.isEmpty() || address.isEmpty() ||
                college.isEmpty() || university.isEmpty() || degree.isEmpty() || 
                branch.isEmpty() || year.equals("Select") || roll.isEmpty()) {
                statusLabel.setText("Please fill in all required fields");
                return;
            }

            if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                statusLabel.setText("Invalid email format");
                return;
            }

            if (!phone.matches("\\d{10}")) {
                statusLabel.setText("Phone must be 10 digits");
                return;
            }

            try {
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/nexclub", "root", "admin");
                String sql = "INSERT INTO profile VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                
                stmt.setString(1, fullName);
                stmt.setString(2, email);
                stmt.setString(3, phone);
                stmt.setString(4, dob);
                stmt.setString(5, gender);
                stmt.setString(6, nationality);
                stmt.setString(7, address);
                stmt.setString(8, college);
                stmt.setString(9, university);
                stmt.setString(10, degree);
                stmt.setString(11, branch);
                stmt.setString(12, year);
                stmt.setString(13, roll);
                stmt.setString(14, achievements);
                
                stmt.executeUpdate();
                conn.close();
                
                statusLabel.setText("Profile saved successfully!");
                statusLabel.setForeground(new Color(0, 128, 0));
                
                new Timer().schedule(new TimerTask() {
                    public void run() {
                        // Return to welcome screen after 2 seconds
                        SwingUtilities.invokeLater(() -> welUI.show(parent));
                    }
                }, 2000);
                
            } catch (SQLException ex) {
                statusLabel.setText("Error saving to database");
                ex.printStackTrace();
            }
        });

        footerPanel.add(statusLabel);
        footerPanel.add(Box.createHorizontalStrut(20));
        footerPanel.add(saveBtn);

        // Assemble all components
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formContainer, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        parent.getContentPane().add(mainPanel);
        parent.revalidate();
        parent.repaint();
    }

    private static JPanel createStyledSection(String title, String[] labels, JTextField[] fields, 
                                           JComboBox<String> genderBox, JComboBox<String> yearBox) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(Color.WHITE);
        section.setBorder(new EmptyBorder(0, 0, 0, 40));

        // Section title with underline
        JLabel sectionTitle = new JLabel(title);
        sectionTitle.setFont(new Font("Inter", Font.BOLD, 28));
        sectionTitle.setForeground(new Color(45, 55, 72));
        sectionTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(224, 224, 224)));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        section.add(sectionTitle);
        section.add(Box.createRigidArea(new Dimension(0, 32)));

        // Create each form row
        for (int i = 0; i < labels.length; i++) {
            JPanel row = new JPanel();
            row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
            row.setBackground(Color.WHITE);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Label with validation indicator
            JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            labelPanel.setBackground(Color.WHITE);
            
            // Add validation indicator
            JLabel validationIcon = new JLabel(getValidationIcon(labels[i]));
            validationIcon.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            validationIcon.setForeground(getValidationColor(labels[i]));
            labelPanel.add(validationIcon);
            
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Inter", Font.PLAIN, 16));
            label.setForeground(new Color(74, 85, 104));
            labelPanel.add(label);

            // Input field
            JComponent input;
            if (labels[i].equals("Gender")) {
                input = genderBox;
                ((JComboBox<?>)input).setRenderer(new StyledComboBoxRenderer());
                ((JComboBox<?>)input).setPreferredSize(new Dimension(400, 40));
            } else if (labels[i].equals("Year of Study")) {
                input = yearBox;
                ((JComboBox<?>)input).setRenderer(new StyledComboBoxRenderer());
                ((JComboBox<?>)input).setPreferredSize(new Dimension(400, 40));
            } else {
                fields[i] = new JTextField();
                input = fields[i];
                input.setPreferredSize(new Dimension(400, 
                    labels[i].contains("Achievements") || labels[i].contains("Address") ? 100 : 40));
            }

            // Common styling for all inputs
            input.setFont(new Font("Inter", Font.PLAIN, 16));
            input.setForeground(new Color(45, 55, 72));
            input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
            input.setBackground(Color.WHITE);
            input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 
                labels[i].contains("Achievements") || labels[i].contains("Address") ? 100 : 40));

            // Add components to row
            row.add(labelPanel);
            row.add(Box.createRigidArea(new Dimension(0, 8)));
            row.add(input);
            section.add(row);
            section.add(Box.createRigidArea(new Dimension(0, 24)));
        }

        return section;
    }

    private static String getValidationIcon(String fieldName) {
        if (fieldName.equals("Full Name")) return "[ ]";
        if (fieldName.equals("Roll Number")) return "[#]";
        return "[x]";
    }

    private static Color getValidationColor(String fieldName) {
        if (fieldName.equals("Full Name")) return new Color(160, 174, 192); // Gray
        return new Color(56, 161, 105); // Green
    }

    static class StyledComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                                                    boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 224, 224)),
                BorderFactory.createEmptyBorder(8, 12, 8, 28)
            ));
            if (!isSelected) setBackground(Color.WHITE);
            return this;
        }
    }
}