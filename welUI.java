import javax.swing.*;
import java.awt.*;
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

        // Create panel and set layout
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Welcome text
        JLabel welcomeLabel = new JLabel("Hi " + fullName + "! Let's begin!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        welcomeLabel.setForeground(new Color(33, 150, 243));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Add spacing and alignment
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(welcomeLabel);

        panel.add(centerPanel, BorderLayout.CENTER);

        // Add to frame
        parent.getContentPane().removeAll();
        parent.getContentPane().add(panel);
        parent.revalidate();
        parent.repaint();
    }
}
