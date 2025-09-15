import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class NexclubDashboardUI extends JFrame {

    public NexclubDashboardUI() {
        setTitle("Nexclub Dashboard");
        setSize(1443, 1051);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(Color.decode("#F8F9FB"));

        // Logo and user icon
        JLabel logo = new JLabel("nexclub.");
        logo.setFont(new Font("SansSerif", Font.BOLD, 22));
        logo.setBounds(30, 10, 200, 40);
        add(logo);

        JLabel userIcon = new JLabel("\uD83D\uDC64"); // user icon emoji
        userIcon.setBounds(1380, 20, 30, 30);
        add(userIcon);

        // Greeting Panel
        JPanel greetingPanel = createCardPanel();
        greetingPanel.setBounds(45, 80, 370, 140);
        greetingPanel.setLayout(new BorderLayout());
        JLabel greeting = new JLabel("Hello , User");
        greeting.setFont(new Font("SansSerif", Font.BOLD, 24));
        greeting.setBorder(new EmptyBorder(20, 20, 0, 0));
        JLabel college = new JLabel("College Name");
        college.setFont(new Font("SansSerif", Font.PLAIN, 14));
        college.setForeground(Color.GRAY);
        college.setBorder(new EmptyBorder(5, 20, 20, 0));
        greetingPanel.add(greeting, BorderLayout.NORTH);
        greetingPanel.add(college, BorderLayout.SOUTH);
        add(greetingPanel);

        // Clubs Enrolled Card
        JPanel clubsEnrolled = createGradientCard("#FAD0C4", "#FFD1FF", "Clubs Enrolled", "7 Clubs");
        clubsEnrolled.setBounds(450, 80, 420, 90);
        // Add mouse listener to open ClubsEnrolledUI
        clubsEnrolled.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clubsEnrolled.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new ClubsEnrolledUI().setVisible(true);
            }
        });
        add(clubsEnrolled);

        // Available Clubs Card
        JPanel availableClubs = createGradientCard("#A1C4FD", "#C2E9FB", "Available Clubs", "20 Clubs");
        availableClubs.setBounds(900, 80, 420, 90);
        // Add mouse listener to open ClubsInfoUI
        availableClubs.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        availableClubs.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new ClubsInfoUI().setVisible(true);
            }
        });
        add(availableClubs);

        // Duty Leaves Panel
        JPanel dutyLeaves = createCardPanel();
        dutyLeaves.setLayout(null);
        dutyLeaves.setBounds(45, 250, 370, 180);
        JLabel dutyTitle = new JLabel("Duty Leaves");
        dutyTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        dutyTitle.setBounds(20, 15, 200, 30);
        dutyLeaves.add(dutyTitle);
        dutyLeaves.add(createStatBox("Applied", "20", new Color(173, 216, 230), 20, 60));
        dutyLeaves.add(createStatBox("Approved", "7", new Color(255, 182, 193), 200, 60));
        add(dutyLeaves);

        // Your Upcoming Events Panel
        JPanel upcomingEvents = createCardPanel();
        upcomingEvents.setLayout(null);
        upcomingEvents.setBounds(450, 200, 870, 250);
        JLabel eventsTitle = new JLabel("Your Upcoming Events");
        eventsTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        eventsTitle.setBounds(20, 15, 300, 30);
        upcomingEvents.add(eventsTitle);
        addMeeting(upcomingEvents, "08:15 am", "Quick Daily Meeting", "Zoom", 60);
        addMeeting(upcomingEvents, "09:30 pm", "John Onboarding", "Google Meet", 100);
        addMeeting(upcomingEvents, "02:30 pm", "Call With a New Team", "Google Meet", 140);
        addMeeting(upcomingEvents, "04:00 pm", "Lead Designers Event", "Zoom", 180);
        JLabel seeAll = new JLabel("See all meetings");
        seeAll.setForeground(new Color(0x3366FF));
        seeAll.setBounds(750, 215, 200, 30);
        upcomingEvents.add(seeAll);
        add(upcomingEvents);

        // Recruitments Panel
        JPanel recruitments = createCardPanel();
        recruitments.setLayout(null);
        recruitments.setBounds(45, 470, 370, 160);
        JLabel recruitTitle = new JLabel("Recruitments");
        recruitTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        recruitTitle.setBounds(20, 10, 200, 30);
        recruitments.add(recruitTitle);
        JLabel sub = new JLabel("Most common areas of interests");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(Color.GRAY);
        sub.setBounds(20, 30, 300, 30);
        recruitments.add(sub);
        addMeeting(recruitments, "08:15 am", "Quick Daily Meeting", "Zoom", 60);
        addMeeting(recruitments, "09:30 pm", "John Onboarding", "Google Meet", 100);
        add(recruitments);

        // Notifications Panel
        JPanel notifications = createCardPanel();
        notifications.setLayout(null);
        notifications.setBounds(450, 470, 870, 160);
        JLabel notifTitle = new JLabel("Notifications");
        notifTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        notifTitle.setBounds(20, 10, 200, 30);
        notifications.add(notifTitle);
        addMeeting(notifications, "08:15 am", "Quick Daily Meeting", "Zoom", 60);
        addMeeting(notifications, "09:30 pm", "John Onboarding", "Google Meet", 100);
        add(notifications);
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true));
        return panel;
    }

    private JPanel createGradientCard(String startHex, String endHex, String title, String stat) {
        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, Color.decode(startHex), getWidth(), getHeight(), Color.decode(endHex));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setBorder(new EmptyBorder(15, 20, 0, 0));
        JLabel statLabel = new JLabel(stat);
        statLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        statLabel.setBorder(new EmptyBorder(0, 20, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(statLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createStatBox(String label, String value, Color bg, int x, int y) {
        JPanel statBox = new JPanel();
        statBox.setLayout(new BorderLayout());
        statBox.setBackground(bg);
        statBox.setBounds(x, y, 140, 80);
        statBox.setBorder(BorderFactory.createLineBorder(Color.WHITE, 0, true));

        JLabel labelTop = new JLabel(label, SwingConstants.CENTER);
        labelTop.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JLabel labelBottom = new JLabel(value, SwingConstants.CENTER);
        labelBottom.setFont(new Font("SansSerif", Font.BOLD, 24));

        statBox.add(labelTop, BorderLayout.NORTH);
        statBox.add(labelBottom, BorderLayout.SOUTH);
        return statBox;
    }

    private void addMeeting(JPanel panel, String time, String title, String platform, int yOffset) {
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        timeLabel.setBounds(20, yOffset, 80, 30);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setBounds(110, yOffset, 300, 30);

        JLabel platformLabel = new JLabel(platform);
        platformLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        platformLabel.setForeground(Color.GRAY);
        platformLabel.setBounds(110, yOffset + 18, 300, 30);

        JLabel arrow = new JLabel("→");
        arrow.setFont(new Font("SansSerif", Font.BOLD, 14));
        arrow.setBounds(820, yOffset, 30, 30);

        panel.add(timeLabel);
        panel.add(titleLabel);
        panel.add(platformLabel);
        panel.add(arrow);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new NexclubDashboardUI().setVisible(true));
    }
}
