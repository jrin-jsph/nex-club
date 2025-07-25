import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class NexClubWelcomePanel {
    private static float contentAlpha = 0f; // For fade-in effect

    public static void show(JFrame parent) {
        parent.setTitle("Nexclub");
        parent.setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        panel.setBackground(Color.WHITE);

        JLabel heading = new JLabel();
        heading.setFont(new Font("Segoe UI", Font.BOLD, 42));
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        panel.add(heading, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);

        String content = "<html><div style='font-family:Microsoft JhengHei; font-size:14px; color:#333333; line-height:1.6;'>" +
                "Your all-in-one college club management system designed to keep students " +
                "and club leaders connected, informed, and organized.<br><br>" +
                "• <b>📅 Stay Updated</b><br>" +
                "&nbsp;&nbsp;Never miss important events, meetings, and workshops.<br><br>" +
                "• <b>🔔 Real-Time Alerts</b><br>" +
                "&nbsp;&nbsp;Receive notifications for club updates.<br><br>" +
                "• <b>🔍 Discover Clubs</b><br>" +
                "&nbsp;&nbsp;Explore and manage campus involvements.<br><br>" +
                "• <b>📋 Club Details</b><br>" +
                "&nbsp;&nbsp;View announcements, activities, and member information.<br><br>" +
                "• <b>📝 Apply for Roles</b><br>" +
                "&nbsp;&nbsp;Submit applications for leadership or interviews.</div></html>";

        JLabel contentLabel = new JLabel(content) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, Math.max(0f, contentAlpha))));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        contentLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        contentPanel.add(contentLabel, BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.CENTER);

        JButton nextButton = new JButton("Next");
        nextButton.setContentAreaFilled(false);
        nextButton.setBorder(null);
        nextButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        nextButton.setForeground(Color.BLACK);
        nextButton.setPreferredSize(new Dimension(110, 44));
        nextButton.setFocusPainted(false);

        nextButton.addActionListener(e -> {
            nextButton.setEnabled(false); // prevent multiple clicks

            new javax.swing.Timer(1000, evt -> {
                ((javax.swing.Timer) evt.getSource()).stop(); // stop the timer

                try {
                    // DB connection
                    Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/nexclub", "root", "admin");

                    // Query most recent nID based on timestamp
                    String query = "SELECT nID FROM login ORDER BY login_time DESC LIMIT 1";
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    if (rs.next()) {
                        String nID = rs.getString("nID");
                        if (nID.length() >= 3) {
                            char type = nID.charAt(2); // 3rd character (index 2)

                            if (type == 'S') {
                                ProfileUpdaterApp.show(parent);
                            } else if (type == 'I') {
                                InstitutionProfileUpdater.show(parent);
                            } else {
                                JOptionPane.showMessageDialog(parent, "Unknown nID type: " + type, "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(parent, "Invalid nID format.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(parent, "No login records found.", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                    rs.close();
                    stmt.close();
                    conn.close();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(parent, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }).start();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(nextButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        parent.getContentPane().removeAll();
        parent.getContentPane().add(panel);
        parent.revalidate();
        parent.repaint();

        new Timer().scheduleAtFixedRate(new TimerTask() {
            int i = 0;
            final String text = "What is Nexclub?";
            boolean headingComplete = false;

            public void run() {
                if (!headingComplete) {
                    if (i <= text.length()) {
                        heading.setText(text.substring(0, i));
                        i++;
                    } else {
                        headingComplete = true;
                        i = 0;
                    }
                } else {
                    if (contentAlpha < 1f) {
                        contentAlpha = Math.min(1f, contentAlpha + 0.05f);
                        contentLabel.repaint();
                    } else {
                        cancel();
                    }
                }
            }
        }, 0, 100);
    }
}
