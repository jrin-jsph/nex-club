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
            parent.setContentPane(mainFrame.getContentPane());
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

    protected boolean isAlphabetic(String input) {
        return input.matches("[A-Za-z ]+");
    }

    protected boolean isValidPhone(String input) {
        return input.matches("\\d{10}");
    }

    protected boolean isValidDate(String input) {
        try {
            new SimpleDateFormat("yyyy-MM-dd").parse(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

class BasicDetailsPanel extends AbstractFormPanel {
    private Font greetingFont = new Font("Microsoft JhengHei", Font.BOLD, 35);

    public BasicDetailsPanel(MainFrame frame) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel greetingPanel = new JPanel();
        greetingPanel.setLayout(new BoxLayout(greetingPanel, BoxLayout.Y_AXIS));
        greetingPanel.setBackground(Color.WHITE);
        greetingPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 0));

        JLabel greeting = new JLabel("Hello, You!");
        greeting.setFont(greetingFont);
        JLabel instruction = new JLabel("Let's update the profile");
        instruction.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));

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
            new String[] {"-- Select Gender --", "Male", "Female", "Other"}, 300, 45);

        JLabel validationLabel = createValidationLabel();
        validationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        validationLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton nextButton = createFlatButton("Next");
        nextButton.addActionListener(e -> {
            String fullName = fullNameField.getText().trim();
            String phone = phoneField.getText().trim();
            String dob = dobField.getText().trim();
            String gender = (String) genderComboBox.getSelectedItem();

            if (fullName.isEmpty() || phone.isEmpty() || dob.isEmpty() || gender.equals("-- Select Gender --")) {
                validationLabel.setText("Please complete all fields.");
                validationLabel.setForeground(Color.RED);
                return;
            }

            if (!isAlphabetic(fullName)) {
                validationLabel.setText("Full name must contain only letters and spaces.");
                validationLabel.setForeground(Color.RED);
                return;
            }

            if (!isValidPhone(phone)) {
                validationLabel.setText("Phone number must be exactly 10 digits.");
                validationLabel.setForeground(Color.RED);
                return;
            }

            if (!isValidDate(dob)) {
                validationLabel.setText("Date of birth must be in YYYY-MM-DD format.");
                validationLabel.setForeground(Color.RED);
                return;
            }

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

        add(greetingPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
    