import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class welUI {

    public static void show(JFrame parent) {
        parent.setTitle("Welcome");

        String fullName = "there";
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/yourdb", "root", "");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT FullName FROM profile ORDER BY nID DESC LIMIT 1");
            if (rs.next()) {
                fullName = rs.getString("FullName");
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Root panel
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Welcome label
        JLabel welcomeLabel = new JLabel("Hi " + fullName + "! Let's begin!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeLabel.setForeground(new Color(33, 150, 243, 0)); // Start fully transparent

        // Center alignment
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(welcomeLabel);

        panel.add(centerPanel, BorderLayout.CENTER);

        // Apply panel to frame
        parent.getContentPane().removeAll();
        parent.getContentPane().add(panel);
        parent.revalidate();
        parent.repaint();

        // Fade-in animation using foreground color's alpha
        Timer timer = new Timer(30, null);
        timer.addActionListener(new ActionListener() {
            int alpha = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                alpha += 5;
                if (alpha > 255) {
                    alpha = 255;
                    timer.stop();
                }
                welcomeLabel.setForeground(new Color(33, 150, 243, alpha));
            }
        });
        timer.start();
    }
}
