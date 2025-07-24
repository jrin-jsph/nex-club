import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;

public class ProfileUpdaterApp {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}

class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;

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

class BasicDetailsPanel extends JPanel {
    public BasicDetailsPanel(MainFrame frame) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        setBackground(Color.WHITE);

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 16);
        Font headerFont = new Font("Segoe UI", Font.BOLD, 26);
        Font sectionFont = new Font("Segoe UI", Font.BOLD, 20);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 15);

        JLabel helloLabel = new JLabel("Hello, You!");
        helloLabel.setFont(headerFont);
        helloLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        helloLabel.setForeground(Color.BLACK);

        JLabel subheaderLabel = new JLabel("Let's update the profile");
        subheaderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subheaderLabel.setForeground(new Color(100, 100, 100));
        subheaderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sectionTitle = new JLabel("Basic Details");
        sectionTitle.setFont(sectionFont);
        sectionTitle.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField fullNameField = createTextField("Enter your full name", fieldFont);
        JTextField phoneField = createTextField("Enter your phone number", fieldFont);
        JFormattedTextField dobField = new JFormattedTextField(new SimpleDateFormat("yyyy / MM / dd"));
        dobField.putClientProperty("JTextField.placeholderText", "yyyy / mm / dd");
        styleField(dobField, fieldFont);

        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"Select gender", "Male", "Female", "Other"});
        genderCombo.setFont(fieldFont);
        genderCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        genderCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton nextButton = createFlatButton("Next →");
        nextButton.addActionListener(e -> frame.showCard("AcademicInfo"));

        Dimension spacing = new Dimension(0, 18);

        add(helloLabel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(subheaderLabel);
        add(sectionTitle);

        add(createLabeledField("Full Name", fullNameField, labelFont));
        add(Box.createRigidArea(spacing));
        add(createLabeledField("Phone Number", phoneField, labelFont));
        add(Box.createRigidArea(spacing));
        add(createLabeledField("Date of Birth", dobField, labelFont));
        add(Box.createRigidArea(spacing));
        add(createLabeledField("Gender", genderCombo, labelFont));
        add(Box.createRigidArea(new Dimension(0, 30)));
        add(nextButton);
    }

    private JPanel createLabeledField(String labelText, JComponent field, Font labelFont) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel(labelText);
        label.setFont(labelFont);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(field);
        return panel;
    }

    private JTextField createTextField(String placeholder, Font font) {
        JTextField field = new JTextField();
        field.putClientProperty("JTextField.placeholderText", placeholder);
        styleField(field, font);
        return field;
    }

    private void styleField(JTextField field, Font font) {
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setFont(font);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JButton createFlatButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setForeground(new Color(0, 102, 204));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        return button;
    }
}

class AcademicInfoPanel extends JPanel {
    public AcademicInfoPanel(MainFrame frame) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        setBackground(Color.WHITE);

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 16);
        Font headerFont = new Font("Segoe UI", Font.BOLD, 26);
        Font sectionFont = new Font("Segoe UI", Font.BOLD, 20);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 15);

        JLabel helloLabel = new JLabel("Hello, You!");
        helloLabel.setFont(headerFont);
        helloLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subheaderLabel = new JLabel("Let's update the profile");
        subheaderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subheaderLabel.setForeground(new Color(100, 100, 100));
        subheaderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sectionTitle = new JLabel("Academic Info");
        sectionTitle.setFont(sectionFont);
        sectionTitle.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField collegeField = createTextField("Enter your college name", fieldFont);
        JTextField universityField = createTextField("Enter your university name", fieldFont);
        JTextField degreeField = createTextField("Enter your degree program", fieldFont);
        JTextField branchField = createTextField("Enter your branch/department", fieldFont);

        JButton saveButton = createFlatButton("Save");
        JButton backButton = createFlatButton("← Back");

        backButton.addActionListener(e -> frame.showCard("BasicDetails"));

        Dimension spacing = new Dimension(0, 18);

        add(helloLabel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(subheaderLabel);
        add(sectionTitle);

        add(createLabeledField("College Name", collegeField, labelFont));
        add(Box.createRigidArea(spacing));
        add(createLabeledField("University Name", universityField, labelFont));
        add(Box.createRigidArea(spacing));
        add(createLabeledField("Degree Program", degreeField, labelFont));
        add(Box.createRigidArea(spacing));
        add(createLabeledField("Branch/Department", branchField, labelFont));
        add(Box.createRigidArea(new Dimension(0, 30)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(backButton);
        buttonPanel.add(saveButton);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(buttonPanel);
    }

    private JPanel createLabeledField(String labelText, JComponent field, Font labelFont) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel(labelText);
        label.setFont(labelFont);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(field);
        return panel;
    }

    private JTextField createTextField(String placeholder, Font font) {
        JTextField field = new JTextField();
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setFont(font);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private JButton createFlatButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setForeground(new Color(0, 102, 204));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        return button;
    }
}
