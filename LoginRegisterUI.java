import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDateTime;

// Make sure all these imported classes exist and have the show(JFrame) method
// import NexClubWelcomePanel;
// import AdminDashboard;
// import StudentDashboard;
// import RegisterPageUI;
// import ResetPasswordUI;
// import NexClubSplash;

public class LoginRegisterUI {

    public static void show(JFrame parent) {
        parent.setTitle("Nexclub");
        parent.setIconImage(new ImageIcon("logo/l2.png").getImage());

        if (!parent.isDisplayable()) {
            parent.setUndecorated(false);
        }
        parent.setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);

        JPanel card = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
        };
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(400, 560));
        card.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 2, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        JLabel heading = new JLabel("Welcome to Nexclub !", SwingConstants.LEFT);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 36));
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        card.add(heading, gbc);

        // Role selection radio buttons
        JLabel roleLabel = new JLabel("<html>Are you? <font color='red'>*</font></html>");
        roleLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        card.add(roleLabel, gbc);

        JRadioButton studentBtn = new JRadioButton("Student");
        JRadioButton orgBtn = new JRadioButton("Institution");
        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(studentBtn);
        roleGroup.add(orgBtn);
        studentBtn.setSelected(true);

        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rolePanel.setBackground(Color.WHITE);
        rolePanel.add(studentBtn);
        rolePanel.add(orgBtn);
        gbc.gridy = 2;
        card.add(rolePanel, gbc);

        JLabel userLabel = new JLabel("<html>Nex ID <font color='red'>*</font></html>");
        userLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        userLabel.setForeground(Color.BLACK);
        gbc.gridy = 3;
        card.add(userLabel, gbc);

        // Create prefix panel with perfect vertical alignment
        JPanel prefixPanel = new JPanel(new BorderLayout());
        prefixPanel.setBackground(Color.WHITE);

        // Unified panel for NX + S/I with fixed padding for alignment
        JPanel prefixTextPanel = new JPanel();
        prefixTextPanel.setLayout(new BoxLayout(prefixTextPanel, BoxLayout.X_AXIS));
        prefixTextPanel.setBackground(Color.WHITE);
        prefixTextPanel.setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 8)); // Match padding with text field

        JLabel nxPrefixLabel = new JLabel("NX");
        nxPrefixLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        nxPrefixLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel rolePrefixLabel = new JLabel("S");
        rolePrefixLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        rolePrefixLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        prefixTextPanel.add(nxPrefixLabel);
        prefixTextPanel.add(rolePrefixLabel);

        prefixPanel.add(prefixTextPanel, BorderLayout.WEST);

        JTextField userField = new JTextField();
        userField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        userField.setBorder(BorderFactory.createEmptyBorder(14, 8, 14, 8));
        userField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                userField.setText(userField.getText().toUpperCase());
            }
        });

        // Main panel for ID field with perfect alignment
        JPanel idPanel = new JPanel(new BorderLayout());
        idPanel.setBackground(Color.WHITE);
        idPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        idPanel.add(prefixPanel, BorderLayout.WEST);
        idPanel.add(userField, BorderLayout.CENTER);

        gbc.gridy = 4;
        card.add(idPanel, gbc);

        // Update role prefix when radio buttons are clicked
        studentBtn.addActionListener(e -> {
            rolePrefixLabel.setText("S");
            idPanel.revalidate();
            idPanel.repaint();
        });

        orgBtn.addActionListener(e -> {
            rolePrefixLabel.setText("I");
            idPanel.revalidate();
            idPanel.repaint();
        });

        JLabel userWarning = new JLabel(" ");
        userWarning.setForeground(Color.RED);
        gbc.gridy = 5;
        card.add(userWarning, gbc);

        JLabel passLabel = new JLabel("<html>Password <font color='red'>*</font></html>");
        passLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        gbc.gridy = 6;
        card.add(passLabel, gbc);

        JPasswordField passField = new JPasswordField();
        passField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        passField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JLabel toggleIcon = new JLabel("👁");
        toggleIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleIcon.addMouseListener(new MouseAdapter() {
            private boolean showing = false;

            public void mouseClicked(MouseEvent e) {
                showing = !showing;
                passField.setEchoChar(showing ? (char) 0 : '•');
                toggleIcon.setText(showing ? "🙈" : "👁");
            }
        });

        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.setBackground(Color.WHITE);
        passPanel.add(passField, BorderLayout.CENTER);
        passPanel.add(toggleIcon, BorderLayout.EAST);
        gbc.gridy = 7;
        card.add(passPanel, gbc);

        JLabel passWarning = new JLabel(" ");
        passWarning.setForeground(Color.RED);
        gbc.gridy = 8;
        card.add(passWarning, gbc);

        JLabel loginStatus = new JLabel(" ");
        loginStatus.setForeground(new Color(0, 153, 0));
        gbc.gridy = 9;
        card.add(loginStatus, gbc);

        JLabel loginBtn = new JLabel("Login", SwingConstants.CENTER);
        loginBtn.setFont(new Font("Microsoft JhengHei", Font.BOLD, 15));
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        loginBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                boolean hasError = false;
                String id = "NX" + rolePrefixLabel.getText() + userField.getText().trim();
                String password = new String(passField.getPassword());

                userWarning.setText(" ");
                passWarning.setText(" ");
                loginStatus.setText(" ");
                loginStatus.setForeground(Color.RED); // Default to error color

                if (userField.getText().trim().isEmpty()) {
                    userWarning.setText("Please enter your Nex ID");
                    hasError = true;
                }
                if (password.isEmpty()) {
                    passWarning.setText("Please enter your password");
                    hasError = true;
                }

                if (!hasError) {
                    try {
                        // Assumes NexClubSplash.getConnection() is a valid static method
                        Connection conn = NexClubSplash.getConnection();
                        PreparedStatement stmt = conn.prepareStatement(
                                "SELECT * FROM register WHERE nID = ? AND Password = BINARY ?");
                        stmt.setString(1, id);
                        stmt.setString(2, password);
                        ResultSet rs = stmt.executeQuery();

                        if (rs.next()) {
                            // User is valid. Now check login_count
                            int currentLoginCount = 0; // This is the count *before* we log in
                            
                            PreparedStatement checkLogin = conn.prepareStatement(
                                    "SELECT login_count FROM login WHERE nID = ?");
                            checkLogin.setString(1, id);
                            ResultSet loginRs = checkLogin.executeQuery();

                            if (loginRs.next()) {
                                // User exists in login table
                                currentLoginCount = loginRs.getInt("login_count");
                                int newCount = currentLoginCount + 1;
                                PreparedStatement updateLogin = conn.prepareStatement(
                                        "UPDATE login SET login_time = ?, login_count = ?, Password = ? WHERE nID = ?");
                                updateLogin.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                                updateLogin.setInt(2, newCount);
                                updateLogin.setString(3, password);
                                updateLogin.setString(4, id);
                                updateLogin.executeUpdate();
                                updateLogin.close();
                            } else {
                                // User's first login, insert into login table
                                // currentLoginCount remains 0
                                PreparedStatement insertLogin = conn.prepareStatement(
                                        "INSERT INTO login (nID, Password, login_time, login_count) VALUES (?, ?, ?, ?)");
                                insertLogin.setString(1, id);
                                insertLogin.setString(2, password);
                                insertLogin.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                                insertLogin.setInt(4, 1); // Set login_count to 1
                                insertLogin.executeUpdate();
                                insertLogin.close();
                            }
                            loginRs.close();
                            checkLogin.close();

                            // --- START: Redirect Logic (This logic is correct) ---
                            // We check the count *before* the login (currentLoginCount)
                            if (currentLoginCount < 1) {
                                // First login (count was 0), go to Welcome Panel
                                // Added SwingUtilities.invokeLater for safety
                                SwingUtilities.invokeLater(() -> NexClubWelcomePanel.show(parent));
                            } else {
                                // Returning user (count was 1 or more), check role
                                char roleChar = id.charAt(2); // Get the 'S' or 'I'
                                if (roleChar == 'I') {
                                    // 🚨🚨 THE BUG IS LIKELY IN THIS FILE 🚨🚨
                                    // 🚨 Go to AdminDashboard.java and check its 'show' method
                                    SwingUtilities.invokeLater(() -> AdminDashboard.show(parent,id));
                                } else if (roleChar == 'S') {
                                    // 🚨🚨 THE BUG IS LIKELY IN THIS FILE 🚨🚨
                                    // 🚨 Go to StudentDashboard.java and check its 'show' method
                                    SwingUtilities.invokeLater(() -> StudentDashboard.show(parent));
                                } else {
                                    // Fallback in case of bad ID data
                                    loginStatus.setText("Login successful, but role is invalid.");
                                    SwingUtilities.invokeLater(() -> NexClubWelcomePanel.show(parent)); // Fallback
                                }
                            }
                            // --- END: Redirect Logic ---

                        } else {
                            // Invalid ID or Password
                            userWarning.setText("Invalid Nex ID or Password");
                        }
                        rs.close();
                        stmt.close();
                    } catch (SQLException ex) {
                        loginStatus.setText("Login failed: " + ex.getMessage());
                    }
                }
            }
        });

        gbc.gridy = 10;
        card.add(loginBtn, gbc);

        JLabel registerLabel = new JLabel("Don't have an account?");
        JLabel registerLink = new JLabel("Register");
        registerLink.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        registerLink.setForeground(Color.BLACK);
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // This call is fine, assuming RegisterPageUI.java exists
                RegisterPageUI.show(parent);
            }
        });

        JPanel regPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        regPanel.setBackground(Color.WHITE);
        regPanel.add(registerLabel);
        regPanel.add(registerLink);

        gbc.gridy = 11;
        card.add(regPanel, gbc);

        JLabel forgotPassword = new JLabel("Forgot Password?");
        forgotPassword.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
        forgotPassword.setForeground(new Color(153, 153, 153));
        forgotPassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotPassword.setHorizontalAlignment(SwingConstants.CENTER);
        forgotPassword.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // This call is fine, assuming ResetPasswordUI.java exists
                ResetPasswordUI.show(parent);
            }
        });

        gbc.gridy = 12;
        card.add(forgotPassword, gbc);

        centerPanel.add(card);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        parent.getContentPane().removeAll();
        parent.getContentPane().add(mainPanel);
        parent.revalidate();
        parent.repaint();
    }
}