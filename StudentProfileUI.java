import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.text.*;
import java.util.regex.*;
import java.text.ParseException;

public class StudentProfileUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;

    private JTextField nameField, emailField, phoneField, nationalityField;
    private JFormattedTextField dobField;
    private JComboBox<String> genderCombo;
    private JTextArea addressArea;

    public StudentProfileUI() {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception ignored) {}

        setTitle("Student Profile");
        setSize(1440, 1024);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(createBasicDetailsPanel(), "basic");
        cardPanel.add(createAcademicInfoPanel(), "academic");

        add(cardPanel);
        cardLayout.show(cardPanel, "basic");
    }

    private JPanel createBasicDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(40, 80, 40, 80));

        JPanel header = createHeader("🧑‍💼 Basic Details");
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        nameField = createStandardField();
        addLabelAndField(form, gbc, y++, "Full Name:", nameField);

        emailField = createStandardField();
        addLabelAndField(form, gbc, y++, "Email ID:", emailField);

        phoneField = createStandardField();
        addLabelAndField(form, gbc, y++, "Phone Number:", phoneField);

        dobField = new JFormattedTextField();
        dobField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        dobField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 224, 224)),
                new EmptyBorder(10, 12, 10, 12)));
        dobField.setColumns(20);
        addLabelAndField(form, gbc, y++, "Date of Birth:", dobField);

        genderCombo = createComboBox(new String[]{"Select gender", "Male", "Female", "Other"});
        addLabelAndField(form, gbc, y++, "Gender:", genderCombo);

        nationalityField = createStandardField();
        addLabelAndField(form, gbc, y++, "Nationality:", nationalityField);

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        form.add(createBoldLabel("Address:"), gbc);
        gbc.gridx = 1;
        addressArea = new JTextArea(4, 20);
        addressArea.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        addressArea.setWrapStyleWord(true);
        addressArea.setLineWrap(true);
        addressArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 224, 224)),
                new EmptyBorder(10, 12, 10, 12)));
        form.add(new JScrollPane(addressArea), gbc);

        JButton nextButton = createButton("Next →");
        nextButton.addActionListener(e -> {
            if (validateBasicForm()) {
                cardLayout.show(cardPanel, "academic");
            }
        });

        panel.add(header, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(wrapButtonPanel(nextButton), BorderLayout.SOUTH);
        return panel;
    }

    private boolean validateBasicForm() {
        if (nameField.getText().trim().isEmpty()) {
            showError("Full Name is required"); return false;
        }
        if (!emailField.getText().matches("^.+@.+\\..+$")) {
            showError("Invalid Email ID"); return false;
        }
        if (!phoneField.getText().matches("\\d{10}")) {
            showError("Phone Number must be 10 digits"); return false;
        }
        String dobText = dobField.getText().trim();
        if (dobText.isEmpty()) {
            showError("Date of Birth is required"); return false;
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy / MM / dd");
                sdf.setLenient(false);
                sdf.parse(dobText);
            } catch (ParseException e) {
                showError("Enter a valid Date of Birth in yyyy / MM / dd format");
                return false;
            }
        }
        if (genderCombo.getSelectedIndex() == 0) {
            showError("Please select a Gender"); return false;
        }
        if (nationalityField.getText().trim().isEmpty()) {
            showError("Nationality is required"); return false;
        }
        if (addressArea.getText().trim().isEmpty()) {
            showError("Address is required"); return false;
        }
        return true;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    private JPanel createAcademicInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(40, 80, 40, 80));

        JPanel header = createHeader("🎓 Academic Info");
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        addLabelAndField(form, gbc, y++, "College Name:", createStandardField());
        addLabelAndField(form, gbc, y++, "University:", createStandardField());
        addLabelAndField(form, gbc, y++, "Degree Program:", createStandardField());
        addLabelAndField(form, gbc, y++, "Branch / Department:", createStandardField());

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        form.add(createBoldLabel("Academic Achievements:"), gbc);
        gbc.gridx = 1;
        form.add(createTextArea(), gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton backButton = createButton("← Back");
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "basic"));
        buttonPanel.add(backButton);

        JButton saveButton = createButton("Save");
        buttonPanel.add(saveButton);

        panel.add(header, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void addLabelAndField(JPanel panel, GridBagConstraints gbc, int y, String label, Component field) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        panel.add(createBoldLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private JLabel createBoldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
        return label;
    }

    private JPanel createHeader(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Hello, You!");
        heading.setFont(new Font("Microsoft JhengHei", Font.BOLD, 36));
        heading.setForeground(new Color(45, 55, 72));

        JLabel subHeading = new JLabel("Let's update the profile");
        subHeading.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));
        subHeading.setForeground(new Color(160, 174, 192));

        JLabel section = new JLabel(title);
        section.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20));
        section.setForeground(new Color(45, 55, 72));
        section.setBorder(new EmptyBorder(24, 0, 24, 0));

        panel.add(heading);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(subHeading);
        panel.add(section);
        return panel;
    }

    private JTextField createStandardField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 224, 224)),
                new EmptyBorder(10, 12, 10, 12)));
        field.setColumns(20);
        return field;
    }

    private JComboBox<String> createComboBox(String[] options) {
        JComboBox<String> combo = new JComboBox<>(options);
        combo.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 224, 224)),
                new EmptyBorder(8, 12, 8, 12)));
        return combo;
    }

    private JScrollPane createTextArea() {
        JTextArea area = new JTextArea(4, 20);
        area.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 224, 224)),
                new EmptyBorder(10, 12, 10, 12)));
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(null);
        return scrollPane;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        button.setForeground(Color.BLACK);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 24, 12, 24));
        return button;
    }

    private JPanel wrapButtonPanel(JButton button) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(30, 0, 0, 0));
        panel.add(button);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StudentProfileUI ui = new StudentProfileUI();
            ui.setVisible(true);
        });
    }
}