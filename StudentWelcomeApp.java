import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StudentWelcomeApp {
    private JFrame frame;
    private JLabel welcomeLabel;
    private JLabel profileLabel;
    private Timer timer;
    private Timer fadeTimer;
    private String fullWelcomeText = "Welcome, Your Campus Journey Begins.";
    private String fullProfileText = "Your profile is all set—find clubs, events, and your people!";
    private int welcomeIndex = 0;
    private int profileIndex = 0;
    private boolean welcomeComplete = false;
    private JButton continueButton;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    public StudentWelcomeApp() {
        setupUI();
    }

    private void setupUI() {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        frame = new JFrame("Student Portal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBackground(Color.WHITE);

        // Create card layout panel
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(Color.WHITE);

        // Create welcome panel
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Welcome label
        welcomeLabel = new JLabel();
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        welcomeLabel.setForeground(new Color(30, 30, 30));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Profile label
        profileLabel = new JLabel();
        profileLabel.setFont(new Font("Segoe UI", Font.PLAIN, 25));
        profileLabel.setForeground(new Color(80, 80, 80));
        profileLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Continue button (initially invisible)
        continueButton = new JButton("Continue");
        continueButton.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        continueButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        continueButton.setVisible(false);
        continueButton.addActionListener(e -> fadeToDashboard());

        // Add components to panel
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(welcomeLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(profileLabel);
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(continueButton);

        // Create dashboard panel
        JPanel dashboardPanel = createDashboardPanel();

        // Add both panels to card layout
        cardPanel.add(mainPanel, "welcome");
        cardPanel.add(dashboardPanel, "dashboard");

        frame.add(cardPanel);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        startTypingAnimation();
    }

    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(Color.WHITE);
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Student Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(new Color(30, 30, 30));

        // Create navigation buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(40, 100, 40, 100));
        buttonPanel.setBackground(Color.WHITE);

        String[] buttonLabels = {"Courses", "Clubs", "Events", "Profile"};
        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            button.setFocusPainted(false);
            button.setPreferredSize(new Dimension(150, 60));
            button.addActionListener(e -> {
                JOptionPane.showMessageDialog(frame, 
                    "You clicked: " + button.getText(), 
                    "Navigation", 
                    JOptionPane.INFORMATION_MESSAGE);
            });
            buttonPanel.add(button);
        }

        dashboardPanel.add(title, BorderLayout.NORTH);
        dashboardPanel.add(buttonPanel, BorderLayout.CENTER);

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
                    cardLayout.show(cardPanel, "dashboard"); // Switch to dashboard
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
        welcomeLabel.setForeground(new Color(30, 30, 30, (int)(opacity * 255)));
        profileLabel.setForeground(new Color(80, 80, 80, (int)(opacity * 255)));
        mainPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StudentWelcomeApp();
        });
    }
}