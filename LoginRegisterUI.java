import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDateTime;

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

        JLabel userLabel = new JLabel("<html>Nex ID <font color='red'>*</font></html>");
        userLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        userLabel.setForeground(Color.BLACK);
        gbc.gridy = 1;
        card.add(userLabel, gbc);

        JTextField userField = new JTextField();
        userField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        userField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(14, 8, 14, 8)));
        userField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                userField.setText(userField.getText().toUpperCase());
            }
        });

        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(Color.WHITE);
        userPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        userPanel.add(new JLabel("NX "), BorderLayout.WEST);
        userPanel.add(userField, BorderLayout.CENTER);
        gbc.gridy = 2;
        card.add(userPanel, gbc);

        JLabel userWarning = new JLabel(" ");
        userWarning.setForeground(Color.RED);
        gbc.gridy = 3;
        card.add(userWarning, gbc);

        JLabel passLabel = new JLabel("<html>Password <font color='red'>*</font></html>");
        passLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        gbc.gridy = 4;
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
        gbc.gridy = 5;
        card.add(passPanel, gbc);

        JLabel passWarning = new JLabel(" ");
        passWarning.setForeground(Color.RED);
        gbc.gridy = 6;
        card.add(passWarning, gbc);

        JLabel loginStatus = new JLabel(" ");
        loginStatus.setForeground(new Color(0, 153, 0));
        gbc.gridy = 7;
        card.add(loginStatus, gbc);

        JLabel loginBtn = new JLabel("Login", SwingConstants.CENTER);
        loginBtn.setFont(new Font("Microsoft JhengHei", Font.BOLD, 15));
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                boolean hasError = false;
                String id = "NX" + userField.getText().trim();
                String password = new String(passField.getPassword());

                userWarning.setText(" ");
                passWarning.setText(" ");
                loginStatus.setText(" ");

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
                        Connection conn = NexClubSplash.getConnection();
                        PreparedStatement stmt = conn.prepareStatement(
                            "SELECT * FROM register WHERE nID = ? AND Password = BINARY ?");
                        stmt.setString(1, id);
                        stmt.setString(2, password);
                        ResultSet rs = stmt.executeQuery();

                        if (rs.next()) {
                            PreparedStatement checkLogin = conn.prepareStatement(
                                "SELECT login_count FROM login WHERE nID = ?");
                            checkLogin.setString(1, id);
                            ResultSet loginRs = checkLogin.executeQuery();

                            if (loginRs.next()) {
                                int count = loginRs.getInt("login_count") + 1;
                                PreparedStatement updateLogin = conn.prepareStatement(
                                    "UPDATE login SET login_time = ?, login_count = ?, Password = ? WHERE nID = ?");
                                updateLogin.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                                updateLogin.setInt(2, count);
                                updateLogin.setString(3, password);
                                updateLogin.setString(4, id);
                                updateLogin.executeUpdate();
                                updateLogin.close();
                            } else {
                                PreparedStatement insertLogin = conn.prepareStatement(
                                    "INSERT INTO login (nID, Password, login_time, login_count) VALUES (?, ?, ?, ?)");
                                insertLogin.setString(1, id);
                                insertLogin.setString(2, password);
                                insertLogin.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                                insertLogin.setInt(4, 1);
                                insertLogin.executeUpdate();
                                insertLogin.close();
                            }
                            loginRs.close();
                            NexClubWelcomePanel.show(parent);
                        } else {
                            userWarning.setText("Invalid Nex ID or Password");
                        }
                        stmt.close();
                    } catch (SQLException ex) {
                        loginStatus.setText("Login failed: " + ex.getMessage());
                    }
                }
            }
        });

        gbc.gridy = 8;
        card.add(loginBtn, gbc);

        JLabel registerLabel = new JLabel("Don't have an account?");
        JLabel registerLink = new JLabel("Register");
        registerLink.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        registerLink.setForeground(Color.BLACK);
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                RegisterPageUI.show(parent);
            }
        });

        JPanel regPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        regPanel.setBackground(Color.WHITE);
        regPanel.add(registerLabel);
        regPanel.add(registerLink);

        gbc.gridy = 9;
        card.add(regPanel, gbc);

        // 👉 Added Forgot Password label
        JLabel forgotPassword = new JLabel("Forgot Password?");
        forgotPassword.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
        forgotPassword.setForeground(new Color(153, 153, 153)); // light grey
        forgotPassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotPassword.setHorizontalAlignment(SwingConstants.CENTER);
        forgotPassword.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                ResetPasswordUI.show(parent);
            }
        });

        gbc.gridy = 10;
        card.add(forgotPassword, gbc);

        centerPanel.add(card);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        parent.getContentPane().removeAll();
        parent.getContentPane().add(mainPanel);
        parent.revalidate();
        parent.repaint();
    }
}
