import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CopyOfWelcomeUI {
    private JFrame frame;
    private JLabel welcomeLabel;
    private JLabel profileLabel;
    private Timer timer;
    private Timer fadeTimer;
    private Timer fadeInTimer;
    private JButton continueButton;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    private String fullWelcomeText = "";
    private String fullProfileText = "";
    private int welcomeIndex = 0;
    private int profileIndex = 0;
    private boolean welcomeComplete = false;

    private String role = "S"; // Default to Student

    public static void show(JFrame parentFrame) {
        SwingUtilities.invokeLater(() -> {
            JFrame newFrame = parentFrame != null ? parentFrame : new JFrame("Student Portal");

            CopyOfWelcomeUI welcomeUI = new CopyOfWelcomeUI(newFrame);
            newFrame.setContentPane(welcomeUI.getContentPane());
            newFrame.revalidate();
            newFrame.repaint();

            if (parentFrame == null) {
                newFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                newFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                newFrame.setVisible(true);
            }
        });
    }

    public CopyOfWelcomeUI(JFrame frame) {
        this.frame = frame;
        fetchUserRoleFromDatabase();
        setupUI();
    }

    private void fetchUserRoleFromDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/nexclub", "root", "admin");

            String sql = "SELECT nID FROM login ORDER BY login_time DESC LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String nID = rs.getString("nID");
                if (nID != null && nID.length() >= 3) {
                    char type = nID.charAt(2);
                    if (type == 'I') role = "I";
                    else if (type == 'S') role = "S";
                }
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.err.println("DB Error: " + e.getMessage());
        }

        if (role.equals("I")) {
            fullWelcomeText = "All Set, Your Club Hub is Ready.";
            fullProfileText = "Start managing members, events, and approvals in one place.";
        } else {
            fullWelcomeText = "Welcome, Your Campus Journey Begins.";
            fullProfileText = "Your profile is all set—find clubs, events, and your people!";
        }
    }

    private void setupUI() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        frame.setBackground(Color.WHITE);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(Color.WHITE);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        welcomeLabel = new JLabel();
        welcomeLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 40));
        welcomeLabel.setForeground(new Color(30, 30, 30));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        profileLabel = new JLabel();
        profileLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 20));
        profileLabel.setForeground(new Color(80, 80, 80));
        profileLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        continueButton = new JButton("Continue");
        continueButton.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        continueButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        continueButton.setVisible(false);
        continueButton.setFocusPainted(false);
        continueButton.setBorderPainted(false);
        continueButton.setContentAreaFilled(false);
        continueButton.addActionListener(e -> fadeToDashboard());

        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(welcomeLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(profileLabel);
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(continueButton);

        JPanel dashboardPanel = createDashboardPanel();

        cardPanel.add(mainPanel, "welcome");
        cardPanel.add(dashboardPanel, "dashboard");

        frame.add(cardPanel);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        startTypingAnimation();
    }

    public Container getContentPane() {
        return this.cardPanel;
    }

    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel();
        dashboardPanel.setLayout(new BoxLayout(dashboardPanel, BoxLayout.Y_AXIS));
        dashboardPanel.setBackground(Color.WHITE);
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(100, 50, 100, 50));

        JLabel title = new JLabel(role.equals("I") ? "Institution Dashboard" : "Student Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Microsoft JhengHei", Font.BOLD, 36));
        title.setForeground(new Color(30, 30, 30));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel infoText = new JLabel();
        infoText.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 25));
        infoText.setForeground(new Color(80, 80, 80, 0)); // start invisible
        infoText.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoText.setText(role.equals("I")
            ? "Manage members, host events, and oversee club operations."
            : "Explore your courses, join clubs, attend events, and manage your profile.");

        dashboardPanel.add(title);
        dashboardPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        dashboardPanel.add(infoText);

        // Fade-in animation for dashboard text
        fadeInTimer = new Timer(50, new ActionListener() {
            float opacity = 0.0f;

            @Override
            public void actionPerformed(ActionEvent e) {
                opacity += 0.05f;
                if (opacity >= 1.0f) {
                    opacity = 1.0f;
                    fadeInTimer.stop();
                }
                infoText.setForeground(new Color(80, 80, 80, (int) (opacity * 255)));
                infoText.repaint();
            }
        });

        return dashboardPanel;
    }

    private void startTypingAnimation() {
        timer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!welcomeComplete) {
                    if (welcomeIndex <= fullWelcomeText.length()) {
                        welcomeLabel.setText(fullWelcomeText.substring(0, welcomeIndex));
                        welcomeIndex++;
                    } else {
                        welcomeComplete = true;
                    }
                } else {
                    if (profileIndex <= fullProfileText.length()) {
                        profileLabel.setText(fullProfileText.substring(0, profileIndex));
                        profileIndex++;
                    } else {
                        timer.stop();
                        continueButton.setVisible(true);
                    }
                }
            }
        });
        timer.start();
    }

    private void fadeToDashboard() {
        continueButton.setVisible(false);
        fadeTimer = new Timer(50, new ActionListener() {
            float opacity = 1.0f;

            @Override
            public void actionPerformed(ActionEvent e) {
                opacity -= 0.05f;
                if (opacity <= 0) {
                    fadeTimer.stop();
                    cardLayout.show(cardPanel, "dashboard");
                    fadeInTimer.start(); // start fade-in for institution text
                } else {
                    setPanelOpacity(opacity);
                }
            }
        });
        fadeTimer.start();
    }

    private void setPanelOpacity(float opacity) {
        Color bgColor = new Color(1f, 1f, 1f, opacity);
        mainPanel.setBackground(bgColor);
        welcomeLabel.setForeground(new Color(30, 30, 30, (int) (opacity * 255)));
        profileLabel.setForeground(new Color(80, 80, 80, (int) (opacity * 255)));
        mainPanel.repaint();
    }

    public static void main(String[] args) {
        show(null);
    }
}
