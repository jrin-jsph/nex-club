import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ClubDetailsUI extends JFrame {
    public ClubDetailsUI(String clubName, String clubCategory, String clubDetails) {
        setTitle(clubName + " - Details");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(24, 24, 24, 24));
        mainPanel.setBackground(new Color(0xF8F9FB));

        JLabel nameLabel = new JLabel(clubName, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        nameLabel.setForeground(new Color(0x3366FF));
        mainPanel.add(nameLabel, BorderLayout.NORTH);

        JLabel categoryLabel = new JLabel(clubCategory, SwingConstants.CENTER);
        categoryLabel.setFont(new Font("Segoe UI", Font.ITALIC, 15));
        categoryLabel.setForeground(new Color(0x3366FF));
        mainPanel.add(categoryLabel, BorderLayout.CENTER);

        JLabel detailsLabel = new JLabel("<html><body style='width:500px; font-family:Segoe UI;'>" + clubDetails + "<br><br><b>Latest Updates:</b><br>- Event 1: Details here<br>- Event 2: Details here</body></html>");
        detailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        detailsLabel.setForeground(new Color(60, 60, 60));
        detailsLabel.setVerticalAlignment(SwingConstants.TOP);

        JScrollPane scrollPane = new JScrollPane(detailsLabel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(0xF8F9FB));
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }
}