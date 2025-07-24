import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StudentProfileUI {
    private static JLabel phoneHintLabel;
    private static JLabel dobHintLabel;
    private static JLabel nameHintLabel;
    private static JLabel genderHintLabel;
    private static JLabel nationalityHintLabel;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JFrame frame = new JFrame("Student Profile");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(950, 650);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new CardLayout());
        mainPanel.setBackground(Color.WHITE);

        // Personal Details Panel
        JPanel personalPanel = new JPanel(new BorderLayout());
        personalPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        personalPanel.setBackground(Color.WHITE);

        JLabel headingLabel = new JLabel("Hello, You!");
        headingLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 32));
        JLabel subHeadingLabel = new JLabel("Let's update the profile");
        subHeadingLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));
        subHeadingLabel.setForeground(Color.GRAY);

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(headingLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(subHeadingLabel);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 14, 10, 14);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        Font labelFont = new Font("Microsoft JhengHei", Font.BOLD, 21);
        Font inputFont = new Font("Microsoft JhengHei", Font.PLAIN, 16);

        // Full Name
        JLabel nameLabel = new JLabel("Full Name:", JLabel.RIGHT);
        nameLabel.setFont(labelFont);
        formPanel.add(nameLabel, gbc);
        gbc.gridx++;
        JTextField nameField = new JTextField(25);
        nameField.setFont(inputFont);
        formPanel.add(nameField, gbc);
        gbc.gridy++;
        gbc.gridx = 1;
        nameHintLabel = new JLabel(" ");
        nameHintLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        nameHintLabel.setForeground(Color.RED);
        formPanel.add(nameHintLabel, gbc);

        // Phone Number
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel phoneLabel = new JLabel("Phone Number:", JLabel.RIGHT);
        phoneLabel.setFont(labelFont);
        formPanel.add(phoneLabel, gbc);
        gbc.gridx++;
        JTextField phoneField = new JTextField(25);
        phoneField.setFont(inputFont);
        formPanel.add(phoneField, gbc);
        gbc.gridy++;
        gbc.gridx = 1;
        phoneHintLabel = new JLabel(" ");
        phoneHintLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        phoneHintLabel.setForeground(Color.RED);
        formPanel.add(phoneHintLabel, gbc);

        // Date of Birth
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel dobLabel = new JLabel("Date of Birth:", JLabel.RIGHT);
        dobLabel.setFont(labelFont);
        formPanel.add(dobLabel, gbc);
        gbc.gridx++;
        JTextField dobField = new JTextField(25);
        dobField.setFont(inputFont);
        formPanel.add(dobField, gbc);
        gbc.gridy++;
        gbc.gridx = 1;
        dobHintLabel = new JLabel(" ");
        dobHintLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        dobHintLabel.setForeground(Color.RED);
        formPanel.add(dobHintLabel, gbc);

        // Gender
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel genderLabel = new JLabel("Gender:", JLabel.RIGHT);
        genderLabel.setFont(labelFont);
        formPanel.add(genderLabel, gbc);
        gbc.gridx++;
        String[] genders = {"Select gender", "Male", "Female", "Other"};
        JComboBox<String> genderCombo = new JComboBox<>(genders);
        genderCombo.setFont(inputFont);
        formPanel.add(genderCombo, gbc);
        gbc.gridy++;
        gbc.gridx = 1;
        genderHintLabel = new JLabel(" ");
        genderHintLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        genderHintLabel.setForeground(Color.RED);
        formPanel.add(genderHintLabel, gbc);

        // Nationality
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel nationalityLabel = new JLabel("Nationality:", JLabel.RIGHT);
        nationalityLabel.setFont(labelFont);
        formPanel.add(nationalityLabel, gbc);
        gbc.gridx++;
        JTextField nationalityField = new JTextField(25);
        nationalityField.setFont(inputFont);
        formPanel.add(nationalityField, gbc);
        gbc.gridy++;
        gbc.gridx = 1;
        nationalityHintLabel = new JLabel(" ");
        nationalityHintLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        nationalityHintLabel.setForeground(Color.RED);
        formPanel.add(nationalityHintLabel, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        JButton nextButton = new JButton("Next");
        nextButton.setFocusPainted(false);
        nextButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        nextButton.setBorderPainted(false);
        buttonPanel.add(nextButton);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(formPanel, BorderLayout.CENTER);

        personalPanel.add(contentPanel, BorderLayout.CENTER);
        personalPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Academic Panel
        JPanel academicPanel = new JPanel(new BorderLayout());
        academicPanel.setBackground(Color.WHITE);
        academicPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel academicLabel = new JLabel("Academic Details");
        academicLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 28));

        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        backButton.setBorderPainted(false);

        JButton saveButton = new JButton("Save");
        saveButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        saveButton.setBorderPainted(false);

        JPanel academicButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        academicButtonPanel.setBackground(Color.WHITE);
        academicButtonPanel.add(backButton);
        academicButtonPanel.add(saveButton);

        academicPanel.add(academicLabel, BorderLayout.NORTH);
        academicPanel.add(academicButtonPanel, BorderLayout.SOUTH);

        // Add cards
        mainPanel.add(personalPanel, "personal");
        mainPanel.add(academicPanel, "academic");

        frame.add(mainPanel);
        frame.setVisible(true);

        CardLayout cl = (CardLayout) mainPanel.getLayout();

        nextButton.addActionListener(e -> {
            boolean valid = true;
            if (nameField.getText().trim().isEmpty()) {
                nameHintLabel.setText("Name is required");
                valid = false;
            } else {
                nameHintLabel.setText(" ");
            }

            if (!phoneField.getText().matches("\\d{10}")) {
                phoneHintLabel.setText("Invalid phone number");
                valid = false;
            } else {
                phoneHintLabel.setText(" ");
            }

            if (dobField.getText().trim().isEmpty()) {
                dobHintLabel.setText("Date of birth required");
                valid = false;
            } else {
                dobHintLabel.setText(" ");
            }

            if (genderCombo.getSelectedIndex() == 0) {
                genderHintLabel.setText("Please select gender");
                valid = false;
            } else {
                genderHintLabel.setText(" ");
            }

            if (nationalityField.getText().trim().isEmpty()) {
                nationalityHintLabel.setText("Nationality required");
                valid = false;
            } else {
                nationalityHintLabel.setText(" ");
            }

            if (valid) {
                cl.show(mainPanel, "academic");
            }
        });

        backButton.addActionListener(e -> cl.show(mainPanel, "personal"));

        saveButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "Saved successfully!");
        });
    }
}
