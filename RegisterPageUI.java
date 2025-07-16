import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.Timer;
import java.sql.*;

public class RegisterPageUI {
    public static void show(JFrame parent) {
        parent.setTitle("Nexclub");
        //parent.setIconImage(new ImageIcon("icon.png").getImage());


        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 40, 20));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(24);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(420, 740));
        card.setBorder(null);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        JLabel title = new JLabel("Create your account", SwingConstants.CENTER);
        title.setFont(new Font("Microsoft JhengHei", Font.BOLD, 36));
        title.setPreferredSize(new Dimension(400, 50));
        gbc.gridy = 0;
        card.add(title, gbc);

        JLabel subtitle = new JLabel("Join NexClub to connect with your academic community", SwingConstants.CENTER);
        subtitle.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
        subtitle.setForeground(Color.GRAY);
        gbc.gridy = 1;
        card.add(subtitle, gbc);

        JLabel roleLabel = new JLabel("<html>Are you? <font color='red'>*</font></html>");
        roleLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        card.add(roleLabel, gbc);

        JRadioButton studentBtn = new JRadioButton("Student");
        JRadioButton orgBtn = new JRadioButton("Institution");
        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(studentBtn);
        roleGroup.add(orgBtn);
        studentBtn.setSelected(true);

        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rolePanel.setBackground(Color.WHITE);
        rolePanel.add(studentBtn);
        rolePanel.add(orgBtn);
        gbc.gridy = 3;
        card.add(rolePanel, gbc);

        JLabel idLabel = new JLabel("<html>Student / Institution ID <font color='red'>*</font></html>");
        idLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        card.add(idLabel, gbc);

        JTextField idField = new JTextField();
        idField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        idField.setPreferredSize(new Dimension(320, 36));
        idField.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        idField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                idField.setText(idField.getText().toUpperCase());
            }
        });

        JLabel rolePrefix = new JLabel("S");
        rolePrefix.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        rolePrefix.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        JPanel idPanel = new JPanel(new BorderLayout());
        idPanel.setBackground(Color.WHITE);
        idPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        JLabel prefixLabel = new JLabel("NX");
        prefixLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        prefixLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        idPanel.add(prefixLabel, BorderLayout.WEST);

        JPanel combinedPanel = new JPanel(new BorderLayout());
        combinedPanel.setBackground(Color.WHITE);
        combinedPanel.add(rolePrefix, BorderLayout.WEST);
        combinedPanel.add(idField, BorderLayout.CENTER);
        idPanel.add(combinedPanel, BorderLayout.CENTER);

        gbc.gridy = 5;
        card.add(idPanel, gbc);

        JLabel idNote = new JLabel("NOTE : This will be your Nex ID");
        idNote.setFont(new Font("SansSerif", Font.BOLD, 12));
        idNote.setForeground(Color.RED);
        gbc.gridy = 6;
        card.add(idNote, gbc);

        JLabel idWarning = new JLabel(" ");
        idWarning.setFont(new Font("SansSerif", Font.BOLD, 12));
        idWarning.setForeground(Color.RED);
        gbc.gridy = 7;
        card.add(idWarning, gbc);

        studentBtn.addActionListener(e -> rolePrefix.setText("S"));
        orgBtn.addActionListener(e -> rolePrefix.setText("I"));

        JLabel emailLabel = new JLabel("<html>Student / Institution Email <font color='red'>*</font></html>");
        emailLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        gbc.gridy = 8;
        card.add(emailLabel, gbc);

        JTextField emailField = new JTextField();
        emailField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        emailField.setPreferredSize(new Dimension(320, 36));
        emailField.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JPanel emailPanel = new JPanel(new BorderLayout());
        emailPanel.setBackground(Color.WHITE);
        emailPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        emailPanel.add(emailField, BorderLayout.CENTER);
        gbc.gridy = 9;
        card.add(emailPanel, gbc);

        JLabel emailWarning = new JLabel(" ");
        emailWarning.setFont(new Font("SansSerif", Font.BOLD, 12));
        emailWarning.setForeground(Color.RED);
        gbc.gridy = 10;
        card.add(emailWarning, gbc);

        JLabel passLabel = new JLabel("<html>Create Password <font color='red'>*</font></html>");
        passLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        gbc.gridy = 11;
        card.add(passLabel, gbc);

        JPasswordField passField = new JPasswordField();
        passField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        passField.setPreferredSize(new Dimension(320, 36));
        passField.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.setBackground(Color.WHITE);
        passPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        passPanel.add(passField, BorderLayout.CENTER);

        JLabel toggleIcon = new JLabel("👁");
        toggleIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleIcon.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        toggleIcon.addMouseListener(new MouseAdapter() {
            private boolean showing = false;
            public void mouseClicked(MouseEvent e) {
                showing = !showing;
                passField.setEchoChar(showing ? (char) 0 : '•');
                toggleIcon.setText(showing ? "🙈" : "👁");
            }
        });
        passPanel.add(toggleIcon, BorderLayout.EAST);
        gbc.gridy = 12;
        card.add(passPanel, gbc);

        JLabel passWarning = new JLabel(" ");
        passWarning.setFont(new Font("SansSerif", Font.BOLD, 12));
        passWarning.setForeground(Color.RED);
        gbc.gridy = 13;
        card.add(passWarning, gbc);

        JLabel successLabel = new JLabel(" ");
        successLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        successLabel.setForeground(new Color(0, 150, 0));
        successLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton registerBtn = new JButton("Register");
        registerBtn.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        registerBtn.setContentAreaFilled(false);
        registerBtn.setBorder(null);
        registerBtn.setForeground(Color.BLACK);

        // ✅ Register logic to MySQL register table
        registerBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            String id = idField.getText().trim();
            String role = studentBtn.isSelected() ? "S" : "I";
            String fullID = "NX" + role + id;

            boolean valid = true;
            idWarning.setText(" ");
            emailWarning.setText(" ");
            passWarning.setText(" ");
            successLabel.setText(" ");

            if (id.isEmpty()) {
                idWarning.setText("Please enter your ID.");
                valid = false;
            }
            if (!email.matches(".*(@gmail\\.com|@.*\\.ac\\.in|@.*\\.edu)$")) {
                emailWarning.setText("Enter a valid academic/Gmail email.");
                valid = false;
            }
            if (password.length() < 6) {
                passWarning.setText("Password must be at least 6 characters.");
                valid = false;
            }

            if (!valid) return;

            try {
                Connection conn = NexClubSplash.getConnection();

                PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT * FROM register WHERE nID = ? OR email = ?");
                checkStmt.setString(1, fullID);
                checkStmt.setString(2, email);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    if (fullID.equals(rs.getString("nID"))) {
                        idWarning.setText("This ID is already registered.");
                    } else {
                        emailWarning.setText("This email is already registered.");
                    }
                    rs.close();
                    checkStmt.close();
                    return;
                }
                rs.close();
                checkStmt.close();

                PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO register (nID, email, Password) VALUES (?, ?, ?)");
                insertStmt.setString(1, fullID);
                insertStmt.setString(2, email);
                insertStmt.setString(3, password);
                insertStmt.executeUpdate();
                insertStmt.close();

                successLabel.setText("Registered Successfully");

                Timer timer = new Timer(2000, evt -> {
                    ((Timer) evt.getSource()).stop();
                    SwingUtilities.invokeLater(() -> LoginRegisterUI.show(parent));
                });
                timer.setRepeats(false);
                timer.start();

            } catch (SQLException ex) {
                successLabel.setText("Registration failed: " + ex.getMessage());
            }
        });

        gbc.gridy = 14;
        card.add(registerBtn, gbc);

        gbc.gridy = 15;
        card.add(successLabel, gbc);

        JLabel loginInfo = new JLabel("Already have an account? ");
        JLabel loginLink = new JLabel("Log In");
        loginLink.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        loginLink.setForeground(Color.BLACK);
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                LoginRegisterUI.show(parent);
            }
        });

        JPanel loginPanel = new JPanel();
        loginPanel.setBackground(Color.WHITE);
        loginPanel.add(loginInfo);
        loginPanel.add(loginLink);

        gbc.gridy = 16;
        card.add(loginPanel, gbc);

        contentPanel.add(card);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        parent.getContentPane().removeAll();
        parent.getContentPane().add(mainPanel);
        parent.revalidate();
        parent.repaint();
    }
}
